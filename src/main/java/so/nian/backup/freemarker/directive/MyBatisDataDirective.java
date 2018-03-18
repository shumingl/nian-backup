package so.nian.backup.freemarker.directive;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.onebank.pmts.core.freemarker.TemplateModelUtil;
import cn.onebank.pmts.provider.report.ReportCache;
import cn.onebank.pmts.bizz.query.service.QueryService;
import cn.onebank.pmts.utilities.ResultSplitUtil;
import cn.onebank.pmts.utilities.spring.ContextUtil;

import freemarker.template.TemplateException;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelIterator;
import freemarker.template.TemplateNumberModel;
import freemarker.template.TemplateScalarModel;

/**
 * <pre>
 * <h3><font color="blue">FreeMarker自定义指令（ibatis），用于查询数据</font></h3>
 * <font color="red">一、指令参数：</font>
 * 1. sqlid;   必输，用于查询数据
 * 2. ispage;  必输，用于判断是否要分页读取
 * 3. params;  可选，params/address 二选一，优先使用params
 * 4. address; 可选，params/address 二选一，优先使用params
 * 5. fields;  可选，用于切割查询结果
 *
 * <font color="red">二、内置变量：</font>
 * 1. data;    查询结果。data.size 查询的数据量，data.rows 被拆分后的数据行数，没有或为空显示0；data.list 数据本身。
 * 2. page;    分页参数。page.index 当前页码；page.next 是否有下一页；page.size 分页大小（不是数据量）
 *
 * <font color="red">三、用法举例：</font>
 * <font color="green">声明查询参数：</font>
 * <#assign args={'areano':'40004','period':'20161229'}/>
 * <font color="green">声明分割参数：</font>
 * <#assign flds={'acct_no':24,'br_no':5}/>
 * <font color="green">开始查询数据：</font>
 * <@ibatis sqlid="queryDataSqlID1" ispage=true params=args address=data.address fields=flds>
 * <font color="green">检查结果集大小：</font>
 * <#if data.size gt 0>
 * <font color="green">遍历结果集：</font>
 * <#list data.list as str>
 * <font color="green">输出每一行数据：</font>
 * ${str_index?string('000')}.[${(str!'')?right_pad(fields.acct_no?number, '+')}]
 * &lt;/#list>
 * &lt;/#if>
 * &lt;/@ibatis>
 * </pre>
 *
 * @author shumingl
 */
@SuppressWarnings("unchecked")
public class MyBatisDataDirective extends BaseDirective {

    @Override
    public void execute(DirectiveHandler handler) throws TemplateException, IOException {

        String address = "";
        Map<String, Object> params = new HashMap<String, Object>();
        Map<String, Object> fields = new HashMap<String, Object>();

        // sqlid
        String sqlid = handler.getString("sqlid");
        if (sqlid == null)
            throw new TemplateException("参数[sqlid]不能为空。", handler.getEnvironment());

        // 获取和解析参数
        TemplateHashModelEx argsFields = (TemplateHashModelEx) handler.getMap("params");
        if (argsFields != null) { // 模板参数
            TemplateModelIterator iterator2 = argsFields.keys().iterator();
            while (iterator2.hasNext()) {
                String key = ((TemplateScalarModel) iterator2.next()).getAsString();
                params.put(key, TemplateModelUtil.getString(argsFields.get(key)));
            }
        }
        //LogConsole.getInstance().info("%s", params);

        // 获取拆分字段长度Map
        TemplateHashModelEx hashFields = (TemplateHashModelEx) handler.getMap("fields");
        if (hashFields != null) {
            TemplateModelIterator iterator1 = hashFields.keys().iterator();
            while (iterator1.hasNext()) {
                String key = ((TemplateScalarModel) iterator1.next()).getAsString();
                fields.put(key, ((TemplateNumberModel) hashFields.get(key)).getAsNumber().intValue());
            }
        }

        // 参数回写，以便能够在标签范围内使用
        handler.put("sqlid", sqlid);
        handler.put("params", params);
        handler.put("fields", fields);
        handler.put("address", address);

        // 是否分页
        Boolean ispage = handler.getBoolean("ispage");
        if (ispage == null)
            throw new TemplateException("参数[ispage]不能为空。", handler.getEnvironment());

        if (ispage)
            this.renderPageData(handler, sqlid, params, address, fields);
        else
            this.renderFullData(handler, sqlid, params, address, fields);

    }

    /**
     * 读取分页数据
     *
     * @param handler
     * @throws TemplateException
     * @throws IOException
     */
    public void renderPageData(DirectiveHandler handler, String sqlid, Map<String, Object> params, String address,
                               Map<String, Object> fields) throws TemplateException, IOException {

        int pageno = 1;

        // 获取页面大小配置
        String objval = ReportCache.config(sqlid, "query.pageSize", null);
        Integer pageSize = Integer.parseInt(objval);

        // 判断是否需要分割数据
        boolean issplit = false;
        if (fields != null && fields.size() > 0)
            issplit = true;

        Map<String, Object> data = new HashMap<String, Object>();
        Map<String, Object> page = new HashMap<String, Object>();

        QueryService queryService = ContextUtil.getBean("queryService");
        boolean hasnext = true;
        while (hasnext) {
            // 读取分页数据
            List<Map<String, Object>> list = queryService.query(sqlid, params, pageno);
            List<Map<String, Object>> result = null;
            // 如果当前页的数据量小于分页大小，则认定没有下一页数据了
            if (list == null || list.size() < pageSize)
                hasnext = false;

            // 分割数据
            if (issplit)
                result = ResultSplitUtil.split(list, fields);
            else
                result = list;

            data.put("list", result);
            data.put("rows", result == null ? 0 : result.size());
            data.put("size", list == null ? 0 : list.size());

            page.put("index", pageno);
            page.put("next", hasnext);
            page.put("size", pageSize);

            handler.put("result", data);
            handler.put("page", page);

            handler.render();

            pageno++;
        }
    }

    /**
     * 读取全部数据
     *
     * @param handler
     * @throws TemplateException
     * @throws IOException
     */
    public void renderFullData(DirectiveHandler handler, String sqlid, Map<String, Object> params, String address,
                               Map<String, Object> fields) throws TemplateException, IOException {

        // 判断是否需要分割数据
        boolean issplit = false;
        if (fields != null && fields.size() > 0)
            issplit = true;

        Map<String, Object> data = new HashMap<String, Object>();
        Map<String, Object> page = new HashMap<String, Object>();

        QueryService queryService = ContextUtil.getBean("queryService");
        List<Map<String, Object>> list = queryService.query(sqlid, params);
        List<Map<String, Object>> result = null;

        // 分割数据
        if (issplit)
            result = ResultSplitUtil.split(list, fields);
        else
            result = list;

        data.put("list", result);
        data.put("size", list == null ? 0 : list.size());
        data.put("rows", result == null ? 0 : result.size());

        page.put("index", 1);
        page.put("next", false);
        page.put("size", 0);

        handler.put("result", data);
        handler.put("page", page);

        handler.render();
    }

    public Object getValue(TemplateModel model) {
        return null;
    }
}