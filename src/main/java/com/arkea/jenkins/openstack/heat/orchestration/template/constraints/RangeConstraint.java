package com.arkea.jenkins.openstack.heat.orchestration.template.constraints;

import java.util.HashMap;
import java.util.Map;

import com.arkea.jenkins.openstack.Constants;
import com.arkea.jenkins.openstack.heat.orchestration.template.Parameter;
import com.google.common.base.Strings;

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
 *         This class is a bean describing a "range" type constraint
 *
 */

public class RangeConstraint extends AbstractConstraint {

	private Map<String, Double> limits = new HashMap<String, Double>();

	RangeConstraint() {
		super(ConstraintType.range);
	}

	RangeConstraint(Map<String, Double> limits, String description) {
		this();
		this.limits = limits;
		this.description = description;
	}

	public Map<String, Double> getLimits() {
		return limits;
	}

	public void setLimits(Map<String, Double> limits) {
		this.limits = limits;
	}

	@Override
	public boolean checkConstraint(Parameter parameter) {
		double value = 0;
		if (!Strings.isNullOrEmpty(parameter.getValue())) {
			value = Double.valueOf(parameter.getValue());
		} else if (!Strings.isNullOrEmpty(String.valueOf(parameter
				.getDefaultValue()))) {
			value = Double.valueOf(String.valueOf(parameter.getDefaultValue()));
		}
		boolean rtn = true;
		if (this.limits.containsKey(Constants.MIN)
				&& value < this.limits.get(Constants.MIN)) {
			rtn = false;
		} else if (this.limits.containsKey(Constants.MAX)
				&& value > this.limits.get(Constants.MAX)) {
			rtn = false;
		}
		return rtn;
	}

}
