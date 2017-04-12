package com.arkea.jenkins.openstack.client;

import java.util.List;
import java.util.Map;

import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.heat.HeatService;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.heat.Event;
import org.openstack4j.model.heat.Stack;
import org.openstack4j.model.heat.builder.StackCreateBuilder;
import org.openstack4j.openstack.OSFactory;
import org.openstack4j.openstack.heat.domain.HeatStackCreate;

import com.arkea.jenkins.openstack.heat.configuration.ProjectOS;
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
 *         Class OpenStack Client to manage Stack
 *
 */
public class OpenStack4jClient {

	private HeatService heatService;

	public OpenStack4jClient(ProjectOS projectOS) {

		// If you need to debug the http call between the client openstack4j and
		// openstack, activate this line to see the transport on the current
		// console jenkins
		// System.getProperties()
		// .setProperty(
		// org.openstack4j.core.transport.internal.HttpLoggingFilter.class
		// .getName(), "true");

		OSClient<?> osClient = null;
		if (projectOS.isCheckV3()) {
			osClient = OSFactory
					.builderV3()
					.endpoint(projectOS.getUrl())
					.credentials(projectOS.getUser(),
							projectOS.getPassword().getPlainText(),
							Identifier.byName(projectOS.getDomain()))
					.scopeToProject(Identifier.byName(projectOS.getProject()),
							Identifier.byName(projectOS.getDomain()))
					.authenticate();
		} else {
			osClient = OSFactory
					.builderV2()
					.endpoint(projectOS.getUrl())
					.credentials(projectOS.getUser(),
							projectOS.getPassword().getPlainText())
					.tenantName(projectOS.getProject()).authenticate();
		}
		this.heatService = osClient.heat();

	}

	public List<? extends Stack> getStacks() {
		return this.heatService.stacks().list();
	}

	/**
	 * @param stackName
	 *            name of the stack
	 * @param fullName
	 *            full name to acces template
	 * @param params
	 *            parameters associated
	 * @param fullEnvFile
	 *            full name to acces env file (optional)
	 * @param timeout
	 *            timeout to abort the creation
	 * @return the stack created
	 */
	public Stack createStack(String stackName, String fullName,
			Map<String, String> params, String fullEnvFile, long timeout) {

		StackCreateBuilder builder = Builders.stack().name(stackName)
				.parameters(params).templateFromFile(fullName)
				.disableRollback(false).timeoutMins((long) timeout);

		if (!Strings.isNullOrEmpty(fullEnvFile)) {
			builder.environmentFromFile(fullEnvFile);
		}

		HeatStackCreate stack = (HeatStackCreate) builder.build();

		return getDetails(stackName, this.heatService.stacks().create(stack)
				.getId());
	}

	public Stack createStack(String stackName, String fullName,
			String tags,
			Map<String, String> params, String fullEnvFile, long timeout) {

		StackCreateBuilder builder = Builders.stack().name(stackName)
				.tags(tags)
				.parameters(params).templateFromFile(fullName)
				.disableRollback(false).timeoutMins((long) timeout);

		if (!Strings.isNullOrEmpty(fullEnvFile)) {
			builder.environmentFromFile(fullEnvFile);
		}

		HeatStackCreate stack = (HeatStackCreate) builder.build();

		return getDetails(stackName, this.heatService.stacks().create(stack)
				.getId());
	}
	
	
	public Stack getStackByName(String stackName) {
		return this.heatService.stacks().getStackByName(stackName);
	}

	public Stack getDetails(String stackName, String stackId) {
		return this.heatService.stacks().getDetails(stackName, stackId);
	}

	public void deleteStack(Stack stack) {
		this.heatService.stacks().delete(stack.getName(), stack.getId());
	}

	public boolean isConnectionOK() {
		boolean rtn = true;
		try {
			this.heatService.stacks().list();
		} catch (Exception e) {
			rtn = false;
		}
		return rtn;
	}

	public List<? extends Event> getEvents(Stack stack) {
		return this.heatService.events().list(stack.getName(), stack.getId());
	}
}