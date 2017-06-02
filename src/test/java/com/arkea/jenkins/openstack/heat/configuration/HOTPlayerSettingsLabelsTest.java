package com.arkea.jenkins.openstack.heat.configuration;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arkea.jenkins.openstack.AbstractTest;
import com.arkea.jenkins.openstack.ToolUtils;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;

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
 *         Class test the differents inputs in the global configuration
 *         for this plugin
 */
public class HOTPlayerSettingsLabelsTest extends AbstractTest {
	
	@Test
	public void testPresent() throws Exception {

		HtmlForm form = j.createWebClient().goTo("configure")
				.getFormByName("config");
		String html = form.asText();

		// Test rubric
		assertTrue(
				"Heat Orchestration Template (HOT) Loader Settings is present ?",
				html.contains("Heat Orchestration Template (HOT) Loader Settings"));
		assertTrue("OpenStack Configuration is present ?",
				html.contains("OpenStack Configuration"));
		assertTrue("OpenStack Time Configuration is present ?",
				html.contains("OpenStack Time Configuration"));

		// Test Heat Orchestration Template (HOT) Loader Settings
		assertTrue("Choose Loader is present ?",
				html.contains("Choose Loader :"));

		// Test LoaderFromDir
		testLoaderFromDir(html);

		// Test LoaderHttpREST
		HtmlSelect selectLoader = ToolUtils.getSelect(form, "0",
				"LoaderFromDir");
		selectLoader.getOptions().get(1).setSelected(true);
		
		// Time to change the page
		Thread.sleep(1000);
				
		form.getInputByName("httpRESTEnv").click();
		
		// Time to change the page
		Thread.sleep(1000);
				
		html = form.asText();
		testLoaderHttpREST(html);

		// Test OpenStack Configuration
		assertTrue("Add a new Project(Tenant) configuration is present ?",
				html.contains("Add a new Project(Tenant) configuration"));
		assertTrue("Project(Tenant) OpenStack is present ?",
				html.contains("Project(Tenant) OpenStack"));
		assertTrue("URL Identity OpenStack is present ?",
				html.contains("URL Identity OpenStack"));
		assertTrue("Identity V3 is present ?",
				html.contains("Identity V3"));
		assertTrue("Domain is present ?",
				html.contains("Domain"));
		assertTrue("User OpenStack is present ?",
				html.contains("User OpenStack"));
		assertTrue("Password OpenStack is present ?",
				html.contains("Password OpenStack"));
		assertTrue("Region OpenStack is present ?",
				html.contains("Region (optional)"));

		// Test OpenStack Time Configuration
		assertTrue("Polling Status OpenStack (s) is present ?",
				html.contains("Polling Status OpenStack (s)"));
		assertTrue("Timeout Operation OpenStack (s) is present ?",
				html.contains("Timeout Operation OpenStack (s)"));

	}
	
	private void testLoaderFromDir(String html) {

		// LoaderFromDir
		assertTrue("Path to the directory Heat Template is present ?",
				html.contains("Path to the directory Heat Template"));
		assertTrue("Extension for the Heat Template File is present ?",
				html.contains("Extension for the Heat Template File"));
		assertTrue("Use optional path to the directory Env File	is present ?",
				html.contains("Use optional path to the directory Env File"));
		assertTrue("Path to the directory Env is present ?",
				html.contains("Path to the directory Env"));
		assertTrue("Extension for the Env File is present ?",
				html.contains("Extension for the Env File"));
		assertTrue("Default Env filename is present ?",
				html.contains("Default Env filename"));
	}

	private void testLoaderHttpREST(String html) {

		// LoaderHttpREST
		assertTrue("Url to access to the Hots API Rest is present ?",
				html.contains("Url to access to the Hot API Rest"));
		assertTrue("Use optional url to Env API Rest is present ?",
				html.contains("Use optional url to Env API Rest"));
		assertTrue("Url to access to the Env API Rest is present ?",
				html.contains("Url to access to the Env API Rest"));
		assertTrue("Default Env filename is present ?",
				html.contains("Default Env filename"));
	}


}
