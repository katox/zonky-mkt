package zonky_mkt;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Scheduler implements Runnable {

    private Processor processor;

    public Scheduler(Processor processor) {
        this.processor = processor;
    }

    @Override
    public void run() {
        try {
            BlockingQueue<PageRequest> requestQueue = processor.getRequestQueue();
            if (requestQueue.isEmpty()) { // avoid pressure when requests are piling
                PageRequest pageRequest = new PageRequest(processor.getLastDateShown(), 0);
                requestQueue.offer(pageRequest, 5, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
