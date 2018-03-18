package so.nian.backup.freemarker.function;

import java.util.List;

import cn.onebank.pmts.utilities.ResultSplitUtil;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;

/**
 * FreeMarker自定义函数（FreeMarker Function）:strsplit<br/>
 * 字符串根据字符宽度进行分割<br/>
 * 参数1：必输。字符串<br/>
 * 参数2：必输。长度<br/>
 *
 * @author shumingl
 */
@SuppressWarnings({"rawtypes"})
public class StringSplit implements TemplateMethodModel {

    public Object exec(List list) throws TemplateModelException {
        if (list == null || list.size() < 2)// 参数错误抛出异常
            throw new TemplateModelException("[strsplit]参数错误。");

        try {
            String str = "";

            // 第1个参数为string
            String string = (String) list.get(0);
            string = (string == null ? "" : string);

            // 第2个参数为长度
            str = (String) list.get(1);
            Integer length = Integer.parseInt(str);

            return ResultSplitUtil.split(string, length);

        } catch (Exception ex) {
            throw new TemplateModelException("[strsplit]异常，参数：" + list, ex);
        }
    }
}
