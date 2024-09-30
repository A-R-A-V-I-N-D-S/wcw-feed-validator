package com.manualtasks.wcwfeedvalidator.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@Configuration
public class ApplicationConfig {

	private static Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);

	@Bean
	@DependsOn
	public ChannelSftp connectSftp(String batchServer, String userName, String password) throws JSchException {
		Session session = new JSch().getSession(userName, batchServer, 22);
		session.setPassword(password);
		session.setConfig("StrictHostKeyChecking", "no");
		session.connect();
		ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
		sftpChannel.connect();
		if (sftpChannel.isConnected()) {
			logger.info("Connected to the host - {}", batchServer);
		}
		return sftpChannel;
	}

	@Bean
	public void disconnectSftp(ChannelSftp sftpChannel) throws JSchException {
		Session session = sftpChannel.getSession();
		if (sftpChannel != null && sftpChannel.isConnected()) {
			sftpChannel.disconnect();
		}
		if (session != null && session.isConnected()) {
			session.disconnect();
		}
		if (!session.isConnected()) {
			logger.info("Disconnected from the host - {}", session.getHost());
		}
	}

	@Bean
	@Primary
	public FreeMarkerConfigurationFactoryBean factoryBean() {
		FreeMarkerConfigurationFactoryBean theFactoryBean = new FreeMarkerConfigurationFactoryBean();
		theFactoryBean.setTemplateLoaderPath("classpath:templates/");
		return theFactoryBean;
	}

	@Bean
	public JavaMailSender javaMailSender() {
		return new JavaMailSenderImpl();
	}

//	@Bean
//	public freemarker.template.Configuration configuration() {
//
//	}

}
