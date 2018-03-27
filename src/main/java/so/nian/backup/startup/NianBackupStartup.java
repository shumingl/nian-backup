package so.nian.backup.startup;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import so.nian.backup.bizz.service.NianHtmlService;
import so.nian.backup.bizz.service.NianJsonService;
import so.nian.backup.config.AppConfig;
import so.nian.backup.config.logback.LogbackConfigure;
import so.nian.backup.http.NianImageDownload;
import so.nian.backup.utils.logger.LogConsole;

public class NianBackupStartup {

    private static ThreadPoolMonitorThread monitor;

    public static void main(String[] args) {
        try {
            startup();
        } catch (Exception e) {
            e.printStackTrace();
            shutdown();
        }
    }

    /**
     * 启动应用
     */
    public static void startup() {
        if (AppConfig.initialize()) {
            LogbackConfigure.configure("config/logback.xml");
            // 加载spring配置文件
            new ClassPathXmlApplicationContext("config/spring/spring-*.xml");
            NianImageDownload.startup();
            NianJsonService.startup();
            NianHtmlService.startup();
            monitor = new ThreadPoolMonitorThread();
            new Thread(monitor, "ThreadPoolMonitor").start();
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
