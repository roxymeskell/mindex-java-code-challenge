package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.service.ReportingStructureService;
import java.util.LinkedList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReportingStructureServiceImplTest {

    private String reportingStructureUrl;

    @MockBean
    private EmployeeRepository employeeRepository;

    @Autowired
    private ReportingStructureService reportingStructureService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        reportingStructureUrl = "http://localhost:" + port + "/reportingStructure/{id}";
    }

    @Test
    /**
     * Test that reading a reporting structure fails when given an invalid employee
     */
    public void failWhenEmployeeDoesNotExist() {
        ResponseEntity<ReportingStructure> response = restTemplate.getForEntity(
            reportingStructureUrl,
            ReportingStructure.class,
            "invalid-employee"
        );
        assertEquals(500, response.getStatusCodeValue());
    }

    @Test
    /**
     * Test that reading a reporting struture returns a report
     * count of 0 when given an employee with no direct reports
     */
    public void readWhenEmployeeHasNoReports() {
        String testEmployeeId = setupEmployees(new int[][]{{}});
        ReportingStructure readReportingStructure = restTemplate.getForEntity(
            reportingStructureUrl,
            ReportingStructure.class,
            testEmployeeId
        ).getBody();
        assertEquals(0, readReportingStructure.getNumberOfReports());
    }

    @Test
    /**
     * Test that reading a report structure returns a report
     * count of 5 when given the following employee structure.
     *       A (Root)
     *       /     \
     *      B _     C
     *     /   \     \
     *    D     E     F
     */
    public void readWhenEmployeeReportsIsTree() {
        String testEmployeeId = setupEmployees(new int[][]{{1, 2}, {3, 4}, {5}, {}, {}, {}});
        ReportingStructure readReportingStructure = restTemplate.getForEntity(
            reportingStructureUrl,
            ReportingStructure.class,
            testEmployeeId
        ).getBody();
        assertEquals(5, readReportingStructure.getNumberOfReports());
    }

    @Test
    /**
     * Test that reading a report structure returns a report
     * count of 5 when given the following employee structure.
     *       A (Root)
     *       /     \
     *      B _   _ C
     *     /   \ /   \
     *    D     E     F
     */
    public void readWhenMultipleEmployeesHaveSameReport() {
        String testEmployeeId = setupEmployees(new int[][]{{1, 2}, {3, 4}, {4, 5}, {}, {}, {}});
        ReportingStructure readReportingStructure = restTemplate.getForEntity(
            reportingStructureUrl,
            ReportingStructure.class,
            testEmployeeId
        ).getBody();
        assertEquals(5, readReportingStructure.getNumberOfReports());
    }

    @Test
    /**
     * Test that reading a report structure returns a report
     * count of 0 when given an employee who reports to themself.
     */
    public void readWhenEmployeeReportsToSelf() {
        String testEmployeeId = setupEmployees(new int[][]{{0}});
        ReportingStructure readReportingStructure = restTemplate.getForEntity(
            reportingStructureUrl,
            ReportingStructure.class,
            testEmployeeId
        ).getBody();
        assertEquals(0, readReportingStructure.getNumberOfReports());
    }

    /**
     * A private helper method to set up employees and direct reports for tests.
     * @param directReportAdj
     * @return First created employee Id
     */
    private String setupEmployees(int[][] directReportAdj) {
        // Make employees
        for (int i = 0; i < directReportAdj.length; i++) {
            String id = Integer.toString(i);
            Employee e = new Employee();
            e.setEmployeeId(id);
            int[] dReportAdj = directReportAdj[i];
            e.setDirectReports(
                new LinkedList<Employee>() {{
                    for (int r : dReportAdj) {
                        String rId = Integer.toString(r);
                        add(new Employee(){{ setEmployeeId(rId); }});
                    }
                }}
            );
            given(employeeRepository.findByEmployeeId(id)).willReturn(e);
        }
        return "0";
    }
}
