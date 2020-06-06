package com.example.finager;

public class Request {
    private String group_id;
    private String invited_by;
    private String name;
    private int answer;

    public Request() {
    }

    public Request(String group_id, String invited_by, String name, int answer) {
        this.group_id = group_id;
        this.invited_by = invited_by;
        this.name = name;
        this.answer = answer;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public int getAnswer() {
        return answer;
    }

    public void setAnswer(int answer) {
        this.answer = answer;
    }

    public String getInvited_by() {
        return invited_by;
    }

    public void setInvited_by(String invited_by) {
        this.invited_by = invited_by;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
