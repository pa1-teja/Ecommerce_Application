FROM ubuntu:latest
MAINTAINER pavan
RUN apt install git-all
RUN git clone https://github.com/pa1-teja/ecommerce_application.git
COPY target/auth-course-0.0.1-SNAPSHOT.jar auth-course-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/auth-course-0.0.1-SNAPSHOT.jar"]
