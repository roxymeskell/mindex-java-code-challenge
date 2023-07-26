[![Test](https://github.com/roxymeskell/mindex-java-code-challenge/actions/workflows/gradle-test.yml/badge.svg)](https://github.com/roxymeskell/mindex-java-code-challenge/actions/workflows/gradle-test.yml)
# Coding Challenge
## What's Provided
A simple [Spring Boot](https://projects.spring.io/spring-boot/) web application has been created and bootstrapped 
with data. The application contains information about all employees at a company. On application start-up, an in-memory 
Mongo database is bootstrapped with a serialized snapshot of the database. While the application runs, the data may be
accessed and mutated in the database without impacting the snapshot.

### How to Run
The application may be executed by running `gradlew bootRun`.

### How to Use
The following endpoints are available to use:
```
* CREATE
    * HTTP Method: POST 
    * URL: localhost:8080/employee
    * PAYLOAD: Employee
    * RESPONSE: Employee
* READ
    * HTTP Method: GET 
    * URL: localhost:8080/employee/{id}
    * RESPONSE: Employee
* UPDATE
    * HTTP Method: PUT 
    * URL: localhost:8080/employee/{id}
    * PAYLOAD: Employee
    * RESPONSE: Employee
```
The Employee has a JSON schema of:
```json
{
  "type":"Employee",
  "properties": {
    "employeeId": {
      "type": "string"
    },
    "firstName": {
      "type": "string"
    },
    "lastName": {
          "type": "string"
    },
    "position": {
          "type": "string"
    },
    "department": {
          "type": "string"
    },
    "directReports": {
      "type": "array",
      "items" : "string"
    }
  }
}
```
For all endpoints that require an "id" in the URL, this is the "employeeId" field.

## What to Implement
Clone or download the repository, do not fork it.

### Task 1
Create a new type, ReportingStructure, that has two properties: employee and numberOfReports.

For the field "numberOfReports", this should equal the total number of reports under a given employee. The number of 
reports is determined to be the number of directReports for an employee and all of their distinct reports. For example, 
given the following employee structure:
```
                    John Lennon
                /               \
         Paul McCartney         Ringo Starr
                               /        \
                          Pete Best     George Harrison
```
The numberOfReports for employee John Lennon (employeeId: 16a596ae-edd3-4847-99fe-c4518e82c86f) would be equal to 4. 

This new type should have a new REST endpoint created for it. This new endpoint should accept an employeeId and return 
the fully filled out ReportingStructure for the specified employeeId. The values should be computed on the fly and will 
not be persisted.

### Task 2
Create a new type, Compensation. A Compensation has the following fields: employee, salary, and effectiveDate. Create 
two new Compensation REST endpoints. One to create and one to read by employeeId. These should persist and query the 
Compensation from the persistence layer.

## Delivery
Please upload your results to a publicly accessible Git repo. Free ones are provided by Github and Bitbucket.


# Challange Notes
A stream of consciousness of how I solved this challange.

### Task 1
Can one Employee have more than one direct supervisor? There does not seem to be any constraint, especially considering the data structure given.
(I.e., an employee having a list of direct reports as opposed to an employee having a singular direct supervisor.)
This means that, if not careful, employees in reporting structures could be counted twice.
The lack on constraints also appear to let an employee possibly report directly to themselves, or in a loop.

If the reporting structure was guaranteed to always be a tree, a recursive depth first search would work.
``` java
int getNumberOfReports(Employee employee) {
    int numberOfReports = 0;
    for(Employee dReport : this.employee.directReports) {
        numberOfReports += 1 + this.getNumberOfReports(dReport);
    }
    return numberOfReports;
}
```

My first thought was nailing down the algorithm. I used a breadth-first search while keeping track of employees I had already seen. Then I discovered that the employee data for direct reports were not fleshed out beyond their IDs (which makes sense). Reworked code to check `employeeRepository` for each employee ID found in order to trace the reporting structure.

For testing, I mocked responses from `employeeRepository` in order to present different scenarios with different reporting structures. The tests were used to validate the algorithm used to discover the reporting structure.


**Note:**
Another way I found of find the `ReportingStructure` be be through a method to `EmployeeRepository` that used an aggregate pipeline to query for all the relations and counting them, which I included in a code snippet below. However, this would require using a more recent version of MongoDB than I was provided.
Though I believe I can possibly specify a version of MongoDB to use by adding the line `spring.mongodb.embedded.version=3.6.4` to the `applications.properties` file.
```java
@Aggregation(pipeline = {
    "{ '$match' : { 'employeeId': '?0' } }",
    "{'$graphLookup': {" +
    "'from': 'employee', 'startWith': '$directReports.employeeId'," +
    "'connectFromField': 'directReports.employeeId', 'connectToField': 'employeeId'," +
    "'as': 'allReports', 'restrictSearchWithMatch': { 'employeeId': '?0' } } }",
    "{ '$project' : { 'employee': { 'employeeId': '$employeeId' }, 'numberOfReports': { '$size': '$allReports' } } }"
})
ReportingStructure findNumberOfReports(String employeeId);
```

### Task 2
First I created the class components I would need: the data class, the service class, and the controller. To persist the data, and repository was also needed.

The description of `Compensation` did not indicate that it had its own primary id, and instead was defined by the id of the `Employee` it was associated with.
This presented an issue when trying to query for `Compensation` using only an `employeeId`, as the field wasn't defined directly on the data class.
Ultimately I fixed this using annotations to define a custom query (and eventually aggeration pipeline) for the find function.

Because `Compensation` is defined with an `Employee` and queried through `employeeId`, I made sure to check that an `Employee` existed before creating a `Compensation` object for that employee. Additionally, when reading compenstaion for an employee, if a `Compensation` object is not found, then it is also checked if an `Employee` object existed, and errors messages are given accordingly.

When creating multiple `Compensation` objects for a single employee, there was an issue with multiple results being returned which was unexpected by the method. To combat this, I set up `CompensationRepository::findByEmployeeId` to query using an aggregation method.
```java
@Aggregation(pipeline = {
    "{ '$match': {'employee': { 'employeeId' : ?0 }} }", 
    "{ '$sort' : { 'effectiveDate' : -1 } }",
    "{ '$limit' : 1 }"
})
Compensation findByEmployeeId(@Param("employeeId") String employeeId);
```
