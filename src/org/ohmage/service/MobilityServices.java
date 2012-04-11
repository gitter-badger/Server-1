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
package org.ohmage.service;

import java.util.Collection;
import java.util.List;

import org.joda.time.DateTime;
import org.ohmage.domain.MobilityPoint;
import org.ohmage.domain.MobilityPoint.LocationStatus;
import org.ohmage.domain.MobilityPoint.Mode;
import org.ohmage.domain.MobilityPoint.SensorData;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.ServiceException;
import org.ohmage.query.IUserMobilityQueries;

import edu.ucla.cens.mobilityclassifier.Classification;
import edu.ucla.cens.mobilityclassifier.MobilityClassifier;
import edu.ucla.cens.mobilityclassifier.Sample;
import edu.ucla.cens.mobilityclassifier.WifiScan;

/**
 * This class is responsible for all services pertaining to Mobility points.
 * 
 * @author John Jenkins
 */
public final class MobilityServices {
	private static MobilityServices instance;
	private IUserMobilityQueries userMobilityQueries;
	
	/**
	 * Default constructor. Privately instantiated via dependency injection
	 * (reflection).
	 * 
	 * @throws IllegalStateException if an instance of this class already
	 * exists
	 * 
	 * @throws IllegalArgumentException if iUserMobilityQueries is null
	 */
	private MobilityServices(IUserMobilityQueries iUserMobilityQueries) {
		if(instance != null) {
			throw new IllegalStateException("An instance of this class already exists.");
		}
		
		if(iUserMobilityQueries == null) {
			throw new IllegalArgumentException("An instance of IUserMobilityQueries is required.");
		}
		
		userMobilityQueries = iUserMobilityQueries;
		instance = this;
	}
	
	/**
	 * @return  Returns the singleton instance of this class.
	 */
	public static MobilityServices instance() {
		return instance;
	}
	
	/**
	 * Adds the Mobility point to the database.
	 * 
	 * @param mobilityPoints A list of Mobility points to be added to the 
	 * 						 database.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public void createMobilityPoint(final String username, 
			final String client, final List<MobilityPoint> mobilityPoints) 
			throws ServiceException {
		
		if(username == null) {
			throw new ServiceException("The username cannot be null.");
		}
		else if(client == null) {
			throw new ServiceException("The client cannot be null.");
		}
		
		try {
			for(MobilityPoint mobilityPoint : mobilityPoints) {
				userMobilityQueries.createMobilityPoint(username, client, mobilityPoint);
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Runs the classifier against all of the Mobility points in the list.
	 * 
	 * @param mobilityPoints The Mobility points that are to be classified by
	 * 						 the server.
	 * 
	 * @throws ServiceException Thrown if there is an error with the 
	 * 							classification service.
	 */
	public void classifyData(
			final List<MobilityPoint> mobilityPoints) 
					throws ServiceException {
		
		// If the list is empty, just exit.
		if(mobilityPoints == null) {
			return;
		}

		// Create a new classifier.
		MobilityClassifier classifier = new MobilityClassifier();
		
		// Create place holders for the previous data.
		WifiScan previousWifiScan = null;
		String previousWifiMode = null;
		
		// For each of the Mobility points,
		for(MobilityPoint mobilityPoint : mobilityPoints) {
			// If the data point is of type error, don't attempt to classify 
			// it.
			if(mobilityPoint.getMode().equals(Mode.ERROR)) {
				continue;
			}
			
			// If the SubType is sensor data,
			if(MobilityPoint.SubType.SENSOR_DATA.equals(mobilityPoint.getSubType())) {
				SensorData currSensorData = mobilityPoint.getSensorData();
				
				List<Sample> samples;
				try {
					samples = mobilityPoint.getSamples();
				}
				catch(DomainException e) {
					throw new ServiceException(
							"There was a problem retrieving the samples.",
							e);
				}
				
				WifiScan wifiScan;
				if(mobilityPoint.getSensorData().getWifiData() == null) {
					wifiScan = null;
				}
				else {
					try {
						wifiScan = mobilityPoint.getWifiScan();
					} 
					catch(DomainException e) {
						throw new ServiceException(
								"The Mobility point does not contain WiFi data.",
								e);
					}
				}
				
				// Classify the data.
				Classification classification =
						classifier.classify(
								samples,
								currSensorData.getSpeed(),
								wifiScan,
								previousWifiScan,
								previousWifiMode);
				
				// Update the place holders for the previous data.
				previousWifiScan = wifiScan;
				previousWifiMode = classification.getWifiMode();
				
				// If the classification generated some results, pull them out
				// and store them in the Mobility point.
				if(classification.hasFeatures()) {
					try {
						mobilityPoint.setClassifierData(
								classification.getFft(), 
								classification.getVariance(),
								classification.getN95Variance(), 
								classification.getAverage(), 
								MobilityPoint.Mode.valueOf(classification.getMode().toUpperCase()));
					}
					catch(DomainException e) {
						throw new ServiceException(
								"There was a problem reading the classification's information.", 
								e);
					}
				}
				// If the features don't exist, then create the classifier data
				// with only the mode.
				else {
					try {
						mobilityPoint.setClassifierModeOnly(MobilityPoint.Mode.valueOf(classification.getMode().toUpperCase()));
					}
					catch(DomainException e) {
						throw new ServiceException(
								"There was a problem reading the classification's mode.", 
								e);
					}
				}
			}
		}
	}
	
