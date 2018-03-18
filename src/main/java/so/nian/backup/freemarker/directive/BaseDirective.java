package so.nian.backup.freemarker.directive;

import java.io.IOException;
import java.util.Map;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class BaseDirective implements TemplateDirectiveModel {

	public void execute(Environment environment, Map parameters, TemplateModel[] loopVars, 
			TemplateDirectiveBody templateDirectiveBody) throws TemplateException, IOException {
		execute(new DirectiveHandler(environment, parameters, loopVars, templateDirectiveBody));
	}

	public abstract void execute(DirectiveHandler handler) throws TemplateException, IOException;
}