package so.nian.backup.freemarker.function;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class NianDateFromat implements TemplateMethodModelEx {
    @Override
    public Object exec(List arguments) throws TemplateModelException {
        SimpleScalar pDateStr = (SimpleScalar) arguments.get(0);
        if (pDateStr != null) {
            String datestr = pDateStr.getAsString();
            try {
                SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                return formater.format(new Date(Long.valueOf(String.valueOf(datestr)) * 1000));
            } catch (Exception e) {
                return datestr;
            }
        } else
            return null;
    }
}
