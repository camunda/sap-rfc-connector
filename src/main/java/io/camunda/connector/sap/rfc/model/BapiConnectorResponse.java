package io.camunda.connector.sap.rfc.model;

import java.util.Map;

public record BapiConnectorResponse(Map<String, Object> tables, Map<String, Object> importing) {}
