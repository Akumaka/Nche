package com.e.nche.MyUserPanel;

public class MyComplainModel {
    private String key, code, unique, model, name, email, phone, remarks, attachment, TimeStamp;

    public MyComplainModel() {
    }

    public MyComplainModel(String key, String code, String unique, String model, String name, String email, String phone, String remarks, String attachment, String TimeStamp) {
        this.key = key;
        this.code = code;
        this.unique = unique;
        this.model = model;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.remarks = remarks;
        this.attachment = attachment;
        this.TimeStamp = TimeStamp;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUnique() {
        return unique;
    }

    public void setUnique(String unique) {
        this.unique = unique;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public String getTimeStamp() {
        return TimeStamp;
    }

    public void setTimeStamp(String TimeStamp) {
        this.TimeStamp = TimeStamp;
    }
}
