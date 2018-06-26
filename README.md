Tiny Search Engine
=====
## System requirements
Java8
maven
spring boot

## Execute the code
mvn spring-boot:run

## Boolean Operators in query

Query operator  meaning  Logical operator
   
&  ->   AND

|  ->   OR
 
!  ->   NOT
 
## Boolean Query

Example Query: { "query":"apache & (!mozillas | contributors)" }

can use brackets for creating arbitrary boolean query

## Phrase Query

Example Query: { "query":"\"To protect your rights\"" }

Quotes are used to specify phrases. Need to escape quotes in JSON structure

## Swagger interface

[Interface URL](http://localhost:8080/app/swagger-ui.html)

