package so.nian.backup.freemarker.function;

import java.util.List;
import java.util.Map;

import cn.onebank.pmts.bizz.query.service.QueryService;
import cn.onebank.pmts.utilities.jackson.JsonUtil;
import cn.onebank.pmts.utilities.spring.ContextUtil;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;

/**
 * FreeMarker自定义函数（FreeMarker Function）<br/>
 * 读取数据库码表SYS_CODE中配置的值<br/>
 * 参数1：必输。code_id<br/>
 * 参数2：必输。table_name<br/>
 * 
 * @author shumingl
 */
@SuppressWarnings({ "rawtypes" })
public class GetIbatisData implements TemplateMethodModel {

	public Object exec(List list) throws TemplateModelException {
		if (list == null || list.size() < 2)// 参数错误抛出异常
			throw new TemplateModelException("[FMFGetIbatisDataImpl]参数错误。");

		try {
			// 第1个参数为sqlId
			String sqlId = (String) list.get(0);
			// 第2个参数为参数json
			String jsonString = (String) list.get(1);
			// 第3个参数为返回值类型
			String type = (String) list.get(2);
			
			Map<String, Object> params = JsonUtil.json2Map(jsonString);
			
			// 查询码表
			QueryService queryService = ContextUtil.getBean("queryService");
			Object returnValue = null;
			if (type.equals("list")) {
				List<Object> value = queryService.query(sqlId, params);
				returnValue = value;
			} else if (type.equals("object")) {
				Object value = queryService.queryObject(sqlId, params);
				returnValue = value;
			}
			return returnValue;
		} catch (Exception ex) {
			throw new TemplateModelException("[FMFGetIbatisDataImpl]异常，参数：" + list, ex);
		}
	}
}
