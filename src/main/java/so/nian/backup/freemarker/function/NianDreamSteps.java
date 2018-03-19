package so.nian.backup.freemarker.function;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import so.nian.backup.http.HttpResultEntity;
import so.nian.backup.http.NianHttpUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NianDreamSteps implements TemplateMethodModelEx {
    @Override
    public Object exec(List arguments) throws TemplateModelException {

        List<Map<String, Object>> temp = new ArrayList<>();
        SimpleScalar pStepId = (SimpleScalar) arguments.get(0);
        if (pStepId != null) {
            String stepId = pStepId.getAsString();
            int idx = 0;
            while (true) {
                HttpResultEntity entity = NianHttpUtil.comments(stepId, idx);
                if (entity.isSuccess()) {
                    Map<String, Object> data = (Map<String, Object>) entity.getResponseMap().get("data");
                    if (data != null) {
                        List<Map<String, Object>> steps = (List<Map<String, Object>>) data.get("steps");
                        if (steps == null || steps.size() == 0)
                            break;
                        else {
                            temp.addAll(steps);
                        }
                        idx++;
                    } else {
                        break;
                    }
                }
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = temp.size() - 1; i >= 0; i--)
            result.add(temp.get(i));
        System.out.println("NianStepComments comments:" + result);
        return result;
    }
}
