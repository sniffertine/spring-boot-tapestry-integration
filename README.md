# spring-boot-tapestry-integration
A simple integration module for Tapestry 5.5.x into Spring Boot

Originally copied from <https://github.com/code8/tapestry-boot> and refactored.

To use spring-boot-tapestry-integration add a new property to your Spring Boot environment:
```
spring.tapestry.integration.appmodule=my.fully.qualified.class.name.AppModule
```


Features:
 - bootstraps tapestry framework inside embedded servlet container managed by spring-boot
 - configure tapestry using spring environment (e.g. application.properties)
 - provides injection of spring services in tapestry
 - provides injection of tapestry services in spring

Example: see DemoApplicationTest.

