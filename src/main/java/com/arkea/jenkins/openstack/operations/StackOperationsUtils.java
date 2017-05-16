package com.arkea.jenkins.openstack.operations;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.openstack4j.model.heat.Stack;

import com.arkea.jenkins.openstack.Constants;
import com.arkea.jenkins.openstack.client.OpenStack4jClient;
import com.arkea.jenkins.openstack.exception.utils.ExceptionUtils;
import com.arkea.jenkins.openstack.heat.HOTPlayerSettings;
import com.arkea.jenkins.openstack.heat.configuration.ProjectOS;
import com.arkea.jenkins.openstack.heat.configuration.TimersOS;
import com.arkea.jenkins.openstack.heat.i18n.Messages;
import com.arkea.jenkins.openstack.heat.loader.AbstractLoader;
import com.arkea.jenkins.openstack.heat.orchestration.template.Bundle;
import com.arkea.jenkins.openstack.heat.orchestration.template.utils.OutputUtils;
import com.arkea.jenkins.openstack.log.ConsoleLogger;
import com.arkea.jenkins.openstack.pool.ProcessStatus;
import com.arkea.jenkins.openstack.pool.StackStatus;
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
 *         Class Utils to operate some action on OpenStack
 * 
 */
public class StackOperationsUtils {

	/**
	 * Delete a stack on Openstack
	 * 
	 * @param stackName
	 *            stack to delete
	 * @param client
	 *            client to access at OpenStack
	 * @param cLog
	 *            logger
	 * @param timersOS
	 *            timeouts
	 * @return a boolean result for the action
	 */
	public static boolean deleteStack(String stackName,
			OpenStack4jClient client, ConsoleLogger cLog, TimersOS timersOS) {

		boolean rtn = true;

		try {
			Stack stackDelete = client.getStackByName(stackName);
			if (stackDelete != null) {
				cLog.logInfo(Messages.stack_existing(stackName));
				cLog.logInfo(Messages.delete_progress());
				client.deleteStack(stackDelete);
				// Check if the stack is deleted ?
				if (ProcessStatus.checkStackStatus(StackStatus.DELETE_COMPLETE,
						client, cLog, stackDelete, timersOS)) {
					cLog.logInfo(Messages.stack_deleted(stackName));
				} else {
					rtn = false;
				}
			}
		} catch (Exception e) {
			cLog.logError(Messages.delete_failed(stackName) + e.getMessage()
					+ ExceptionUtils.getStackTrace(e));
			rtn = false;
		}
		return rtn;

	}

	/**
	 * Create a stack
	 * 
	 * @param eVU
	 *            variables environments
	 * @param bundle
	 *            bundle corresponding at the stack
	 * @param projectOS
	 *            project OpenStack
	 * @param client
	 *            client to access at OpenStack
	 * @param cLog
	 *            logger
	 * @param timersOS
	 *            timeouts
	 * @return a boolean result for the action
	 */
	public static boolean createStack(EnvVarsUtils eVU, Bundle bundle,
			ProjectOS projectOS, OpenStack4jClient client, ConsoleLogger cLog,
			TimersOS timersOS) {

		boolean rtn = true;
		String bundleName = eVU.getVar(bundle.getName());

		try {
			cLog.logInfo(Messages.stack_start(bundleName,
					projectOS.getProject()));

			// Parameters for the stack
			Map<String, String> vars = eVU.getVars(bundle.getParamsOS());
			cLog.logDebugMap(Messages.stack_parameters(), vars);

			AbstractLoader loader = ((HOTPlayerSettings) Jenkins.getInstance()
					.getDescriptor(HOTPlayerSettings.class)).getLoader();

			// Get the full path for the env file if it presents
			String envFile = bundle.getEnvName();
			if (!Strings.isNullOrEmpty(envFile)) {
				envFile = loader.getFullPathEnv(envFile);
			}

			// Create the stack with the variables presents in the context
			Stack stackCreate = client.createStack(bundleName,
					loader.getFullPathHot(bundle.getHotName()),
					eVU.getVars(bundle.getParamsOS()), envFile,
					timersOS.getTimeoutProcessInMin());

			// Check the status of the creation ?
			if (ProcessStatus.checkStackStatus(StackStatus.CREATE_COMPLETE,
					client, cLog, stackCreate, timersOS)) {

				Map<String, String> outputs = new TreeMap<String, String>();

				// Sort the outputs
				List<Map<String, Object>> exits = client.getDetails(bundleName,
						stackCreate.getId()).getOutputs();
				Collections
						.sort(exits, OutputUtils.getComparatorAlphabetical());
				for (Map<String, Object> output : exits) {
					if (output.get(Constants.OUTPUT_VALUE) instanceof String) {
						outputs.put((String) output.get(Constants.OUTPUT_KEY),
								String.valueOf(output
										.get(Constants.OUTPUT_VALUE)));
					} else {
						outputs.put(
								(String) output.get(Constants.OUTPUT_KEY),
								JSONObject.fromObject(
										output.get(Constants.OUTPUT_VALUE))
										.toString());
					}
				}
				cLog.logDebugMap(Messages.stack_outputs(), outputs);
				// If the status is complete, push the value of the outputs in
				// the context if necessary
				eVU.setVars(bundle.getOutputs(), outputs);
				// Set stackname to env
				eVU.setEnv(bundle.getenvStackName(), bundle.getName());

			} else {
				rtn = false;
			}
		} catch (Exception e) {
			cLog.logError(Messages.stack_failed(bundleName)
					+ ExceptionUtils.getStackTrace(e));
			rtn = false;
		}
		return rtn;
	}
}
