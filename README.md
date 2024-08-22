# macro-hub

Visualizing macro-economic trends and parameters.  

Potential future architecture:
![macro-hub-services-arch.drawio.png](macro-hub-services-arch.drawio.png)

### Set Up Guide

1. Configure the microk8s cluster
2. Run postgres pod in the cluster
3. Run the backend application in IntelliJ
4. Install the frontend repo
5. Run the frontend repo
6. Run individual scrape queries below to populate the database

---

## marco-analyzer

### generate jooq model

`mvn org.jooq:jooq-codegen-maven:3.18.5:generate`

### sources

- [Riskbanken REST API](https://www.riksbank.se/sv/statistik/sok-rantor--valutakurser/hamta-rantor-och-valutakurser-via-api/)
- [Riskbanken series](https://www.riksbank.se/sv/statistik/sok-rantor--valutakurser/oppet-api/serier-for-webbservices/)
- [Hexagonal architecture](https://medium.com/ssense-tech/hexagonal-architecture-there-are-always-two-sides-to-every-story-bc0780ed7d9c)
- [Commit message guidelines](https://gist.github.com/robertpainsi/b632364184e70900af4ab688decf6f53)
- [Kafka with microk8s](https://medium.com/@prasanta.mohanty/deploy-kafka-cluster-on-microk8s-in-15-mins-f3d5081991e8)

## macro-frontend

NextJS + react-chartjs-2

### Sources

- [react-chartjs-2 documentation](https://react-chartjs-2.js.org/components/line)
- [Tanstack Query](https://tanstack.com/query/latest)
