package com.mindex.challenge.data;

import java.util.List;
import java.util.LinkedList;

public class ReportingStructure {
    private Employee employee;
    private int numberOfReports;

    public ReportingStructure() {
    }

    public ReportingStructure(Employee employee) {
        this.employee = employee;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public int getNumberOfReports() {
        return numberOfReports;
    }

    public void setNumberOfReports(int numberOfReports) {
        this.numberOfReports = numberOfReports;
    }

    // This is a depth-first search
    // Assuming that the reporting structure is a tree with no loops
    // Could maybe thread to make faster
    int getNumberOfReportsDFS() {
        List<Employee> directReports = employee.getDirectReports();

        // Check if any direct reports, return if not
        if (directReports == null || directReports.isEmpty())
            return 0;
        
        int numberOfReports = 0;
        for(Employee dReport : directReports) {
            numberOfReports += 1 + (new ReportingStructure() {{ setEmployee(dReport); }} ).getNumberOfReports();
        }
        return numberOfReports;
    }

    // Using a breadth-first search and no recursing
    // Stores visited nodes so that no employee is counted twice in the case of loops
    int getNumberOfReportsBFS()
    {
        Employee e = employee;
        LinkedList<Employee> visited = new LinkedList<Employee>();
        LinkedList<Employee> queue = new LinkedList<Employee>();
        List<Employee> directReports;
 
        visited.add(e);                 
        queue.add(e);
 
        while (queue.size() != 0)
        {
            e = queue.poll(); // Pop employee from queue
            directReports = e.getDirectReports();
            if (directReports == null || directReports.isEmpty())
                continue;
 
            // Iterate through direct reports and into queue if not yet visited
            for (Employee dReport : directReports)
            {
                // Only insert into queue if not yet visited
                if (!visited.contains(dReport))
                {
                    visited.add(dReport);
                    queue.add(dReport);
                }
            }  
        }
        // Employee is in visited, so reduce size by 1
        return visited.size() - 1;
    }
}
