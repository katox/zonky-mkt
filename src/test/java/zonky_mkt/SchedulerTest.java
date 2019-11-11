package zonky_mkt;

import com.google.common.util.concurrent.RateLimiter;
import org.junit.Test;
import zonky_mkt.model.Loan;
import zonky_mkt.model.PageRequest;
import zonky_mkt.service.Processor;
import zonky_mkt.service.Scheduler;
import zonky_mkt.service.StubbedProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
public class SchedulerTest {
    @Test
    public void testRun() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(App.THREAD_POOL_SIZE);
        try {

            RateLimiter rateLimiter = RateLimiter.create(1000); // 1 per 1ms
            LinkedBlockingDeque<PageRequest> requestQueue = new LinkedBlockingDeque<>();
            LinkedBlockingDeque<Loan> resultQueue = new LinkedBlockingDeque<>();
            Processor processor = new StubbedProcessor("https://test.url/page1", requestQueue, resultQueue, rateLimiter, 3, null);
            Scheduler scheduler = new Scheduler(processor);

            executor.execute(processor);
            Thread.sleep(250);

            assertTrue(resultQueue.isEmpty());
            executor.scheduleAtFixedRate(scheduler, 0, 250, TimeUnit.MILLISECONDS);

            Thread.sleep(650);
            assertFalse(resultQueue.isEmpty());

            List<Loan> loans = new ArrayList<>();
            resultQueue.drainTo(loans);
            assertEquals(3 * App.PAGE_SIZE, loans.size());

        } catch (Exception e) {
            // ignore
        } finally {
            executor.shutdown();
        }
    }

}
