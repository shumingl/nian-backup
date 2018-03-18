package so.nian.backup.utils.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

@SuppressWarnings("unchecked")
public class ContextUtil implements ApplicationContextAware {

	private static ApplicationContext	context	= null;

	public void setApplicationContext(ApplicationContext context) throws BeansException {
		ContextUtil.context = context;
	}

	public static <T> T getBean(String beanId, Class<T> clazz) throws BeansException {
		return context.getBean(beanId, clazz);
	}

	public static <T> T getBean(Class<T> clazz) throws BeansException {
		return context.getBean(clazz);
	}

	public static <T> T getBean(String beanId) throws BeansException {
		return (T) context.getBean(beanId);
	}
	
	public static <T> void registerBean(String id, Object bean) {
		ConfigurableListableBeanFactory factory = ((ConfigurableApplicationContext) context).getBeanFactory();
		if (!factory.containsSingleton(id))
			factory.registerSingleton(id, bean);
	}
	
}
