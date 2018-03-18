package so.nian.backup.freemarker.function;

import java.io.UnsupportedEncodingException;
import java.util.List;

import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;

/**
 * FreeMarker自定义函数（FreeMarker Function）:subbytes<br/>
 * 按字节截取字符串长度，如果最后一个是中文字符并且即将被截断，则舍弃该半个字符，使用空格替换<br/>
 * 参数1：必输。字符集 。<br/>
 * 参数2：必输。待截取的字符串，为空则直接返回 。<br/>
 * 参数3：必输。起始字节位置。 <br/>
 * 参数4：可选。要截取的字节长度，没有取从偏移位置后面所有内容。<br/>
 * 
 * @author shumingl
 */
@SuppressWarnings({ "rawtypes" })
public class Subbytes implements TemplateMethodModel {

	public Object exec(List list) throws TemplateModelException {
		if (list == null || list.size() < 3)// 参数为空或长度小于3，抛出异常
			throw new TemplateModelException("[subbytes]参数错误。");

		try {
			// 第1个参数为编码
			String encode = (String) list.get(0);
			// 第2个参数为字符串
			String src = (String) list.get(1);
			if (src == null || "".equals(src)) return "";
			// 将字符串转换成目标字符集
			String string = new String(src.getBytes(encode), encode);
			
			byte[] bytes = string.getBytes(encode);

			// 第3个参数开始位置，索引下标的的计算从0开始
			Integer offset = Integer.parseInt((String) list.get(2));
			// 第4个可选参数，要截取的长度
			Integer length = bytes.length - offset;
			if (list.size() >= 3) length = Integer.parseInt((String) list.get(3));

			// 如果参数超过本身长度，则使用本身长度
			if (length + offset > bytes.length)
				length = bytes.length - offset;
			else {
				int len = 0;
				for (int i = 0; i < string.length(); i++) {
					int l = ((Character) string.charAt(i)).toString().getBytes(encode).length;
					if (len + l <= length)
						len += l;
				}
				length = len;
			}
			String ret = new String(bytes, offset, length, encode);
			return ret;
		} catch (UnsupportedEncodingException ex) {
			throw new TemplateModelException("[subbytes]不支持的字符集编码：" + list.get(0), ex);
		} catch (Exception ex) {
			throw new TemplateModelException("[subbytes]异常，参数：" + list, ex);
		}
	}
}
