# Camunda-SAP integration: SAP RFC protocol outbound Connector

Camunda Connector to interact with an SAP S/4 and ECC system via RFC.

This project provides a Camunda connector for SAP RFC (Remote Function Calls) to BAPIs (Business Application Programming
Interface) and RFM (remote-enabled Function Modules). It allows you to interact with SAP systems by sending and
receiving data through RFC.

The reason that this Connector is not distributed as a Docker image like other Camunda Connectors is a technical
requirement: the SAP Java Connector (JCo) needs to be installed on the system where the Connector is running. 
Due to SAP's license restrictions, this is not possible within a redistributable Docker image. 
So the Connector needs to be deployed as a `.war` Java application on Cloud Foundry.

## Development

(optional) compile in a version of JCo for local dev: `mvn install:install-file -Dfile=sapjco3.jar -DgroupId=com.sap.conn.jco -DartifactId=com.sap.conn.jco.sapjco3 -Dversion=3.1.10 -Dpackaging=jar`

- create a `<dest>.jcoDestination` file in the classpath to configure the connection to the SAP system
- add the environment variable to the "Run/Debug" configuration (needs to be a JSON array)    
  `destinations=[{"name": "<dest>", "type": "RFC"}]`
- uncomment `sapjco` section in `pom.xml` to include the SAP JCo dependency
- make IDE recognize local `sapjco3.jar`, fex by setting "add dependencies with 'provided' scope to classpath"
- source code formatting is done with `maven-spotless-plugin` upon build/compile
- on PRs
    - always bump the patch version first in `pom.xml`
    - don't change major or minor, as they indicate the Camunda 8 release association
      &rarr; `sap-rfc-connector-8.5.2` is the version for Camunda 8.5, and the connector version 2

### Release cutting

&rarr; will do GH release

:warning: GH releases is only done upon changes to `pom.xml` in a push to this branch.

- create release branch: `release/8.x`
- adjust version in `/src/pom.xml`
- update dependencies in `/src/pom.xml` so they fit the C8 target (fex connectors sdk version)
- in `/.github/workflows/reusable-{deploy,e2e-test}.yml`,   
  adjust `secrets.C8x_...` to the target cluster version (and eventually create those gh secrets)
- push (to) the release branch
