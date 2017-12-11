scala-merchant-demo
===================

[![Build Status](https://travis-ci.org/howyp/scala-merchant-demo.svg?branch=master)](https://travis-ci.org/howyp/scala-merchant-demo)

Demo project showing how to build a REST service using Scala, http4s and Circe.

Building
--------
To build, test and then run the service, use:

```
sbt clean test run
```

The service runs on port 8080. Offers can be queried and created at `/offers`:

```
$ curl -v http://localhost:8080/offers
* Connected to localhost (127.0.0.1) port 8080 (#0)
> GET /offers HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.54.0
> Accept: */*
>
Connection to /127.0.0.1:51554 accepted at Mon Dec 11 22:22:51 GMT 2017.
< HTTP/1.1 200 OK
< Content-Type: application/json
< Date: Mon, 11 Dec 2017 22:22:51 GMT
< Content-Length: 2
<
* Connection #0 to host localhost left intact
[]
```

