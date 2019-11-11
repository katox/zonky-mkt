package zonky_mkt.util;

import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Logging {
    public static void setupTestLogging() {
        try {
            Level loggingLevel = Level.FINE;
            OneLineFormatter formatter = new OneLineFormatter();
            Logger rootLogger = LogManager.getLogManager().getLogger("");
            rootLogger.setLevel(loggingLevel);

            for (Handler handler : rootLogger.getHandlers()) {
                handler.setLevel(loggingLevel);
                handler.setFormatter(formatter);
            }
        } catch (Exception e) {
            System.err.println("Cannot setup test logging!");
            e.printStackTrace();
        }
    }
}
