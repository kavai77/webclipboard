# Web Clipboard
https://webclipboard.appspot.com/

## Setup
```
gcloud config set project webclipboard
```

## Run locally
```
gcloud auth application-default login
gcloud iam service-accounts keys create ~/.config/gcloud/webclipboard-firebase-admin-service.json --iam-account=firebase-adminsdk-u7opc@webclipboard.iam.gserviceaccount.com
gcloud beta emulators datastore start --host-port localhost:8484
mvn spring-boot:run
```

## Deploy
```
mvn package appengine:deploy
```