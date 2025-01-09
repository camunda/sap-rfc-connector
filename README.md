# SAP RFC Connector

This project provides a Camunda connector for SAP RFC (Remote Function Calls) to BAPIs (Business Application Programming Interface) and RFM (remote-enabled Function Modules). It allows you to interact with SAP systems by sending and receiving data through RFC.

The reason that this Connector is not distributed as a Docker image like other Camunda Connectors is a technical requirement: the SAP Java Connector (JCo) needs to be installed on the system where the Connector is running. This is not possible due to SAP's license restrictions within a redistributable Docker image. That's why you need to deploy the Connector as a `.war` Java application on Cloud Foundry.

## How to use it

- configure a BTP destination of type "RFC" to your SAP system
- configure the Spring Boot basis application of the Connector (see `src/main/resources/application.properties`)
- deploy as Java application on Cloud Foundry (-> see `mta(d).yaml.example`)
  - `cf deploy ./ -f` (for `mtad.yaml`)
  - `cf deploy ./mta_archive/*.mtar -f` (for `mta.yaml`)
- import the Element Template into your Camunda Modeler environment (see `element-templates/sap-rfc-connector.json`)
- use the Element Template in your BPMN process ("SAP RFC Connector") and observe jobs dispatched to your Connector on Cloud Foundry

## Development

- on PRs
  - always bump the patch version first in `pom.xml`
  - don't change major or minor, as they indicate the Camunda 8 release assocation
&rarr; `sap-rfc-connector-8.5.2` is the version for Camunda 8.5, and the connector version 2

### Testing
