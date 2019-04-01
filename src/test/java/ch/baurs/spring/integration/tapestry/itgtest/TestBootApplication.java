package ch.baurs.spring.integration.tapestry.itgtest;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 *
 */
@SpringBootApplication
public class TestBootApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(TestBootApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }

}
