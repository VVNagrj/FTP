package com.fileupload.ws.controller;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fileupload.ws.entity.FtpsrvrEntity;
import com.fileupload.ws.entity.FtpsrvrResponse;
import com.fileupload.ws.res.FtpRes;
import com.fileupload.ws.service.FtpsrvrService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(value = "/srv")
public class FtpsrvrController {
	
	private static final Logger LOG = Logger.getLogger(FtpsrvrController.class.getName());

	@Autowired
	private FtpsrvrService sftpsrvrService;
	
	@Autowired
    private HttpServletRequest request;
	
	
	public FtpsrvrEntity connector(String host, int port, String username, String password,String folderpath) {
		FtpsrvrEntity ftp = new FtpsrvrEntity();
		try {

			ftp.setHost(host);
			ftp.setPassword(password);
			ftp.setPort(port);
			ftp.setUsername(username);
			ftp.setFolderpath(folderpath);

		} catch (Exception ex) {
			LOG.info("Error: " + ex.getMessage());
			ex.printStackTrace();
			ftp = null;
		}
		return ftp;
	}

	// This method to upload the file given by the user
	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	@ApiOperation(value = "Upload")
	@ApiResponses(value = { @ApiResponse(code = 500, message = "Server error"),
			@ApiResponse(code = 200, message = "Upload") })
	public ResponseEntity<FtpRes> upload(@RequestPart("file") MultipartFile fileDetail,
										 @RequestParam  String host,
										 @RequestParam  int port,
										 @RequestParam  String username,
										 @RequestParam  String password,
										 @RequestParam  String folderpath) {
		
		FtpsrvrEntity ftp =connector(host, port, username, password,folderpath);
		FtpRes res = sftpsrvrService.uploadFile(fileDetail,request,ftp);
		
		if (res.getStatus().equals("Success")) {
			return new ResponseEntity<FtpRes>(res, HttpStatus.OK);
		} else {
			res.setStatus("Failed");
			return new ResponseEntity<FtpRes>(res, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	// This method used to upload the file given by the user
		@RequestMapping(value = "/delete", method = RequestMethod.POST)
		@ApiOperation(value = "delete")
		@ApiResponses(value = { @ApiResponse(code = 500, message = "Server error"),
				@ApiResponse(code = 200, message = "delete") })
		public ResponseEntity<String> delete(@RequestBody FtpsrvrEntity dow) {

			String res = sftpsrvrService.deleteFile(dow);

			if (res.equals("Success")) {
				return ResponseEntity.status(HttpStatus.OK).body("File Deleted Sucessfully");
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Internal Server Error");
			}
		}
		//Download Document
		@RequestMapping(value = "/download", method = RequestMethod.POST)
		@ApiOperation(value = "download")
		@ApiResponses(value = { @ApiResponse(code = 500, message = "Server error"),
				@ApiResponse(code = 200, message = "download") })
		public ResponseEntity<Object> download(@RequestBody FtpsrvrEntity dow) {

			try {
				
		    	//byte[] data= Files.readAllBytes(Paths.get(dow.getLocalpath()));

				byte[] data= sftpsrvrService.downloadFileFromFtpServer(dow); 
				
		        HttpHeaders headers = new HttpHeaders();
		        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		        headers.add("Content-Disposition", "attachment;filename=" + FilenameUtils.getBaseName(dow.getFilename()) + '.' + FilenameUtils.getExtension(dow.getFilename()));

		        if (data != null)
		            return new ResponseEntity<>(new InputStreamResource(new ByteArrayInputStream(data)),headers, HttpStatus.OK);
		        else
		            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);


			} catch (IOException e) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
			}
		}	
}
