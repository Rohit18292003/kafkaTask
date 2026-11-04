# kafkaTask
Pranav and rohit working on this repository

## Build and run

`Producer` and `Consumer` are separate Spring Boot modules. Build or run each from
its own directory; converting `Consumer` to Maven does not combine the modules.

On Windows, run the Consumer tests and application with:

```powershell
cd Consumer
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

Use `.\mvnw.cmd package` to create the Consumer JAR under `Consumer\target`.
The existing Producer module can still be built independently with
`cd Producer` followed by `.\mvnw.cmd clean test`.
