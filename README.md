# Trellis Application

How to start the Trellis application
---

1. Run `gradle clean install` to build your application
1. Start application with `java -jar build/trellis.jar server config.yml`
1. To check that your application is running enter url `http://localhost:8080`

Health Check
---

To see your applications health enter url `http://localhost:8081/healthcheck`
