package io.camunda.connector.sap.rfc;

import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.s4hana.connectivity.exception.RequestExecutionException;
import com.sap.cloud.sdk.s4hana.connectivity.exception.RequestSerializationException;
import com.sap.cloud.sdk.s4hana.connectivity.rfc.AbstractRemoteFunctionRequest;
import com.sap.cloud.sdk.s4hana.connectivity.rfc.AbstractRemoteFunctionRequestResult;
import com.sap.cloud.sdk.s4hana.connectivity.rfc.BapiRequest;
import com.sap.cloud.sdk.s4hana.connectivity.rfc.RfmRequest;
import com.sap.conn.jco.JCoRuntimeException;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import io.camunda.connector.generator.java.annotation.ElementTemplate;
import io.camunda.connector.sap.rfc.model.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import org.apache.commons.lang.ArrayUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"deprecation", "rawtypes"})
@OutboundConnector(
    name = "SAPRFCOUTBOUNDCONNECTOR",
    inputVariables = {
      "destination",
      "moduleName",
      "moduleType",
      "exporting",
      "importing",
      "changing",
      "tables"
    },
    type = "io.camunda:sap:rfc:outbound:1")
@ElementTemplate(
    id = "io.camunda.connector.sap.rfc.outbound.v1",
    name = "SAP RFC connector",
    version = 1,
    icon = "sap-rfc-connector-outbound.svg",
    documentationRef = "https://docs.camunda.io/xxx",
    inputDataClass = RFCConnectorRequest.class)
public class RFCConnector implements OutboundConnectorFunction {

  private static final Logger LOGGER = LoggerFactory.getLogger(RFCConnector.class);

  @Override
  public Object execute(OutboundConnectorContext context) {
    // note: a custom validator is registered via SPI (see META-INF/services)
    // in place of the default validator usage in .bindVariables
    // b/c @Pattern regex annotation doesn't account for anything but Strings
    RFCConnectorRequest request = context.bindVariables(RFCConnectorRequest.class);
    return executeRequest(request);
  }

