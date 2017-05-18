package com.arkea.jenkins.openstack.heat.orchestration.template;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

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
 *         This class is a bean describing a stack
 * 
 */
public class Bundle {

	/**
	 * Properties
	 */
	private String hotName;
	private String name;
	private String tags;
	private boolean exist;
	private boolean debug;
	private boolean tag;
	private Map<String, Parameter> parameters = new TreeMap<String, Parameter>();
	private Map<String, Output> outputs = new TreeMap<String, Output>();
	private String envName;
/*
	public Bundle(String hotName, String name, boolean exist, boolean debug) {
		this.hotName = hotName;
		this.name = name;
		this.exist = exist;
		this.debug = debug;
	}
*/	
	public Bundle(String hotName, String name, String tags,boolean tag,boolean exist, boolean debug) {
		this.hotName = hotName;
		this.name = name;
		this.exist = exist;
		this.debug = debug;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getHotName() {
		return hotName;
	}

	public String getName() {
		return name;
	}

	public void setTag(boolean tag){
		this.tag = tag;
	}
	
	public boolean isTag() {
		return tag;
	}
	
	public boolean isExist() {
		return exist;
	}

	public boolean isDebug() {
		return debug;
	}

	public Map<String, Parameter> getParameters() {
		return parameters;
	}

	public Map<String, Output> getOutputs() {
		return outputs;
	}

	public void setParameters(Map<String, Parameter> parameters) {
		this.parameters = parameters;
	}

	public void setOutputs(Map<String, Output> outputs) {
		this.outputs = outputs;
	}

	public String getEnvName() {
		return envName;
	}

	public void setEnvName(String envName) {
		this.envName = envName;
	}

	/**
	 * @return the parameters to create a stack via OpenStack4jClient.
	 */
	public Map<String, String> getParamsOS() {
		Map<String, String> paramsOS = new TreeMap<String, String>();
		for (Entry<String, Parameter> entry : this.parameters.entrySet()) {
			if (entry.getValue().getValue() != null
					&& !StringUtils.isEmpty(entry.getValue().getValue())) {
				paramsOS.put(entry.getKey(), convertValue(entry.getValue()
						.getValue()));
			} else if (entry.getValue().getDefaultValue() != null
					&& !StringUtils.isEmpty(convertValue(entry.getValue()
							.getDefaultValue()))) {
				paramsOS.put(entry.getKey(), convertValue(entry.getValue()
						.getDefaultValue()));
			} else {
				paramsOS.put(entry.getKey(), "");
			}
		}
		return paramsOS;
	}

	/**
	 * Convert an object value from the yaml to string
	 * 
	 * @param value
	 *            the object value from the yaml
	 * @return the object in string format
	 */
	@SuppressWarnings("unchecked")
	private String convertValue(Object value) {
		if (value instanceof java.util.LinkedHashMap) {
			StringBuilder rtn = new StringBuilder("{");
			java.util.LinkedHashMap<String, String> data = (java.util.LinkedHashMap<String, String>) value;
			for (Entry<String, String> entry : data.entrySet()) {
				rtn.append(entry.getKey()).append(":").append(entry.getValue())
						.append(",");
			}
			rtn.deleteCharAt(rtn.length() - 1);
			rtn.append("}");
			return rtn.toString();
		}
		return String.valueOf(value);
	}
}
