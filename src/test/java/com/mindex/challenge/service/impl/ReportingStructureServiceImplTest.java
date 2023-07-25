package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.service.ReportingStructureService;
import java.util.LinkedList;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("Assert fails on employee not existing")
    public void testReadForEmployeeDoesNotExist() {
        // Read check
        ResponseEntity<ReportingStructure> response = restTemplate.getForEntity(
            reportingStructureUrl,
            ReportingStructure.class,
            "invalid-employee"
        );
        assertEquals(500, response.getStatusCodeValue());
    }

    @Test
    @DisplayName("Assert returns 0 reports when given an employee with no reports")
    public void testReadForNoReports() {
        String testEmployeeId = setupEmployees(new int[][]{{}});
        ReportingStructure readReportingStructure = restTemplate.getForEntity(
            reportingStructureUrl,
            ReportingStructure.class,
            testEmployeeId
        ).getBody();
        assertEquals(0, readReportingStructure.getNumberOfReports());
    }

    @Test
    @DisplayName("Assert returns reports when given tree")
    public void testReadForHasReports() {
        String testEmployeeId = setupEmployees(new int[][]{{1, 2}, {3, 4}, {5}, {}, {}, {}});
        ReportingStructure readReportingStructure = restTemplate.getForEntity(
            reportingStructureUrl,
            ReportingStructure.class,
            testEmployeeId
        ).getBody();
        assertEquals(5, readReportingStructure.getNumberOfReports());
    }

    @Test
    @DisplayName("Assert returns reports when given an employee who reports to two others")
    public void testReadForMultipleSupervisers() {
        String testEmployeeId = setupEmployees(new int[][]{{1, 2}, {3, 4}, {4, 5}, {}, {}, {}});
        ReportingStructure readReportingStructure = restTemplate.getForEntity(
            reportingStructureUrl,
            ReportingStructure.class,
            testEmployeeId
        ).getBody();
        assertEquals(5, readReportingStructure.getNumberOfReports());
    }

    @Test
    @DisplayName("Assert returns reports when given employee reporting to themself")
    public void testReadForReportsToSelf() {
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
