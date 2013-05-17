/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
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
package org.ohmage.domain.campaign.response;

import java.math.BigDecimal;

import org.ohmage.domain.campaign.PromptResponse;
import org.ohmage.domain.campaign.prompt.NumberPrompt;
import org.ohmage.exception.DomainException;

/**
 * A number prompt response.
 * 
 * @author John Jenkins
 */
public class NumberPromptResponse extends PromptResponse {
	/**
	 * Creates a number prompt response.
	 * 
	 * @param prompt The HoursBeforeNowPrompt used to generate this response.
	 * 
	 * @param noResponse A 
	 * 					 {@link org.ohmage.domain.campaign.Response.NoResponse}
	 * 					 value if the user didn't supply an answer to this 
	 * 					 prompt.
	 * 
	 * @param repeatableSetIteration If the prompt was part of a repeatable 
	 * 								 set, this is the iteration of that 
	 * 								 repeatable set on which this response was
	 * 								 made.
	 * 
	 * @param number The response from the user.
	 * 
	 * @throws DomainException Thrown if any of the parameters are invalid.
	 */
	public NumberPromptResponse(
			final NumberPrompt prompt,
			final Integer repeatableSetIteration, 
			final Object response) 
			throws DomainException {
		
		super(prompt, repeatableSetIteration, response);
	}
	
	/**
	 * Returns the number response from the user.
	 * 
	 * @return The number response from the user.
	 * 
	 * @throws DomainException The prompt does not have a response.
	 */
	public BigDecimal getNumber() throws DomainException {
		if(wasNotDisplayed()) {
			throw new DomainException("The prompt was not displayed.");
		}
		else if(wasSkipped()) {
			throw new DomainException("The prompt was skipped.");
		}
		
		return (BigDecimal) getResponse();
	}
}