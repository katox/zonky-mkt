package zonky_mkt;

import com.google.common.util.concurrent.RateLimiter;
import org.junit.BeforeClass;
import org.junit.Test;
import zonky_mkt.model.Loan;
import zonky_mkt.model.PageRequest;
import zonky_mkt.service.Processor;
import zonky_mkt.service.StubbedProcessor;
import zonky_mkt.util.Logging;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for Presenter.
 */
public class ProcessorTest {

    @BeforeClass
    public static void beforeAllTestMethods() {
        Logging.setupTestLogging();
    }

    @Test
    public void blocksOnEmptyRequestQueue() {
        try {
            RateLimiter rateLimiter = RateLimiter.create(10); // 1 per 100ms
            LinkedBlockingDeque<PageRequest> requestQueue = new LinkedBlockingDeque<>();
            LinkedBlockingDeque<Loan> resultQueue = new LinkedBlockingDeque<>();
            Processor processor = new Processor("https://test.url", requestQueue, resultQueue, rateLimiter, 3, null);

            Thread t = new Thread(processor);
            t.start();
            Thread.sleep(250);

            assertTrue(resultQueue.isEmpty());

            t.interrupt();
        } catch (InterruptedException e) {
            // ignore
        }
    }

    @Test
    public void doesNotStopIfBadUrl() {
        try {
            RateLimiter rateLimiter = RateLimiter.create(10); // 1 per 100ms
            LinkedBlockingDeque<PageRequest> requestQueue = new LinkedBlockingDeque<>();
            LinkedBlockingDeque<Loan> resultQueue = new LinkedBlockingDeque<>();
            Processor processor = new Processor("https://nonexistent.url", requestQueue, resultQueue, rateLimiter, 3, null);

            Thread t = new Thread(processor);
            t.start();
            Thread.sleep(250);

            assertTrue(resultQueue.isEmpty());
            assertFalse(t.isInterrupted());

            t.interrupt();
        } catch (InterruptedException e) {
            // ignore
        }
    }

    @Test
    public void singlePageProcessing() {
        try {

            RateLimiter rateLimiter = RateLimiter.create(10); // 1 per 100ms
            LinkedBlockingDeque<PageRequest> requestQueue = new LinkedBlockingDeque<>();
            LinkedBlockingDeque<Loan> resultQueue = new LinkedBlockingDeque<>();
            Processor processor = new StubbedProcessor("https://test.url/page0", requestQueue, resultQueue, rateLimiter, 3, null);

            PageRequest page0Request = new PageRequest(null, 0);
            requestQueue.put(page0Request);

            Thread t = new Thread(processor);
            t.start();
            Thread.sleep(250);

            assertFalse(resultQueue.isEmpty());

            List<Loan> loans = new ArrayList<>();
            resultQueue.drainTo(loans);

            assertEquals((int) App.PAGE_SIZE, loans.size());
            String ids = loans.stream()
                    .map(loan -> String.valueOf(loan.getId()))
                    .collect(Collectors.joining(","));
            assertEquals("6,18,8,15,11,33,25,27,26,35,34,23,37,41,42,38,46,54,53,72,97,84,94,40,122", ids);

            for (Loan l : loans) {
                assertNotNull(l.getName());
                assertNotNull(l.getStory());
                assertNotNull(l.getUrl());
                assertNotNull(l.getDatePublished());
            }

            t.interrupt();
        } catch (InterruptedException e) {
            // ignore
        }
    }
}
