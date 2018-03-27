package so.nian.backup.config;

import org.springframework.core.io.ClassPathResource;
import so.nian.backup.utils.logger.LogConsole;

import java.util.*;

/**
 * 应用程序配置文件加载
 *
 * @author shumingl
 */
public class AppConfig {
    //private static Logger logger = LoggerFactory.getLogger(AppConfig.class);
    private static LogConsole logger = LogConsole.getInstance();
    private static Map<String, Object> configTemplates;
    private static boolean initialize = false;
    private static Map<String, String> config;
    private static Map<String, String> configMap;
    private static Properties properties;
    private static final String configFile = "config/app.properties";
    private static final String logbackConfig = "logback.configfile";
    private static final String logbackFilePath = "logback.logbasepath";
    private static final String nianViewsBase = "nian.views.base";
    private static final String nianImageBase = "nian.image.base";
    private static final String nianCacheBase = "nian.cache.base";
    private static final String nianRenderModel = "nian.render.model";
    private static final String nianRenderTemplate = "nian.render.template";
    private static final String freemarkerTemplatePath = "freemarker.template.path";
    private static final String freemarkerTemplateEncode = "freemarker.template.encode";
    private static final String applicationFontPath = "application.fontpath";

    private static final String dataSourceDriver = "dataSource.driver";
    private static final String dataSourceUrl = "dataSource.url";
    private static final String dataSourceUser = "dataSource.username";
    private static final String dataSourcePass = "dataSource.password";

    public static boolean initialize() {
        if (initialize)
            return true;
        try {
            config = new HashMap<>();
            configMap = new LinkedHashMap<>();
            properties = new Properties();

            configMap.put(logbackConfig, "LogBack配置文件路径");
            configMap.put(logbackFilePath, "LogBack日志存放路径");
            configMap.put(nianViewsBase, "念的视图根路径");
            configMap.put(nianImageBase, "念的图片根路径");
            configMap.put(nianCacheBase, "念数据缓存路径");
            configMap.put(nianRenderModel, "念的数据渲染模式");
            configMap.put(nianRenderTemplate, "念的数据渲染模板");

            configMap.put(freemarkerTemplatePath, "FreeMarker模板路径");
            configMap.put(freemarkerTemplateEncode, "FreeMarker模板编码");
            configMap.put(applicationFontPath, "应用程序字体路径");
            /*
            configMap.put(dataSourceDriver, "数据库驱动类");
            configMap.put(dataSourceUrl, "数据库连接地址");
            configMap.put(dataSourceUser, "数据库用户");
            configMap.put(dataSourcePass, "数据库密码");
            */

            ClassPathResource resource = new ClassPathResource(configFile);
            properties.load(resource.getInputStream());
            for (String key : configMap.keySet()) {
                config.put(key, getProperty(key)); //读取配置参数
            }

            logger.info("加载程序配置文件[%s]完成", resource.getURL());
            initialize = true;
            return true;
        } catch (Exception e) {
            logger.error(String.format("加载程序配置文件[%s]失败", configFile), e);
            return false;
        } finally {
            properties = null;
            configMap = null;
        }
    }

    private static String getProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            String msg = String.format("读取[%s]失败：%s=null", configMap.get(key), key);
            logger.error(msg);
            throw new RuntimeException(msg);
        } else {
            System.setProperty(key, value);
            logger.info("读取[%s]成功：%s=%s", configMap.get(key), key, value);
        }
        return value;
    }


    public static String getConfigFile() {
        return configFile;
    }

    public static String getLogbackConfig() {
        return config.get(logbackConfig);
    }

    public static String getLogbackLogBase() {
        return config.get(logbackFilePath);
    }

    public static String getFreemarkerTemplatePath() {
        return config.get(freemarkerTemplatePath);
    }

    public static String getFreemarkerTemplateEncode() {
        return config.get(freemarkerTemplateEncode);
    }

    public static String getApplicationFontPath() {
        return config.get(applicationFontPath);
    }

    public static String getDataSourceDriver() {
        return config.get(dataSourceDriver);
    }

    public static String getDataSourceUrl() {
        return config.get(dataSourceUrl);
    }

    public static String getDataSourceUser() {
        return config.get(dataSourceUser);
    }

    public static String getDataSourcePass() {
        return config.get(dataSourcePass);
    }

    public static String getNianViewsBase() {
        return config.get(nianViewsBase);
    }

    public static String getNianCacheBase() {
        return config.get(nianCacheBase);
    }

    public static String getNianRenderModel() {
        return config.get(nianRenderModel);
    }

    public static String getNianRenderTemplate() {
        return config.get(nianRenderTemplate);
    }

    public static String getNianImageBase() {
        return config.get(nianImageBase);
    }
}
