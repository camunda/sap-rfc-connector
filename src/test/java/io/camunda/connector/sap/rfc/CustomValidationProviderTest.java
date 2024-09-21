package io.camunda.connector.sap.rfc;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.camunda.connector.api.error.ConnectorInputException;
import io.camunda.connector.sap.rfc.model.*;
import io.camunda.connector.sap.rfc.model.validator.CustomValidationProvider;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class CustomValidationProviderTest {

  private final CustomValidationProvider validationProvider = new CustomValidationProvider();

  @ParameterizedTest
  @MethodSource("provideValidRequests")
  public void testValidRequest(RFCConnectorRequest request) {
    validationProvider.validate(request);
  }

  @ParameterizedTest
  @MethodSource("provideInvalidRequests")
  public void testInvalidRequest(RFCConnectorRequest request) {
    assertThrows(ConnectorInputException.class, () -> validationProvider.validate(request));
  }

  private static Stream<RFCConnectorRequest> provideValidRequests() {
    return Stream.of(
        // bapi - minimal
        new RFCConnectorRequest(
            "bapi-minimal",
            "bapi",
            "moduleName",
            new Exporting[] {new Exporting("name", "type", "value")},
            null,
            null,
            new Tables[] {new Tables("BAPIRET2", null, null, true)} //> with BAPIRET2
            ),
        // bapi - full
        new RFCConnectorRequest(
            "bapi-full",
            "bapi",
            "moduleName",
            new Exporting[] {new Exporting("name", "type", "value")},
            new Importing[] {new Importing("name", "type")},
            null,
            new Tables[] {new Tables("name", "type", null, false)}),
        // rfm - full
        new RFCConnectorRequest(
            "rfm-full",
            "rfm",
            "moduleName",
            new Exporting[] {new Exporting("name", "type", "value")},
            new Importing[] {new Importing("name", "type")},
            new Changing[] {new Changing("name", "type", "value")},
            new Tables[] {new Tables("name", "type", null, false)}),
        // bapi/rm - multiple exporting params with different types
        new RFCConnectorRequest(
            "bapi-rfm-multiple-exporting-params",
            "bapi",
            "moduleName",
            new Exporting[] {
              new Exporting("name", "type", "string"),
              new Exporting("name", "type", true),
              new Exporting("name", "type", 1),
              new Exporting("name", "type", 1.0),
              new Exporting("name", "type", 1.0f),
              new Exporting("name", "type", 1L),
              new Exporting("name", "type", 'c'),
              new Exporting("name", "type", new Object()),
              new Exporting("name", "type", new Object[0])
            },
            null,
            null,
            new Tables[] {new Tables("BAPIRET2", null, null, true)} //> with BAPIRET2
            ),
        // rfm - multiple importing params (type is ABAP type, relayed as string)
        new RFCConnectorRequest(
            "rfm-multiple-importing-params",
            "rfm",
            "moduleName",
            new Exporting[] {new Exporting("name", "type", "string")},
            new Importing[] {
              new Importing("name", "type"),
              new Importing("name", "foo-bar"),
              new Importing("name", "FOOBAR")
            },
            null,
            new Tables[] {new Tables("BAPIRET2", null, null, true)} //> with BAPIRET2
            ),
        // bapi/rfm - multiple changing params (type is ABAP type, relayed as string)
        new RFCConnectorRequest(
            "bapi-rfm-multiple-changing-params",
            "bapi",
            "moduleName",
            null,
            null,
            new Changing[] {
              new Changing("name", "type", "string"),
              new Changing("name", "foo-bar", true),
              new Changing("name", "FOOBAR", 1),
              new Changing("name", "type", 1.0),
              new Changing("name", "type", 1.0f),
              new Changing("name", "type", 1L),
              new Changing("name", "type", 'c'),
              new Changing("name", "type", new Object()),
              new Changing("name", "type", new Object[0])
            },
            new Tables[] {new Tables("BAPIRET2", null, null, true)} //> with BAPIRET2
            ),
        // bapi/rfm - multiple tables params, including RETURN table
        // type is ABAP type, relayed as string
        new RFCConnectorRequest(
            "bapi-rfm-multiple-exporting-params",
            "bapi",
            "moduleName",
            null,
            null,
            null,
            new Tables[] {
              new Tables("name", "type", null, false),
              new Tables("name1", "type1", null, false),
              new Tables("BAPIRET2", null, null, true)
            }));
  }

  private static Stream<RFCConnectorRequest> provideInvalidRequests() {
    return Stream.of(
        // empty destination
        new RFCConnectorRequest(null, "bapi", "moduleName", null, null, null, null),
        // empty module name
        new RFCConnectorRequest("destination", "bapi", null, null, null, null, null),
        // wrong format of exporting param
        new RFCConnectorRequest(
            "destination",
            "rfm",
            "moduleName",
            new Exporting[] {new Exporting(null, "type", "value")},
            null,
            null,
            null),
        // wrong format of importing param
        new RFCConnectorRequest(
            "destination",
            "bapi",
            "moduleName",
            null,
            new Importing[] {new Importing(null, "type")},
            null,
            null),
        // wrong format of changing param
        new RFCConnectorRequest(
            "destination",
            "bapi",
            "moduleName",
            null,
            null,
            new Changing[] {new Changing(null, "type", "value")},
            null),
        // wrong format of tables param
        new RFCConnectorRequest(
            "destination",
            "bapi",
            "moduleName",
            null,
            null,
            null,
            new Tables[] {new Tables("name", "type", null, true)}));
  }
}
