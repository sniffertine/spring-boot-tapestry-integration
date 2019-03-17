package ch.baurs.spring.integration.tapestry;

/**
 * Configuration the defines parameters for the spring boot tapestry integration. Implementations are read via {@link java.util.ServiceLoader} API.
 */
public interface SpringBootTapestryIntegrationConfig {
    public String getTapestryPackage();
}
