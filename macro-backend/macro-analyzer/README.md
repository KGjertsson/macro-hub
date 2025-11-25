## marco-analyzer


### Build docker image

```
sudo docker build -t macro-analyzer:latest .
```

### Run docker image

```
docker run -e "SPRING_PROFILES_ACTIVE=prod -p 8080:8080 macro-analyzer
```

### Upload build docker image to local microk8s storage

```
sudo docker save macro-analyzer:latest | microk8s ctr image import -
```

### Apply postgres pods

```
k apply -f postgres/
```

### Port forward db from microk8s to local

```
k port-forward <pod-name> 5432:5432
```

### Maven
generate jooq model
```
mvn -P jooq-codegen generate-sources
```
