FROM java:8-alpine
#MAINTAINER Your Name <your@email.tld>

EXPOSE 8080

RUN mkdir -p /app
COPY target/scala-2.12/*.jar /app/

WORKDIR /app

CMD java -jar ./AkkaPersistenceCassandraDemo.jar
# override CMD from your run command, or k8s yaml, or marathon json, etc...

