package so.nian.backup.freemarker.function;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class NianDealContent implements TemplateMethodModelEx {
    @Override
    public Object exec(List arguments) throws TemplateModelException {
        SimpleScalar pContent = (SimpleScalar) arguments.get(0);
        if (pContent != null) {
            String content = pContent.getAsString();
            return content.replace("&amp;nbsp;", " ").replace("&nbsp;", " ").replace("&lt;", "<").replace("&gt;", ">");
        } else
            return null;
    }
}
