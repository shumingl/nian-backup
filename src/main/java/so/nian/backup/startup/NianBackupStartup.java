package so.nian.backup.startup;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import so.nian.backup.bizz.service.NianHtmlService;
import so.nian.backup.bizz.service.NianJsonService;
import so.nian.backup.config.AppConfig;
import so.nian.backup.config.logback.LogbackConfigure;
import so.nian.backup.http.NianImageDownload;
import so.nian.backup.utils.StringUtil;
import so.nian.backup.utils.logger.LogConsole;

import java.util.Map;

public class NianBackupStartup {

    private static ThreadPoolMonitorThread monitor;

    /**
     * 启动应用
     */
    public static void startup(Map<String, Object> config) {
        if (AppConfig.initialize()) {
            LogbackConfigure.configure("config/logback.xml");
            // 加载spring配置文件
            new ClassPathXmlApplicationContext("config/spring/spring-*.xml");

            Integer htmlCrawlerSize = StringUtil.MAPGET(config, "html/crawler.poolSize");
            Integer htmlRenderSize = StringUtil.MAPGET(config, "html/render.poolSize");
            Integer jsonCrawlerSize = StringUtil.MAPGET(config, "json/crawler.poolSize");
            Integer jsonRenderSize = StringUtil.MAPGET(config, "json/render.poolSize");
            Integer imageCrawlerSize = StringUtil.MAPGET(config, "image/crawler.poolSize");

            NianImageDownload.startup(imageCrawlerSize);
            NianJsonService.startup(jsonCrawlerSize, jsonRenderSize);
            NianHtmlService.startup(htmlCrawlerSize, htmlRenderSize);
            monitor = new ThreadPoolMonitorThread();
            monitor.setDaemon(true);
            Thread thread = new Thread(monitor, "ThreadPoolMonitor");
            thread.setDaemon(true);
            thread.start();
        } else {
            String msg = String.format("配置文件[%s]加载失败，程序终止。", AppConfig.getConfigFile());
            LogConsole.getInstance().info(msg);
            throw new RuntimeException(msg);
        }
    }

    /**
     * 关闭应用
     */
    public static void shutdown() {
        NianHtmlService.shutdownPool();
        NianJsonService.shutdownPool();
        NianImageDownload.shutdownPool();
        if (monitor != null) monitor.shutdown();
        LogbackConfigure.stop();
    }
}
