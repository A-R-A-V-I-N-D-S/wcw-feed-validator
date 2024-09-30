package com.manualtasks.wcwfeedvalidator.components;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.manualtasks.wcwfeedvalidator.config.ApplicationConfig;
import static com.manualtasks.wcwfeedvalidator.utils.ClassDataUtils.MODULE_NAMES_LIST;

@Component
public class WcwCertFeedValidationTasklet {

	@Value("${batch.server.username}")
	private String username;

	@Value("${batch.server.password}")
	private String password;

	@Value("${batch.server.name}")
	private String batchServer;

	private String filePath = "/usr/app/blcs/BrokerOversight/";

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private ApplicationConfig applicationConfig;

	private static Logger logger = LoggerFactory.getLogger(WcwCertFeedValidationTasklet.class);

	public boolean validateWcwFeed() throws SftpException, IOException, JSchException {

		logger.info("Validating WCW Cert file - START");

		ChannelSftp sftpChannel = applicationContext.getBean(ChannelSftp.class, batchServer, username, password);
//		ChannelSftp sftpChannel = applicationConfig.connectSftp(batchServer, username, password);
		Vector<LsEntry> listOfFiles = sftpChannel.ls(filePath);
		String wcwFileName = "";
		for (LsEntry fileName : listOfFiles) {
			if (fileName.toString().contains("SBO_WCW_Certification_")) {
				wcwFileName = fileName.toString();
				break;
			}
		}

		InputStream inputStream = sftpChannel.get(filePath + wcwFileName);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String line;
		List<List<String>> wcwFeedRecordArray = new ArrayList<>();
		while ((line = reader.readLine()) != null) {
			wcwFeedRecordArray.add(Arrays.asList(line.split("\\|")));
		}

		applicationConfig.disconnectSftp(sftpChannel);

		logger.info("Validating WCW Cert feed - END");

		for (List<String> wcwFeedRecord : wcwFeedRecordArray) {
			if (wcwFeedRecord.size() > 19) {
				return false;
			}
			if (!MODULE_NAMES_LIST.contains(wcwFeedRecord.get(12))) {
				return false;
			}
		}
		return true;
	}

}
