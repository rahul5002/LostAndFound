package com.lostandfound.model;

import java.io.Serializable;
import java.util.Objects;

public class Person implements Serializable {
    private static final long serialVersionUID = 1L;

    private String personId;
    private String name;
    private String contactNumber;
    private String email;
    private String address;

    public Person() {}

    public Person(String personId, String name, String contactNumber, String email, String address) {
        this.personId = personId;
        this.name = name;
        this.contactNumber = contactNumber;
        this.email = email;
        this.address = address;
    }

    // Getters and Setters
    public String getPersonId() { return personId; }
    public void setPersonId(String personId) { this.personId = personId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    @Override
    public String toString() {
        return String.format("Person{id='%s', name='%s', contact='%s', email='%s'}",
                personId, name, contactNumber, email);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person)) return false;
        Person person = (Person) o;
        return Objects.equals(personId, person.personId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(personId);
    }
}
