# How-to build the application

This guide describes how to build an executable distribution of the application.


## Requirements

* Java 11+
* Maven 3.6.x ([http://maven.apache.org](http://maven.apache.org))


## Install Java JDK

* Install Java JDK and create a `JAVA_HOME` environment variable that points to the location of your JDK

	set PATH=C:\Program Files\Java\jdk1.8.0_121\bin;%PATH%

	set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_121


## Install Apache Maven

* Install Maven and create Maven environment variables

	set PATH=C:\develop\lib\apache-maven-3.6.1\bin;%PATH%

	set M2_HOME=C:\develop\lib\apache-maven-3.6.1

	set M2=%M2_HOME%\bin


## Build the installation package

* Navigate to the working directory

* Run `mvn clean package` in the root directory of the application

** Note: This only executes unit test during the build. To additionally run the integration tests run `mvn clean package -Pci`


* Navigate to `distribution/target` where you find the `paperless_v[VERSION].zip`
