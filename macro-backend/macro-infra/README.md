## Set up cluster locally

### Install microk8s locally

1. Install microk8s

```
sudo snap install microk8s --classic
```

2. Add your user to the MicroK8s group

```
sudo usermod -a -G microk8s $USER
sudo chown -f -R $USER ~/.kube
newgrp microk8s
```

3. Enable necessary MicroK8s add-ons

```
microk8s enable dns storage helm3
```

4. Start the micrk8s cluster

```
microk8s start
```

5. Verify microk8s status

```
microk8s status --wait-ready
```

6. Check if the nodes are ready

```
microk8s kubectl get nodes
```

7. Create a Helm client alias

``` 
alias helm='microk8s helm3'
```

### Deploy Kafka and Zookeeper

1. Add the Bitnami repository for Helm charts

```
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update
```

2. Install Zookeper and Kafka

```
helm install zookeeper bitnami/zookeeper
helm install kafka bitnami/kafka
```