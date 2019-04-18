# cspace-public-gateway

A (prototype) gateway server for accessing public CollectionSpace data. This server allows limited access to a back-end Elasticsearch API and CollectionSpace REST API.

## Building

The program may be built as a jar containing an embedded tomcat server, or as a war to be installed in a CollectionSpace tomcat server.

### To build as a jar:

```bash
git clone https://github.com/collectionspace/cspace-public-gateway.git
cd cspace-public-gateway
mvn clean package
```

This produces org.collectionspace.publicbrowser-{version}.jar in the target directory. The jar file may be copied to any server.

### To build as a war:

```bash
git clone https://github.com/collectionspace/cspace-public-gateway.git
cd cspace-public-gateway
git checkout war
mvn clean package
```

This produces org.collectionspace.publicbrowser-{version}.war in the target directory. The war file may be installed into a CollectionSpace tomcat server.

## Running

### Running the jar:

The jar includes an embedded Tomcat server.

```bash
java -jar org.collectionspace.publicbrowser-{version}.jar
```

To configure the application, including the port on which it listens, supply external properties using one of the [configuration methods supported by Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html).
The available properties are listed in the default [application.properties](./src/main/resources/application.properties) file.

To run the application as a service using init.d or systemd, follow the [Spring Boot installation instructions](https://docs.spring.io/spring-boot/docs/current/reference/html/deployment-install.html).

### Running the war:

Copy the war into the webapps directory of a CollectionSpace tomcat server. The war is specifically packaged to run in the tomcat that is delivered as part of a standard CollectionSpace installation, and assumes the presence of libraries that are part of that installation. The application may not run in other tomcat installations.

To configure the application, supply external properties using one of the [configuration methods supported by Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html).
The available properties are listed in the default [application.properties](./src/main/resources/application.properties) file. Some properties (for example, port number) do not apply when the application is run in an external tomcat.

## Docker

To build and test locally:

```bash
docker build -t cspace-public-gateway .
docker run --rm -it -p 8181:8181 cspace-public-gateway

# to use a different port
docker run --rm -it -p 8282:8282 \
  -e SERVER_PORT=8282 \
  cspace-public-gateway

# with configuration via environment variables
docker run --rm -it -p 8181:8181 \
  -e ES_INDEX=nuxeo \
  -e ZUUL_ROUTES_CSPACE_SERVICES_PASSWORD=reader \
  -e ZUUL_ROUTES_CSPACE_SERVICES_URL=https://core.collectionspace.org/cspace-services \
  -e ZUUL_ROUTES_CSPACE_SERVICES_USERNAME=reader@core.collectionspace.org \
  -e ZUUL_ROUTES_ES_URL=$ES_DOMAIN_ENDPOINT \
  cspace-public-gateway
```
