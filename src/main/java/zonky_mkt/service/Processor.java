package zonky_mkt.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import zonky_mkt.util.UrlUtils;
import zonky_mkt.model.Loan;
import zonky_mkt.model.PageRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Fetches requested pages coming from the `requestQueue` at the maximal rate given by `rateLimiter`
 * and sends parsed Loans to the `resultQueue` to be presented to the user.
 */
public class Processor implements Runnable {
    private final static Logger log = Logger.getLogger(Processor.class.getName());
    private static final int CONNECT_TIMEOUT = 2000;
    private static final int READ_TIMEOUT = 2500;

    private String baseUrl;
    private BlockingQueue<PageRequest> requestQueue;
    private BlockingQueue<Loan> resultQueue;
    private RateLimiter rateLimiter;
    private Integer pageSize;

    private ObjectMapper mapper;
    private volatile OffsetDateTime lastDateShown;

    /**
     * @param baseUrl the marketplace URL without the query or fragment parts
     * @param requestQueue pages to be fetched and processed
     * @param resultQueue loans to be displayed
     * @param rateLimiter token service supplying tokens at the defined rate
     * @param pageSize fixed page size (number of items per request)
     * @param lastDateShown only newer items will be fetched
     */
    public Processor(String baseUrl, BlockingQueue<PageRequest> requestQueue, BlockingQueue<Loan> resultQueue, RateLimiter rateLimiter, Integer pageSize, OffsetDateTime lastDateShown) {
        this.baseUrl = baseUrl;
        this.requestQueue = requestQueue;
        this.resultQueue = resultQueue;
        this.rateLimiter = rateLimiter;
        this.pageSize = pageSize;
        this.lastDateShown = lastDateShown;
        this.mapper = new ObjectMapper();
    }

    private void parseAndSendToDisplay(InputStream in) throws IOException, InterruptedException {
        List<Map> loanRecords = mapper.readValue(in, List.class);
        for (Map<String, Object> record : loanRecords) {
            Loan loan = Loan.fromMap(record);
            resultQueue.put(loan);
            lastDateShown = loan.getDatePublished();
        }
    }

    private void queueRemaining(OffsetDateTime lastTs, Integer pageNo, int total) throws InterruptedException {
        if (pageNo == 0 && total > pageSize) {
            int pages = (total + pageSize - 1) / pageSize;
            for (int i = 1; i < pages; i++) {
                requestQueue.put(new PageRequest(lastTs, i));
            }
        }
    }

    private URL getUrl(OffsetDateTime lastTs) throws MalformedURLException {
        Map<String, String> params = new HashMap<>();
        params.put("fields", Loan.getFields());
        if (lastTs != null) {
            params.put("datePublished__gt", lastTs.toString());
        }
        return UrlUtils.withParams(baseUrl, params);
    }

    private void processPage(OffsetDateTime lastTs, Integer pageNo) throws InterruptedException {
        log.fine("Processing pageNo " + pageNo + " (since " + lastTs + ")");
        try {
            HttpURLConnection con = (HttpURLConnection) getUrl(lastTs).openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("User-Agent", "zonky-mkt/1.0 (https://github.com/katox/zonky-mkt");
            con.setRequestProperty("X-Order", "datePublished");
            con.setRequestProperty("X-Page", pageNo.toString());
            con.setRequestProperty("X-Size", pageSize.toString());
            con.setConnectTimeout(CONNECT_TIMEOUT);
            con.setReadTimeout(READ_TIMEOUT);

            int status = con.getResponseCode();
            if (status >= 200 && status < 300) {
                try (InputStream in = con.getInputStream()) {
                    parseAndSendToDisplay(in);
                }
                String totalField = con.getHeaderField("X-Total");
                if (totalField != null) {
                    int totalItems = Integer.parseInt(totalField);
                    queueRemaining(lastTs, pageNo, totalItems);
                }
            } else if (status == 429) {
                requestQueue.clear();
                log.warning("API rate limit exceeded. Waiting for the next run.");
            } else if (status >= 400 && status < 500) {
                requestQueue.clear();
                log.severe("Zonky Marketplace reports a bad request. Send me a bug report.");
            } else {
                requestQueue.clear();
                log.severe("Zonky Marketplace reports an internal server error. Send them a bug report.");
            }

            con.disconnect();
        } catch (InterruptedException e) {
            throw (e);
        } catch (IOException e) {
            requestQueue.clear();
            log.log(Level.SEVERE, "Error connecting to the Zonky Marketplace!", e);
        } catch (Exception e) {
            requestQueue.clear();
            log.log(Level.SEVERE, "Cant' process the response from the Zonky Marketplace!", e);
        }
    }

    private void processRequest(PageRequest pageRequest) throws InterruptedException {
        rateLimiter.acquire(1);
        processPage(pageRequest.getSinceTs(), pageRequest.getPageNo());
    }

    public OffsetDateTime getLastDateShown() {
        return lastDateShown;
    }

    public BlockingQueue<PageRequest> getRequestQueue() {
        return requestQueue;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                PageRequest pageRequest = requestQueue.take();
                processRequest(pageRequest);
            }
        } catch (InterruptedException e) {
            log.info("Interrupted. Aborting marketplace data processing.");
            Thread.currentThread().interrupt();
        }
    }
}
