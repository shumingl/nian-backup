package so.nian.backup.freemarker.function;

import freemarker.template.*;
import so.nian.backup.http.HttpResultEntity;
import so.nian.backup.http.NianHttpUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NianStepComments implements TemplateMethodModelEx {
    @Override
    public Object exec(List arguments) throws TemplateModelException {

        List<Map<String, Object>> temp = new ArrayList<>();
        SimpleScalar pStepId = (SimpleScalar) arguments.get(0);
        if (pStepId != null) {
            String stepId = pStepId.getAsString();
            System.out.printf("NianStepComments(%s)\n", stepId);
            int page = 1;
            while (true) {
                HttpResultEntity entity = NianHttpUtil.comments(stepId, page);
                if (entity.isSuccess()) {
                    Map<String, Object> data = (Map<String, Object>) entity.getResponseMap().get("data");
                    if (data != null) {
                        List<Map<String, Object>> comments = (List<Map<String, Object>>) data.get("comments");
                        if (comments == null || comments.size() == 0)
                            break;
                        else
                            temp.addAll(comments);
                        page++;
                    } else {
                        break;
                    }
                }
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = temp.size() - 1; i >= 0; i--)
            result.add(temp.get(i));
        return result;
    }
}
