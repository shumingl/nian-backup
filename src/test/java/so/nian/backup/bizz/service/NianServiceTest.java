package so.nian.backup.bizz.service;

import org.junit.Test;
import so.nian.backup.startup.NianBackupStartup;

import static org.junit.Assert.*;

public class NianServiceTest {
    @Test
    public void dealInfo() throws Exception {
    }

    @Test
    public void findDreamList() throws Exception {
        NianBackupStartup.startup();
        NianService service = new NianService();
        service.findDreamList("142171");
    }

    @Test
    public void dealDream() throws Exception {
    }

}