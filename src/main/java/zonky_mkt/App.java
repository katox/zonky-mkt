package zonky_mkt;

import com.google.common.util.concurrent.RateLimiter;

import java.time.OffsetDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * tail -f Zonky
 */
public class App {
    private final static Logger log = Logger.getLogger(App.class.getName());
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

    public void setupLogging() {
        try {
            Level loggingLevel = Level.FINE;
            OneLineFormatter formatter = new OneLineFormatter();
            Logger rootLogger = LogManager.getLogManager().getLogger("");
            rootLogger.setLevel(loggingLevel);

            // suppress the logging output to the console
            Handler[] handlers = rootLogger.getHandlers();
            if (handlers[0] instanceof ConsoleHandler) {
                rootLogger.removeHandler(handlers[0]);
            }

            rootLogger.addHandler(new FileHandler("zonky_mkt.log"));

            for (Handler handler : rootLogger.getHandlers()) {
                handler.setLevel(loggingLevel);
                handler.setFormatter(formatter);
            }
        } catch (Exception e) {
            System.err.println("Cannot setup logging!");
            e.printStackTrace();
        }
    }

    public void tailLoans() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);
        try {
            RateLimiter rateLimiter = RateLimiter.create(MAX_REQUESTS_PER_SECOND);
            Processor processor = new Processor(baseUrl, requestQueue, resultQueue, rateLimiter, PAGE_SIZE, showSince);
            Presenter presenter = new Presenter(resultQueue, System.out);
            Scheduler scheduler = new Scheduler(processor);
            log.config("App configured to fetch from: " + baseUrl);
            log.config("Thread pool core capacity: " + THREAD_POOL_SIZE);
            log.config("Maximum outgoing requests per second: " + MAX_REQUESTS_PER_SECOND);
            log.config("Request page size: " + PAGE_SIZE);

            executor.execute(presenter);
            executor.execute(processor);
            executor.scheduleAtFixedRate(scheduler, 0, refreshInterval, TimeUnit.SECONDS);

            log.info("Processing started. Starting publish date: " + showSince);
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            executor.shutdown();
        }
    }

    public static void main(String[] args) {
        OffsetDateTime showSince = OffsetDateTime.parse("2019-11-01T00:00:00.000+01:00"); // or null for all
        App app = new App("https://api.zonky.cz/loans/marketplace", showSince, 300);
        app.setupLogging();
        app.tailLoans();
    }
}
