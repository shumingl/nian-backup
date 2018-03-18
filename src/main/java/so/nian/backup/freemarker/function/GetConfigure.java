package so.nian.backup.freemarker.function;

import java.util.List;
import java.util.Map;

import cn.onebank.pmts.provider.report.ReportCache;

import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;

/**
 * FreeMarker自定义函数（FreeMarker Function）<br/>
 * 读取程序运行期间配置<br/>
 * 参数1：必输。sqlId/taskId...<br/>
 * 参数2：必输。name<br/>
 * 
 * @author shumingl
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class GetConfigure implements TemplateMethodModel {

	public Object exec(List list) throws TemplateModelException {
		if (list == null || list.size() < 1)// 参数错误抛出异常
			throw new TemplateModelException("[FMFGetConfigureImpl]参数错误。");

		try {
			// 第1个参数为configKey
			String key = (String) list.get(0);
			String value = "";
			// 如果有第2个参数
			if (list.size() >= 2) {
				// 第2个参数为address,根据address获取DataCache
				String address = (String) list.get(1);
				Map<String, Object> params = (Map<String, Object>) ReportCache.data(address);
				value = ReportCache.config(key, params);
			} else {
				value = ReportCache.config(key, null);
			}
			value = (value == null ? "" : value);
			return value;
		} catch (Exception ex) {
			throw new TemplateModelException("[FMFGetConfigureImpl]异常，参数：" + list, ex);
		}
	}
}
