/**
 *
 */
package so.nian.backup.freemarker.factory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Locale;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.core.io.ClassPathResource;
import so.nian.backup.utils.StringUtil;

public class ReportTemplateFactory {

    private Configuration configuration;

    private String encoding = "UTF-8";
    private String templatePath = "";

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public ReportTemplateFactory() throws IOException {
        ClassPathResource resource = new ClassPathResource("config/templates/");
        templatePath = resource.getPath();
        getInstance(resource.getFile());
    }

    public ReportTemplateFactory(String templatePath) throws IOException {
        this.templatePath = templatePath;
        getInstance(new File(this.templatePath));
    }

    private Configuration getInstance(File file) throws IOException {
        if (null == configuration) {
            configuration = new Configuration(Configuration.VERSION_2_3_23);
            // 设置编码
            configuration.setEncoding(Locale.CHINA, encoding);
            // 设置非空值，如不设置为True为空时会出错
            configuration.setClassicCompatible(true);
            configuration.setDirectoryForTemplateLoading(file);
        }
        return configuration;
    }

    public Template getTemplate(String name) throws IOException {
        return configuration.getTemplate(name);
    }

    public void process(Template template, String outfile, Object object, String encoding)
            throws IOException, TemplateException {
        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile, true), encoding));
        template.process(object, writer);
        writer.flush();
        writer.close();
    }

    /**
     * 判断模板文件是否存在
     *
     * @param filename 模板文件名
     * @return
     */
    public boolean isTemplateFileExists(String filename) {
        String fullname = StringUtil.generatePath(templatePath, filename);
        File file = new File(fullname);
        if (file.exists() && file.isFile())
            return true;
        return false;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public String getTemplatePath() {
        return templatePath;
    }

}
