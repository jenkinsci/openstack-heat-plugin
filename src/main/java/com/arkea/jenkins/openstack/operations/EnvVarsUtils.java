package com.arkea.jenkins.openstack.operations;

import hudson.EnvVars;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.util.VariableResolver;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.arkea.jenkins.openstack.exception.utils.ExceptionUtils;
import com.arkea.jenkins.openstack.heat.i18n.Messages;
import com.arkea.jenkins.openstack.heat.orchestration.template.Output;
import com.arkea.jenkins.openstack.log.ConsoleLogger;

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
 *         Class utils to retrieve and push variable between step of the
 *         build
 */
public class EnvVarsUtils {

	private EnvVars env;
	private VariableResolver<String> vr;
	private ConsoleLogger cLog;
	private AbstractBuild<?, ?> build;

	public EnvVarsUtils(AbstractBuild<?, ?> build, BuildListener listener,
			ConsoleLogger cLog) {

		this.cLog = cLog;
		this.build = build;

		vr = build.getBuildVariableResolver();
		try {
			env = build.getEnvironment(listener);
		} catch (IOException | InterruptedException e) {
			cLog.logError(Messages.environment_notFound() + e.getMessage()
					+ ExceptionUtils.getStackTrace(e));
		}
	}

	/**
	 * Tranfrom a list of $VARIABLE in good value
	 * 
	 * @param toResolves
	 *            variables to transform
	 * @return variables resolveds
	 */
	public Map<String, String> getVars(Map<String, String> toResolves) {

		Map<String, String> resolveds = new TreeMap<String, String>();
		for (Entry<String, String> entry : toResolves.entrySet()) {
			resolveds.put(entry.getKey(), this.env.expand(Util.replaceMacro(
					entry.getValue(), this.vr)));
			if (!entry.getValue().equals(resolveds.get(entry.getKey()))) {
				this.cLog.logDebug(Messages.environment_variable(
						entry.getKey(), entry.getValue(),
						resolveds.get(entry.getKey())));
			}
		}

		return resolveds;
	}

	/**
	 * Tranfrom $VARIABLE in good value
	 * 
	 * @param toResolve
	 *            variable to transform
	 * @return varaible resolved
	 */
	public String getVar(String toResolve) {

		return this.env.expand(Util.replaceMacro(toResolve, this.vr));

	}

	public void setEnv(final String key, final String value) {
		String key1 = key;
		if (key.startsWith("$")) {
			key1 = key.substring(1);
		}
		this.cLog.logDebug(Messages.environment_output(key, value));
		PublishEnvVar publish = new PublishEnvVar(key1, value);
		this.build.addAction(publish);
		publish.buildEnvVars(this.build, this.env);
	}

	public String getEnv(final String key) {
		return this.env.get(key);
	}

	/**
	 * 
	 * Put $VARIBLE in context to be disponble at different step of the build
	 * 
	 * @param toPuts
	 *            variables to set
	 * @param outputs
	 *            values possibles
	 */
	public void setVars(Map<String, Output> toPuts, Map<String, String> outputs) {

		for (Entry<String, Output> entry : toPuts.entrySet()) {
			Output output = entry.getValue();
			// If the value starts with $ then if a variable and it presents in
			// the list of possibles values
			if (output.getValue().startsWith("$")
					&& outputs.containsKey(entry.getKey())) {
				this.cLog.logDebug(Messages.environment_output(
						output.getValue(), outputs.get(entry.getKey())));
				// Push the variable in the context without the $ in the
				// name
				PublishEnvVar publish = new PublishEnvVar(output.getValue()
						.substring(1), outputs.get(entry.getKey()));
				this.build.addAction(publish);
				publish.buildEnvVars(this.build, this.env);
			}
		}
	}
}
