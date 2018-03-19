package so.nian.backup.freemarker;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;
import freemarker.template.TemplateScalarModel;
import org.apache.commons.lang3.StringUtils;

public class TemplateModelUtil {

	public static ThreadLocal<DateFormat> FULL_DATE_FORMAT = new ThreadLocal<DateFormat>();
	public static final int               FULL_DATE_LENGTH = 19;

	public static ThreadLocal<DateFormat> SHORT_DATE_FORMAT = new ThreadLocal<DateFormat>();
	public static final int               SHORT_DATE_LENGTH = 10;

	static {
		FULL_DATE_FORMAT.set(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
		SHORT_DATE_FORMAT.set(new SimpleDateFormat("yyyy-MM-dd"));
	}
	
	/**
	 * @param model
	 * @return
	 * @throws TemplateModelException
	 */
	public static TemplateHashModel getMap(TemplateModel model) throws TemplateModelException {
		if (null == model) {
			return null;
		}
		if (model instanceof TemplateHashModelEx) {
			return (TemplateHashModelEx) model;
		} else if (model instanceof TemplateHashModel) {
			return (TemplateHashModel) model;
		} else {
			return null;
		}
	}

	/**
	 * @param model
	 * @param defaultValue
	 * @return
	 * @throws TemplateException
	 */
	public static String getString(TemplateModel model, String defaultValue) throws TemplateException {
		String result = getString(model);
		if (null == result)
			return defaultValue;
		else
			return result;
	}

	/**
	 * @param model
	 * @return
	 * @throws TemplateException
	 */
	public static String getString(TemplateModel model) throws TemplateException {
		if (null == model) {
			return null;
		}
		if (model instanceof TemplateScalarModel) {
			return ((TemplateScalarModel) model).getAsString();
		} else if ((model instanceof TemplateNumberModel)) {
			return ((TemplateNumberModel) model).getAsNumber().toString();
		} else {
			return null;
		}
	}

	/**
	 * @param model
	 * @param defaultValue
	 * @return
	 * @throws TemplateException
	 */
	public static Integer getInteger(TemplateModel model, int defaultValue) throws TemplateException {
		Integer result = getInteger(model);
		if (null == result)
			return defaultValue;
		else
			return result;
	}

	/**
	 * @param model
	 * @return
	 * @throws TemplateException
	 */
	public static Integer getInteger(TemplateModel model) throws TemplateException {
		if (null == model) {
			return null;
		}
		if (model instanceof TemplateNumberModel) {
			return ((TemplateNumberModel) model).getAsNumber().intValue();
		} else if (model instanceof TemplateScalarModel) {
			String s = ((TemplateScalarModel) model).getAsString();
			if (StringUtils.isBlank(s)) {
				return null;
			}
			try {
				return Integer.parseInt(s);
			} catch (NumberFormatException e) {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * @param model
	 * @return
	 * @throws TemplateException
	 */
	public static Short getShort(TemplateModel model) throws TemplateException {
		if (null == model) {
			return null;
		}
		if (model instanceof TemplateNumberModel) {
			return ((TemplateNumberModel) model).getAsNumber().shortValue();
		} else if (model instanceof TemplateScalarModel) {
			String s = ((TemplateScalarModel) model).getAsString();
			if (StringUtils.isBlank(s)) {
				return null;
			}
			try {
				return Short.parseShort(s);
			} catch (NumberFormatException e) {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * @param model
	 * @return
	 * @throws TemplateException
	 */
	public static Long getLong(TemplateModel model) throws TemplateException {
		if (null == model) {
			return null;
		}
		if (model instanceof TemplateNumberModel) {
			return ((TemplateNumberModel) model).getAsNumber().longValue();
		} else if (model instanceof TemplateScalarModel) {
			String s = ((TemplateScalarModel) model).getAsString();
			if (StringUtils.isBlank(s)) {
				return null;
			}
			try {
				return Long.parseLong(s);
			} catch (NumberFormatException e) {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * @param model
	 * @return
	 * @throws TemplateException
	 */
	public static Double getDouble(TemplateModel model) throws TemplateException {
		if (null == model) {
			return null;
		}
		if (model instanceof TemplateNumberModel) {
			return ((TemplateNumberModel) model).getAsNumber().doubleValue();
		} else if (model instanceof TemplateScalarModel) {
			String s = ((TemplateScalarModel) model).getAsString();
			if (StringUtils.isBlank(s)) {
				return null;
			}
			try {
				return Double.parseDouble(s);
			} catch (NumberFormatException e) {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * @param model
	 * @return
	 * @throws TemplateException
	 */
	public static Integer[] getIntegerArray(TemplateModel model) throws TemplateException {
		String[] arr = getStringArray(model);
		if (null != arr) {
			Integer[] ids = new Integer[arr.length];
			int i = 0;
			try {
				for (String s : arr) {
					ids[i++] = Integer.valueOf(s);
				}
				return ids;
			} catch (NumberFormatException e) {
				return null;
			}
		} else
			return null;

	}

	/**
	 * @param model
	 * @return
	 * @throws TemplateException
	 */
	public static Long[] getLongArray(TemplateModel model) throws TemplateException {
		String[] arr = getStringArray(model);
		if (null != arr) {
			Long[] ids = new Long[arr.length];
			int i = 0;
			try {
				for (String s : arr) {
					ids[i++] = Long.valueOf(s);
				}
				return ids;
			} catch (NumberFormatException e) {
				return null;
			}
		} else
			return null;

	}

	/**
	 * @param model
	 * @return
	 * @throws TemplateException
	 */
	public static String[] getStringArray(TemplateModel model) throws TemplateException {
		String str = getString(model);
		if (StringUtils.isBlank(str)) {
			return null;
		}
		return StringUtils.split(str, ',');
	}

	/**
	 * @param model
	 * @param defaultValue
	 * @return
	 * @throws TemplateException
	 */
	public static Boolean getBoolean(TemplateModel model, Boolean defaultValue) throws TemplateException {
		Boolean result = getBoolean(model);
		if (null == result)
			return defaultValue;
		else
			return result;
	}

	/**
	 * @param model
	 * @return
	 * @throws TemplateException
	 */
	public static Boolean getBoolean(TemplateModel model) throws TemplateException {
		if (null == model) {
			return null;
		}
		if (model instanceof TemplateBooleanModel) {
			return ((TemplateBooleanModel) model).getAsBoolean();
		} else if (model instanceof TemplateNumberModel) {
			return !(0 == ((TemplateNumberModel) model).getAsNumber().intValue());
		} else if (model instanceof TemplateScalarModel) {
			String s = ((TemplateScalarModel) model).getAsString();
			if (StringUtils.isNotBlank(s)) {
				return !("0".equals(s) || "false".equalsIgnoreCase(s));
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * @param model
	 * @return
	 * @throws TemplateException
	 */
	public static Date getDate(TemplateModel model) throws TemplateException {
		if (null == model) {
			return null;
		}
		if (model instanceof TemplateDateModel) {
			return ((TemplateDateModel) model).getAsDate();
		} else if (model instanceof TemplateScalarModel) {
			String temp = StringUtils.trimToEmpty(((TemplateScalarModel) model).getAsString());
			try {
				if (FULL_DATE_LENGTH == temp.length()) {
					return FULL_DATE_FORMAT.get().parse(temp);
				} else if (SHORT_DATE_LENGTH == temp.length()) {
					return SHORT_DATE_FORMAT.get().parse(temp);
				} else {
					return null;
				}
			} catch (ParseException e) {
				return null;
			}
		} else {
			return null;
		}
	}

}
