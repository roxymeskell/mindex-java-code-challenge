package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.CompensationRepository;
import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.service.CompensationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class CompensationServiceImpl implements CompensationService {
    private static final Logger LOG = LoggerFactory.getLogger(CompensationServiceImpl.class);
    
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private CompensationRepository compensationRepository;

    @Override
    public Compensation create(Compensation compensation) {

        // Checking if for valid employee
        // if (!employeeRepository.existsById(compensation.getEmployee().getEmployeeId())) {
        if (employeeRepository.findByEmployeeId(compensation.getEmployee().getEmployeeId()) == null) {
            throw new RuntimeException("Invalid employee: " + compensation.getEmployee().getEmployeeId());
        }

        compensation.setId(UUID.randomUUID().toString());

        compensationRepository.insert(compensation);

        return compensation;
    }

    @Override
    public Compensation read(String employeeId) {
        LOG.debug("Looking for compensation for employee [{}]", employeeId);
        Compensation compensation = compensationRepository.findByEmployeeId(employeeId);

        if (compensation == null) {
            Employee employee = employeeRepository.findByEmployeeId(employeeId);
            if (employee == null) {
                throw new RuntimeException("Invalid employeeId: " + employeeId);
            } else {
                throw new RuntimeException("No compensation for employee id: " + employeeId);
            }
        }

        return compensation;
    }
}
