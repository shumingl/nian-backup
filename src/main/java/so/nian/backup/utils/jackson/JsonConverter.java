package so.nian.backup.utils.jackson;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class JsonConverter {
	private static JsonConverter	jsonConverter	= new JsonConverter();
	private ObjectMapper			objMapper;
	private TypeFactory				typeFactory;

	private JsonConverter() {
		this.objMapper = new ObjectMapper();
		this.objMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		this.objMapper.configure(Feature.ALLOW_SINGLE_QUOTES, true);
		this.objMapper.configure(Feature.ALLOW_NON_NUMERIC_NUMBERS, true);
		this.objMapper.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		this.objMapper.configure(Feature.ALLOW_NUMERIC_LEADING_ZEROS, true);
		this.objMapper.configure(Feature.ALLOW_MISSING_VALUES, true);
		this.typeFactory = TypeFactory.defaultInstance();
	}

	public static JsonConverter getInstance() {
		return jsonConverter;
	}

	public String object2Json(Object objValue) {
		String rtnValue = null;
		try {
			rtnValue = this.objMapper.writeValueAsString(objValue);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		return rtnValue;
	}

	public JsonNode json2JsonNode(String jsonString) {
		try {
			return ((JsonNode) this.objMapper.readValue(jsonString, JsonNode.class));
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public <T> T json2Object(Class<T> targetClzz, Object jsonObject) {
		try {
			return this.objMapper.readValue(jsonObject.toString(), targetClzz);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public <T> List<T> json2List(Class<T> targetClzz, Object jsonObject) {
		try {
			return ((List<T>) this.objMapper.readValue(jsonObject.toString(), this.typeFactory.constructParametricType(ArrayList.class, new Class[] { targetClzz })));
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}