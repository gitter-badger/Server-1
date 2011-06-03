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

import org.apache.log4j.Logger;
import org.ohmage.request.InputKeys;
import org.ohmage.util.CookieUtils;
import org.ohmage.util.StringUtils;


/**
 * Validates an incoming survey deletion HTTP request.
 * 
 * @author Joshua Selsky
 */
public class SurveyResponseDeleteValidator extends AbstractHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(SurveyResponseDeleteValidator.class);
	
	/**
	 * Default constructor.
	 */
	public SurveyResponseDeleteValidator() {
		// Do nothing.
	}
	
	/**
	 * Validates that the required parameters exist and represent sane values based on their lengths.
	 * 
	 * @throws MissingAuthTokenException Thrown if the authentication / session
	 * 									 token is missing or invalid. 
	 */
	@Override
	public boolean validate(HttpServletRequest httpRequest) throws MissingAuthTokenException {
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
		
		String campaignUrn = httpRequest.getParameter(InputKeys.CAMPAIGN_URN);
		String surveyKey = httpRequest.getParameter(InputKeys.SURVEY_KEY);
		String client = httpRequest.getParameter(InputKeys.CLIENT);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(token)) {
			throw new MissingAuthTokenException("The required authentication / session token is missing or invalid.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(campaignUrn)) {
			return false;
		} 
		else if(StringUtils.isEmptyOrWhitespaceOnly(surveyKey)) {
			return false;
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(client)) {
			return false;
		}
		
		if(greaterThanLength(InputKeys.AUTH_TOKEN, InputKeys.AUTH_TOKEN, token, 36)) {
			_logger.warn(InputKeys.AUTH_TOKEN + " is too long.");
			return false;
		}
		else if(greaterThanLength(InputKeys.CAMPAIGN_URN, InputKeys.CAMPAIGN_URN, campaignUrn, 255)) {
			_logger.warn(InputKeys.CAMPAIGN_URN + " is too long.");
			return false;
		} 
		else if(greaterThanLength(InputKeys.SURVEY_KEY, InputKeys.SURVEY_KEY, surveyKey, 10)) {
			_logger.warn(InputKeys.SURVEY_KEY + " is too long.");
			return false;
		}
		else if(greaterThanLength(InputKeys.CLIENT, InputKeys.CLIENT, client, 250)) {
			_logger.warn(InputKeys.CLIENT + " is too long.");
			return false;
		}
		
		return true;
	}
}