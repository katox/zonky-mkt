package zonky_mkt;

import com.google.common.util.concurrent.RateLimiter;

import java.time.OffsetDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * tail -f Zonky
 */
public class App {

    public static final Integer PAGE_SIZE = 25;
    public static final double MAX_REQUESTS_PER_SECOND = 1.0;
    public static final int THREAD_POOL_SIZE = 3;

    private String baseUrl;
    private OffsetDateTime showSince;
    private int refreshInterval;

    private BlockingQueue<PageRequest> requestQueue;
    private BlockingQueue<Loan> resultQueue;

    public App(String baseUrl, OffsetDateTime showSince, int refreshInterval) {
        this.baseUrl = baseUrl;
        this.showSince = showSince;
        this.refreshInterval = refreshInterval;
        this.requestQueue = new LinkedBlockingDeque<>();
        this.resultQueue  = new LinkedBlockingDeque<>();
    }

    public void tailLoans() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);
        try {
            RateLimiter rateLimiter = RateLimiter.create(MAX_REQUESTS_PER_SECOND);
            Processor processor = new Processor(baseUrl, requestQueue, resultQueue, rateLimiter, PAGE_SIZE, showSince);
            Presenter presenter = new Presenter(resultQueue, System.out);
            Scheduler scheduler = new Scheduler(processor);

            executor.execute(presenter);
            executor.execute(processor);
            executor.scheduleAtFixedRate(scheduler, 0, refreshInterval, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            executor.shutdown();
        }
    }

    public static void main(String[] args) {
        OffsetDateTime showSince = OffsetDateTime.parse("2019-11-01T00:00:00.000+01:00"); // or null for all
        App app = new App("https://api.zonky.cz/loans/marketplace", showSince, 300);
        app.tailLoans();
    }
}
