package com.arkea.jenkins.openstack.heat.build;

import hudson.model.FreeStyleBuild;
import hudson.model.Result;
import hudson.model.FreeStyleProject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jenkins.model.Jenkins;

import org.apache.commons.io.FileUtils;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.openstack4j.model.heat.Stack;
import org.openstack4j.openstack.heat.domain.HeatStack;

import com.arkea.jenkins.openstack.AbstractTest;
import com.arkea.jenkins.openstack.client.OpenStack4jClient;
import com.arkea.jenkins.openstack.heat.HOTClean;
import com.arkea.jenkins.openstack.heat.HOTPlayer;
import com.arkea.jenkins.openstack.heat.HOTPlayerSettings;
import com.arkea.jenkins.openstack.heat.orchestration.template.Bundle;
import com.arkea.jenkins.openstack.heat.orchestration.template.Parameter;
import com.arkea.jenkins.openstack.heat.orchestration.template.Type;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractStackTest extends AbstractTest {

	protected boolean testPerform(String taskName, String stackName,
			Result result, boolean delete, boolean debug,
			List<String> testLogDebug, String jsonFileTest, boolean testClean) {

		try {

			OpenStack4jClient clientOS = Mockito.mock(OpenStack4jClient.class);

			// Configure the project and the hot file
			String pathHot = getClass().getResource("/hot/demo-template.yaml")
					.getPath();

			ArrayList<HeatStack> stacks = (new ObjectMapper()).readValue(
					new File(getClass().getResource("/" + jsonFileTest)
							.getPath()),
					new TypeReference<ArrayList<HeatStack>>() {
					});

			// Create the bundle to test
			Bundle bundle = new Bundle("demo-template.yaml", stackName, delete,
					debug);
			Map<String, Parameter> params = new HashMap<String, Parameter>();
			params.put("NetID", new Parameter("NetID", Type.String, "", "", "",
					false, "NetID"));
			bundle.setParameters(params);

			AnswerStack answerStack = new AnswerStack(stacks);

			// Mock the stack
			Mockito.when(clientOS.getStackByName(Mockito.anyString()))
					.thenAnswer(answerStack);

			Mockito.when(
					clientOS.getDetails(Mockito.anyString(),
							Mockito.anyString())).thenAnswer(answerStack);

			// Create Jenkins project
			FreeStyleProject project = j.createFreeStyleProject(taskName);

			// Global configuration
			HOTPlayerSettings hPS = (HOTPlayerSettings) Jenkins.getInstance()
					.getDescriptor(HOTPlayerSettings.class);

			// Mock isConnectionOK
			Mockito.when(clientOS.isConnectionOK()).thenReturn(true);

			// Mock the create stack
			Mockito.when(
					clientOS.createStack(bundle.getName(), pathHot, bundle
							.getParamsOS(), null, hPS.getTimersOS()
							.getTimeoutProcessInMin())).thenAnswer(answerStack);

			if (testClean) {
				// Mock the delete stack
				Mockito.doNothing().when(clientOS)
						.deleteStack(Mockito.any(Stack.class));
			}

			// Create task HotPlayer
			HOTPlayer hotPlayer = new HOTPlayer("projectTest", bundle, clientOS);
			project.getBuildersList().add(hotPlayer);
			if (testClean) {
				project.getPublishersList().add(new HOTClean(clientOS));
			}
			project.save();

			// Execute the test
			FreeStyleBuild build = project.scheduleBuild2(0).get();
			if (debug) {
				String s = FileUtils.readFileToString(build.getLogFile());
				for (String test : testLogDebug) {
					if (!s.contains(test)) {
						return false;
					}
				}
			}

			if (result.equals(build.getResult())) {
				return true;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	protected boolean testPerform(String taskName, String stackName,
			Result result, boolean delete, boolean debug,
			List<String> testLogDebug, String jsonFileTest) {

		return testPerform(taskName, stackName, result, delete, debug,
				testLogDebug, jsonFileTest, false);

	}

}
