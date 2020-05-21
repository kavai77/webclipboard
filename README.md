# webclipboard

## setup
gcloud config set project webclipboard

## run locally
gcloud auth application-default login
gcloud beta emulators datastore start
mvn spring-boot:run

## deploy
mvn package appengine:deploy