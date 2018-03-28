package so.nian.backup.bizz.service;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolidx = new AtomicInteger(1);
    private final AtomicInteger threadidx = new AtomicInteger(0);
    private String prefix;

    public NamedThreadFactory(String prefix) {
        this.prefix = String.format("%s/P%03d/T", prefix, poolidx.getAndIncrement());
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, String.format(prefix + "%03d", threadidx.getAndIncrement()));
        if (thread.isDaemon())
            thread.setDaemon(false);
        if (thread.getPriority() != Thread.NORM_PRIORITY)
            thread.setPriority(Thread.NORM_PRIORITY);
        return thread;
    }
}
