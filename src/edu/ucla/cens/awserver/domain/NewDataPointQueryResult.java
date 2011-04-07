package edu.ucla.cens.awserver.domain;

import java.util.Map;

/**
 * Cf. DataPointQueryResult except this domain class handles results for multiple users and additional properties from 
 * XML configurations.
 * 
 * @author selsky
 */
public class NewDataPointQueryResult {
	private Object _response;
	private Integer _repeatableSetIteration;
	private String _locationStatus;
	private String _location;
	private String _timestamp;
	private String _timezone;
	private String _surveyId;
	private String _promptId;
	private String _promptType;
	private String _repeatableSetId;
	private String _displayLabel;
	private Object _displayValue;
	private String _unit;
	private String _displayType;
	private String _utcTimestamp;
	private String _client;
	private String _launchContext;
	private String _surveyTitle;
	private String _surveyDescription;
	private String _loginId;
	private Map<String, PromptProperty> _choiceGlossary;
	
	public String getLocationStatus() {
		return _locationStatus;
	}

	public void setLocationStatus(String locationStatus) {
		_locationStatus = locationStatus;
	}

	public String getLocation() {
		return _location;
	}

	public void setLocation(String location) {
		_location = location;
	}

	public String getDisplayType() {
		return _displayType;
	}

	public void setDisplayType(String displayType) {
		_displayType = displayType;
	}

	public Object getResponse() {
		return _response;
	}

	public void setResponse(Object response) {
		_response = response;
	}
	
	public Integer getRepeatableSetIteration() {
		return _repeatableSetIteration;
	}
	
	public void setRepeatableSetIteration(Integer repeatableSetIteration) {
		_repeatableSetIteration = repeatableSetIteration;
	}
	
	public String getTimestamp() {
		return _timestamp;
	}
	
	public void setTimestamp(String timestamp) {
		_timestamp = timestamp;
	}
	
	public String getTimezone() {
		return _timezone;
	}
	
	public void setTimezone(String timezone) {
		_timezone = timezone;
	}
	
	public String getPromptId() {
		return _promptId;
	}
	
	public void setPromptId(String promptId) {
		_promptId = promptId;
	}
	
	public String getPromptType() {
		return _promptType;
	}
	
	public void setPromptType(String promptType) {
		_promptType = promptType;
	}
	
	public String getRepeatableSetId() {
		return _repeatableSetId;
	}
	
	public void setRepeatableSetId(String repeatableSetId) {
		_repeatableSetId = repeatableSetId;
	}

	public boolean isRepeatableSetResult() {
		return null != _repeatableSetId;
	}
	
	public String getSurveyId() {
		return _surveyId;
	}
	
	public void setSurveyId(String surveyId) {
		_surveyId = surveyId;
	}
	
	public String getUnit() {
		return _unit;
	}

	public void setUnit(String unit) {
		_unit = unit;
	}
	
	public String getDisplayLabel() {
		return _displayLabel;
	}

	public void setDisplayLabel(String displayLabel) {
		_displayLabel = displayLabel;
	}

	public Object getDisplayValue() {
		return _displayValue;
	}

	public void setDisplayValue(Object displayValue) {
		_displayValue = displayValue;
	}

	public String getUtcTimestamp() {
		return _utcTimestamp;
	}

	public void setUtcTimestamp(String utcTimestamp) {
		_utcTimestamp = utcTimestamp;
	}

	public String getClient() {
		return _client;
	}

	public void setClient(String client) {
		_client = client;
	}

	public String getLaunchContext() {
		return _launchContext;
	}

	public void setLaunchContext(String launchContext) {
		_launchContext = launchContext;
	}

	public String getSurveyTitle() {
		return _surveyTitle;
	}

	public void setSurveyTitle(String surveyTitle) {
		_surveyTitle = surveyTitle;
	}

	public String getSurveyDescription() {
		return _surveyDescription;
	}

	public void setSurveyDescription(String surveyDescription) {
		_surveyDescription = surveyDescription;
	}
	
	public String getLoginId() {
		return _loginId;
	}

	public void setLoginId(String loginId) {
		_loginId = loginId;
	}
	
	public Map<String, PromptProperty> getChoiceGlossary() {
		return _choiceGlossary;
	}

	public void setChoiceGlossary(Map<String, PromptProperty> choiceGlossary) {
		_choiceGlossary = choiceGlossary;
	}

	@Override
	public String toString() {
		return "NewDataPointQueryResult [_choiceGlossary=" + _choiceGlossary
				+ ", _client=" + _client + ", _displayLabel=" + _displayLabel
				+ ", _displayType=" + _displayType + ", _displayValue="
				+ _displayValue + ", _launchContext=" + _launchContext
				+ ", _location=" + _location + ", _locationStatus="
				+ _locationStatus + ", _loginId=" + _loginId + ", _promptId="
				+ _promptId + ", _promptType=" + _promptType
				+ ", _repeatableSetId=" + _repeatableSetId
				+ ", _repeatableSetIteration=" + _repeatableSetIteration
				+ ", _response=" + _response + ", _surveyDescription="
				+ _surveyDescription + ", _surveyId=" + _surveyId
				+ ", _surveyTitle=" + _surveyTitle + ", _timestamp="
				+ _timestamp + ", _timezone=" + _timezone + ", _unit=" + _unit
				+ ", _utcTimestamp=" + _utcTimestamp + "]";
	}
}