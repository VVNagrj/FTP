package com.fileupload.ws.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fileupload.ws.entity.FtpsrvrEntity;
import com.fileupload.ws.res.FtpRes;

@Service
public class FtpsrvrService {

	private static final Logger LOG = Logger.getLogger(FtpsrvrService.class.getName());

	public FtpRes uploadFile(MultipartFile fileDetail, HttpServletRequest request, FtpsrvrEntity ent) {

		FtpRes response = new FtpRes();
		FTPClient ftpClient = new FTPClient();

		try {
			ftpClient.connect(ent.getHost(), ent.getPort());
			ftpClient.login(ent.getUsername(), ent.getPassword());
			ftpClient.enterLocalPassiveMode();
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

			String filename = fileDetail.getOriginalFilename();

			String secondRemoteFile = ent.getFolderpath() + "/" + fileDetail.getOriginalFilename();
			InputStream inputStream = fileDetail.getInputStream();

			LOG.info("Start uploading file");
			OutputStream outputStream = ftpClient.storeFileStream(secondRemoteFile);
			byte[] bytesIn = new byte[4096];
			int read = 0;

			while ((read = inputStream.read(bytesIn)) != -1) {
				outputStream.write(bytesIn, 0, read);
			}
			
			inputStream.close();
			outputStream.close();
			

			boolean completed = ftpClient.completePendingCommand();
			if (completed) {
				LOG.info("File is uploaded successfully.");
			}
			            			
			response.setFilename(filename);
			response.setFolderpath(ent.getFolderpath() + "/" + fileDetail.getOriginalFilename());
			response.setDescription("File is uploaded successfully");
			response.setDate(new Date());
			response.setStatus("Success");
			
		} catch (IOException ex) {
			LOG.info("Error: " + ex.getMessage());
			ex.printStackTrace();
			response = null;
		} finally {
			try {
				if (ftpClient.isConnected()) {
					ftpClient.logout();
					ftpClient.disconnect();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		return response;
	}

	public String deleteFile(FtpsrvrEntity req) {
		
		String res=" ";
		
		FTPClient ftpClient = new FTPClient();
		try {

			ftpClient.connect(req.getHost(), req.getPort());

			int replyCode = ftpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(replyCode)) {
				LOG.info("Connect failed");
			}

			boolean success = ftpClient.login(req.getUsername(), req.getPassword());

			if (!success) {
				LOG.info("Could not login to the server");
			}
			String fileToDelete = req.getFolderpath() + "/" + req.getFilename();

			boolean deleted = ftpClient.deleteFile(fileToDelete);
			if (deleted) {
				LOG.info("The file was deleted successfully.");
			} else {
				LOG.info("Could not delete the  file, it may not exist.");
			}
				
			res = "Success";
		} catch (IOException ex) {
			LOG.info("Oh no, there was an error: " + ex.getMessage());
			ex.printStackTrace();
			res = "Failed";
		} finally {
			// logs out and disconnects from server
			try {
				if (ftpClient.isConnected()) {
					ftpClient.logout();
					ftpClient.disconnect();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}		
		return res;
	}

	public byte[] downloadFileFromFtpServer(FtpsrvrEntity ent) throws IOException {
        FTPClient ftpClient = new FTPClient();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        try {
        	ftpClient.connect(ent.getHost(), ent.getPort());
            ftpClient.login(ent.getUsername(), ent.getPassword());
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                        
            boolean success = ftpClient.retrieveFile(ent.getFolderpath(), outputStream);
            if (success) {
                System.out.println("File downloaded successfully.");
                return outputStream.toByteArray();
            } else {
                System.err.println("File download failed.");
                return null; // or throw an exception
            }
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
