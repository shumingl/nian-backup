/**
 *
 */
package so.nian.backup.freemarker.factory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;

import cn.onebank.pmts.core.config.AppConfig;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class StringTemplateFactory {

    private Configuration configuration;
    private StringTemplateLoader stringTemplateLoader;
    private final String TextTplPlaceHolder = "_StringTemplateContentPlaceHolder_";
    private String functionTplStr = "";
    private ReportTemplateFactory reportTemplateFactory;

    private String encoding = "UTF-8";

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public StringTemplateFactory() throws IOException {
        getInstance();
    }

    private Configuration getInstance() throws IOException {
        if (null == configuration) {
            configuration = new Configuration(Configuration.VERSION_2_3_23);
            // 设置编码
            configuration.setEncoding(Locale.CHINA, encoding);
            // 设置非空值，如不设置为True为空时会出错
            configuration.setClassicCompatible(true);

            // configuration.setDirectoryForTemplateLoading(file);
            stringTemplateLoader = new StringTemplateLoader();
            configuration.setTemplateLoader(stringTemplateLoader);
        }
        return configuration;
    }

    public Template getTemplate(String name) throws IOException {
        return configuration.getTemplate(name);
    }

    public Template getTemplate(String name, String encoding) throws IOException {
        return configuration.getTemplate(name, encoding);
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * 注册字符串模板
     *
     * @param tplName
     * @param tplContent
     */
    public void register(String tplName, String tplContent) {
        stringTemplateLoader.putTemplate(tplName, tplContent);
        configuration.setTemplateLoader(stringTemplateLoader);
    }

    /**
     * 加载函数模板内容
     */
    private void loadFunctionTemplate() {

        try {
            // 读取外层公共函数模板内容
            Object tplFile = reportTemplateFactory.getConfiguration().getTemplateLoader().findTemplateSource("functions.tpl");

            StringBuffer buffer = new StringBuffer();
            if (tplFile instanceof File) {
                File file = (File) tplFile;
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                reader.close();
                functionTplStr = buffer.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 注册配置文件中的FreeMarker 字符串模板
     *
     * @throws IOException
     */
    public void registerConfigTemplates() throws IOException {
        loadFunctionTemplate();
        Map<String, Object> tpls = AppConfig.getConfigTemplates();
        for (String configKey : tpls.keySet()) {
            // 读取配置文件中配置的内容
            String configContent = (String) tpls.get(configKey);
            String tplContent = functionTplStr.replace(TextTplPlaceHolder, configContent);
            // 注册模板
            stringTemplateLoader.putTemplate(configKey, tplContent);
        }
        configuration.setTemplateLoader(stringTemplateLoader);
    }

    /**
     * 生成配置参数值
     *
     * @param configKey 配置文件中的配置Key
     * @param dataModel 数据 dataModel
     * @return 解析 configKey 得到具体的值
     * @throws IOException       IO异常
     * @throws TemplateException 没有找到模板或其他
     */
    public String getConfigString(String configKey, Object dataModel) throws IOException, TemplateException {
        // 获取模板
        Template template = configuration.getTemplate(configKey);
        // 输出为字符串
        StringWriter writer = new StringWriter();
        template.process(dataModel, writer);
        writer.flush();
        writer.close();

        String value = writer.toString();
        value = (value == null ? "" : value);

        return value;
    }

    /**
     * 生成配置参数值
     *
     * @param sqlId
     * @param subKey
     * @param dataModel 数据 dataModel
     * @return 解析 sqlId + subKey 得到具体的值
     * @throws IOException       IO异常
     * @throws TemplateException 没有找到模板或其他
     */
    public String getConfigString(String sqlId, String subKey, Object dataModel) throws IOException, TemplateException {
        String str = String.format("%s.%s", sqlId, subKey);
        return getConfigString(str, dataModel);
    }

    public ReportTemplateFactory getReportTemplateFactory() {
        return reportTemplateFactory;
    }

    public void setReportTemplateFactory(ReportTemplateFactory reportTemplateFactory) {
        this.reportTemplateFactory = reportTemplateFactory;
    }

}
