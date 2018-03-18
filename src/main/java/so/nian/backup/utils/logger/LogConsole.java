package so.nian.backup.utils.logger;

import org.joda.time.DateTime;

import static java.lang.System.out;

/**
 * 控制台日志类，用于没有日志框架情况下的日志输出
 * @author shumingl
 */
public class LogConsole {

    private static final LogConsole INSTANCE = new LogConsole();
    private static final String LONG_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final String LOGGER_TEMPLATE = "%1$s %2$-5s %3$s";
    private LogLevel currentLevel = LogLevel.DEBUG;
    private Integer currentLevelNumber;

    private LogConsole() {
        currentLevelNumber = currentLevel.LevelNumber();
    }

    public static LogConsole getInstance() {
        return INSTANCE;
    }

    public LogLevel getLevel() {
        return currentLevel;
    }

    public void setLevel(LogLevel level) {
        currentLevel = level;
        currentLevelNumber = currentLevel.LevelNumber();
    }

    private String NOW() {
        return DateTime.now().toString(LONG_TIME_PATTERN);
    }

    public void trace(String message) {
        log(LogLevel.TRACE, message);
    }

    public void trace(String message, Object... parameters) {
        log(LogLevel.TRACE, message, parameters);
    }

    public void trace(String message, Throwable throwable) {
        log(LogLevel.TRACE, message, throwable);
    }

    public void trace(String message, Throwable throwable, Object... parameters) {
        log(LogLevel.TRACE, message, throwable, parameters);
    }

    public void debug(String message) {
        log(LogLevel.DEBUG, message);
    }

    public void debug(String message, Object... parameters) {
        log(LogLevel.DEBUG, message, parameters);
    }

    public void debug(String message, Throwable throwable) {
        log(LogLevel.DEBUG, message, throwable);
    }

    public void debug(String message, Throwable throwable, Object... parameters) {
        log(LogLevel.DEBUG, message, throwable, parameters);
    }


    public void info(String message) {
        log(LogLevel.INFO, message);
    }

    public void info(String message, Object... parameters) {
        log(LogLevel.INFO, message, parameters);
    }

    public void info(String message, Throwable throwable) {
        log(LogLevel.INFO, message, throwable);
    }

    public void info(String message, Throwable throwable, Object... parameters) {
        log(LogLevel.INFO, message, throwable, parameters);
    }

    public void warn(String message) {
        log(LogLevel.WARN, message);
    }

    public void warn(String message, Object... parameters) {
        log(LogLevel.WARN, message, parameters);
    }

    public void warn(String message, Throwable throwable) {
        log(LogLevel.WARN, message, throwable);
    }

    public void warn(String message, Throwable throwable, Object... parameters) {
        log(LogLevel.WARN, message, throwable, parameters);
    }

    public void error(String message) {
        log(LogLevel.ERROR, message);
    }

    public void error(String message, Object... parameters) {
        log(LogLevel.ERROR, message, parameters);
    }

    public void error(String message, Throwable throwable) {
        log(LogLevel.ERROR, message, throwable);
    }

    public void error(String message, Throwable throwable, Object... parameters) {
        log(LogLevel.ERROR, message, throwable, parameters);
    }

    public void fatal(String message) {
        log(LogLevel.FATAL, message);
    }

    public void fatal(String message, Object... parameters) {
        log(LogLevel.FATAL, message, parameters);
    }

    public void fatal(String message, Throwable throwable) {
        log(LogLevel.FATAL, message, throwable);
    }

    public void fatal(String message, Throwable throwable, Object... parameters) {
        log(LogLevel.FATAL, message, throwable, parameters);
    }

    /**
     * 打印日志
     * @param level 日志级别
     * @param message 日志内容
     */
    public void log(LogLevel level, String message) {
        if (level.LevelNumber() < currentLevelNumber) return;
        out.println(String.format(LOGGER_TEMPLATE, NOW(), level, message));
    }

    /**
     * 打印日志
     * @param level 日志级别
     * @param message 带参数的内容,参考String.format
     * @param parameters 参数
     */
    public void log(LogLevel level, String message, Object... parameters) {
        if (level.LevelNumber() < currentLevelNumber) return;
        String msg = String.format(message, parameters);
        out.println(String.format(LOGGER_TEMPLATE, NOW(), level, msg));
    }

    /**
     * 打印日志
     * @param level 日志级别
     * @param message 日志内容
     * @param throwable 异常对象
     */
    public void log(LogLevel level, String message, Throwable throwable) {
        if (level.LevelNumber() < currentLevelNumber) return;
        out.println(String.format(LOGGER_TEMPLATE, NOW(), level, message));
        throwable.printStackTrace(out);
    }

    /**
     * 打印日志
     * @param level
     * @param message 带参数的内容,参考String.format
     * @param throwable 一场对象
     * @param parameters 参数
     */
    public void log(LogLevel level, String message, Throwable throwable, Object... parameters) {
        if (level.LevelNumber() < currentLevelNumber) return;
        String msg = String.format(message, parameters);
        out.println(String.format(LOGGER_TEMPLATE, NOW(), level, msg));
        throwable.printStackTrace(out);
    }
}
