package so.nian.backup.freemarker.function;

import java.util.List;

import cn.onebank.pmts.core.freemarker.factory.ReportTemplateFactory;
import cn.onebank.pmts.utilities.spring.ContextUtil;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;

/**
 * FreeMarker自定义函数（FreeMarker Function）:tplExists<br/>
 * 判断模板是否存在<br/>
 * 参数1：必输。模板文件名<br/>
 *
 * @author shumingl
 */
@SuppressWarnings({"rawtypes"})
public class TemplateExists implements TemplateMethodModel {

    public Object exec(List list) throws TemplateModelException {
        if (list == null || list.size() < 1)// 参数错误抛出异常
            throw new TemplateModelException("[TemplateExists]参数错误。");

        try {
            // 第1个参数为模板名称
            String tplName = (String) list.get(0);
            ReportTemplateFactory reportTemplateFactory = ContextUtil.getBean("reportTemplateFactory");
            return reportTemplateFactory.isTemplateFileExists(tplName);
        } catch (Exception ex) {
            return false;
        }
    }
}
