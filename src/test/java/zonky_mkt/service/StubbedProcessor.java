package zonky_mkt.service;

import com.google.common.util.concurrent.RateLimiter;
import zonky_mkt.model.Loan;
import zonky_mkt.model.PageRequest;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.time.OffsetDateTime;
import java.util.concurrent.BlockingQueue;

public class StubbedProcessor extends Processor {

    public StubbedProcessor(String baseUrl, BlockingQueue<PageRequest> requestQueue, BlockingQueue<Loan> resultQueue, RateLimiter rateLimiter, Integer pageSize, OffsetDateTime lastDateShown) {
        super(baseUrl, requestQueue, resultQueue, rateLimiter, pageSize, lastDateShown);
    }

    @Override
    URL getUrl(OffsetDateTime lastTs) throws MalformedURLException {
        final URLStreamHandler handler = new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(final URL url) {
                return new MockHttpURLConnection(url);
            }
        };
        return new URL(super.getUrl(lastTs), "", handler);
    }
}
