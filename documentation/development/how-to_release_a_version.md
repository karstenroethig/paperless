# How-to release a version

This guide describes how to release a version of the application.


## Set version number

* Navigate to the working directory

* Run `mvn versions:set -DgenerateBackupPoms=false` in the root directory of the application

* Specify the version number in the following format: [MAJOR].[MINOR].[PATCH] (see [Semantic Versioning](https://semver.org/))


## Fill the changelog

* Edit the file `[WORKING_DIRECTORY]/documentation/distribution/CHANGELOG.md`
** Note the template from the file `[WORKING_DIRECTORY]/documentation/development/templates/CHANGELOG-template.md`
** Give the user helpful hints to what has changed
** At least provide information about the upgrade guide


## Clean up your developer configuration

* Note the file `[WORKING_DIRECTORY]/webapp/src/main/resources/application.yml`
** Default port: 8080
** Set database to H2 in-memory database

* Note the file `[WORKING_DIRECTORY]/webapp/src/main/resources/db/master.xml`
** Do not include any test data (`testdata.xml`)


## Commit all changes

* Make sure that all changes have been committed in to the SCM.

* (optional) Run Jenkins build to make sure everything has been committed.


## Tag the version

* Create a tag in the SCM for the current state

* Use the following format: v[VERSION]

* Example:
** Name: v1.5.0
** Commit message: Release 1.5.0


## Build the installation package

* see also: [Build the application](documentation/development/how-to_build_the_application.md)

* Navigate to the working directory

* Run `mvn clean package` in the root directory of the application

* Navigate to `distribution/target` where you find the `paperless_v[VERSION].zip`


## Provide the installation package

* All installation packages must be stored with the release in Github


## Prepare next version

### Set version number

* Navigate to the working directory

* Run `mvn versions:set -DgenerateBackupPoms=false` in the root directory of the application

* Specify the version number in the following format: [MAJOR].[MINOR].[PATCH]-SNAPSHOT (see [Semantic Versioning](https://semver.org/))
** Example: 1.5.1-SNAPSHOT

### Fill the changelog

* Edit the file `[WORKING_DIRECTORY]/documentation/distribution/CHANGELOG.md`
** Note the template from the file `[WORKING_DIRECTORY]/documentation/development/templates/CHANGELOG-template.md`

### Commit all changes

* Make sure that all changes have been committed in to the SCM so other developers know about it.
