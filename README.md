# swagger-java-client

## Requirements

Building the API client library requires:
1. Java 1.8
2. Gradle

## Installation

To copy the API client to your computer copy link of the repository, open command prompt, go to the location where project will be copied and execute git clone command:

```git
git clone https://github.com/VitaliiYaremko/AccountsAPI.git
```

Before building project go to .../src/main/resources/application and set up your credentials.

Then open command prompt and go to project directory:

```shell
cd .../AccountsAPI
```

Build project:

```shell
gradle build
```

And execute jar file:

```shell
java -jar build/libs/swagger-java-client-1.0.0.jar
```

If the application runs successfully, you can go to the link:

```shell
http://localhost:8080/accounts
```
