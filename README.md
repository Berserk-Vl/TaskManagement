    Default values for database(PostgreSQL):
    username  -  postgres
    password  -  admin
    dbname    -  tasks
    port      -  5432

To run locally with default settings run the next command in CLI from the root directory of the project:

`mvnw clean spring-boot:run`

or to set your values run the same command with one(or all) of the parameter(s):

 -Dspring.datasource.url=jdbc:postgresql://localhost:_**port**_/_**dbname**_\
 -Dspring.datasource.username=_**username**_\
 -Dspring.datasource.password=_**password**_

or set these parameters in application.properties file which is in _**src/main/resources**_ folder.

To run tests(CLI from root directory):
    
    mvnw clean test -Dspring.jpa.show-sql=false

To run in docker container(CLI from root directory):

    docker compose up --build
 
***
OpenAPI:

http://localhost:8080/openapi/swagger-ui

Two default users to interact with API:

    email:        admin@sb.ru

    password:     admin

and

    email:       user@mail.ru

    password:    123