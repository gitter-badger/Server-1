/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.jee.servlet.validator;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.ohmage.request.CampaignCreationAwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.util.CookieUtils;
import org.ohmage.util.StringUtils;


/**
 * Validator for HTTP requests to create a new campaign to the database.
 * 
 * @author John Jenkins
 */
public class CampaignCreationValidator extends AbstractHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(CampaignCreationValidator.class);
	
	private DiskFileItemFactory _diskFileItemFactory;
	private int _fileSizeMax;
	
	/**
	 * Basic constructor that sets up the list of required parameters.
	 */
	public CampaignCreationValidator(DiskFileItemFactory diskFileItemFactory, int fileSizeMax) {
		if(diskFileItemFactory == null) {
			throw new IllegalArgumentException("a DiskFileItemFactory is required");
		}
		_diskFileItemFactory = diskFileItemFactory;
		_fileSizeMax = fileSizeMax;
	}

	/**
	 * Ensures that all the required parameters exist and that each parameter
	 * is of a sane length.
	 */
	@Override
	public boolean validate(HttpServletRequest httpRequest) throws MissingAuthTokenException {
		
		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(_diskFileItemFactory);
		upload.setHeaderEncoding("UTF-8");
		upload.setFileSizeMax(_fileSizeMax);
		
		// Parse the request
		List<?> uploadedItems = null;
		try {
		
			uploadedItems = upload.parseRequest(httpRequest);
		
		} catch(FileUploadException fue) { 			
			_logger.error("Caught exception while uploading XML to create a new campaign.", fue);
			return false;
		}
		
		// Get the number of items were in the request.
		int numberOfUploadedItems = uploadedItems.size();
		
		// Get the authentication / session token from the header.
		String token;
		List<String> tokens = CookieUtils.getCookieValue(httpRequest.getCookies(), InputKeys.AUTH_TOKEN);
		if(tokens.size() == 0) {
			throw new MissingAuthTokenException("The required authentication / session token is missing.");
		}
		else if(tokens.size() > 1) {
			throw new MissingAuthTokenException("More than one authentication / session token was found in the request.");
		}
		else {
			token = tokens.get(0);
		}
		
		// Parse the request for each of the parameters.
		String runningState = null;
		String privacyState = null;
		String xml = null;
		String classes = null;
		String description = null;
		for(int i = 0; i < numberOfUploadedItems; i++) {
			FileItem fi = (FileItem) uploadedItems.get(i);
			if(fi.isFormField()) {
				String name = fi.getFieldName();
				String value = StringUtils.urlDecode(fi.getString());
				
				if(InputKeys.DESCRIPTION.equals(name)) {
					if(greaterThanLength("description", InputKeys.DESCRIPTION, value, 65535)) {
						return false;
					}
					description = value;
				}
				else if(InputKeys.RUNNING_STATE.equals(name)) {
					if(greaterThanLength("runningState", InputKeys.RUNNING_STATE, value, 50)) {
						return false;
					}
					runningState = value;
				}
				else if(InputKeys.PRIVACY_STATE.equals(name)) {
					if(greaterThanLength("privacyState", InputKeys.PRIVACY_STATE, value, 50)) {
						return false;
					}
					privacyState = value;
				}
				else if(InputKeys.CLASS_URN_LIST.equals(name)) {
					// Note: This is based on the maximum size of a campaign
					// times 100 plus 100 commas.
					if(greaterThanLength("classes", InputKeys.CLASS_URN_LIST, value, 25600)) {
						return false;
					}
					classes = value;
				}
			} else {
				if(InputKeys.XML.equals(fi.getFieldName())) {					
					xml = new String(fi.get()); // Gets the XML file.
				}
			}
		}
		
		CampaignCreationAwRequest request;
		try {
			request = new CampaignCreationAwRequest(runningState, privacyState, xml, classes, description);
		}
		catch(IllegalArgumentException e) {
			_logger.warn("Attempting to create a campaign with insufficient data after data presence has been checked: " + e.getMessage());
			return false;
		}
		request.setUserToken(token);
		httpRequest.setAttribute("awRequest", request);
		
		return true;
	}

}