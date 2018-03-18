package so.nian.backup.freemarker.function;

import java.util.List;

import cn.onebank.pmts.utilities.StringUtil;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;

/**
 * FreeMarker自定义函数（FreeMarker Function）:bytespad<br/>
 * 字符串按照字节数对齐，用于竖线文件生成<br/>
 * 参数1：必输。编码<br/>
 * 参数2：必输。字符串<br/>
 * 参数3：必输。对齐长度<br/>
 * 参数4：选输。对齐方式<br/>
 * 参数5：选输。填充字符<br/>
 * 
 * @author shumingl
 */
@SuppressWarnings({ "rawtypes" })
public class BytesPad implements TemplateMethodModel {

	public Object exec(List list) throws TemplateModelException {
		if (list == null || list.size() < 3)// 参数错误抛出异常
			throw new TemplateModelException("[bytespad]参数错误。");

		try {
			String str = "";

			// 第1个参数为编码 encode
			String  encode = (String) list.get(0);
			 encode = ( encode == null ? "" :  encode);

			// 第2个参数为string
			String string = (String) list.get(1);
			string = (string == null ? "" : string);

			// 第3个参数为对齐长度
			str = (String) list.get(2);
			Integer length = Integer.parseInt(str);
			if (list.size() == 3)
				return StringUtil.StringPadB(encode, string, length);

			// 第4个参数为对齐方式
			Integer type = -1;
			if (list.size() == 4) {
				str = (String) list.get(3);
				type = Integer.parseInt(str);
				return StringUtil.StringPadB(encode, string, length, type);
			}

			// 第5个参数为填充字符
			Character fillchr = ' ';
			if (list.size() >= 5) {
				str = (String) list.get(3);
				type = Integer.parseInt(str);
				fillchr = ((String) list.get(4)).charAt(0);
			}

			return StringUtil.StringPadB(encode, string, length, type, fillchr);

		} catch (Exception ex) {
			throw new TemplateModelException("[bytespad]异常，参数：" + list, ex);
		}
	}
}
