FROM maven:3.5.4-jdk-8-alpine AS MAVEN_TOOL_CHAIN
COPY pom.xml /tmp/
COPY src /tmp/src/
WORKDIR /tmp/
RUN mvn clean package

FROM openjdk:13-alpine
COPY --from=MAVEN_TOOL_CHAIN /tmp/target/*.jar /
CMD java -jar /org.collectionspace.publicbrowser-*.jar

ENV ES_ALLOWEDPUBLISHTOVALUES=all \
    ES_ALLOWEDRECORDTYPES=CollectionObject \
    ES_INDEX=nuxeo \
    ES_RECORDTYPES_COLLECTIONOBJECT_PUBLISHTOFIELD=collectionobjects_common:publishToList.shortid \
    SERVER_PORT=8181 \
    ZUUL_ROUTES_CSPACE_SERVICES_PASSWORD=reader \
    ZUUL_ROUTES_CSPACE_SERVICES_SENSITIVEHEADERS=Cookie,Set-Cookie \
    ZUUL_ROUTES_CSPACE_SERVICES_URL=http://localhost:8180/cspace-services \
    ZUUL_ROUTES_CSPACE_SERVICES_USERNAME=reader@core.collectionspace.org \
    ZUUL_ROUTES_ES_URL=http://localhost:9200

HEALTHCHECK --interval=1m --timeout=3s CMD wget --quiet --tries=1 --spider http://localhost:${SERVER_PORT}/ || exit 1
