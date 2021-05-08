package com.e.nche.SecurityAgentsModel;

public class ComplainModel {
    private String code, unique, model, name, email, phone, remarks, attachment;

    public ComplainModel() {
    }

    public ComplainModel(String code, String model, String name, String email, String phone, String remarks, String attachment) {
        this.code = code;
        this.model = model;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.remarks = remarks;
        this.attachment = attachment;
    }

    public ComplainModel(String code, String unique, String model, String name, String email, String phone, String remarks, String attachment) {
        this.code = code;
        this.unique = unique;
        this.model = model;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.remarks = remarks;
        this.attachment = attachment;
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
}
