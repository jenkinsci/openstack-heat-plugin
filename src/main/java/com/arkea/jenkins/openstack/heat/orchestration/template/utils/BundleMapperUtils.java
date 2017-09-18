package com.arkea.jenkins.openstack.heat.orchestration.template.utils;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.sf.json.JSONObject;

import com.arkea.jenkins.openstack.Constants;
import com.arkea.jenkins.openstack.heat.orchestration.template.Bundle;
import com.arkea.jenkins.openstack.heat.orchestration.template.Output;
import com.arkea.jenkins.openstack.heat.orchestration.template.Parameter;
import com.arkea.jenkins.openstack.heat.orchestration.template.constraints.ConstraintUtils;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

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
 *         Class utils to transform a JSONObject to a Bundle
 * 
 */
public class BundleMapperUtils {

	@SuppressWarnings("unchecked")
	public static Bundle getBundleFromJson(String data)
			throws JsonParseException, JsonMappingException, IOException {

		JSONObject json = JSONObject.fromObject(data);

		// Properties globals
		Bundle bundle = new Bundle(json.getString(Constants.HOTNAME),
				json.getString(Constants.NAME), json.getString(Constants.TAGS),
				json.getBoolean(Constants.TAG),
				json.getBoolean(Constants.EXIST),
				json.getBoolean(Constants.DEBUG));

		bundle.setTags(json.getString(Constants.TAGS));
		bundle.setTag(json.getBoolean(Constants.TAG));

		// Parameters
		Map<String, Parameter> params = new TreeMap<String, Parameter>();
		Map<String, Object> parameters = json
				.getJSONObject(Constants.PARAMETERS);

		for (Entry<String, Object> entry : parameters.entrySet()) {
			Map<String, Object> properties = (Map<String, Object>) entry
					.getValue();
			Parameter param = new Parameter(
					(String) properties.get(Constants.NAME),
					TypeMapperUtils.getType((String) properties
							.get(Constants.TYPE)),
					(String) properties.get(Constants.LABEL),
					(String) properties.get(Constants.DESCRIPTION),
					properties.get(Constants.DEFAULT_VALUE),
					(boolean) properties.get(Constants.HIDDEN),
					(String) properties.get(Constants.VALUE),
					ConstraintUtils.getContraintsFromJSONParameter(properties));
			params.put(entry.getKey(), param);
		}

		bundle.setParameters(params);

		// Outputs
		Map<String, Output> exits = new TreeMap<String, Output>();
		Map<String, Object> outputs = json.getJSONObject(Constants.OUTPUTS);

		for (Entry<String, Object> entry : outputs.entrySet()) {
			Map<String, Object> properties = (Map<String, Object>) entry
					.getValue();
			Output exit = new Output((String) properties.get(Constants.NAME),
					(String) properties.get(Constants.DESCRIPTION),
					(String) properties.get(Constants.VALUE));
			exits.put(entry.getKey(), exit);
		}

		bundle.setOutputs(exits);

		return bundle;
	}

}
