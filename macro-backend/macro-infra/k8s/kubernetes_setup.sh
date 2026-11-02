#!/bin/bash
sudo apt update
sudo apt upgrade
sudo apt install snapd -y
snap version

sudo snap install microk8s --classic
sudo usermod -a -G microk8s $USER
sudo chown -f -R $USER ~/.kube
su - $USER

alias k='microk8s kubectl'

microk8s start
microk8s status --wait-ready

k cluster-info
microk8s enable dns storage

k get all --all-namespaces
k get nodes
