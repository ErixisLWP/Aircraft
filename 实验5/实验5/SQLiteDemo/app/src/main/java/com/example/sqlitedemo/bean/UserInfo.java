package com.example.sqlitedemo.bean;

public class UserInfo {
	public int userid;
	public String name;
	public int age;
	public boolean sex;
	public long credits;
	public float totals;
	public String update_time;
	public String phone;
	public String password;
	
	public UserInfo() {
		userid = 0;
		name = "";
		age = 0;
		sex=true;
		credits = 0l;
		totals = 0.0f;
		update_time = "";
		phone = "";
		password = "";
	}
}
