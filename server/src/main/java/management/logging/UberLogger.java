package management.logging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class UberLogger {

    private static int o = 0;
    private String logger_name;
    private Level this_log_level;
    private static final Level global_log_level; // shared by all;
    private static final Map<String, UberLogger> loggers;

    static {
        global_log_level = Level.ALL;
        loggers = new HashMap<>();
    }

    private UberLogger(String name) {
        this.logger_name = name;
        this_log_level = global_log_level;
    }

    public void setName(String name) {
        logger_name = name;
    }

    public void setLevel(Level level) {
        this_log_level = level;
    }

    public static UberLogger getLogger(String name) {
        return loggers.getOrDefault(name, new UberLogger(name));
    }


    public void log(Level level, String str) {
        if (level.intValue() < this_log_level.intValue()) return;
        synchronized (UberLogger.class) {
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            String callingMethodName = "";
            if (stackTraceElements.length > 2) {
                callingMethodName = stackTraceElements[2].getMethodName();
            }
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String time = (dtf.format(now));

            StringBuilder builder = new StringBuilder();
            builder
                    .append(time).append(" ").append(logger_name).append(" @ ").append(callingMethodName).append(" \n")
                    .append(level.toString()).append("\t").append(str).append("\n");
            System.out.println(builder.toString());
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UberLogger logger = (UberLogger) o;
        return Objects.equals(logger_name, logger.logger_name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(logger_name);
    }
}