	/**
	 * Retrieves the information about all of the Mobility points that satisify
	 * the parameters. The username is required as that is how Mobility points
	 * are referenced; however, all other parameters are optional and limit the
	 * results based on their value.<br />
	 * <br />
	 * For example, if only a username is given, the result is all of the
	 * Mobility points for that user. If the username and start date are given,
	 * then all of the Mobility points made by that user after that date are
	 * returned. If the username, start date, and end date are all given, the
	 * result is the list of Mobility points made by that user on or after the
	 * start date and on or before the end date.
	 * 
	 * @param username The username of the user whose points are being queried.
	 * 				   Required.
	 * 
	 * @param startDate A date to which all returned points must be on or 
	 * 					after. Optional.
	 * 
	 * @param endDate A date to which all returned points must be on or before.
	 * 				  Optional.
	 * 
	 * @param privacyState A privacy state to limit the results to only those
	 * 					   with this privacy state. Optional.
	 * 
	 * @param locationStatus A location status to limit the results to only 
	 * 						 those with this location status. Optional.
	 * 
	 * @param mode A mode to limit the results to only those with this mode.
	 * 			   Optional.
	 * 
	 * @return A list of MobilityInformation objects where each object 
	 * 		   represents a single Mobility point that satisfies the  
	 * 		   parameters.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public List<MobilityPoint> retrieveMobilityData(
			final String username,  
			final DateTime startDate, final DateTime endDate, 
			final MobilityPoint.PrivacyState privacyState,
			final LocationStatus locationStatus, final Mode mode) 
			throws ServiceException {
		
		try {
			return userMobilityQueries.getMobilityInformation(
					username, 
					startDate, 
					endDate, 
					privacyState, 
					locationStatus, 
					mode);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Retrieves all of the dates on which the user has created a Mobility 
	 * point within the date range.
	 * 
	 * @param startDate The earliest date to check if the user has any Mobility
	 * 					points.
	 * 
	 * @param endDate The latest date to check if the user has any Mobility
	 * 				  points.
	 * 
	 * @param username The user's username.
	 * 
	 * @return A collection of all of the dates.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public Collection<DateTime> getDates(
			final DateTime startDate,
			final DateTime endDate,
			final String username) 
			throws ServiceException {
		
		try {
			return userMobilityQueries.getDates(startDate, endDate, username);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
}
