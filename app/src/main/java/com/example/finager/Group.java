package com.example.finager;

public class Group {
    String name;
    String description;
    String members;
    String admin;

    public Group() {
    }

    public Group(String name, String description, String admin, String members) {
        this.name = name;
        this.description = description;
        this.admin = admin;
        this.members = members;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMembers() {
        return members;
    }

    public void setMembers(String members) {
        this.members = members;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }
}
