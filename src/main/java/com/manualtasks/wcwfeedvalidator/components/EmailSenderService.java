package com.manualtasks.wcwfeedvalidator.components;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

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
	private String mailTo;

	private static Logger logger = LoggerFactory.getLogger(EmailSenderService.class);

	private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");

	public void sendSuccessStatusEmail() {
		MimeMessage emailMessage = emailSender.createMimeMessage();
		try {
			Template template = configuration.getTemplate("success-email.ftl");
			String processedText = FreeMarkerTemplateUtils.processTemplateIntoString(template, null);
			MimeMessageHelper helper = new MimeMessageHelper(emailMessage,
					MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
			helper.setFrom(mailFrom);
			helper.setTo(mailTo);
			helper.setSubject("TEST - WCW Cert file status - " + dateFormat.format(new Date()));
			helper.setText(processedText, true);

			emailSender.send(emailMessage);

			logger.info("Success status email sent successfully");

		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public void sendFailureStatusEmail() {
		MimeMessage emailMessage = emailSender.createMimeMessage();
		try {
			Map<String, Object> model = new HashMap<>();
			model.put("text", "not ready to process");
			Template template = configuration.getTemplate("failure-email.html");
			String processedText = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
			MimeMessageHelper helper = new MimeMessageHelper(emailMessage, MimeMessageHelper.MULTIPART_MODE_NO,
					StandardCharsets.UTF_8.name());
			helper.setFrom(mailFrom);
			helper.setTo(mailTo);
			helper.setSubject("TEST - WCW Cert file validation status - " + dateFormat.format(new Date()));
			helper.setText(processedText, true);

			emailSender.send(emailMessage);

			logger.info("Failure status email sent successfully");

		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

}
