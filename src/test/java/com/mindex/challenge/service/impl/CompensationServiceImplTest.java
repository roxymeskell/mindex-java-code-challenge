package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.service.CompensationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CompensationServiceImplTest {

    private String compensationUrl;
    private String compensationIdUrl;

    @Autowired
    private CompensationService compensationService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        compensationUrl = "http://localhost:" + port + "/compensation";
        compensationIdUrl = "http://localhost:" + port + "/compensation/{id}";
    }

    @Test
    @DisplayName("Assert fails when Compenstation is created for an Employee that does not exist")
    public void testCreateForEmployeeDoesNotExist() {
        Compensation testCompensation = new Compensation() {{
            setEmployee(new Employee() {{ setEmployeeId("invalid-employee"); }});
            setSalary(125000);
            setEffectiveDate(new Date(2009, 11, 15));
        }};
        ResponseEntity<Compensation> response = restTemplate.postForEntity(compensationUrl, testCompensation, Compensation.class);
        assertEquals(500, response.getStatusCodeValue());
    }


    @Test
    @DisplayName("Test if create and read for compensation works as expected")
    public void testCreateRead() {
        String employeeId = "16a596ae-edd3-4847-99fe-c4518e82c86f";
        Compensation testCompensation = new Compensation() {{
            setEmployee(new Employee() {{ setEmployeeId(employeeId ); }});
            setSalary(125000);
            setEffectiveDate(new Date(2009, 11, 15));
        }};

        Compensation testCompensationNew = new Compensation() {{
            setEmployee(new Employee() {{ setEmployeeId(employeeId ); }});
            setSalary(150000);
            setEffectiveDate(new Date(2013, 11, 15));
        }};

        // Create check
        Compensation createdCompensation = restTemplate.postForEntity(compensationUrl, testCompensation, Compensation.class).getBody();
        assertNotNull(createdCompensation.getEmployee());
        assertNotNull(createdCompensation.getEmployee().getEmployeeId());
        assertCompensationEquivalence(testCompensation, createdCompensation);


        // Read check
        Compensation readCompensation = restTemplate.getForEntity(compensationIdUrl, Compensation.class, employeeId ).getBody();
        assertNotNull(readCompensation.getEmployee());
        assertEquals(createdCompensation.getEmployee().getEmployeeId(), readCompensation.getEmployee().getEmployeeId());
        assertCompensationEquivalence(createdCompensation, readCompensation);

        // 2nd create check for same employee
        createdCompensation = restTemplate.postForEntity(compensationUrl, testCompensationNew, Compensation.class).getBody();
        assertNotNull(createdCompensation.getEmployee());
        assertNotNull(createdCompensation.getEmployee().getEmployeeId());
        assertCompensationEquivalence(testCompensationNew, createdCompensation);

        // 2nd read check for same employee (should return a single Compensation object which is the more recent of the two)
        readCompensation = restTemplate.getForEntity(compensationIdUrl, Compensation.class, employeeId).getBody();
        assertNotNull(readCompensation.getEmployee());
        assertEquals(createdCompensation.getEmployee().getEmployeeId(), readCompensation.getEmployee().getEmployeeId());
        assertCompensationEquivalence(createdCompensation, readCompensation);
    }

    private void assertCompensationEquivalence(Compensation expected, Compensation actual) {
        assertEquals(expected.getEmployee().getEmployeeId(), actual.getEmployee().getEmployeeId());
        assertEquals(expected.getSalary(), actual.getSalary(), 1e-15);
        assertEquals(expected.getEffectiveDate(), actual.getEffectiveDate());
    }
    
}
