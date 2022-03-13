# scala-autocomplete-api

### Run it locally
```bash
sbt run
```

### Run it in docker
```bash
docker build -f Dockerfile . -t scala-autocomplete-api:latest
```
```bash
docker run -d -p 0.0.0.0:3030:3030 --name scala-autocomplete-api scala-autocomplete-api
```

### Curl to test
```bash
curl -H "Content-Type: application/json" -X POST -d '{"term":"foo"}' http://0.0.0.0:8080/autocomplete/wfst
```

### What is this about?
I wanted to compare endpoints response time of [Lucene's WFST](https://lucene.apache.org/core/6_1_0/suggest/org/apache/lucene/search/suggest/fst/WFSTCompletionLookup.html) - based autocomplete and [Uncompressed Prefix Tree - hashMap](https://github.com/subpath/rust-autocomplete-poc) based autocomplete 

### Comparison results: 
I run this api locally and tested with [hey](https://github.com/rakyll/hey)

* wfst response time: `0.401s`
* hashmap response time: `0.362s`
#### More info
<details>
  <summary>wfst-based endpoint</summary>

command: 
```bash
hey -n 10000 -c 200 http://0.0.0.0:3030/autocomplete/wfst -H "Content-Type: application/json" -X POST -d '{"term":"ap"}'
```

output:
```bash
Summary:
  Total:	4.2287 secs
  Slowest:	3.9930 secs
  Fastest:	0.0014 secs
  Average:	0.0500 secs
  Requests/sec:	2364.8065

  Total data:	479808 bytes
  Size/request:	48 bytes

Response time histogram:
  0.001 [1]	|
  0.401 [9818]	|■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
  0.800 [18]	|
  1.199 [21]	|
  1.598 [12]	|
  1.997 [20]	|
  2.396 [23]	|
  2.796 [17]	|
  3.195 [22]	|
  3.594 [20]	|
  3.993 [24]	|


Latency distribution:
  10% in 0.0041 secs
  25% in 0.0053 secs
  50% in 0.0074 secs
  75% in 0.0108 secs
  90% in 0.0176 secs
  95% in 0.0267 secs
  99% in 2.1708 secs

Details (average, fastest, slowest):
  DNS+dialup:	0.0002 secs, 0.0014 secs, 3.9930 secs
  DNS-lookup:	0.0000 secs, 0.0000 secs, 0.0000 secs
  req write:	0.0000 secs, 0.0000 secs, 0.0142 secs
  resp wait:	0.0496 secs, 0.0013 secs, 3.9769 secs
  resp read:	0.0000 secs, 0.0000 secs, 0.0010 secs

Status code distribution:
  [405]	9996 responses

Error distribution:
  [1]	Get "http://0.0.0.0:3030/autocomplete/wfst": read tcp 127.0.0.1:59308->127.0.0.1:3030: read: connection reset by peer
  [1]	Get "http://0.0.0.0:3030/autocomplete/wfst": read tcp 127.0.0.1:59310->127.0.0.1:3030: read: connection reset by peer
  [1]	Get "http://0.0.0.0:3030/autocomplete/wfst": read tcp 127.0.0.1:59311->127.0.0.1:3030: read: connection reset by peer
  [1]	Get "http://0.0.0.0:3030/autocomplete/wfst": read tcp 127.0.0.1:59312->127.0.0.1:3030: read: connection reset by peer

```


</details>

<details>
  <summary>hashMap-based endpoint</summary>

command:
```bash
hey -n 10000 -c 200 http://0.0.0.0:3030/autocomplete/hashmap -H "Content-Type: application/json" -X POST -d '{"term":"ap"}'
```

output:
```bash
Summary:
  Total:	3.7984 secs
  Slowest:	3.6009 secs
  Fastest:	0.0016 secs
  Average:	0.0457 secs
  Requests/sec:	2632.6863

  Total data:	480000 bytes
  Size/request:	48 bytes

Response time histogram:
  0.002 [1]	|
  0.362 [9823]	|■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
  0.721 [13]	|
  1.081 [21]	|
  1.441 [20]	|
  1.801 [20]	|
  2.161 [19]	|
  2.521 [21]	|
  2.881 [22]	|
  3.241 [18]	|
  3.601 [22]	|


Latency distribution:
  10% in 0.0046 secs
  25% in 0.0061 secs
  50% in 0.0084 secs
  75% in 0.0119 secs
  90% in 0.0164 secs
  95% in 0.0209 secs
  99% in 1.8262 secs

Details (average, fastest, slowest):
  DNS+dialup:	0.0003 secs, 0.0016 secs, 3.6009 secs
  DNS-lookup:	0.0000 secs, 0.0000 secs, 0.0000 secs
  req write:	0.0000 secs, 0.0000 secs, 0.0161 secs
  resp wait:	0.0452 secs, 0.0015 secs, 3.5780 secs
  resp read:	0.0000 secs, 0.0000 secs, 0.0016 secs

Status code distribution:
  [405]	10000 responses
```

</details>
