package com.example.dto;

public class UserPlain {
    public int id;
    public String name;

    public UserPlain() {} // Default constructor for Flink serialization

    public UserPlain(int id, String name) {
        this.id = id;
        this.name = name;
    }
    public int getId() { return id; }
    public String getName() { return name; }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

}