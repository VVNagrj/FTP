package com.fileupload.ws.res;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FtpRes {
	
	@JsonProperty("Description")
	private String description;
	@JsonProperty("Date")
	private Date date;
	@JsonProperty("FolderPath")
	private String folderpath;
	@JsonProperty("FileName")
	private String filename;
	@JsonProperty("Status")
	private String status;

}
