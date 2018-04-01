package so.nian.backup.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DataUtil {

    private static final ExpressionParser parser = ExpressionParser.getDefault();

    /**
     * 将最新数据根据某些字段的值进行合并
     *
     * @param cacheData  缓存数据
     * @param newestData 最新数据
     * @param fieldExpr  字段表达式
     * @return
     */
    public static List<Map<String, Object>> merge(
            List<Map<String, Object>> cacheData, List<Map<String, Object>> newestData, String fieldExpr) {

        if (newestData == null || newestData.size() == 0) return cacheData;
        if (cacheData == null || cacheData.size() == 0) return newestData;
        Map<String, Map<String, Object>> resultDataMap = new LinkedHashMap<>();
        for (Map<String, Object> value : cacheData)
            resultDataMap.put(parser.parse(fieldExpr, value), value);
        for (Map<String, Object> value : newestData)
            resultDataMap.put(parser.parse(fieldExpr, value), value);
        List<Map<String, Object>> result = new ArrayList<>();
        for (String key : resultDataMap.keySet())
            result.add(resultDataMap.get(key));
        return result;

    }

    /**
     * 对List&lt;Map&gt;结构进行排序，排序字段需要为数字
     *
     * @param list  待排序list
     * @param field 排序字段
     * @param type  排序方式：1-升序；-1-降序；
     * @return
     */
    public static List<Map<String, Object>> sort(List<Map<String, Object>> list, final String field, int type) {
        if (list == null || list.size() == 0) return list;
        list.sort((left, right) -> {
            if (left == null || right == null) throw new RuntimeException("排序数据不能为空");
            Long leftval = Long.valueOf((String) left.get(field));
            Long rightval = Long.valueOf((String) right.get(field));
            if (leftval > rightval) return type;
            else if (leftval < rightval) return 0 - type;
            else return 0;
        });
        return list;
    }

}
