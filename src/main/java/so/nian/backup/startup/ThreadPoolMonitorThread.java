package so.nian.backup.startup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import so.nian.backup.bizz.service.NianHtmlService;
import so.nian.backup.bizz.service.NianJsonService;
import so.nian.backup.http.NianImageDownload;

import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPoolMonitorThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolMonitorThread.class);

    private static Map<String, ThreadPoolExecutor> pools = null;
    private boolean stop = false;

    public ThreadPoolMonitorThread() {
        pools = new LinkedHashMap<>();
        stop = false;
        pools.put("JSON .HTTP", NianJsonService.getHttpThreadPool());
        pools.put("JSON .JSON", NianJsonService.getJsonThreadPool());
        pools.put("HTML .HTTP", NianHtmlService.getHttpThreadPool());
        pools.put("HTML .HTML", NianHtmlService.getHtmlThreadPool());
        pools.put("IMAGE.HTTP", NianImageDownload.getImageThreadPool());
    }

    public void shutdown() {
        this.stop = true;
    }

    public void run() {
        try {
            if (pools == null) return;
            while (!stop) {
                for (String key : pools.keySet()) {
                    ThreadPoolExecutor pool = pools.get(key);
                    logger.info(String.format("ThreadPool[%-10s] : RUNNING(%d), STATUS(%d/%d), QUEUE(%d)",
                            key,
                            pool.getActiveCount(),
                            pool.getCompletedTaskCount(),
                            pool.getTaskCount(),
                            pool.getQueue().size()));
                }
                Thread.sleep(30000);
            }
        } catch (Exception e) {
            logger.error("MonitorThread-ERROR : {}", e.getMessage());
        }
    }
}
