package io.camunda.connector.sap.rfc.model.validator;

import io.camunda.connector.api.error.ConnectorInputException;
import io.camunda.connector.api.validation.ValidationProvider;
import io.camunda.connector.sap.rfc.model.RFCConnectorRequest;
import jakarta.validation.ValidationException;
import java.util.Arrays;
import java.util.Objects;

public class CustomValidationProvider implements ValidationProvider {
  @Override
  public void validate(Object o) {
    if (!(o instanceof RFCConnectorRequest request)) {
      throw new ConnectorInputException(new ValidationException("received invalid Request object"));
    }

    if (request.destination() == null) {
      throw new ConnectorInputException(
          new ValidationException("BTP destination must be a string"));
    }
    if (request.moduleName() == null) {
      throw new ConnectorInputException(new ValidationException("Module name must be a string"));
    }

    if (request.exporting() != null) {
      if (!Arrays.stream(request.exporting()).allMatch(Objects::nonNull)) {
        throw new ConnectorInputException(
            new ValidationException("Exporting array must contain only Exporting objects"));
      }
      if (!Arrays.stream(request.exporting())
          .allMatch(e -> e.name() != null && e.type() != null && e.value() != null)) {
        throw new ConnectorInputException(
            new ValidationException(
                "every entry of the exporting parameter must have a name, type, and value property!"));
      }
    }

    if (request.importing() != null) {
      if (!Arrays.stream(request.importing()).allMatch(Objects::nonNull)) {
        throw new ConnectorInputException(
            new ValidationException("Importing array must contain only Importing objects"));
      }
      if (!Arrays.stream(request.importing()).allMatch(i -> i.name() != null && i.type() != null)) {
        throw new ConnectorInputException(
            new ValidationException(
                "every entry of the importing parameter must have a name and type!"));
      }
    }

    if (request.changing() != null) {
      if (!Arrays.stream(request.changing()).allMatch(Objects::nonNull)) {
        throw new ConnectorInputException(
            new ValidationException("Changing array must contain only Changing objects"));
      }
      if (!Arrays.stream(request.changing()).allMatch(c -> c.name() != null && c.type() != null)) {
        throw new ConnectorInputException(
            new ValidationException(
                "every entry of the changing parameter must have a name and type!"));
      }
    }

    if (request.tables() != null) {
      if (!Arrays.stream(request.tables()).allMatch(Objects::nonNull)) {
        throw new ConnectorInputException(
            new ValidationException("Tables array must contain only Table objects"));
      }
      if (!Arrays.stream(request.tables())
          .allMatch(t -> t.name() != null && (t.type() != null || t.isReturn()))) {
        throw new ConnectorInputException(
            new ValidationException(
                "every entry of the tables parameter must have a name and type or a name and 'isReturn': true!"));
      }
      if (Arrays.stream(request.tables())
          .anyMatch(
              t -> t.name() != null && t.type() != null && Boolean.TRUE.equals(t.isReturn()))) {
        throw new ConnectorInputException(
            new ValidationException(
                "tables parameter can either have a name and type or a name and 'isReturn': true!"));
      }
    }
  }
}
