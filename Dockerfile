 # renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=3.7.1

FROM crmdvrepo01.azurecr.io/registry.hub.docker.com/library/openjdk:21-jdk-slim

ENV JAR_FILE_NAME=api-cp-springboot-template.jar

COPY build/libs/$JAR_FILE_NAME /opt/app/
COPY lib/applicationinsights.json /opt/app/

EXPOSE 4550
RUN chmod 755 /opt/app/$JAR_FILE_NAME
CMD [ "java", "-jar", "/opt/app/$JAR_FILE_NAME" ]
