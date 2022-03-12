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