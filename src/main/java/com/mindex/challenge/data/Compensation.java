package com.mindex.challenge.data;

import java.util.Date;

public class Compensation {
    private String id; // Adding unique id to compensation
    private Employee employee;
    private float salary;
    private Date effectiveDate;

    public Compensation() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public float getSalary() {
        return salary;
    }

    public void setSalary(float salary) {
        this.salary = salary;
    }
    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }
}
