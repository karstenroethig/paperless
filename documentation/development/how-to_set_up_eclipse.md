# How-to set up eclipse

This guide describes how to prepare the Eclipse to further develop the application.


## Requirements

* Java 11+
* Maven 3.6.x ([http://maven.apache.org](http://maven.apache.org))
* Lombok ([https://projectlombok.org/](https://projectlombok.org/))


## Download

* Go to [https://www.eclipse.org](https://www.eclipse.org)

* Download the "Eclipse IDE for Enterprise Java Developers" package (Version: Eclipse IDE 2019-09)


## Install

* Extract the ZIP archive to an installation directory

* Go to [https://projectlombok.org/](https://projectlombok.org/) and download the latest version

* Run `java -jar lombok.jar` and install Lombok in your Eclipse installation directory


## First start and configuration

* after the first start the following plugins have to be installed via the Eclipse Marketplace (Help > Eclipse Marketplace...)
** Spring Tools 4 - for Spring Boot (aka Spring Tool Suite 4)

* Map Java JDK installation directory (Window > Preferences > Java > Installed JREs)

* Map Maven installation directory (Window > Preferences > Maven > Installations)


## Import the project and run the application

* Import the project
** Open the import wizard (File > Import...)
** Select "Existing Maven Projects" and click [Next >]
** Choose the directory of your working copy
** Select all projects and click [Finish]

* Run the application
** Navigate to the project `webapp`
** Via the context menu click on "Run As > Spring Boot App"
** This will launch the application and creates a run configuration that can be used on the next run
*** Switch to the profile "dev" in the run configuration
** Browse to [http://localhost:8080/](http://localhost:8080/)
