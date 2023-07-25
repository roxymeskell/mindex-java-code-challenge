package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.ReportingStructureService;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportingStructureServiceImpl implements ReportingStructureService {

    private static final Logger LOG = LoggerFactory.getLogger(ReportingStructureServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public ReportingStructure read(String employeeId) {
        LOG.debug("Creating employee with id [{}]", employeeId);

        Employee employee = employeeRepository.findByEmployeeId(employeeId);

        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + employeeId);
        }

        LOG.debug("Creating reporting structure for employee with id [{}]", employeeId);
        ReportingStructure reportingStructure = new ReportingStructure(employee);
        reportingStructure.setNumberOfReports(countReports(employeeId));

        LOG.debug("Reporting structure for employee with id [{}] has {} report(s)", employeeId, reportingStructure.getNumberOfReports());

        return reportingStructure;
    }

    /**
     * Counts number of reports for a given employee using a breadth-first search and no recursing.
     * Keeps track of already viewed employees to prevent loops.
     * @param e Employee
     * @return Count of reports
     */
    private int countReports(String eId) {
        LinkedList<String> visited = new LinkedList<String>();
        LinkedList<String> queue = new LinkedList<String>();
        List<Employee> directReports;
        Employee e;
 
        visited.add(eId);                 
        queue.add(eId);
 
        while (queue.size() != 0)
        {
            eId = queue.poll(); // Pop employee from queue
            e = employeeRepository.findByEmployeeId(eId); // Find employee by ID to get all info about employee
            if (e == null) {
                throw new RuntimeException("Invalid employeeId: " + eId);
            }
            directReports = e.getDirectReports();
            if (directReports == null || directReports.isEmpty())
                continue;
 
            // Iterate through direct reports and into queue if not yet visited
            for (Employee dReport : directReports)
            {
                // Only insert into queue if not yet visited
                if (!visited.contains(dReport.getEmployeeId()))
                {
                    visited.add(dReport.getEmployeeId());
                    queue.add(dReport.getEmployeeId());
                }
            }  
        }
        // First employee is in visited, so reduce size by 1
        return visited.size() - 1;
    }
}
