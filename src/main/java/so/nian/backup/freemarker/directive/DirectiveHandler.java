package so.nian.backup.freemarker.directive;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import cn.onebank.pmts.core.freemarker.TemplateModelUtil;
import org.apache.commons.lang3.StringUtils;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

@SuppressWarnings("unused")
public class DirectiveHandler {
    private Environment environment;
    private Map<String, TemplateModel> parameters;
    private TemplateModel[] loopVars;
    private TemplateDirectiveBody templateDirectiveBody;
    private Map<String, Object> datamap;

    /**
     * @param environment env
     * @param parameters 参数
     * @param loopVars 循环变量
     * @param templateDirectiveBody 指令body
     */
    public DirectiveHandler(Environment environment, Map<String, TemplateModel> parameters,
                            TemplateModel[] loopVars, TemplateDirectiveBody templateDirectiveBody) {

        datamap = new HashMap<String, Object>();
        this.environment = environment;
        this.loopVars = loopVars;
        this.parameters = parameters;
        this.templateDirectiveBody = templateDirectiveBody;
    }

    /**
     * 渲染
     *
     * @throws IOException
     * @throws TemplateException
     */
    public void render() throws IOException, TemplateException {
        Map<String, TemplateModel> reduceMap = reduce();
        if (null != templateDirectiveBody)
            templateDirectiveBody.render(environment.getOut());
        reduce(reduceMap);
    }

    /**
     * 控制变量不为空时，导出所有变量
     *
     * @param notEmptyObject 非空对象
     * @throws IOException
     * @throws TemplateException
     */
    public void renderIfNotNull(Object notEmptyObject) throws IOException, TemplateException {
        if (null != notEmptyObject) {
            render();
        }
    }

    /**
     * 打印变量
     *
     * @param str
     * @throws IOException
     * @throws TemplateException
     */
    public void print(String str) throws IOException, TemplateException {
        environment.getOut().append(str);
    }

    /**
     * @param key
     * @param value
     * @return
     */
    public DirectiveHandler put(String key, Object value) {
        datamap.put(key, value);
        return this;
    }

    /**
     * @return
     * @throws TemplateModelException
     */
    private Map<String, TemplateModel> reduce() throws TemplateModelException {
        Map<String, TemplateModel> reduceMap = new HashMap<String, TemplateModel>();
        for (String key : datamap.keySet()) {
            TemplateModel value = environment.getVariable(key);
            if (null != value)
                reduceMap.put(key, environment.getVariable(key));
            environment.setVariable(key, environment.getObjectWrapper().wrap(datamap.get(key)));
        }
        return reduceMap;
    }

    /**
     * @param map
     * @throws TemplateModelException
     */
    private void reduce(Map<String, TemplateModel> map) throws TemplateModelException {
        for (String key : map.keySet()) {
            environment.setVariable(key, map.get(key));
        }
    }

    /**
     * @param name
     * @return
     * @throws TemplateModelException
     */
    public TemplateHashModel getMap(String name) throws TemplateModelException {
        TemplateModel model = parameters.get(name);
        return TemplateModelUtil.getMap(model);
    }

    /**
     * @param name name
     * @param defaultValue defaultValue
     * @return
     * @throws TemplateException
     */
    public String getString(String name, String defaultValue) throws TemplateException {
        String result = getString(name);
        if (null == result)
            return defaultValue;
        else
            return result;
    }

    /**
     * @param name
     * @return
     * @throws TemplateException
     */
    public String getString(String name) throws TemplateException {
        TemplateModel model = parameters.get(name);
        return TemplateModelUtil.getString(model);
    }

    /**
     * @param name
     * @param defaultValue
     * @return
     * @throws TemplateException
     */
    public Integer getInteger(String name, int defaultValue) throws TemplateException {
        Integer result = getInteger(name);
        if (null == result)
            return defaultValue;
        else
            return result;
    }

    /**
     * @param name
     * @return
     * @throws TemplateException
     */
    public Integer getInteger(String name) throws TemplateException {
        TemplateModel model = parameters.get(name);
        return TemplateModelUtil.getInteger(model);
    }

    /**
     * @param name
     * @return
     * @throws TemplateException
     */
    public Short getShort(String name) throws TemplateException {
        TemplateModel model = parameters.get(name);
        return TemplateModelUtil.getShort(model);
    }

    /**
     * @param name
     * @return
     * @throws TemplateException
     */
    public Long getLong(String name) throws TemplateException {
        TemplateModel model = parameters.get(name);
        return TemplateModelUtil.getLong(model);
    }

    /**
     * @param name
     * @return
     * @throws TemplateException
     */
    public Double getDouble(String name) throws TemplateException {
        TemplateModel model = parameters.get(name);
        return TemplateModelUtil.getDouble(model);
    }

    /**
     * @param name
     * @return
     * @throws TemplateException
     */
    public Integer[] getIntegerArray(String name) throws TemplateException {
        String[] arr = getStringArray(name);
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
     * @param name
     * @return
     * @throws TemplateException
     */
    public Long[] getLongArray(String name) throws TemplateException {
        String[] arr = getStringArray(name);
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
     * @param name
     * @return
     * @throws TemplateException
     */
    public String[] getStringArray(String name) throws TemplateException {
        String str = getString(name);
        if (StringUtils.isBlank(str)) {
            return null;
        }
        return StringUtils.split(str, ',');
    }

    /**
     * @param name
     * @param defaultValue
     * @return
     * @throws TemplateException
     */
    public Boolean getBoolean(String name, Boolean defaultValue) throws TemplateException {
        Boolean result = getBoolean(name);
        if (null == result)
            return defaultValue;
        else
            return result;
    }

    /**
     * @param name
     * @return
     * @throws TemplateException
     */
    public Boolean getBoolean(String name) throws TemplateException {
        TemplateModel model = parameters.get(name);
        return TemplateModelUtil.getBoolean(model);
    }

    /**
     * @param name
     * @return
     * @throws TemplateException
     */
    public Date getDate(String name) throws TemplateException {
        TemplateModel model = parameters.get(name);
        return TemplateModelUtil.getDate(model);
    }

    /**
     * @return the parameters
     */
    public Map<String, TemplateModel> getParameters() {
        return parameters;
    }

    /**
     * @return the environment
     */
    public Environment getEnvironment() {
        return environment;
    }

    /**
     * @return the templateDirectiveBody
     */
    public TemplateDirectiveBody getTemplateDirectiveBody() {
        return templateDirectiveBody;
    }

    public TemplateModel[] getLoopVars() {
        return loopVars;
    }

    public void setLoopVars(TemplateModel[] loopVars) {
        this.loopVars = loopVars;
    }
}