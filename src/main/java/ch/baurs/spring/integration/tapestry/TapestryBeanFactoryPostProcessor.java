package ch.baurs.spring.integration.tapestry;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.SingleKeySymbolProvider;
import org.apache.tapestry5.internal.TapestryAppInitializer;
import org.apache.tapestry5.internal.util.DelegatingSymbolProvider;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.services.ServiceActivityScoreboard;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Post-processor that orchestrates the whole integration
 */
public class TapestryBeanFactoryPostProcessor implements BeanFactoryPostProcessor, Ordered {

    public static final String SPRING_CONTEXT_PATH = "server.servlet.context-path";
    public static final String PROPERTY_APPMODULE = "spring.tapestry.integration.appmodule";

    protected final AnnotationConfigServletWebServerApplicationContext applicationContext;

    private Registry registry = null;
    private TapestryAppInitializer appInitializer = null;

    public TapestryBeanFactoryPostProcessor(AnnotationConfigServletWebServerApplicationContext applicationContext) {
        super();
        this.applicationContext = applicationContext;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String appModuleClass = findAppModuleClass(applicationContext.getEnvironment());
        String filterName = appModuleClass.substring(appModuleClass.lastIndexOf('.') + 1).replace("Module", "");
        SymbolProvider combinedProvider = setupTapestryContext(appModuleClass, filterName);
        String executionMode = combinedProvider.valueForSymbol(SymbolConstants.EXECUTION_MODE);
        LogHelper.info("TB: About to start Tapestry app module: {}, filterName: {}, executionMode: {} ", appModuleClass, filterName, executionMode);
        appInitializer = new TapestryAppInitializer(LogHelper.LOG, combinedProvider, filterName, executionMode);
        appInitializer.addModules(new SpringModuleDef(applicationContext));
        appInitializer.addModules(AssetSourceModule.class);
        LogHelper.info("TB: creating tapestry registry");
        registry = appInitializer.createRegistry();

        beanFactory.addBeanPostProcessor(new TapestryFilterPostProcessor());

        registerTapestryServices(applicationContext.getBeanFactory(),
                combinedProvider.valueForSymbol(InternalConstants.TAPESTRY_APP_PACKAGE_PARAM) + ".services",
                registry);

        // This will scan and find TapestryFilter which in turn will be post
        // processed be TapestryFilterPostProcessor completing tapestry initialisation
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(applicationContext);
        scanner.scan(TapestryBeanFactoryPostProcessor.class.getPackage().getName());

    }

    private class TapestryFilterPostProcessor implements BeanPostProcessor {

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
            if (bean.getClass() == TapestryFilter.class) {
                LogHelper.info("TB: About to start TapestryFilter, begin Registry initialization");
                registry.performRegistryStartup();
                registry.cleanupThread();
                appInitializer.announceStartup();
                LogHelper.info("TB: About to start TapestryFilter, Registry initialization complete");
            }
            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            return bean;
        }

    }

    protected SymbolProvider setupTapestryContext(String appModuleClass, String filterName) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        Map<String, Object> tapestryContext = new HashMap<>();

        tapestryContext.put("tapestry.filter-name", filterName);

        //read contextPath from two possible properties
        String servletContextPath = environment.getProperty(SymbolConstants.CONTEXT_PATH, environment.getProperty(SPRING_CONTEXT_PATH, ""));
        tapestryContext.put(SymbolConstants.CONTEXT_PATH, servletContextPath);

        String executionMode = environment.getProperty(SymbolConstants.EXECUTION_MODE, "production");
        tapestryContext.put(SymbolConstants.EXECUTION_MODE, executionMode);

        String rootPackageName = appModuleClass.substring(0, appModuleClass.lastIndexOf('.')).replace(".services", "");
        tapestryContext.put(InternalConstants.TAPESTRY_APP_PACKAGE_PARAM, rootPackageName);

        environment.getPropertySources().addFirst(new MapPropertySource("tapestry-context", tapestryContext));

        return new DelegatingSymbolProvider(
                //TODO confirm not needed new SystemPropertiesSymbolProvider(),
                new ApplicationContextSymbolProvider(applicationContext),
                new SingleKeySymbolProvider(SymbolConstants.CONTEXT_PATH, servletContextPath),
                new SingleKeySymbolProvider(InternalConstants.TAPESTRY_APP_PACKAGE_PARAM, rootPackageName),
                new SingleKeySymbolProvider(SymbolConstants.EXECUTION_MODE, executionMode)
        );
    }

    protected String findAppModuleClass(Environment environment) {
        String appModuleClassName = environment.getProperty(PROPERTY_APPMODULE, "");

        if (StringUtils.isEmpty(appModuleClassName)) {
            String message = String.format("Tapestry AppModule not found. Set the property '%s=<fqdn.of.AppModule>' in your environment (e.g. application.properties)", PROPERTY_APPMODULE);
            throw new IllegalStateException(message);
        }
        LogHelper.info("Found Tapestry AppModule class: {} ", appModuleClassName);

        return appModuleClassName;
    }

    protected void registerTapestryServices(ConfigurableListableBeanFactory beanFactory, String servicesPackage,
                                            Registry registry) {
        ServiceActivityScoreboard scoreboard = registry.getService(ServiceActivityScoreboard.class);
        scoreboard.getServiceActivity().forEach(service -> {
            if (service.getServiceInterface().getPackage().getName().startsWith(servicesPackage)
                    || !service.getMarkers().isEmpty() || service.getServiceInterface().getName().contains("tapestry5")) {
                Object proxy = registry.getService(service.getServiceId(), (Class<?>) service.getServiceInterface());
                beanFactory.registerResolvableDependency(service.getServiceInterface(), proxy);
                LogHelper.debug("TB: tapestry service {} exposed to spring", service.getServiceId());
            }
        });
        beanFactory.registerResolvableDependency(Registry.class, registry);
        LogHelper.info("TB: tapestry Registry registered with spring (Still pending initialization)");
    }

    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private static String defaultString(final String str) {
        return defaultString(str, "");
    }

    private static String defaultString(final String str, final String defaultStr) {
        return str == null ? defaultStr : str;
    }

}
