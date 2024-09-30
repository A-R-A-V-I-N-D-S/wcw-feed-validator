package com.manualtasks.wcwfeedvalidator.controller;

import java.io.IOException;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.manualtasks.wcwfeedvalidator.components.EmailSenderService;
import com.manualtasks.wcwfeedvalidator.components.FeedValidatorService;

@RestController
public class ApplicationController {

	@Autowired
	private FeedValidatorService validationService;

	@Autowired
	private EmailSenderService emailSenderService;

	private static Logger logger = LoggerFactory.getLogger(ApplicationController.class);

	public void executeTheTask() throws SftpException, IOException, JSchException {
		if (!validationService.validateWcwFeed()) {
			emailSenderService.sendFailureStatusEmail();
		} else {
			emailSenderService.sendSuccessStatusEmail();
		}
	}

	@GetMapping(path = "/validateWcwFeed", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> validateWcwFeed() {
		try {
			boolean validationStatus = validationService.validateWcwFeed();
			if (validationStatus) {
				logger.info("Success response is sent!");
				return new ResponseEntity<>(new HashMap<>().put("status", "success"), HttpStatus.OK);
			} else {
				logger.info("Failure response is sent!");
				return new ResponseEntity<>(new HashMap<>().put("status", "failure"), HttpStatus.OK);
			}
		} catch (SftpException | IOException | JSchException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<>(new HashMap<>().put("status", "error"), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping(path = "/sendSuccessEmail", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> sendSuccessEmail() {
		try {
			emailSenderService.sendSuccessStatusEmail();
			logger.info("Email has been sent successfully");
			return new ResponseEntity<>(new HashMap<>().put("status", "success"), HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			if (e.getMessage().contains("timeout"))
				return new ResponseEntity<>(new HashMap<>().put("status", "timeout"), HttpStatus.REQUEST_TIMEOUT);
			else
				return new ResponseEntity<>(new HashMap<>().put("status", "error"), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
