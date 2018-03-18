package so.nian.backup.freemarker.function;

import java.util.List;

import cn.onebank.pmts.core.freemarker.factory.ReportTemplateFactory;

import cn.onebank.pmts.utilities.spring.ContextUtil;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;

/**
 * FreeMarker自定义函数（FreeMarker Function）:getTpl<br/>
 * 获取模板名称，不存在则使用默认值<br/>
 * 参数1：必输。模板文件名称<br/>
 * 参数2：必输。默认模板文件名<br/>
 *
 * @author shumingl
 */
@SuppressWarnings({"rawtypes"})
public class GetTemplate implements TemplateMethodModel {

    public Object exec(List list) throws TemplateModelException {
        if (list == null || list.size() < 2)// 参数错误抛出异常
            throw new TemplateModelException("[FMFGetTemplateImpl]参数错误。");

        // 第1个参数为模板名称
        String tplName = (String) list.get(0);
        String defaultTpl = (String) list.get(1);

        try {
            ReportTemplateFactory reportTemplateFactory = ContextUtil.getBean("reportTemplateFactory");
            if (reportTemplateFactory.isTemplateFileExists(tplName))
                return tplName;
            return defaultTpl;

        } catch (Exception ex) {
            return defaultTpl;
        }
    }
}
