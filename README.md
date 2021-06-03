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

Set following environment variables:

* TOKEN_URL
* CLIENT_ID
* CLIENT_SECRET

For example:

```shell
set TOKEN_URL=*******
```

Open command prompt, go to your project directory and execute this command to install gradle wrapper:

```shell
gradlew wrapper --gradle-version 7.0.2
```

Then, to compile the project without executing any tests so build reports execute this  command:

```shell
gradlew assemble
```

Finally, to run application execute this:

```shell
gradlew clean bootRun
```

If the application runs successfully, you can go to the link:

```shell
http://localhost:8080/accounts
```

To terminate application press ***Ctrl + C*** and execute ***y*** after this appears:

```shell
Terminate batch job (Y/N)?
```

####Another way to run application:

Build project:

```shell
gradle build
```

And execute jar file:

```shell
java -jar "localPath/build/libs/swagger-java-client-1.0.0.jar"
```

Instead of "localPath" input your local directory to the project together with project name.