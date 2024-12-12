package io.camunda.connector.sap.rfc;

import io.camunda.connector.sap.rfc.model.BapiConnectorResponse;
import io.camunda.zeebe.client.ZeebeClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfEnvironmentVariable(named = "e2e", matches = "true")
public class E2eTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(E2eTest.class);

  ZeebeClient zeebeClient;

  E2eTest() {
    // the evn vars are set in the github action
    // derived from the repo secrets
    zeebeClient =
        ZeebeClient.newCloudClientBuilder()
            .withClusterId(System.getenv("clusterId"))
            .withClientId(System.getenv("clientId"))
            .withClientSecret(System.getenv("clientSecret"))
            .withRegion(System.getenv("region"))
            .build();

  }

  @Test
  void bapi() {
    zeebeClient.newDeployResourceCommand().addResourceFromClasspath("bapi.bpmn").send().join();

    var processInstanceResult =
        zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId("bapi")
            .latestVersion()
            .withResult()
            .send()
            .join();

    var result = processInstanceResult.getVariablesAsMap();
    BapiConnectorResponse costCenter = (BapiConnectorResponse) result.get("CostCenter");
    int costCenterListSize = ((ArrayList) costCenter.tables().get("COSTCENTERLIST")).size();
    assertTrue(costCenterListSize >= 1, costCenterListSize + " cost center returned");
  }

  @Test
  void rfm() {
    zeebeClient.newDeployResourceCommand().addResourceFromClasspath("rfm.bpmn").send().join();

    var processInstanceResult =
        zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId("rfm")
            .latestVersion()
            .withResult()
            .send()
            .join();

    LOGGER.info("//> rfm result");
    LOGGER.info(processInstanceResult.getVariablesAsMap().toString());
  }
}
