package ch.baurs.spring.integration.tapestry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogHelper {

    static final Logger LOG = LoggerFactory.getLogger("spring-boot-tapestry-integration");


    public static void trace(String format, Object... arguments) {
        LOG.trace(format, arguments);
    }

    public static void debug(String format, Object... arguments) {
        LOG.debug(format, arguments);
    }

    public static void info(String format, Object... arguments) {
        LOG.info(format, arguments);
    }

    public static void warn(String format, Object... arguments) {
        LOG.warn(format, arguments);
    }

    public static void error(String format, Object... arguments) {
        LOG.error(format, arguments);
    }

}
