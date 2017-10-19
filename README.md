# Trellis Application

[![Build Status](https://travis-ci.org/trellis-ldp/trellis-app.png?branch=master)](https://travis-ci.org/trellis-ldp/trellis-app)
[![Build status](https://ci.appveyor.com/api/projects/status/xu5qujp9ky2xq0uf?svg=true)](https://ci.appveyor.com/project/acoburn/trellis-app)
[![Coverage Status](https://coveralls.io/repos/github/trellis-ldp/trellis-app/badge.svg?branch=master)](https://coveralls.io/github/trellis-ldp/trellis-app?branch=master)

How to start the Trellis application
---

1. Run `./gradlew clean build` to build your application
2. Start application with `java -jar build/libs/trellis.jar server config.yml`
3. To check that your application is running, enter url `http://localhost:8080`

Health Check
---

Application health checks are available at `http://localhost:8081/healthcheck`
