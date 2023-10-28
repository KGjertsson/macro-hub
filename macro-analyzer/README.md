### Build docker image

```
docker build -t macro-analyzer .
```

### Run docker image

```
docker run -e "SPRING_PROFILES_ACTIVE=prod -p 8080:8080 macro-analyzer
```
