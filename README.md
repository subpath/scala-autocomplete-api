# scala-autocomplete-api

### Build and start locally
```bash
sbt run
```

### Curl to test
```bash
curl -H "Content-Type: application/json" -X POST -d '{"term":"foo"}' http://localhost:8080/autocomplete/wfst
```