# cspace-public-gateway

A (prototype) gateway server for accessing public CollectionSpace data. This middleware allows limited anonymous (unauthenticated) access to a back-end Elasticsearch API and CollectionSpace REST API.

## Building

The program may be built as a war to be installed in a CollectionSpace tomcat server, or as an executable jar containing an embedded tomcat server.

### To build as a war (default):

```bash
git clone https://github.com/collectionspace/cspace-public-gateway.git
cd cspace-public-gateway
mvn clean package [-Dpackaging=war]
```

This produces org.collectionspace.publicbrowser-{version}.war in the target directory. The war file may be installed into a CollectionSpace tomcat server by copying it into the server's webapps directory.

### To build as a jar:

```bash
git clone https://github.com/collectionspace/cspace-public-gateway.git
cd cspace-public-gateway
mvn clean package -Dpackaging=jar
```

This produces org.collectionspace.publicbrowser-{version}.jar in the target directory. The jar file may be copied to a server, and executed with `java -jar`.

## Running

### Running the war:

Copy the war into the webapps directory of a CollectionSpace tomcat server. The war is specifically packaged to run in the tomcat that is delivered as part of a standard CollectionSpace installation, and assumes the presence of libraries that are part of that installation. The application may not run in other tomcat installations.

To configure the application, supply external properties using one of the [configuration methods supported by Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html).
The available properties are listed in the default [application.yml](./src/main/resources/application.yml) file. Some properties (for example, port number) do not apply when the application is run as a war in an external tomcat.

### Running the jar:

The jar includes an embedded Tomcat server.

```bash
java -jar org.collectionspace.publicbrowser-{version}.jar
```

To configure the application, including the port on which it listens, supply external properties using one of the [configuration methods supported by Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html).
The available properties are listed in the default [application.yml](./src/main/resources/application.yml) file.

To run the application as a service using init.d or systemd, follow the [Spring Boot installation instructions](https://docs.spring.io/spring-boot/docs/current/reference/html/deployment-install.html).

## Configuration

You must configure routes to access the CollectionSpace Services REST API and the Elasticsearch API for your installation. An example is included in [application.yml](https://github.com/collectionspace/cspace-public-gateway/blob/0f1959d92a5bf98a0271b166d097d532cc3f2d71/src/main/resources/application.yml#L57-L72).

## Verifying the installation

If the application is installed and configured correctly, the CollectionSpace REST API and Elasticsearch API should be accessible.

For example, if the application is:

- Built as a war, and copied into a CollectionSpace Tomcat server at `webapps/gateway.war`
- Configured to access the CollectionSpace API at `/core/cspace-services/**` (see [example](https://github.com/collectionspace/cspace-public-gateway/blob/0f1959d92a5bf98a0271b166d097d532cc3f2d71/src/main/resources/application.yml#L58-L66))
- Configured to access the Elasticsearch API at `/core/es/**` (see [example](https://github.com/collectionspace/cspace-public-gateway/blob/0f1959d92a5bf98a0271b166d097d532cc3f2d71/src/main/resources/application.yml#L68-L72))

Then:

- A valid XML response should be returned from http://{hostname}/gateway/core/cspace-services/systeminfo
- A valid JSON response should be returned from http://{hostname}/gateway/core/es/_count

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
