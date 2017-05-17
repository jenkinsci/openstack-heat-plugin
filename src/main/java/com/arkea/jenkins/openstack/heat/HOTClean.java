package com.arkea.jenkins.openstack.heat;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;

import com.arkea.jenkins.openstack.Constants;
import com.arkea.jenkins.openstack.client.OpenStack4jClient;
import com.arkea.jenkins.openstack.exception.utils.ExceptionUtils;
import com.arkea.jenkins.openstack.heat.configuration.ProjectOS;
import com.arkea.jenkins.openstack.heat.i18n.Messages;
import com.arkea.jenkins.openstack.log.ConsoleLogger;
import com.arkea.jenkins.openstack.operations.EnvVarsUtils;
import com.arkea.jenkins.openstack.operations.StackOperationsUtils;
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
 *         Principal class to play the template orchestration Heat
 *
 */

public class HOTClean extends Publisher {

	private OpenStack4jClient clientOS;

	@SuppressWarnings("deprecation")
	@DataBoundConstructor
	public HOTClean() {
	}

	/**
	 * Using for the tests
	 * 
	 * @param clientOS
	 *            inject by test
	 */
	@SuppressWarnings("deprecation")
	public HOTClean(OpenStack4jClient clientOS) {
		this.clientOS = clientOS;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean perform(AbstractBuild build, Launcher launcher,
			BuildListener listener) {

		// Specific logger with color
		ConsoleLogger cLog = new ConsoleLogger(listener.getLogger(),
				"HOT Clean ", false);
		try {

			// Variable in context
			EnvVarsUtils eVU = new EnvVarsUtils(build, listener, cLog);

			if (Strings.isNullOrEmpty(eVU.getValue(Constants.STACKS_TO_DELETE))) {
				cLog.logInfo(Messages.stacksToDelete_empty());
			} else {
				// Global configuration
				HOTPlayerSettings hPS = (HOTPlayerSettings) Jenkins
						.getInstance().getDescriptor(HOTPlayerSettings.class);

				JSONArray stacks = JSONArray.fromObject(eVU
						.getValue(Constants.STACKS_TO_DELETE));

				for (Object stackToDelete : stacks) {
					JSONObject stack = (JSONObject) stackToDelete;
					String stackName = stack.getString(Constants.STACKNAME);
					ProjectOS project = new ProjectOS(
							stack.getJSONObject(Constants.PROJECT));

					// Client OpenStack inject during test or client failed
					if (clientOS == null || !clientOS.isConnectionOK()) {
						clientOS = new OpenStack4jClient(project);
					}

					cLog.logInfo(Messages.stacksToDelete_process(stackName,
							project.getProject()));
					if (!StackOperationsUtils.deleteStack(
							eVU.getVar(stackName), clientOS, cLog,
							hPS.getTimersOS())) {
						cLog.logError(Messages.stacksToDelete_error(stackName));
					}
				}

			}
		} catch (Exception e) {
			cLog.logError(Messages.stacksToDelete_failed()
					+ ExceptionUtils.getStackTrace(e));
			return false;
		}
		return true;
	}

	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	@Extension
	public static final class DescriptorImpl extends
			BuildStepDescriptor<Publisher> {

		@SuppressWarnings("rawtypes")
		@Override
		public boolean isApplicable(Class<? extends AbstractProject> arg0) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return "Delete HOT player stacks when build done";
		}

	}
}
