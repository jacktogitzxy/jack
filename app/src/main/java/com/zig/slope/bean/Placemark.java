package com.zig.slope.bean;

import java.io.Serializable;

/**
 * Created by 17120 on 2018/7/2.
 */

public class Placemark implements Serializable {
    private String name;//编号
    private double [] latLng;//经纬度
    private String newname;//note
    private String citynum;//全市统一编号
    private String street ;//街道
    private String community ;//社区
    private String company ;//牵头单位
    private String dangername;//隐患点名称
    private String address;//隐患点最新交通位置
    private String type;//隐患类型（市、省厅文件）
    private String x;//x
    private String y ;//y
    private String features;//土质
    private String longs;//坡长
    private String height ;//坡高
    private String slope;//坡度
    private String stability;//预测稳定性
    private String object;//威胁对象
    private String number ;//涉险数量
    private String area ;//涉险面积（m2）
    private String peoples;//威胁人数
    private String loss ;//潜在经济损失（万）
    private String grade ;//隐患等级
    private String danger ;//预测危险性
    private String work;//防灾责任单位（社区工作站）
    private String contacts  ;//联系人
    private String tel ;//电话
    private String precautions  ;//采取预防措施
    private String process;//治理进度
    private String isdoing;//落实管理维护
    private String year;//纳入年度防治方案治理计划
    private String management   ;//管理维护单位
    private String doself;//自行治理
    private String note;//备注

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public double[] getLatLng() {
        return latLng;
    }

    public void setLatLng(double[] latLng) {
        this.latLng = latLng;
    }

    public String getNewname() {
        return newname;
    }

    public void setNewname(String newname) {
        this.newname = newname;
    }

    public String getCitynum() {
        return citynum;
    }

    public void setCitynum(String citynum) {
        this.citynum = citynum;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDangername() {
        return dangername;
    }

    public void setDangername(String dangername) {
        this.dangername = dangername;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

    public String getFeatures() {
        return features;
    }

    public void setFeatures(String features) {
        this.features = features;
    }

    public String getLongs() {
        return longs;
    }

    public void setLongs(String longs) {
        this.longs = longs;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getSlope() {
        return slope;
    }

    public void setSlope(String slope) {
        this.slope = slope;
    }

    public String getStability() {
        return stability;
    }

    public void setStability(String stability) {
        this.stability = stability;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getPeoples() {
        return peoples;
    }

    public void setPeoples(String peoples) {
        this.peoples = peoples;
    }

    public String getLoss() {
        return loss;
    }

    public void setLoss(String loss) {
        this.loss = loss;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getDanger() {
        return danger;
    }

    public void setDanger(String danger) {
        this.danger = danger;
    }

    public String getWork() {
        return work;
    }

    public void setWork(String work) {
        this.work = work;
    }

    public String getContacts() {
        return contacts;
    }

    public void setContacts(String contacts) {
        this.contacts = contacts;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getPrecautions() {
        return precautions;
    }

    public void setPrecautions(String precautions) {
        this.precautions = precautions;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public String getIsdoing() {
        return isdoing;
    }

    public void setIsdoing(String isdoing) {
        this.isdoing = isdoing;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getManagement() {
        return management;
    }

    public void setManagement(String management) {
        this.management = management;
    }

    public String getDoself() {
        return doself;
    }

    public void setDoself(String doself) {
        this.doself = doself;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Placemark(String name, double[] latLng, String newname, String citynum, String street, String community, String company, String dangername, String address, String type, String x, String y, String features, String longs, String height, String slope, String stability, String object, String number, String area, String peoples, String loss, String grade, String danger, String work, String contacts, String tel, String precautions, String process, String isdoing, String year, String management, String doself, String note) {
        this.name = name;
        this.latLng = latLng;
        this.newname = newname;
        this.citynum = citynum;
        this.street = street;
        this.community = community;
        this.company = company;
        this.dangername = dangername;
        this.address = address;
        this.type = type;
        this.x = x;
        this.y = y;
        this.features = features;
        this.longs = longs;
        this.height = height;
        this.slope = slope;
        this.stability = stability;
        this.object = object;
        this.number = number;
        this.area = area;
        this.peoples = peoples;
        this.loss = loss;
        this.grade = grade;
        this.danger = danger;
        this.work = work;
        this.contacts = contacts;
        this.tel = tel;
        this.precautions = precautions;
        this.process = process;
        this.isdoing = isdoing;
        this.year = year;
        this.management = management;
        this.doself = doself;
        this.note = note;
    }
}
