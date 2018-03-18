package so.nian.backup.utils.logger;

import java.util.HashMap;
import java.util.Map;

public enum LogLevel {

    TRACE("TRACE"), DEBUG("DEBUG"), INFO("INFO"), WARN("WARN"), ERROR("ERROR"), FATAL("FATAL");

    private String level;
    private static Map<String, Integer> levelMap;

    static {
        levelMap = new HashMap<String, Integer>();
        levelMap.put("TRACE", 0);
        levelMap.put("DEBUG", 1);
        levelMap.put("INFO", 2);
        levelMap.put("WARN", 3);
        levelMap.put("ERROR", 4);
        levelMap.put("FATAL", 5);
    }

    LogLevel(String level) {
        this.level = level;
    }

    public int LevelNumber() {
        Integer res = levelMap.get(level);
        if (res == null) return 2;
        return res;
    }
    @Override
    public String toString(){
        return level;
    }
}
