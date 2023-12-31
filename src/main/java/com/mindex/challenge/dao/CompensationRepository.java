package com.mindex.challenge.dao;

import com.mindex.challenge.data.Compensation;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

@Repository
public interface CompensationRepository extends MongoRepository<Compensation, String> {

    @Aggregation(pipeline = {
        "{ '$match': {'employee': { 'employeeId' : ?0 }} }", 
        "{ '$sort' : { 'effectiveDate' : -1 } }",
        "{ '$limit' : 1 }"
    })
    Compensation findByEmployeeId(String employeeId);

}
