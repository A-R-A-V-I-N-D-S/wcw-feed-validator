package com.manualtasks.wcwfeedvalidator.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.manualtasks.wcwfeedvalidator.components.EmailSenderService;
import com.manualtasks.wcwfeedvalidator.components.WcwCertFeedValidationTasklet;

@Controller
public class ApplicationController {

	@Autowired
	private WcwCertFeedValidationTasklet validationTasklet;

	@Autowired
	private EmailSenderService emailSenderService;

	public void executeTheTask() throws SftpException, IOException, JSchException {
		if (!validationTasklet.validateWcwFeed()) {
			emailSenderService.sendFailureStatusEmail();
		} else {
			emailSenderService.sendSuccessStatusEmail();
		}
	}

}
