package ch.baurs.spring.integration.tapestry.itgtest.services;

import org.apache.tapestry5.annotations.Service;
import org.springframework.web.servlet.ViewResolver;

/**
 * implementation of the test service
 */
public class TestServiceImpl implements TestService {
    final ViewResolver springViewResolver;

    public TestServiceImpl(@Service("mvcViewResolver") ViewResolver springViewResolver) {
        this.springViewResolver = springViewResolver;
    }

    @Override
    public ViewResolver getSpringMvcViewResolver() {
        return springViewResolver;
    }
}
