package io.camunda.connector.sap.rfc;

import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import io.camunda.connector.api.error.ConnectorException;

public class DestinationProvider {
  private DestinationProvider() {}

  public static Destination getDestination(String destination) {
    try {
      Destination d = DestinationAccessor.getDestination(destination).asRfc();
      return d;
    } catch (Exception e) {
      throw new ConnectorException(ErrorCodes.DESTINATION_ERROR.name(), e.getMessage(), e);
    }
  }
}
