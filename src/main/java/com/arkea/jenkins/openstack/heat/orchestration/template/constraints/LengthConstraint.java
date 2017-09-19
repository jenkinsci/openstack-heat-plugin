package com.arkea.jenkins.openstack.heat.orchestration.template.constraints;

import java.util.HashMap;
import java.util.Map;

import com.arkea.jenkins.openstack.Constants;
import com.arkea.jenkins.openstack.heat.orchestration.template.Parameter;

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
 *         This class is a bean describing a "length" type constraint
 * 
 */

public class LengthConstraint extends AbstractConstraint {

	private Map<String, Integer> limits = new HashMap<String, Integer>();

	LengthConstraint() {
		super(ConstraintType.length);
	}

	LengthConstraint(Map<String, Integer> limits, String description) {
		this();
		this.setLimits(limits);
		this.description = description;
	}

	public Map<String, Integer> getLimits() {
		return limits;
	}

	public void setLimits(Map<String, Integer> limits) {
		this.limits = limits;
	}

	@Override
	public boolean checkConstraint(Parameter parameter) {
		int length = parameter.getValue().length() == 0 ? (String.valueOf(parameter
				.getDefaultValue())).length() : parameter.getValue().length();
		if (limits.containsKey(Constants.MIN)
				&& length < limits.get(Constants.MIN)) {
			return false;
		} else if (limits.containsKey(Constants.MAX)
				&& length > limits.get(Constants.MAX)) {
			return false;
		}
		return true;
	}
}
