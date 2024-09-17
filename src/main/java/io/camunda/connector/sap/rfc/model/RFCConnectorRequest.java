package io.camunda.connector.sap.rfc.model;

import io.camunda.connector.generator.dsl.Property;
import io.camunda.connector.generator.java.annotation.TemplateProperty;
import jakarta.validation.constraints.NotEmpty;

public record RFCConnectorRequest(
    @TemplateProperty(
            label = "BTP destination name",
            description = "BTP destination pointing to the SAP System to connect to (e.g. a4h)",
            feel = Property.FeelMode.optional)
        @NotEmpty
        String destination,
    @TemplateProperty(
            label = "BAPI or RFC?",
            type = TemplateProperty.PropertyType.Dropdown,
            defaultValue = "bapi",
            choices = {
              @TemplateProperty.DropdownPropertyChoice(value = "bapi", label = "BAPI"),
              @TemplateProperty.DropdownPropertyChoice(value = "rfm", label = "RFM"),
            },
            constraints = @TemplateProperty.PropertyConstraints(notEmpty = true))
        String moduleType,
    @TemplateProperty(label = "BAP/module name", feel = Property.FeelMode.optional) @NotEmpty
        String moduleName,
    @TemplateProperty(
            label = "exporting parameter",
            description = "variables to send to the the module/BAPI",
            feel = Property.FeelMode.optional,
            optional = true,
            defaultValue = "=[{}]")
        Exporting[] exporting,
    @TemplateProperty(
            label = "importing parameter",
            description = "which variables the module/BAPI returns should be picked up",
            feel = Property.FeelMode.optional,
            optional = true,
            defaultValue = "=[{}]")
        Importing[] importing,
    @TemplateProperty(
            label = "changing parameter",
            description =
                "which variables the module/BAPI receives and returns should be processed",
            feel = Property.FeelMode.optional,
            optional = true,
            defaultValue = "=[{}]",
            condition =
                @TemplateProperty.PropertyCondition(property = "moduleType", equals = "rfm"))
        Changing[] changing,
    @TemplateProperty(
            label = "tables parameter",
            description =
                "which tables the module/BAPI receives and returns should be managed,\n for RETURN table BAPIRET2, set 'isReturn' to true",
            feel = Property.FeelMode.optional,
            optional = true,
            defaultValue = "=[{}]")
        Tables[] tables) {}
