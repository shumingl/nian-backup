package so.nian.backup.freemarker.function;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import so.nian.backup.http.NianImageDownload;

import java.util.List;

public class NianStepImages implements TemplateMethodModelEx {
    @Override
    public Object exec(List arguments) throws TemplateModelException {

        SimpleScalar pUserid = (SimpleScalar) arguments.get(0);
        SimpleScalar pType = (SimpleScalar) arguments.get(1);
        SimpleScalar pImage = (SimpleScalar) arguments.get(2);
        if (pUserid == null || pType == null || pImage == null)
            return "";
        String userid = pUserid.getAsString();
        String type = pType.getAsString();
        String image = pImage.getAsString();
        NianImageDownload.download(userid, type, image);
        return "";
    }
}
