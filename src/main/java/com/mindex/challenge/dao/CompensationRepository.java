package com.mindex.challenge.dao;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface CompensationRepository extends MongoRepository<Compensation, String> {

    @Aggregation(pipeline = {
        "{ '$match': {'employee': { 'employeeId' : ?0 }} }", 
        "{ '$sort' : { 'effectiveDate' : -1 } }",
        "{ '$limit' : 1 }"
    })
    Compensation findByEmployeeId(@Param("employeeId") String employeeId);

}
