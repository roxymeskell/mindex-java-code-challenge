package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.service.CompensationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
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
    /**
     * Test that creating a compensation fails when given an invalid employee
     */
    public void failCreateWhenEmployeeDoesNotExist() {
        Compensation testCompensation = new Compensation() {{
            setEmployee(new Employee() {{ setEmployeeId("invalid-employee"); }});
            setSalary(125000);
            setEffectiveDate(new Date(2009, 11, 15));
        }};
        ResponseEntity<Compensation> response = restTemplate.postForEntity(
            compensationUrl, 
            testCompensation, 
            Compensation.class
        );
        assertEquals(500, response.getStatusCodeValue());
    }

    @Test
    /**
     * Test that reading a compensation fails when given an invalid employee
     */
    public void failReadWhenEmployeeDoesNotExist() {
        ResponseEntity<Compensation> response = restTemplate.getForEntity(
            compensationIdUrl, 
            Compensation.class, 
            "invalid-employee"
            );
        assertEquals(500, response.getStatusCodeValue());
    }

    @Test
    /**
     * Test that reading a compensation fails when given an employee with no compensation
     */
    public void failReadWhenEmployeeExistsWithoutCompensation() {
        String employeeId = "16a596ae-edd3-4847-99fe-c4518e82c86f";
        ResponseEntity<Compensation> response = restTemplate.getForEntity(
            compensationIdUrl, 
            Compensation.class, 
            employeeId
        );
        Compensation readCompensation = response.getBody();
        assertEquals(500, response.getStatusCodeValue());
    }

    @Test
    @DirtiesContext
    /**
     * Test creating compensation for a valid employee
     */
    public void createCompensation() {
        String employeeId = "16a596ae-edd3-4847-99fe-c4518e82c86f";
        Compensation testCompensation = new Compensation() {{
            setEmployee(new Employee() {{ setEmployeeId(employeeId); }});
            setSalary(125000);
            setEffectiveDate(new Date(2009, 11, 15));
        }};
        Compensation createdCompensation = restTemplate.postForEntity(
            compensationUrl,
            testCompensation, 
            Compensation.class
        ).getBody();
        assertNotNull(createdCompensation.getEmployee());
        assertNotNull(createdCompensation.getEmployee().getEmployeeId());
        assertCompensationEquivalence(testCompensation, createdCompensation);
    }

    @Test
    @DirtiesContext
    /**
     * Test reading compensation for a valid employee with compensation
     */
    public void readCompensation() {
        String employeeId = "16a596ae-edd3-4847-99fe-c4518e82c86f";
        Compensation testCompensation = new Compensation() {{
            setEmployee(new Employee() {{ setEmployeeId(employeeId); }});
            setSalary(125000);
            setEffectiveDate(new Date(2009, 11, 15));
        }};

        // Setup by creating compensation
        restTemplate.postForEntity(
            compensationUrl,
            testCompensation, 
            Compensation.class
        );

        // Read
        Compensation readCompensation = restTemplate.getForEntity(
            compensationIdUrl, 
            Compensation.class,
            employeeId
        ).getBody();
        assertNotNull(readCompensation.getEmployee());
        assertCompensationEquivalence(testCompensation, readCompensation);
    }

    @Test
    @DirtiesContext
    /**
     * Test creating multiple compensations for the same employee
     */
    public void createMultipleForSameEmployee() {
        String employeeId = "16a596ae-edd3-4847-99fe-c4518e82c86f";
        Compensation firstCompensation = new Compensation() {{
            setEmployee(new Employee() {{ setEmployeeId(employeeId); }});
            setSalary(125000);
            setEffectiveDate(new Date(2009, 11, 15));
        }};
        Compensation testCompensation = new Compensation() {{
            setEmployee(new Employee() {{ setEmployeeId(employeeId); }});
            setSalary(150000);
            setEffectiveDate(new Date(2013, 11, 15));
        }};

        // Setup by creating first compensation
        restTemplate.postForEntity(
            compensationUrl,
            firstCompensation, 
            Compensation.class
        );

        // Create second compensation
        Compensation createdCompensation = restTemplate.postForEntity(
            compensationUrl,
            testCompensation, 
            Compensation.class
        ).getBody();
        assertNotNull(createdCompensation.getEmployee());
        assertNotNull(createdCompensation.getEmployee().getEmployeeId());
        assertCompensationEquivalence(testCompensation, createdCompensation);
    }


    @Test
    @DirtiesContext
    /**
     * Test reading compensation for an employee with multiple compensations
     * Should return compensation with the most recent effective date
     */
    public void readWhenEmployeeHasMultipleCompensations() {
        String employeeId = "16a596ae-edd3-4847-99fe-c4518e82c86f";
        Compensation firstCompensation = new Compensation() {{
            setEmployee(new Employee() {{ setEmployeeId(employeeId); }});
            setSalary(125000);
            setEffectiveDate(new Date(2009, 11, 15));
        }};
        Compensation secondCompensation = new Compensation() {{
            setEmployee(new Employee() {{ setEmployeeId(employeeId); }});
            setSalary(150000);
            setEffectiveDate(new Date(2013, 11, 15));
        }};

        // Setup by creating compensations
        restTemplate.postForEntity(
            compensationUrl,
            firstCompensation, 
            Compensation.class
        );
        restTemplate.postForEntity(
            compensationUrl,
            secondCompensation, 
            Compensation.class
        );

        // Read
        Compensation readCompensation = restTemplate.getForEntity(
            compensationIdUrl, 
            Compensation.class,
            employeeId
        ).getBody();
        assertNotNull(readCompensation.getEmployee());
        assertCompensationEquivalence(secondCompensation, readCompensation);
    }

    /**
     * Helper method to assert the two Compensation instances match
     * @param expected
     * @param actual
     */
    private void assertCompensationEquivalence(Compensation expected, Compensation actual) {
        assertEquals(expected.getEmployee().getEmployeeId(), actual.getEmployee().getEmployeeId());
        assertEquals(expected.getSalary(), actual.getSalary(), 1e-15);
        assertEquals(expected.getEffectiveDate(), actual.getEffectiveDate());
    }
    
}
