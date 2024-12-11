package io.camunda.connector.sap.rfc;

import io.camunda.zeebe.client.ZeebeClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named = "e2e", matches = "true")
public class e2eTest {

  ZeebeClient zeebeClient;

  e2eTest() {
    // the evn vars are set in the github action
    // derived from the repo secrets
    zeebeClient = ZeebeClient.newCloudClientBuilder()
        .withClusterId(System.getenv("clusterId"))
        .withClientId(System.getenv("clientId"))
        .withClientSecret(System.getenv("clientSecret"))
        .withRegion(System.getenv("region"))
        .build();
  }

  @Test
  void bapi() {
    zeebeClient.newDeployResourceCommand()
        .addResourceFromClasspath("bapi.bpmn")
        .send()
        .join();
    var processInstanceResult =
        zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId("bapi")
            .latestVersion()
            .withResult()
            .send()
            .join();


  }
}
