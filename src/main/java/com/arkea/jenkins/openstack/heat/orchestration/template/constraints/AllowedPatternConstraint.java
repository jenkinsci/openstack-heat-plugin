package com.arkea.jenkins.openstack.heat.orchestration.template.constraints;

import java.util.regex.Pattern;

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
 *         This class is a bean describing an "allowed_pattern" type constraint
 *
 */
public class AllowedPatternConstraint extends AbstractConstraint {

	private String allowed_pattern;

	public AllowedPatternConstraint() {
		super(ConstraintType.allowed_pattern);
	}

	public AllowedPatternConstraint(String allowed_pattern) {
		super(ConstraintType.allowed_pattern);
		this.allowed_pattern = allowed_pattern;
	}

	public String getAllowed_pattern() {
		return allowed_pattern;
	}

	public void setAllowed_pattern(String allowed_pattern) {
		this.allowed_pattern = allowed_pattern;
	}

	@Override
	public boolean checkConstraint(Parameter parameter) {
		Pattern p = Pattern.compile(allowed_pattern);
		if (!Strings.isNullOrEmpty(parameter.getValue())) {
			return p.matcher(parameter.getValue()).matches();
		} else if (!Strings.isNullOrEmpty(String.valueOf(parameter.getDefaultValue()))) {
			return p.matcher(String.valueOf(parameter.getDefaultValue())).matches();
		} else {
			return false;
		}
	}
}
