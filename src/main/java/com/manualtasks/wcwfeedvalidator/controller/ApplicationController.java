package com.manualtasks.wcwfeedvalidator.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.manualtasks.wcwfeedvalidator.components.EmailSenderService;
import com.manualtasks.wcwfeedvalidator.components.FeedValidatorService;
import com.manualtasks.wcwfeedvalidator.utils.ReqRespBody;

@RestController
public class ApplicationController {

	@Autowired
	private FeedValidatorService validationService;

	@Autowired
	private EmailSenderService emailSenderService;
	
	private List<String> filesList = null;

	private static Logger logger = LoggerFactory.getLogger(ApplicationController.class);

	@GetMapping(path = "/validateWcwFeed", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> validateWcwFeed() {
		ReqRespBody reqRespBody = new ReqRespBody();
		HttpHeaders headers = new HttpHeaders();
		try {
			int validationStatus = validationService.validateWcwFeed();
			if (validationStatus == 0) {
				logger.info("Validation Success response is sent!");
				reqRespBody.setStatus("success");
				reqRespBody.setEmailResp("validation-success");
			} else if (validationStatus == 1) {
				logger.info("Validation Failure response is sent!");
				reqRespBody.setStatus("failure");
				reqRespBody.setEmailResp("validation-failure-delimiters");
			} else if (validationStatus == 2) {
				logger.info("Validation Failure response is sent!");
				reqRespBody.setStatus("failure");
				reqRespBody.setEmailResp("validation-failure-module-name");
			} else if (validationStatus == 4) {
				logger.info("Validation Failure response is sent!");
				reqRespBody.setStatus("failure");
				reqRespBody.setEmailResp("validation-failure-quotes");
			} else {
				logger.info("File not found response is sent!");
				reqRespBody.setStatus("file-not-found");
				reqRespBody.setEmailResp("file-not-found");
			}
		} catch (SftpException | IOException | JSchException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			reqRespBody.setStatus("error");
			reqRespBody.setEmailResp("internal-error");
			return new ResponseEntity<>(reqRespBody, headers, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(reqRespBody, headers, HttpStatus.OK);
	}

	@GetMapping(path = "/isFilePresentAtDestination", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> isFilePresentAtDestination() {
		Map<String, Object> response = new HashMap<>();
		try {
			List<String> filesList = validationService.isFileMoved();
			if (filesList != null) {
				response.put("timestamp", LocalDateTime.now().toString().replace('T', ' '));
				response.put("success", "The file(s) are present in the destination server.");
				response.put("files", filesList);
			} else {
				response.put("timestamp", LocalDateTime.now().toString().replace('T', ' '));
				response.put("failure", "The file(s) are not present in the destination server.");
			}
		} catch (JSchException | SftpException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			response.put("error", e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping(path = "/sendStatusEmail", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> sendStatusEmail(@RequestBody ReqRespBody reqStatus) {
		ReqRespBody respStatus = new ReqRespBody();
		HttpHeaders headers = new HttpHeaders();
		String emailSentStatus = "";
		if (reqStatus.getStatus().equals("success")) {
			emailSentStatus = emailSenderService.sendSuccessStatusEmail(filesList);
		} else if (reqStatus.getStatus().equals("failure")) {
			emailSentStatus = emailSenderService.sendFailureStatusEmail(reqStatus);
		} else if (reqStatus.getStatus().equals("file-not-found")) {
			emailSentStatus = emailSenderService.sendFileNotFoundEmail();
		}
		if (emailSentStatus.equals("timeout")) {
			respStatus.setStatus("failure");
			respStatus.setEmailResp("connection timeout");
			return new ResponseEntity<>(respStatus, headers, HttpStatus.REQUEST_TIMEOUT);
		} else if (emailSentStatus.equals("error")) {
			respStatus.setStatus("failure");
			respStatus.setEmailResp("internal server error");
			return new ResponseEntity<>(respStatus, headers, HttpStatus.INTERNAL_SERVER_ERROR);
		} else {
			respStatus.setStatus("email-sent");
			respStatus.setEmailResp("success");
			return new ResponseEntity<>(respStatus, headers, HttpStatus.OK);
		}
	}

}
