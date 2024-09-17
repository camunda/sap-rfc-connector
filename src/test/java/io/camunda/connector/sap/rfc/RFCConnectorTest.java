package io.camunda.connector.sap.rfc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@Disabled
public class RFCConnectorTest {
  @Test
  void connect() {
    var function = new RFCConnector();
    var response = function.execute(null);
    assertThat(response).isNotNull();
  }
}
