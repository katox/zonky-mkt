package zonky_mkt.service;

import zonky_mkt.model.PageRequest;

import java.time.OffsetDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Schedules a new marketplace refresh by the `Processor`.
 *
 * The scheduling interval is configured by the `ScheduledExecutorService`.
 * It doesn't schedule a new refresh if the processor hasn't finished yet.
 */
public class Scheduler implements Runnable {
    private final static Logger log = Logger.getLogger(Scheduler.class.getName());

    private Processor processor;

    public Scheduler(Processor processor) {
        this.processor = processor;
    }

    @Override
    public void run() {
        try {
            BlockingQueue<PageRequest> requestQueue = processor.getRequestQueue();
            if (requestQueue.isEmpty()) { // avoid pressure when requests are piling
                OffsetDateTime lastDate = processor.getLastDateShown();
                PageRequest pageRequest = new PageRequest(lastDate, 0);
                log.fine("Scheduling a refresh for records published later than " + lastDate);
                requestQueue.offer(pageRequest, 5, TimeUnit.SECONDS);
            } else {
                log.fine("The processing doesn't keep up with the scheduling interval. Skipping the refresh.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
