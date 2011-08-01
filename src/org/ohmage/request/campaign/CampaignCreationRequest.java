package org.ohmage.request.campaign;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.CampaignServices;
import org.ohmage.service.CampaignServices.CampaignIdAndName;
import org.ohmage.service.ServiceException;
import org.ohmage.service.UserClassServices;
import org.ohmage.service.UserServices;
import org.ohmage.util.CookieUtils;
import org.ohmage.validator.CampaignValidators;
import org.ohmage.validator.ClassValidators;
import org.ohmage.validator.ValidationException;

/**
 * <p>A request to create a campaign. The creator must associate it with at 
 * least one class.</p>
 * <table border="1">
 *   <tr>
 *     <td>Parameter Name</td>
 *     <td>Description</td>
 *     <td>Required</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLIENT}</td>
 *     <td>A string describing the client that is making this request.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#XML}</td>
 *     <td>The XML file describing this campaign.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#RUNNING_STATE}</td>
 *     <td>The initial running state of this campaign.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#PRIVACY_STATE}</td>
 *     <td>The initial privacy state of this campaign.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLASS_URN_LIST}</td>
 *     <td>A list of classes with which this campaign will initially be 
 *       associated.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#DESCRIPTION}</td>
 *     <td>An optional description of this campaign.</td>
 *     <td>false</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class CampaignCreationRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(CampaignCreationRequest.class);

	private final String xml;
	private final String description;
	private final String runningState;
	private final String privacyState;
	private final List<String> classIds;
	
	/**
	 * Builds a campaign creation request from the HttpServletRequest.
	 * 
	 * @param httpRequest The HttpServletRequest that contains the parameters
	 * 					  necessary for servicing this request.
	 */
	public CampaignCreationRequest(HttpServletRequest httpRequest) {
		super(CookieUtils.getCookieValue(httpRequest.getCookies(), InputKeys.AUTH_TOKEN), httpRequest.getParameter(InputKeys.CLIENT));
		
		LOGGER.info("Creating a campaign creation request.");
		
		String tXml = null;
		String tDescription = null;
		String tRunningState = null;
		String tPrivacyState = null;
		List<String> tClassIds = null;
		
		try {
			byte[] xml = getMultipartValue(httpRequest, InputKeys.XML);
			if(xml == null) {
				setFailed(ErrorCodes.CAMPAIGN_INVALID_XML, "Missing required campaign XML: " + InputKeys.XML);
				throw new ValidationException("Missing required campaign XML.");
			}
			else {
				tXml = CampaignValidators.validateXml(this, new String(xml));
			}
			if(tXml == null) {
				setFailed(ErrorCodes.CAMPAIGN_INVALID_XML, "Missing required campaign XML.");
				throw new ValidationException("Missing required campaign XML.");
			}
			
			tRunningState = CampaignValidators.validateRunningState(this, httpRequest.getParameter(InputKeys.RUNNING_STATE));
			if(tRunningState == null) {
				setFailed(ErrorCodes.CAMPAIGN_INVALID_RUNNING_STATE, "Missing the required initial running state.");
				throw new ValidationException("Missing required running state.");
			}
			
			tPrivacyState = CampaignValidators.validatePrivacyState(this, httpRequest.getParameter(InputKeys.PRIVACY_STATE));
			if(tPrivacyState == null) {
				setFailed(ErrorCodes.CAMPAIGN_INVALID_PRIVACY_STATE, "Missing the required initial privacy state.");
				throw new ValidationException("Missing required privacy state.");
			}
			
			tClassIds = ClassValidators.validateClassIdList(this, httpRequest.getParameter(InputKeys.CLASS_URN_LIST));
			if((tClassIds == null) || (tClassIds.size() == 0)) {
				setFailed(ErrorCodes.CLASS_INVALID_ID, "Missing the required class ID list.");
				throw new ValidationException("Missing required class ID list.");
			}
			
			tDescription = CampaignValidators.validateDescription(this, httpRequest.getParameter(InputKeys.DESCRIPTION));
		}
		catch(ValidationException e) {
			LOGGER.info(e.toString());
		}
		
		xml = tXml;
		description = tDescription;
		runningState = tRunningState;
		privacyState = tPrivacyState;
		classIds = tClassIds;
	}

	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the campaign creation request.");
		
		if(! authenticate(false)) {
			return;
		}
		
		try {
			// Get the campaign's URN and name from the XML.
			CampaignIdAndName campaignInfo = CampaignServices.getCampaignUrnAndNameFromXml(this, xml);
			
			LOGGER.info("Verifying that the campaign doesn't already exist.");
			CampaignServices.checkCampaignExistence(this, campaignInfo.getCampaignId(), false);
			
			LOGGER.info("Verifying that the user is allowed to create campaigns.");
			UserServices.verifyUserCanCreateCampaigns(this, user.getUsername());
			
			LOGGER.info("Verifying that all of the classes and that the user is enrolled in call of the classes.");
			UserClassServices.classesExistAndUserBelongs(this, classIds, user.getUsername());
			
			LOGGER.info("Creating the campaign.");
			CampaignServices.createCampaign(this, campaignInfo.getCampaignId(), campaignInfo.getCampaignName(), 
					xml, description, runningState, privacyState, classIds, user.getUsername());
		}
		catch(ServiceException e) {
			e.logException(LOGGER);
		}
	}

	/**
	 * Responds to the request with a failure message should the request have
	 * failed or a success message.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		respond(httpRequest, httpResponse, null);
	}
}