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

    // Any requests besides read should fail with a 400 error
    
    @Test
    public void testRead() {
        // Read check
        ReportingStructure readReportingStructure = restTemplate.getForEntity(
            reportingStructureUrl,
            ReportingStructure.class,
            "16a596ae-edd3-4847-99fe-c4518e82c86f"
        ).getBody();
        assertEquals(4, readReportingStructure.getNumberOfReports());
    }

    @Test
    public void testReadNoReports() {
        String testEmployeeId = setupEmployees(new int[][]{{}});
        ReportingStructure readReportingStructure = restTemplate.getForEntity(
            reportingStructureUrl,
            ReportingStructure.class,
            testEmployeeId
        ).getBody();
        assertEquals(0, readReportingStructure.getNumberOfReports());
    }

    @Test
    public void testReadHasReports() {
        String testEmployeeId = setupEmployees(new int[][]{{1, 2}, {3, 4}, {5}, {}, {}, {}});
        ReportingStructure readReportingStructure = restTemplate.getForEntity(
            reportingStructureUrl,
            ReportingStructure.class,
            testEmployeeId
        ).getBody();
        assertEquals(5, readReportingStructure.getNumberOfReports());
    }

    @Test
    public void testReadMultipleSupervisers() {
        String testEmployeeId = setupEmployees(new int[][]{{1, 2}, {3, 4}, {4, 5}, {}, {}, {}});
        ReportingStructure readReportingStructure = restTemplate.getForEntity(
            reportingStructureUrl,
            ReportingStructure.class,
            testEmployeeId
        ).getBody();
        assertEquals(5, readReportingStructure.getNumberOfReports());
    }

    @Test
    public void testReadReportsToSelf() {
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

        // LinkedList<Employee> employees = new LinkedList<>();
        // // Make employees
        // for (int i = 0; i < directReportAdj.length; i++) {
        //     String id = Integer.toString(i);
        //     employees.add(new Employee());
        // }

        // given(employeeRepository.findByEmployeeId(testEId)).willReturn(testEmployee);

        // for (int i = 0; i < directReportAdj.length; i++) {
        //     String id = Integer.toString(i);
        //     int[] dReportAdj = directReportAdj[i];
        //     employees.get(i).setEmployeeId(id);
        //     employees.get(i).setDirectReports(
        //         new LinkedList<Employee>() {{
        //             for (int r : dReportAdj) {
        //                 // if (r < 0 || r >= directReportAdj.length)
        //                 //     throw new Exception("Index for direct report specified is out of bounds.");
        //                 add(employees.get(r));
        //             }
        //         }}
        //     );
        // }

        // // Return first employee
        // return employees.peek().getEmployeeId();
    }
}
