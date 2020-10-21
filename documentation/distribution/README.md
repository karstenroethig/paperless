# paperless

A spring boot web application for the paperless processing of personal documents.


## Requirements

* Java SDK 11
* Supported databases: MariaDB, MySQL


## Connect to MySQL

### Create the MySQL database

* Create a database for the application to store the data.

	create database paperless;

* Create a database user which the application will connect as and grant the required permissions.

	create user 'paperless_user'@'localhost' identified by 'paperless_password';
	grant CREATE, DROP, DELETE, INSERT, SELECT, UPDATE, ALTER, REFERENCES on paperless.* to 'paperless_user'@'localhost';

* When starting the application, the table structure of the database is automatically set up.

### Configure the application to connect to your MariaDB database

* Edit the following properties in the config file `config/application.yml`

	spring.jpa.databasePlatform=org.hibernate.dialect.MySQL5Dialect
	spring.datasource.url=jdbc:mariadb://localhost/paperless?useSSL=false
	spring.datasource.username=paperless_user
	spring.datasource.password=paperless_password

### Configure the application to connect to your MySQL database

* Edit the following properties in the config file `config/application.yml`

	spring.jpa.databasePlatform=org.hibernate.dialect.MySQL5Dialect
	spring.datasource.url=jdbc:mysql://localhost/paperless?useSSL=false
	spring.datasource.username=paperless_user
	spring.datasource.password=paperless_password


## Run the application

* [IMPORTANT] The steps described are only for execution in test mode. For productive operation, the application must be run as a service.

* Create a installation directory

	cd [PATH_TO_APPS]
	mkdir paperless

* Extract the installation package `paperless_v[VERSION].zip`

* (Optional) Edit the file `bin/run.conf.example` and rename it to `bin/run.conf` to configure Java parameters (Windows: `bin/run.conf.bat.example` to `bin/run.conf.bat`)

* (Optional) Edit the file `config/application.yml.example` and rename it to `config/application.yml` to configure application parameters

* Navigate to the installation directory and start the application

	cd [PATH_TO_APPS]/paperless
	bash bin/run.sh

* Browse to [http://localhost:8080/](http://localhost:8080/)


## Application parameters

* Application parameters can be set in the file `config/application.yml`

### Common application parameters

* Spring boot provides a rich set of application parameters (see [common application properties](https://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html))

* The following parameters have already been overwritten with the application

** `server.address=127.0.0.1`: 
** `server.session.timeout=14400`: Session timeout to 4 hours

### Custom application parameters

* `tomcat.ajp.enabled=false`: Activation of the Apache JServ Protocol (AJP)
* `tomcat.ajp.port=8009`: Server AJP (Apache JServ Protocol) port


## General Upgrade Guide

* Each installation package contains the file `CHANGELOG.md`, which may contain important information for an update.

* All upgrade guides from the initial version to the target version must be observed.

* At least the following steps are to be considered with each upgrade:
	1. Stop running application
	2. (Optional) Create backup for existing installation directory
	3. Unzip installation package in the existing installation directory (all files must be overwritten)
	4. Execute steps of Upgrade Guides for target version and versions in between
	5. Start application
