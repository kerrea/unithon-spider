package unithon.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;

public final class Log {

    private static final int LOG_CALLER = 3;

    private static final HashMap<String, Logger> loggerHashMap = new LinkedHashMap<>();

    /**
     * lock constructor
     */
    private Log() {

    }

    /**
     * Resolve caller and produce logger instance.
     * add lock avoid chaos output.
     *
     * @return instance of logger with specified name.
     */
    private static Logger produceLogger() {
        StackTraceElement traceElement = Thread.currentThread().getStackTrace()[LOG_CALLER];
        String className = traceElement.getClassName();
        if (loggerHashMap.containsKey(className)) {
            return loggerHashMap.get(className);
        }
        Logger logger = LoggerFactory.getLogger(className);
        loggerHashMap.put(className, logger);
        return logger;
    }

    /**
     * Log error message
     *
     * @param message error message
     */
    public static void e(String message) {
        produceLogger().error(message);
    }

    /**
     * Log exception stack trace
     *
     * @param e exception caught by try block.
     */
    public static void e(Exception e) {
        Logger logger = produceLogger();
        StackTraceElement[] traceElements = e.getStackTrace();
        logger.error("-------error--------");
        logger.error(e.toString());
        for (StackTraceElement element : traceElements) {
            logger.error(element.toString());
        }
    }

    /**
     * log info message
     *
     * @param message info message
     */
    public static void i(String message) {
        produceLogger().info(message);
    }

    /**
     * log debug message
     *
     * @param message debug message
     */
    public static void d(String message) {
        produceLogger().debug(message);
    }

    /**
     * log warn message
     *
     * @param message warn message
     */
    public static void w(String message) {
        produceLogger().warn(message);
    }

    public static void d() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        System.out.println("[" + Thread.currentThread().getName() + "]" + stackTraceElements[2].toString());
    }
}