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
< HTTP/1.1 200 OK
< Content-Type: application/json
< Date: Mon, 11 Dec 2017 22:22:51 GMT
< Content-Length: 2
<
* Connection #0 to host localhost left intact
[]

$ curl -v -d '@examples/offer.json' -H "Content-Type: application/json" -X POST http://localhost:8080/offers
* Connected to localhost (127.0.0.1) port 8080 (#0)
> POST /offers HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.54.0
> Accept: */*
> Content-Type: application/json
> Content-Length: 136
>
* upload completely sent off: 136 out of 136 bytes
< HTTP/1.1 201 Created
< Location: /offers/8c597459-db0c-4c0d-a9e2-b5b3511e5ed1
< Date: Tue, 12 Dec 2017 07:35:24 GMT
< Content-Length: 0
<
* Connection #0 to host localhost left intact

$ curl -v http://localhost:8080/offers/8c597459-db0c-4c0d-a9e2-b5b3511e5ed1
* Connected to localhost (127.0.0.1) port 8080 (#0)
> GET /offers/8c597459-db0c-4c0d-a9e2-b5b3511e5ed1 HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.54.0
> Accept: */*
>
< HTTP/1.1 200 OK
< Content-Type: application/json
< Date: Tue, 12 Dec 2017 07:36:02 GMT
< Content-Length: 119
<
* Connection #0 to host localhost left intact
{"merchantId":1234,"productId":"AM039827X","price":{"currency":"GBP","amount":503762},"expires":"2099-01-01T00:00:00Z"}
$

$ curl -v http://localhost:8080/offers
* Connected to localhost (127.0.0.1) port 8080 (#0)
> GET /offers HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.54.0
> Accept: */*
>
< HTTP/1.1 200 OK
< Content-Type: application/json
< Date: Tue, 12 Dec 2017 07:38:45 GMT
< Content-Length: 184
<
* Connection #0 to host localhost left intact
[{"href":"/offers/8c597459-db0c-4c0d-a9e2-b5b3511e5ed1","item":{"merchantId":1234,"productId":"AM039827X","price":{"currency":"GBP","amount":503762},"expires":"2099-01-01T00:00:00Z"}}]
$

```