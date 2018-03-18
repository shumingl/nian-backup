package so.nian.backup.config.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

public class LogbackConfigure {
    private static LoggerContext loggerContext = null;

    public static void configure(String configfile) {
        ClassPathResource res = new ClassPathResource(configfile);

        loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(loggerContext);

        loggerContext.reset();
        try {
            configurator.doConfigure(res.getInputStream());
        } catch (JoranException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void stop() {
        if (loggerContext != null)
            loggerContext.stop();
        loggerContext = null;
    }
}