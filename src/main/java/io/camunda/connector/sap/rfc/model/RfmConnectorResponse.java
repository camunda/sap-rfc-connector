package io.camunda.connector.sap.rfc.model;

import java.util.Map;

public record RfmConnectorResponse(
    Map<String, Object> tables, Map<String, Object> importing, Map<String, Object> changing) {}
