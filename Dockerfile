FROM maven:3.8.3-openjdk-17 as builder
# create app folder for sources
RUN mkdir -p /build
COPY settings.xml /build
WORKDIR /build
COPY pom.xml /build
#Download all required dependencies into one layer
RUN mvn -B dependency:resolve dependency:resolve-plugins
#Copy source code
COPY src /build/src
RUN mvn clean
#Deploy package
RUN mvn deploy -s settings.xml
