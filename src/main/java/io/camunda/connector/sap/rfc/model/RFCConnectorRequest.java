package io.camunda.connector.sap.rfc.model;

import io.camunda.connector.generator.dsl.Property;
import io.camunda.connector.generator.java.annotation.TemplateProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

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
    @TemplateProperty(label = "BAPI/module name", feel = Property.FeelMode.optional) @NotEmpty
        String moduleName,
    @TemplateProperty(
            label = "exporting parameter",
            description = "variables to send to the the module/BAPI",
            feel = Property.FeelMode.optional,
            optional = true,
            defaultValue = "=[{name:\"param\", type:\"type\", value:\"value\"}]")
        @Pattern(
            regexp =
                "^\\s*(=\\[\\s*(\\{\\s*name\\s*:\\s*[^,{}]*,\\s*type\\s*:\\s*[^,{}]*,\\s*value\\s*:\\s*[^,{}]*\\}\\s*(,\\s*\\{\\s*name\\s*:\\s*[^,{}]*,\\s*type\\s*:\\s*[^,{}]*,\\s*value\\s*:\\s*[^,{}]*\\}\\s*)*)?\\s*\\])?\\s*$",
            message =
                "must be an array list with each entry having 'name', 'type' and 'value' keys")
        Exporting[] exporting,
    @TemplateProperty(
            label = "importing parameter",
            description = "which variables the module/BAPI returns should be picked up",
            feel = Property.FeelMode.optional,
            optional = true,
            defaultValue = "=[{name:\"param\", type:\"type\"}]")
        @Pattern(
            regexp =
                "^\\s*(=\\[\\s*(\\{\\s*name\\s*:\\s*[^,{}]*,\\s*type\\s*:\\s*[^,{}]*\\}\\s*(,\\s*\\{\\s*name\\s*:\\s*[^,{}]*,\\s*type\\s*:\\s*[^,{}]*\\}\\s*)*)?\\s*\\])?\\s*$",
            message = "must be an array list with each entry having 'name' and 'type' keys")
        Importing[] importing,
    @TemplateProperty(
            label = "changing parameter",
            description =
                "which variables the module/BAPI receives and returns should be processed",
            feel = Property.FeelMode.optional,
            optional = true,
            defaultValue = "=[{name:\"param\", type:\"type\", value:\"value\"}]",
            condition =
                @TemplateProperty.PropertyCondition(property = "moduleType", equals = "rfm"))
        // TODO: test
        @Pattern(
            regexp =
                "^\\s*(=\\[\\s*(\\{\\s*name\\s*:\\s*[^,{}]*,\\s*type\\s*:\\s*[^,{}]*(,\\s*value\\s*:\\s*[^,{}]*?)?\\}\\s*(,\\s*\\{\\s*name\\s*:\\s*[^,{}]*,\\s*type\\s*:\\s*[^,{}]*(,\\s*value\\s*:\\s*[^,{}]*?)?\\}\\s*)*)?\\s*\\])?\\s*$",
            message =
                "must be an array list with each entry having 'name', 'type' and (optional) 'value' keys")
        Changing[] changing,
    @TemplateProperty(
            label = "tables parameter",
            description =
                "which tables the module/BAPI receives and returns should be managed,\n for RETURN table BAPIRET2, set 'isReturn' to true",
            feel = Property.FeelMode.optional,
            optional = true,
            type = TemplateProperty.PropertyType.String,
            defaultValue =
                "=[{name:\"table\", type: \"some_type\"},{name:\"BAPIRET2\", isReturn:true}]")
        @Pattern(
            regexp =
                "^\\s*(=\\[\\s*(\\{\\s*name\\s*:\\s*[^,{}]*,\\s*(type\\s*:\\s*[^,{}]*|isReturn\\s*:\\s*true)\\s*\\}\\s*(,\\s*\\{\\s*name\\s*:\\s*[^,{}]*,\\s*(type\\s*:\\s*[^,{}]*|isReturn\\s*:\\s*true)\\s*\\}\\s*)*)?\\s*\\])?\\s*$",
            message =
                "must be an array list with each entry having 'name'; type and isReturn are mutually exclusive")
        Tables[] tables) {}
