package com.manualtasks.wcwfeedvalidator.utils;

public class ReqRespBody {

	private String status;
	private String emailResp;

	public String getEmailResp() {
		return emailResp;
	}

	public void setEmailResp(String emailResp) {
		this.emailResp = emailResp;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public ReqRespBody() {

	}

	public ReqRespBody(String status, String emailRespType) {
		this.status = status;
		this.emailResp = emailRespType;
	}

}
