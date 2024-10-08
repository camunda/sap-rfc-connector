package io.camunda.connector.sap.rfc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class ApplicationRuntime extends SpringBootServletInitializer {

  public static void main(String[] args) {
    SpringApplication.run(ApplicationRuntime.class, args);
  }
}