  private Object executeRequest(RFCConnectorRequest request) {
    Destination destination = DestinationProvider.getDestination(request.destination());
    LOGGER.debug("Destination {}: {}", request.destination(), destination.getPropertyNames());

    //> TODO: relay via element template
    //    try {
    //      RemoteFunctionCache.clearCache(destination);
    //    } catch (RemoteFunctionException e) {
    //      throw new RuntimeException(e);
    //    }

    AbstractRemoteFunctionRequest req =
        request.moduleType().equals("bapi") ? buildBapi(request) : buildRfm(request);
    try {

      LOGGER.debug(
          "RFC request start at {}",
          LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS")));
      AbstractRemoteFunctionRequestResult result =
          (AbstractRemoteFunctionRequestResult) req.execute(destination);

      LOGGER.debug(
          "RFC request finished at {}",
          LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS")));
      var _r = buildResponse(request, result);

      return request.moduleType().equals("bapi")
          ? new BapiConnectorResponse(
              ((JSONObject) _r.get("tables")).toMap(), ((JSONObject) _r.get("importing")).toMap())
          : new RfmConnectorResponse(
              ((JSONObject) _r.get("tables")).toMap(),
              ((JSONObject) _r.get("importing")).toMap(),
              ((JSONObject) _r.get("changing")).toMap());

    } catch (RequestSerializationException e) {
      throw new ConnectorException(
          ErrorCodes.REQUEST_SERIALIZATION_ERROR.name(),
          buildErrorMsg(e, "Request serialization error: "),
          e);
    } catch (RequestExecutionException e) {
      throw new ConnectorException(
          ErrorCodes.REQUEST_EXECUTION_ERROR.name(),
          buildErrorMsg(e, "Request execution error: "),
          e);
    } catch (JCoRuntimeException e) {
      throw new ConnectorException(
          ErrorCodes.JCO_RUNTIME_ERROR.name(), buildErrorMsg(e, "JCo runtime error: "), e);
    } catch (Exception e) {
      throw new ConnectorException(
          ErrorCodes.GENERIC_ERROR.name(), buildErrorMsg(e, "Unknown error: "), e);
    }
  }

  private static String buildErrorMsg(Exception e, String prefix) {
    String msg = !prefix.isBlank() ? prefix + e.getMessage() : prefix;
    msg += e.getCause() != null ? " caused by: " + e.getCause().getMessage() : "";
    return msg;
  }

  private static JSONObject buildResponse(
      RFCConnectorRequest request, AbstractRemoteFunctionRequestResult result) {

    var finalResult = new JSONObject();

    var tablesNode = new JSONObject();
    if (request.tables() != null && request.tables().length > 0) {
      LOGGER.debug("received {} TABLES", request.tables().length);
      for (Tables t :
          Arrays.stream(request.tables())
              .filter(x -> x.isReturn() == null)
              .toArray(Tables[]::new)) { //> we only want the "non-returntables"
        var _t = result.get(t.name()).getAsCollection().asList(HashMap.class);
        tablesNode.put(t.name(), _t);
      }
    }
    finalResult.put("tables", tablesNode);

    var importingNode = new JSONObject();
    if (request.importing() != null && request.importing().length > 0) {
      LOGGER.debug("received {} IMPORTING", request.importing().length);
      for (Importing i : request.importing()) {
        var resultItem = result.get(i.name());
        Object _i;
        if (resultItem.isResultCollection()) {
          _i = resultItem.getAsCollection().asList(HashMap.class);
        } else if (resultItem.isResultPrimitive()) {
          _i = resultItem.getAsPrimitive().asString();
        } else if (resultItem.isResultObject()) {
          _i = resultItem.getAsObject().toString();
        } else {
          throw new ConnectorException(
              ErrorCodes.REQUEST_SERIALIZATION_ERROR.name(),
              "Unknown result type for importing parameter: " + i.name());
        }
        importingNode.put(i.name(), _i);
      }
    }
    finalResult.put("importing", importingNode);

    // only with RFMs
    var changingNode = new JSONObject();
    if (request.changing() != null && request.changing().length > 0) {
      LOGGER.debug("received {} CHANGING", request.changing().length);
      for (Changing c : request.changing()) {
        var _c = result.get(c.name()).getAsCollection().asList(HashMap.class);
        changingNode.put(c.name(), _c);
      }
    }
    finalResult.put("changing", changingNode);

    return finalResult;
  }

  private BapiRequest buildBapi(RFCConnectorRequest request) {
    LOGGER.debug("Building BAPI request for {}", request.moduleName());
    BapiRequest bapi = new BapiRequest(request.moduleName());
    configureRequest(bapi, request, true);
    return bapi;
  }

  private RfmRequest buildRfm(RFCConnectorRequest request) {
    LOGGER.debug("Building RFM request for {}", request.moduleName());
    RfmRequest rfm = new RfmRequest(request.moduleName());
    configureRequest(rfm, request, false);
    return rfm;
  }

  private void configureRequest(
      AbstractRemoteFunctionRequest rfc, RFCConnectorRequest request, Boolean isBapi) {
    if (request.exporting() != null) {
      LOGGER.debug(
          "sending EXPORTING: {}",
          ArrayUtils.toString(request.exporting())); //> what we're sending to the called module
      Arrays.stream(request.exporting())
          .forEach(e -> invoke(rfc, "withExporting", e.name(), e.type(), e.value(), isBapi));
    }

    if (request.importing() != null) {
      LOGGER.debug(
          "sending IMPORTING: {}",
          ArrayUtils.toString(
              request.importing())); //> what's expected back from the called module
      Arrays.stream(request.importing())
          .forEach(i -> invoke(rfc, "withImporting", i.name(), i.type(), isBapi));
    }

    if (request.changing() != null) {
      LOGGER.debug(
          "sending CHANGING: {}",
          ArrayUtils.toString(
              request
                  .changing())); //> what we're both sending to and expecting back from the called
      // module
      Arrays.stream(request.changing())
          .forEach(
              c -> {
                if (c.value() != null) {
                  invoke(rfc, "withChanging", c.name(), c.type(), c.value(), isBapi);
                } else {
                  invoke(rfc, "withChanging", c.name(), c.type(), isBapi);
                }
              });
    }

    if (request.tables() != null) {
      LOGGER.debug(
          "sending TABLES: {}",
          ArrayUtils.toString(
              request
                  .tables())); //> what result tables we're expecting back from the called module
      Arrays.stream(request.tables())
          .forEach(
              t -> {
                if (Boolean.TRUE.equals(t.isReturn())) {
                  invoke(
                      rfc, "withTableAsReturn", t.name(), isBapi); //> specific BAPIRET2 handling
                } else {
                  invoke(rfc, "withTable", t.name(), t.type(), isBapi);
                }
              });
    }
  }

  // exporting/changing
  private void invoke(
      AbstractRemoteFunctionRequest rfc,
      String methodName,
      String name,
      String type,
      Object value,
      Boolean isBapi) {
    try {
      Method method =
          isBapi
              ? BapiRequest.class.getMethod(
                  methodName, String.class, String.class, value.getClass())
              : RfmRequest.class.getMethod(
                  methodName, String.class, String.class, value.getClass());
      method.invoke(rfc, name, type, value);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
      throw new IllegalArgumentException(ex);
    }
  }

  // importing/tables
  private void invoke(
      AbstractRemoteFunctionRequest rfc,
      String methodName,
      String name,
      String type,
      Boolean isBapi) {
    if (methodName.equals("withTable")) {
      rfc.withTable(name, type).end();
    } else {
      try {
        Method method =
            isBapi
                ? BapiRequest.class.getMethod(methodName, String.class, String.class)
                : RfmRequest.class.getMethod(methodName, String.class, String.class);
        method.invoke(rfc, name, type);
      } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
        throw new IllegalArgumentException(ex);
      }
    }
  }

  // designated bapiret2 mapping
  private void invoke(
      AbstractRemoteFunctionRequest rfc, String methodName, String name, Boolean isBapi) {
    try {
      Method method =
          isBapi
              ? BapiRequest.class.getMethod(methodName, String.class)
              : RfmRequest.class.getMethod(methodName, String.class);
      method.invoke(rfc, name);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
      throw new IllegalArgumentException(ex);
    }
  }
}
