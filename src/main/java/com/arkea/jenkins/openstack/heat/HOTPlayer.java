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
import net.sf.json.JSONObject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import com.arkea.jenkins.openstack.Constants;
import com.arkea.jenkins.openstack.client.OpenStack4jClient;
import com.arkea.jenkins.openstack.exception.utils.ExceptionUtils;
import com.arkea.jenkins.openstack.heat.configuration.ProjectOS;
import com.arkea.jenkins.openstack.heat.i18n.Messages;
import com.arkea.jenkins.openstack.heat.orchestration.template.utils.HOTMapperUtils;
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

	private String project;
	private String hotName;
	private String envContent;
	private String envStackName;
	private boolean deleteExist;
	private boolean debug;
	private OpenStack4jClient clientOS;

	@DataBoundConstructor
	public HOTPlayer(String project, String hotName, String envContent, String envStackName, boolean deleteExist, boolean debug) {
		this.project = project;
		this.hotName = hotName;
		this.envContent = envContent;
		this.envStackName = envStackName;
		this.deleteExist = deleteExist;
		this.debug = debug;
	}

	public HOTPlayer(String project, String hotName, String envContent, String envStackName, boolean deleteExist,
			 boolean debug, OpenStack4jClient clientOS) {
		this.project = project;
		this.hotName = hotName;
		this.envContent = envContent;
		this.envStackName = envStackName;
		this.deleteExist = deleteExist;
		this.debug = debug;
		this.clientOS = clientOS;
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	public String getProject() {
		return project;
	}

	public String getHotName() {
		return hotName;
	}

	public String getEnvContent() {
		return envContent;
	}

	public String getEnvStackName() {
		return  envStackName;
	}

	public boolean isDeleteExist() {
		return deleteExist;
	}

	public boolean isDebug() {
		return debug;
	}

	private String getUniStackName() {
		return "stack_" + DigestUtils.sha1Hex(this.toString());
	}
	@SuppressWarnings("rawtypes")
	@Override
	public boolean perform(AbstractBuild build, Launcher launcher,
			BuildListener listener) {
		final String stackName = getUniStackName();
		// Specific logger with color
		ConsoleLogger cLog = new ConsoleLogger(listener.getLogger(),
				"HOT Player", debug);
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
				if (this.deleteExist) {
					if (!StackOperationsUtils.deleteStack(
							stackName, clientOS, cLog,
							hPS.getTimersOS())) {
						return false;
					}
				}

				// Create stack
				if (!StackOperationsUtils.createStack(eVU,
					HOTMapperUtils.getBundleFromHOT(
							stackName, envStackName, hotName, envContent, hPS.getLoader().getHot(hotName)),
						projectOS, clientOS, cLog, hPS.getTimersOS())) {
					return false;
				}
			} else {
				cLog.logError(Messages.project_notFound(project));
				return false;
			}

		} catch (Exception e) {
			cLog.logError(Messages.processing_failed(stackName)
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
		public String getParameters(String hotName) {
			String body = hotPlayerSettings.getLoader().getHot(hotName);
			if (Strings.isNullOrEmpty(body)) {
				return null;
			} else {
				return HOTMapperUtils.getParameters(body);
			}
		}


		@Override
		public HOTPlayer newInstance(StaplerRequest req, JSONObject formData)
				throws hudson.model.Descriptor.FormException {

			return new HOTPlayer(formData.getString(Constants.PROJECT),
					formData.getString(Constants.HOTNAME),
					formData.getString(Constants.ENV_CONTENT),
					formData.getString(Constants.ENV_STACKNAME),
					formData.getBoolean(Constants.DEL_EXIST),
					formData.getBoolean(Constants.DEBUG));
		}
	}

}
