# How-to build the application

This guide describes how to build an executable distribution of the application.


## Requirements

* Java 11+
* Optional: Maven 3.6.3 ([http://maven.apache.org](http://maven.apache.org)) (Not available? Use the built-in Maven wrapper)


## Install Java JDK

* Install Java JDK and create a `JAVA_HOME` environment variable that points to the location of your JDK

	set PATH=C:\Program Files\Java\jdk1.8.0_121\bin;%PATH%

	set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_121


## Install Apache Maven (or use the built-in Maven wrapper)

* Install Maven and create Maven environment variables

	set PATH=C:\develop\lib\apache-maven-3.6.1\bin;%PATH%

	set M2_HOME=C:\develop\lib\apache-maven-3.6.1

	set M2=%M2_HOME%\bin

* If Maven cannot be installed: Use the built-in Maven wrapper)

** Maven commands can be executed like this for the Unix system:

	./mvnw clean install

** And the following command for Batch:

	./mvnw.cmd clean install


## Build the installation package

* Navigate to the working directory

* Run `mvn clean package` in the root directory of the application

** Note: This only executes unit test during the build. To additionally run the integration tests run `mvn clean package -Pci`

* Navigate to `distribution/target` where you find the `paperless_v[VERSION].zip`
