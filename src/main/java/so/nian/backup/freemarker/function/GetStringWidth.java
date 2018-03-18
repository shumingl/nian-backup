package so.nian.backup.freemarker.function;

import java.util.List;

import cn.onebank.pmts.utilities.FontUtil;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;

/**
 * FreeMarker自定义函数（FreeMarker Function）<br/>
 * 获取字符的像素宽度，返回ASCII字符的像素宽度整数倍<br/>
 * 参数1：必输。字符串<br/>
 * 
 * @author shumingl
 */
@SuppressWarnings({ "rawtypes" })
public class GetStringWidth implements TemplateMethodModel {

	public Object exec(List list) throws TemplateModelException {
		if (list == null || list.size() < 1)// 参数错误抛出异常
			throw new TemplateModelException("[FMFGetStringWidthImpl]参数错误。");

		try {
			// 第1个参数为string
			String string = (String) list.get(0);
			Integer width = FontUtil.getStringWidth(string);
			return width;
		} catch (Exception ex) {
			throw new TemplateModelException("[FMFGetStringWidthImpl]异常，参数：" + list, ex);
		}
	}
}
