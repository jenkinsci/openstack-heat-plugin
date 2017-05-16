package com.arkea.jenkins.openstack.heat.orchestration.template.utils;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.yaml.snakeyaml.Yaml;

import com.arkea.jenkins.openstack.Constants;
import com.arkea.jenkins.openstack.heat.orchestration.template.Bundle;
import com.arkea.jenkins.openstack.heat.orchestration.template.Output;
import com.arkea.jenkins.openstack.heat.orchestration.template.Parameter;
import com.arkea.jenkins.openstack.heat.orchestration.template.constraints.ConstraintUtils;

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
 *         Class utils to parse the HOT
 * 
 */
public class HOTMapperUtils {

	/**
	 * @param hotName
	 *            name HOT
	 * @param hot
	 *            the contents
	 * @return the bundle
	 */
	@SuppressWarnings("unchecked")
	public static Bundle getBundleFromHOT(String hotName, String hot) {

		Bundle stack = new Bundle(hotName, "","",false, false, false);

		// Find paramaters or outputs in HOT
		if (hot.contains(Constants.PARAMETERS)
				|| hot.contains(Constants.OUTPUTS)) {
			Map<String, Object> hotObjects = (Map<String, Object>) (new Yaml())
					.load(hot);
			if (hot.contains(Constants.PARAMETERS)) {
				stack.setParameters(getParameters(hotObjects));
			}
			if (hot.contains(Constants.OUTPUTS)) {
				stack.setOutputs(getOutputs(hotObjects));
			}
			if (hot.contains(Constants.TAGS)){
				stack.setTags(getTags(hotObjects));
			}
		}

		return stack;

	}

	/**
	 * Transform parameters from HOT to parameters in JAVA
	 * 
	 * @param hotObjects
	 *            the objects parameters content in the HOT
	 * @return the list of parameters
	 */
	@SuppressWarnings("unchecked")
	private static Map<String, Parameter> getParameters(
			Map<String, Object> hotObjects) {
		Map<String, Parameter> params = new TreeMap<String, Parameter>();
		Map<String, Object> parameters = (Map<String, Object>) hotObjects
				.get(Constants.PARAMETERS);
		for (Entry<String, Object> entry : parameters.entrySet()) {
			Map<String, Object> data = (Map<String, Object>) entry.getValue();
			params.put(entry.getKey(), populateParameter(entry.getKey(), data));
		}
		return params;
	}
	
	/**
	 * Transform parameters from HOT to parameters in JAVA
	 * 
	 * @param hotObjects
	 *            the objects parameters content in the HOT
	 * @return the list of parameters
	 */
	@SuppressWarnings("unchecked")
	private static String getTags(Map<String, Object> hotObjects){	
		return (String)hotObjects.get(Constants.TAGS);
	}	
	
	
	
	

	/**
	 * Create Paramater in JAVA
	 * 
	 * @param name
	 *            name of the parameter
	 * @param properties
	 *            properties of the paramater
	 * @return parameter
	 */
	private static Parameter populateParameter(String name,
			Map<String, Object> properties) {

		Parameter param = new Parameter(
				name,
				TypeMapperUtils.getType((String) properties.get(Constants.TYPE)));

		if (properties.get(Constants.LABEL) != null) {
			param.setLabel((String) properties.get(Constants.LABEL));
		}

		if (properties.get(Constants.DESCRIPTION) != null) {
			param.setDescription((String) properties.get(Constants.DESCRIPTION));
		}

		if (properties.get(Constants.DEFAULT) != null) {
			param.setDefaultValue(properties.get(Constants.DEFAULT));
		}

		if (properties.get(Constants.HIDDEN) != null) {
			param.setHidden((boolean) properties.get(Constants.HIDDEN));
		}

		if (properties.get(Constants.CONSTRAINTS) != null) {
			param.setConstraints(ConstraintUtils
					.getContraintsToPopulatParameters(properties));
		}

		return param;
	}

	/**
	 * Transform outputs from HOT to outputs in JAVA
	 * 
	 * @param hotObjects
	 *            the objects outputs content in the HOT
	 * @return the list of outputs
	 */
	@SuppressWarnings("unchecked")
	private static Map<String, Output> getOutputs(Map<String, Object> hotObjects) {
		Map<String, Output> exits = new TreeMap<String, Output>();
		Map<String, Object> outputs = (Map<String, Object>) hotObjects
				.get(Constants.OUTPUTS);
		for (Entry<String, Object> entry : outputs.entrySet()) {
			exits.put(
					entry.getKey(),
					new Output(entry.getKey(),
							(String) ((Map<String, Object>) entry.getValue())
									.get(Constants.DESCRIPTION)));
		}
		return exits;
	}
}
