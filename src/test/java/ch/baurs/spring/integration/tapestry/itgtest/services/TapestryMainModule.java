package ch.baurs.spring.integration.tapestry.itgtest.services;

import org.apache.tapestry5.ioc.ServiceBinder;

/**
 *
 */
public class TapestryMainModule {

    public static void bind(ServiceBinder binder) {
        binder.bind(TestService.class, TestServiceImpl.class);
    }

}
