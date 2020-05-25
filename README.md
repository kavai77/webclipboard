# Web Clipboard
https://webclipboard.appspot.com/

## Setup
```
gcloud config set project webclipboard
```

## Run locally
```
gcloud auth application-default login
gcloud beta emulators datastore start --host-port localhost:8484
mvn spring-boot:run
```

## Deploy
```
mvn package appengine:deploy
```