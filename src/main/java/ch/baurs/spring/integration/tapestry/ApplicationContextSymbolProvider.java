package ch.baurs.spring.integration.tapestry;

import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.springframework.context.ApplicationContext;

public class ApplicationContextSymbolProvider implements SymbolProvider {
    private ApplicationContext applicationContext;

    public ApplicationContextSymbolProvider(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public String valueForSymbol(String symbolName) {
        return applicationContext.getEnvironment().getProperty(symbolName);
    }

}
