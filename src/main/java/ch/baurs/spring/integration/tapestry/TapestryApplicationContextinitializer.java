package ch.baurs.spring.integration.tapestry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;

/**
 * initializer that registers a {@link BeanFactoryPostProcessor} that bootstraps the tapestry framework.
 *
 * @see TapestryBeanFactoryPostProcessor for implementation details
 */
public class TapestryApplicationContextinitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(TapestryApplicationContextinitializer.class);

    private TapestryBeanFactoryPostProcessor tapestryBeanFactoryPostProcessor = null;

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        if (applicationContext instanceof AnnotationConfigServletWebServerApplicationContext) {
            if (tapestryBeanFactoryPostProcessor != null) {
                throw new IllegalStateException("Tapestry applicationContext already initialized");
            }

            tapestryBeanFactoryPostProcessor = new TapestryBeanFactoryPostProcessor((AnnotationConfigServletWebServerApplicationContext) applicationContext);
            applicationContext.addBeanFactoryPostProcessor(tapestryBeanFactoryPostProcessor);
        } else {
            logger.warn("TB: tapestry-spring-boot works only with EmbeddedWebApplicationContext (Supplied context class was"
                    + applicationContext.getClass() + ") delaying initialization");
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

}
