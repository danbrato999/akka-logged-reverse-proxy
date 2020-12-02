## Akka Logged Reverse Proxy

Basic reverse proxy to store the request/response data pair into
a Firebase database.

To run, copy your Firebase Service Account json to
src/main/resources/firebaseCredentials.json and set the env variable
FIREBASE_DB to point to the right database, then run

```
sbt clean compile run
```

By default, the service will bind to port 9000. A toy api is
provided on port 8080 and reverse proxied by default for test purposes.