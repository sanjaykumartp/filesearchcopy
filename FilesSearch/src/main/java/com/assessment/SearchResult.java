package com.assessment;

import java.util.Date;

public class SearchResult {
	private String fileName;
	private String name;
	private String email;
	private String mobileNumber;
	private String Search_criteria;
	private static int counter = 0;
	private int sl_no;
	private String resumeCreatedDate;
	private String resumeModifiedDate;
	public SearchResult(String fileName, String name, String email, String mobileNumber, String Search_criteria, String resumeCreatedDate,String resumeModifiedDate ) {
		this.fileName = fileName;
		this.name = name;
		this.email = email;
		this.mobileNumber = mobileNumber;
		this.Search_criteria = Search_criteria;
		this.resumeCreatedDate=resumeCreatedDate;
		this.sl_no = ++counter;
		this.resumeModifiedDate=resumeModifiedDate;
	}

	public SearchResult() {
		super();
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
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

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getSearch_criteria() {
		return Search_criteria;
	}

	public void setSearch_criteria(String search_criteria) {
		Search_criteria = search_criteria;
	}

	public static int getCounter() {
		return counter;
	}

	public static void setCounter(int counter) {
		SearchResult.counter = counter;
	}

	public int getSl_no() {
		return sl_no;
	}

	public void setSl_no(int sl_no) {
		this.sl_no = sl_no;
	}

	public String getResumeCreatedDate() {
		return resumeCreatedDate;
	}

	public void setResumeCreatedDate(String resumeCreatedDate) {
		this.resumeCreatedDate = resumeCreatedDate;
	}

	public String getResumeModifiedDate() {
		return resumeModifiedDate;
	}

	public void setResumeModifiedDate(String resumeModifiedDate) {
		this.resumeModifiedDate = resumeModifiedDate;
	}




}

