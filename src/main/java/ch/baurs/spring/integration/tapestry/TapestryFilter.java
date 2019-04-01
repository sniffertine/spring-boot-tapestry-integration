package ch.baurs.spring.integration.tapestry;

import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.services.HttpServletRequestHandler;
import org.apache.tapestry5.services.ServletApplicationInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 */
@Component
public class TapestryFilter extends FilterRegistrationBean implements Filter {
    private final Logger logger = LoggerFactory.getLogger(TapestryFilter.class);

    private FilterConfig config;

    private final Registry registry;

    private HttpServletRequestHandler handler;

    @Autowired
    public TapestryFilter(Registry registry) {
        this.registry = registry;
    }

    public final void init(FilterConfig filterConfig) throws ServletException {
        config = filterConfig;

        final ServletContext context = config.getServletContext();

        handler = registry.getService(HttpServletRequestHandler.class);

        ServletApplicationInitializer ai = registry.getService(ServletApplicationInitializer.class);
        ai.initializeApplication(context);
    }

    public final void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            boolean handled = handler.service((HttpServletRequest) request, (HttpServletResponse) response);

            if (!handled) {
                logger.debug("Tapestry did not handle the request.. continuing with filter chain");
                chain.doFilter(request, response);
            } else {
                logger.debug("Tapestry handled the request.. stop the filter chain");
            }

        } finally {
            registry.cleanupThread();
        }
    }

    @Override
    public void destroy() {
        registry.shutdown();
    }

    @Override
    public Filter getFilter() {
        return this;
    }
}
