package com.arkea.jenkins.openstack.heat;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;

import java.util.List;

import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import com.arkea.jenkins.openstack.Constants;
import com.arkea.jenkins.openstack.client.OpenStack4jClient;
import com.arkea.jenkins.openstack.exception.utils.ExceptionUtils;
import com.arkea.jenkins.openstack.heat.configuration.ProjectOS;
import com.arkea.jenkins.openstack.heat.i18n.Messages;
import com.arkea.jenkins.openstack.heat.orchestration.template.Bundle;
import com.arkea.jenkins.openstack.heat.orchestration.template.utils.BundleMapperUtils;
import com.arkea.jenkins.openstack.heat.orchestration.template.utils.EnvMapperUtils;
import com.arkea.jenkins.openstack.heat.orchestration.template.utils.HOTMapperUtils;
import com.arkea.jenkins.openstack.heat.orchestration.template.utils.ParameterUtils;
import com.arkea.jenkins.openstack.log.ConsoleLogger;
import com.arkea.jenkins.openstack.operations.EnvVarsUtils;
import com.arkea.jenkins.openstack.operations.StackOperationsUtils;
import com.google.common.base.Strings;
import com.google.inject.Inject;

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
public class HOTPlayer extends Builder {

	private String hotName;
	private String envName;
	private Bundle bundle;
	private String project;
	private OpenStack4jClient clientOS;

	@DataBoundConstructor
	public HOTPlayer(String project, Bundle bundle) {
		this.project = project;
		this.hotName = bundle.getHotName();
		this.envName = bundle.getEnvName();
		this.bundle = bundle;
	}

	public HOTPlayer(String project, Bundle bundle, OpenStack4jClient clientOS) {
		this.project = project;
		this.hotName = bundle.getHotName();
		this.envName = bundle.getEnvName();
		this.bundle = bundle;
		this.clientOS = clientOS;
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	public String getHotName() {
		return hotName;
	}

	public String getEnvName() {
		return envName;
	}

	/**
	 * @return the bundle in JSON Format, it's easier to the render JavaScript
	 */
	public JSONObject getBundle() {
		return JSONObject.fromObject(this.bundle);
	}

	public String getProject() {
		return project;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean perform(AbstractBuild build, Launcher launcher,
			BuildListener listener) {

		// Specific logger with color
		ConsoleLogger cLog = new ConsoleLogger(listener.getLogger(),
				"HOT Player ", bundle.isDebug());
		try {

			// Variable in context
			EnvVarsUtils eVU = new EnvVarsUtils(build, listener, cLog);

			// Global configuration
			HOTPlayerSettings hPS = (HOTPlayerSettings) Jenkins.getInstance()
					.getDescriptor(HOTPlayerSettings.class);

			// Project OpenStack to use
			ProjectOS projectOS = (ProjectOS) CollectionUtils.find(
					hPS.getProjects(), new Predicate() {
						public boolean evaluate(Object o) {
							return project.equals(((ProjectOS) o).getProject());
						}
					});

			// Test if the project is found
			if (projectOS != null) {

				// Client OpenStack inject during test or client failed
				if (clientOS == null || !clientOS.isConnectionOK()) {
					clientOS = new OpenStack4jClient(projectOS);
				}

				// Delete stack if it exists ?
				if (this.bundle.isExist()) {
					if (!StackOperationsUtils.deleteStack(
							eVU.getVar(bundle.getName()), clientOS, cLog,
							hPS.getTimersOS())) {
						return false;
					}
				}

				// Create stack
				if (!StackOperationsUtils.createStack(eVU, bundle, projectOS,
						clientOS, cLog, hPS.getTimersOS())) {
					return false;
				}

				// Push stack created to delete at the end if the task post
				// build is activated and stack created succeed
				JSONArray stacks = new JSONArray();
				if (StringUtils.isNotEmpty(eVU
						.getValue(Constants.STACKS_TO_DELETE))) {
					stacks = JSONArray.fromObject(eVU
							.getValue(Constants.STACKS_TO_DELETE));
				}
				stacks.add(new JSONObject().accumulate(Constants.PROJECT,
						projectOS.toJSON()).accumulate(Constants.STACKNAME,
						eVU.getVar(bundle.getName())));
				eVU.setVar(Constants.STACKS_TO_DELETE, stacks.toString());

			} else {
				cLog.logError(Messages.project_notFound(project));
				return false;
			}

		} catch (Exception e) {
			cLog.logError(Messages.processing_failed(bundle.getName())
					+ ExceptionUtils.getStackTrace(e));
			return false;
		}
		return true;
	}

	@Extension
	public static class DescriptorImpl extends BuildStepDescriptor<Builder> {

		private HOTPlayerSettings hotPlayerSettings;

		public DescriptorImpl() {
		}

		@Inject
		public DescriptorImpl(HOTPlayerSettings hotPlayerSettings) {
			this.hotPlayerSettings = hotPlayerSettings;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			try {
				return hotPlayerSettings.checkData();
			} catch (hudson.model.Descriptor.FormException e) {
				return false;
			}
		}

		@Override
		public String getDisplayName() {
			return "Heat Orchestration Template (HOT) player";
		}

		/**
		 * @return the list of template orchestration Heat
		 */
		public String[] getHotItems() {
			return hotPlayerSettings.getLoader().getHots();
		}

		/**
		 * @return the list of env files
		 */
		public String[] getEnvItems() {
			return hotPlayerSettings.getLoader().getEnvs();
		}

		/**
		 * @return the default env filename
		 */
		public String getDefaultEnvFileName() {
			return hotPlayerSettings.getLoader().getDefaultEnvFileName();
		}

		/**
		 * @return the list project
		 */
		public List<ProjectOS> getProjects() {
			return hotPlayerSettings.getProjects();
		}

		/**
		 * Method to interact between the server and the javascript code
		 * 
		 * @param hotName
		 *            hot selected
		 * @return the bundle with the informations in JSONObject format
		 */
		@JavaScriptMethod
		public JSONObject getBundle(String hotName) {
			String body = hotPlayerSettings.getLoader().getHot(hotName);
			if (Strings.isNullOrEmpty(body)) {
				return null;
			} else {
				return JSONObject.fromObject(HOTMapperUtils.getBundleFromHOT(
						hotName, body));
			}
		}

		/**
		 * Method to interact between the server and the javascript code
		 * 
		 * @param envName
		 *            envName selected
		 * @return the json contents of the envFile
		 */
		@JavaScriptMethod
		public JSONObject getEnv(String envName) {
			return JSONObject.fromObject(EnvMapperUtils
					.getEnv(hotPlayerSettings.getLoader().getEnv(envName)));
		}

		@Override
		public HOTPlayer newInstance(StaplerRequest req, JSONObject formData)
				throws hudson.model.Descriptor.FormException {
			// Creation and transformation from JSON to Object JAVA
			String data = formData.getString(Constants.BUNDLE);
			if (Strings.isNullOrEmpty(data)) {
				throw new FormException(
						Messages.bundle_configurationException(),
						"ConfigurationException");
			}
			Bundle bundle = BundleMapperUtils.getBundleFromJson(data);
			if (!bundle.getParameters().isEmpty()) {
				ParameterUtils.checkContraints(bundle.getParameters());
			}
			if (formData.containsKey(Constants.ENV_NAME)
					&& !Strings.isNullOrEmpty(formData
							.getString(Constants.ENV_NAME))) {
				bundle.setEnvName(formData.getString(Constants.ENV_NAME));
			}
			return new HOTPlayer(formData.getString(Constants.PROJECT), bundle);
		}
	}

}
