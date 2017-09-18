package com.arkea.jenkins.openstack.heat.orchestration.template.constraints;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.arkea.jenkins.openstack.Constants;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Credit Mutuel Arkea
 * 
 *         Copyright 2015 Credit Mutuel Arkea
 *
 *         Licensed under the Apache License, Version 2.0 (the "License");
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *         implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 * 
 *         This class is a utilitary class with mapping methods about yaml
 *         constraints :
 *         - conversion from yaml to Java
 *         - conversion from JSOn to Java
 *
 */
public class ConstraintUtils {

	/**
	 * Build a constraint bean map from JSON constraints part parsing in entry
	 * file.
	 * 
	 * @param properties
	 *            the constraints list String structure
	 * @return
	 *         the equivalent constraints JAVA Map
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, AbstractConstraint> getContraintsToPopulatParameters(
			Map<String, Object> properties) {
		Map<String, AbstractConstraint> constraints = new HashMap<String, AbstractConstraint>();

		if (properties != null) {
			if (properties.get(Constants.CONSTRAINTS) != null) {
				List<Map<String, Object>> cons = (List<Map<String, Object>>) properties
						.get(Constants.CONSTRAINTS);
				for (Map<String, Object> entry : cons) {
					AbstractConstraint constraint = populateConstraint(entry);
					constraints
							.put(constraint.getType().getConstraintTypeName(),
									populateConstraint(entry));
				}
			}
		}

		return constraints;
	}

	@SuppressWarnings("unchecked")
	private static AbstractConstraint populateConstraint(
			Map<String, Object> jsonConstraint) {

		AbstractConstraint constraints = null;
		String description = "";

		for (Entry<String, Object> entry : jsonConstraint.entrySet()) {
			switch (entry.getKey()) {
			case Constants.CONSTRAINT_ALLOWED_VALUES:
				Map<String, String> allowedValues = new HashMap<String, String>();
				for (Object value : (ArrayList<Object>) entry.getValue()) {
					allowedValues.put(value.toString(), value.toString());
				}
				constraints = new AllowedValuesConstraint(allowedValues);
				break;
			case Constants.CONSTRAINT_LENGTH:
				constraints = new LengthConstraint();
				Map<String, Integer> lengthList = (Map<String, Integer>) entry
						.getValue();
				if (lengthList.get(Constants.MIN) != null) {
					((LengthConstraint) constraints).getLimits().put(
							Constants.MIN, lengthList.get(Constants.MIN));
				}
				if (lengthList.get(Constants.MAX) != null) {
					((LengthConstraint) constraints).getLimits().put(
							Constants.MAX, lengthList.get(Constants.MAX));
				}
				break;
			case Constants.CONSTRAINT_RANGE:
				Map<String, Double> rangeList = (Map<String, Double>) entry
						.getValue();
				constraints = new RangeConstraint();
				if (rangeList.get(Constants.MIN) != null) {
					((RangeConstraint) constraints).getLimits().put(
							Constants.MIN, rangeList.get(Constants.MIN));
				}
				if (rangeList.get(Constants.MAX) != null) {
					((RangeConstraint) constraints).getLimits().put(
							Constants.MAX, rangeList.get(Constants.MAX));
				}
				break;
			case Constants.CONSTRAINT_ALLOWED_PATTERN:
				constraints = new AllowedPatternConstraint(
						(String) entry.getValue());
				break;
			case Constants.CONSTRAINT_CUSTOM_CONSTRAINT:
				constraints = new CustomConstraint((String) entry.getValue());
				break;

			case Constants.DESCRIPTION:
				description = (String) entry.getValue();
				break;
			}
		}
		constraints.setDescription(description);
		return constraints;

	}

	/**
	 * Build a constraint bean map from JSON constraints part parsing in JSON
	 * parameter.
	 * 
	 * @param properties
	 *            the constraints list JSON structure
	 * @return
	 *         the equivalent constraints JAVA map
	 * @throws IOException
	 *             convert map error
	 * @throws JsonMappingException
	 *             convert map error
	 * @throws JsonParseException
	 *             convert map error
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, AbstractConstraint> getContraintsFromJSONParameter(
			Map<String, Object> properties) throws JsonParseException,
			JsonMappingException, IOException {
		Map<String, AbstractConstraint> constraints = new HashMap<String, AbstractConstraint>();

		if (properties.get(Constants.CONSTRAINTS) != null
				&& !"null".equals(properties.get(Constants.CONSTRAINTS)
						.toString())) {
			Map<String, Map<String, Object>> cons = (Map<String, Map<String, Object>>) properties
					.get(Constants.CONSTRAINTS);
			final ObjectMapper objMap = new ObjectMapper();
			for (Entry<String, Map<String, Object>> entry : cons.entrySet()) {
				switch (entry.getKey()) {
				case Constants.CONSTRAINT_ALLOWED_PATTERN:
					AllowedPatternConstraint apc = new AllowedPatternConstraint(
							(String) entry.getValue().get(
									Constants.CONSTRAINT_ALLOWED_PATTERN));
					apc.setDescription((String) entry.getValue().get(
							Constants.DESCRIPTION));
					constraints.put(entry.getKey(), apc);
					break;
				case Constants.CONSTRAINT_ALLOWED_VALUES:
					AllowedValuesConstraint avc = new AllowedValuesConstraint(
							(Map<String, String>) objMap
									.readValue(
											entry.getValue()
													.get(Constants.CONSTRAINT_ALLOWED_VALUES)
													.toString(),
											new TypeReference<Map<String, String>>() {
											}));
					avc.setDescription((String) entry.getValue().get(
							Constants.DESCRIPTION));
					constraints.put(entry.getKey(), avc);
					break;
				case Constants.CONSTRAINT_LENGTH:
					LengthConstraint lc = new LengthConstraint(
							(Map<String, Integer>) objMap.readValue(entry
									.getValue().get(Constants.LIMITS)
									.toString(),
									new TypeReference<Map<String, Integer>>() {
									}), (String) entry.getValue().get(
									Constants.DESCRIPTION));
					constraints.put(entry.getKey(), lc);
					break;
				case Constants.CONSTRAINT_RANGE:
					RangeConstraint rc;
					rc = new RangeConstraint(
							(Map<String, Double>) objMap.readValue(entry
									.getValue().get(Constants.LIMITS)
									.toString(),
									new TypeReference<Map<String, Double>>() {
									}), (String) entry.getValue().get(
									Constants.DESCRIPTION));
					constraints.put(entry.getKey(), rc);
					break;
				case Constants.CONSTRAINT_CUSTOM_CONSTRAINT:
					CustomConstraint cc = new CustomConstraint((String) entry
							.getValue().get(Constants.KEY));
					cc.setDescription((String) entry.getValue().get(
							Constants.DESCRIPTION));
					constraints.put(entry.getKey(), cc);
					break;
				}
			}
		}
		return constraints;
	}
}
