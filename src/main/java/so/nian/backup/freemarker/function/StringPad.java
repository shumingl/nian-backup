package so.nian.backup.freemarker.function;

import java.util.List;

import cn.onebank.pmts.utilities.FontUtil;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;

/**
 * FreeMarker自定义函数（FreeMarker Function）:stringpad<br/>
 * 字符串对齐显示，需要文字对齐的报表<br/>
 * 参数1：必输。字符串<br/>
 * 参数2：必输。对齐长度<br/>
 * 参数3：选输。对齐方式<br/>
 * 参数4：选输。填充字符<br/>
 *
 * @author shumingl
 */
@SuppressWarnings({"rawtypes"})
public class StringPad implements TemplateMethodModel {

    public Object exec(List list) throws TemplateModelException {
        if (list == null || list.size() < 2)// 参数错误抛出异常
            throw new TemplateModelException("[stringpad]参数错误。");

        try {
            String str = "";

            // 第1个参数为string
            String string = (String) list.get(0);
            string = (string == null ? "" : string);

            // 第2个参数为对齐长度
            str = (String) list.get(1);
            Integer length = Integer.parseInt(str);
            if (list.size() == 2)
                return FontUtil.StringPadW(string, length);

            // 第3个参数为对齐方式
            Integer type = -1;
            if (list.size() == 3) {
                str = (String) list.get(2);
                type = Integer.parseInt(str);
                return FontUtil.StringPadW(string, length, type);
            }

            // 第4个参数为填充字符
            Character fillchr = ' ';
            if (list.size() >= 4) {
                str = (String) list.get(2);
                type = Integer.parseInt(str);
                fillchr = ((String) list.get(3)).charAt(0);
            }

            return FontUtil.StringPadW(string, length, type, fillchr);

        } catch (Exception ex) {
            throw new TemplateModelException("[stringpad]异常，参数：" + list, ex);
        }
    }
}
