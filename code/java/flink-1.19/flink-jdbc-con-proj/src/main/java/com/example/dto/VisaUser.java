package com.example.dto;

public class VisaUser {
    private int id;
    private String name;
    private String country;
    private String visaType;


    public VisaUser() {
    }
    public VisaUser(int id, String name, String country, String visaType) {
        this.id = id;
        this.name = name;
        this.country = country;
        this.visaType = visaType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getVisaType() {
        return visaType;
    }

    public void setVisaType(String visaType) {
        this.visaType = visaType;

    }

    @Override
    public String toString() {
        return "VisaUser{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", country='" + country + '\'' +
                ", visaType='" + visaType + '\'' +
                '}';
    }
}
