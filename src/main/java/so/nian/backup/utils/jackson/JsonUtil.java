package so.nian.backup.utils.jackson;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked", "rawtypes"})
public class JsonUtil {
    public static String object2Json(Object objValue) {
        return JsonConverter.getInstance().object2Json(objValue);
    }

    public static <T> T json2Object(Class<T> targetClzz, String fieldName, String jsonString) {
        Object rtnValue = null;
        if (jsonString == null)
            return null;
        JsonConverter jsonConverter = JsonConverter.getInstance();
        if (fieldName == null) {
            rtnValue = jsonConverter.json2Object(targetClzz, jsonString);
        } else {
            JsonNode root = jsonConverter.json2JsonNode(jsonString);
            String[] fields = fieldName.split("/");

            JsonNode node = root;
            for (String field : fields) {
                node = node.get(field);
            }
            rtnValue = jsonConverter.json2Object(targetClzz, node);
        }
        return (T) rtnValue;
    }

    public static Map<String, Object> json2Map(String fieldName, String jsonString) {
        return ((Map<String, Object>) json2Object(HashMap.class, fieldName, jsonString));
    }

    public static Map<String, Object> json2Map(String jsonString) {
        return ((Map<String, Object>) json2Object(HashMap.class, null, jsonString));
    }

    public static <T> List<T> json2List(Class<T> targetClzz, String fieldName, String jsonString) {
        List<T> rtnValue = null;
        JsonConverter jsonConverter = JsonConverter.getInstance();
        if (fieldName == null) {
            rtnValue = jsonConverter.json2List(targetClzz, jsonString);
        } else {
            JsonNode root = jsonConverter.json2JsonNode(jsonString);
            rtnValue = jsonConverter.json2List(targetClzz, root.get(fieldName));
        }
        return rtnValue;
    }

    public static List<HashMap> json2MapList(String fieldName, String jsonString) {
        return json2List(HashMap.class, fieldName, jsonString);
    }

    public static JsonNode json2JsonNode(String jsonString) {
        return JsonConverter.getInstance().json2JsonNode(jsonString);
    }
}