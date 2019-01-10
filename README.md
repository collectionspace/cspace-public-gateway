# cspace-public-gateway

A (prototype) gateway server for accessing public CollectionSpace data. This server allows limited access to a back-end Elasticsearch API and CollectionSpace REST API.

## Building

```
$ git clone https://github.com/collectionspace/cspace-public-gateway.git
$ cd cspace-public-gateway
$ mvn clean install
```

This produces org.collectionspace.publicbrowser-{version}.jar in the target directory. The jar file may be installed anywhere.

## Running

The jar includes an embedded Tomcat server.

```
$ java -jar org.collectionspace.publicbrowser-{version}.jar
```

To configure the application, including the port on which it listens, supply external properties using one of the [configuration methods supported by Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html).
The available properties are listed in the default [application.properties](./src/main/resources/application.properties) file.

To run the application as a service using init.d or systemd, follow the [Spring Boot installation instructions](https://docs.spring.io/spring-boot/docs/current/reference/html/deployment-install.html).
