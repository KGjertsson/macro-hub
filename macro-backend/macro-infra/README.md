## Java

Build new image and make available to microk8s:

```
mvn install
sudo docker build -t macro-analyzer:latest .
sudo docker save macro-analyzer:latest | microk8s ctr image import -
```

## Kafka

Consume from topic inside a Kafka pod
```
/opt/bitnami/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:29092 --topic testtopic --from-beginning
```

Produce to topic inside a Kafka pod
```
echo "Hello Kafka from the Kafka pod!" | /opt/bitnami/kafka/bin/kafka-console-producer.sh --broker-list localhost:29092 --topic testtopic
```

## Sources
 - [Run postgres on kubernetes](https://phoenixnap.com/kb/postgresql-kubernetes)
 - [Run kafka on kubernetes](https://phoenixnap.com/kb/kafka-on-kubernetes)

