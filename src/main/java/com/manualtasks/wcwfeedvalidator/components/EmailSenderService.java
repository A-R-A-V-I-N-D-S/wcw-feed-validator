package com.manualtasks.wcwfeedvalidator.components;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import com.manualtasks.wcwfeedvalidator.utils.ReqRespBody;

import freemarker.template.Configuration;
import freemarker.template.Template;

@Service
public class EmailSenderService {

	@Autowired
	private Configuration configuration;

	@Autowired
	private JavaMailSender emailSender;

	@Value("${mail.smtp.from}")
	private String mailFrom;

	@Value("${mail.smtp.to}")
	private String[] mailTo;

	@Value("${mail.smtp.cc}")
	private String mailCc;

	@Value("${mail.smtp.attachment.javacode}")
	private String attachmentFilePath;

	private static Logger logger = LoggerFactory.getLogger(EmailSenderService.class);

	private String currentDate = new SimpleDateFormat("MM/dd").format(new Date());

	public String sendSuccessStatusEmail(List<String> filesList) {
		logger.info("Initiating the process to send the status email");
		MimeMessage emailMessage = emailSender.createMimeMessage();
		try {
			Map<String, Object> model = new HashMap<>();
			model.put("text", "ready to process");
			model.put("isFaulty", "is valid");
			model.put("reason", "");
			model.put("date", currentDate);
			model.put("actionDone", "has been moved");
			model.put("files", filesList);
			Template template = configuration.getTemplate("validation-status-email.ftl");
			String processedText = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
			MimeMessageHelper helper = new MimeMessageHelper(emailMessage, MimeMessageHelper.MULTIPART_MODE_NO,
					StandardCharsets.UTF_8.name());
			helper.setFrom(mailFrom);
			helper.setTo(mailTo);
			helper.setCc(mailCc);
			helper.setSubject("WCW Cert file status " + currentDate + " - READY TO PROCESS");
			helper.setText(processedText, true);

			emailSender.send(emailMessage);

			logger.info("Success status email sent successfully");

			return "success";

		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			if (e.getMessage().contains("timeout")) {
				return "timeout";
			}
			return "error";
		}
	}

	public String sendFailureStatusEmail(ReqRespBody status) {
		logger.info("Initiating the process to send the status email");
		MimeMessage emailMessage = emailSender.createMimeMessage();
		try {
			Map<String, Object> model = new HashMap<>();
			model.put("text", "not ready to process");
			model.put("isFaulty", "is not valid");
			if (status.getEmailResp().contains("delimiters"))
				model.put("reason", " due to extra delimiters");
			else if (status.getEmailResp().contains("module"))
				model.put("reason", " due to unusual module names");
			else
				model.put("reason", " due to quotes");
			model.put("date", currentDate);
			model.put("actionDone", "has not been moved");
			Template template = configuration.getTemplate("validation-status-email.ftl");
			String processedText = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
			MimeMessageHelper helper = new MimeMessageHelper(emailMessage, MimeMessageHelper.MULTIPART_MODE_NO,
					StandardCharsets.UTF_8.name());
			helper.setFrom(mailFrom);
			helper.setTo(mailTo);
			helper.setCc(mailCc);
			helper.setSubject("WCW Cert file status " + currentDate + " - FAULTY");
			helper.setText(processedText, true);

			emailSender.send(emailMessage);

			logger.info("Failure status email sent successfully");

			return "success";

		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			if (e.getMessage().contains("timeout")) {
				return "timeout";
			}
			return "error";
		}
	}

	public String sendInternalErrorEmail() {
		logger.info("Initiating the process to send the status email");
		MimeMessage emailMessage = emailSender.createMimeMessage();
		try {
//			Map<String, Object> model = new HashMap<>();
			Template template = configuration.getTemplate("internal-error-email.ftl");
			String processedText = FreeMarkerTemplateUtils.processTemplateIntoString(template, null);
			MimeMessageHelper helper = new MimeMessageHelper(emailMessage, MimeMessageHelper.MULTIPART_MODE_NO,
					StandardCharsets.UTF_8.name());
			helper.setFrom(mailFrom);
			helper.setTo(mailTo);
			helper.setCc(mailCc);
			helper.setSubject("500 Internal error - WCW Cert file validation status - " + currentDate);
			helper.setText(processedText, true);

			emailSender.send(emailMessage);

			logger.info("Internal server error email sent successfully");

			return "success";

		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			if (e.getMessage().contains("timeout")) {
				return "timeout";
			}
			return "error";
		}
	}

	public String sendFileNotFoundEmail() {
		logger.info("Initiating the process to send the status email");
		MimeMessage emailMessage = emailSender.createMimeMessage();
		try {
			Template template = configuration.getTemplate("file-not-found-email.ftl");
			MimeMessageHelper helper = new MimeMessageHelper(emailMessage, true, StandardCharsets.UTF_8.name());
			String processedText = FreeMarkerTemplateUtils.processTemplateIntoString(template, null);
			helper.setFrom(mailFrom);
			helper.setTo(mailTo);
			helper.setCc(mailCc);
			helper.setText(processedText, true);
			helper.setSubject("WCW Cert file status " + currentDate + " - FILE_NOT_FOUND");

			FileSystemResource attachmentFile = new FileSystemResource(new File(attachmentFilePath));
			helper.addAttachment("WCWCertFileValidation.java", attachmentFile);

			emailSender.send(emailMessage);

			logger.info("File not found email sent successfully");

			return "success";
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			if (e.getMessage().contains("timeout")) {
				return "timeout";
			}
			return "error";
		}
	}

}
