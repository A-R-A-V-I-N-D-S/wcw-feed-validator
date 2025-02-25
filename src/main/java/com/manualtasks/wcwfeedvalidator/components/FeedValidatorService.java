package com.manualtasks.wcwfeedvalidator.components;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.manualtasks.wcwfeedvalidator.config.ApplicationConfig;
import static com.manualtasks.wcwfeedvalidator.utils.ClassDataUtils.MODULE_NAMES_LIST;

@Service
@Lazy
public class FeedValidatorService {

	@Value("${batch.server.username}")
	private String username;

	@Value("${batch.server.password}")
	private String password;

	@Value("${batch.server.name}")
	private String batchServer;

	private String filePath = "/usr/app/blcs/BrokerOversight/";

	@Autowired
	private ApplicationConfig applicationConfig;

	private static Logger logger = LoggerFactory.getLogger(FeedValidatorService.class);

	public int validateWcwFeed() throws SftpException, IOException, JSchException {

		logger.info("Validating WCW Cert file in the server " + batchServer);

//		ChannelSftp sftpChannel = applicationContext.getBean(ChannelSftp.class, batchServer, username, password);
		ChannelSftp sftpChannel = applicationConfig.connectSftp(batchServer, username, password);
		Vector<LsEntry> listOfFiles = sftpChannel.ls(filePath);
		String wcwFileName = "";
		String regexPattern = "^SBO_WCW_Certification_.*";
		for (LsEntry fileName : listOfFiles) {
			String requiredFileName = fileName.toString().substring(56);
			if (Pattern.matches(regexPattern, requiredFileName)) {
				logger.info("File found is - {}", requiredFileName);
				wcwFileName = requiredFileName;
				break;
			}
		}

		if (wcwFileName == "") {
			applicationConfig.disconnectSftp(sftpChannel);
			return 3;
		}

		InputStream inputStream = sftpChannel.get(filePath + wcwFileName);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String line;
		List<List<String>> wcwFeedRecordArray = new ArrayList<>();
		int countOfQuotes;
		while ((line = reader.readLine()) != null) {
			countOfQuotes = StringUtils.countOccurrencesOf(line, "\"");
			if (countOfQuotes % 2 != 0)
				return 4;
			wcwFeedRecordArray.add(Arrays.asList(line.split("\\|")));
		}
		reader.close();
		inputStream.close();

		applicationConfig.disconnectSftp(sftpChannel);

		for (List<String> wcwFeedRecord : wcwFeedRecordArray) {
			if (wcwFeedRecord.size() > 19) {
				return 1;
			}
			if (wcwFeedRecord.size() >= 19 && !MODULE_NAMES_LIST.contains(wcwFeedRecord.get(12))) {
				return 2;
			}
		}

		return 0;
	}

	public List<String> isFileMoved() throws JSchException, SftpException {
		ChannelSftp sftpChannel = applicationConfig.connectSftp("dc04plvbuc300", username, password);
		Vector<LsEntry> listOfFiles = sftpChannel.ls("/apps/ftp/sbofeed");
		List<String> files = new ArrayList<>();
		logger.info("Checking the presence of the file in the destination server.");
		for (LsEntry entry : listOfFiles) {
			if (!(entry.toString().substring(60).length() < 3 && entry.toString().substring(60).equals("outbound")
					&& entry.toString().substring(60).equals("backup"))) {
				files.add(entry.toString().substring(60));
			}
		}
		applicationConfig.disconnectSftp(sftpChannel);
		if (files.size() > 0) {
			return files;
		}
		return null;
	}

}
