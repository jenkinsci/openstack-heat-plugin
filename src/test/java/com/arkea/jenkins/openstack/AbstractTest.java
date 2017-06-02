package com.arkea.jenkins.openstack;

import org.junit.Before;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

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
public abstract class AbstractTest {

	@Rule
	public JenkinsRule j = new JenkinsRule();

	@Before
	public void init() throws Exception {

		String pathHot = getClass().getResource("/hot/demo-template.yaml")
				.getPath();
		String pathEnv = getClass().getResource("/env/default.yaml").getPath();

		HtmlPage configPage = j.createWebClient().goTo("configure");
		HtmlForm form = configPage.getFormByName("config");

		ToolUtils.getButton(form, "Add a new Project(Tenant) configuration")
				.click();
		form.getInputByName("_.project").setValueAttribute("projectTest");
		form.getInputByName("_.url").setValueAttribute("http://openstack.com");
		form.getInputByName("v3").click();
		form.getInputByName("_.domain").setValueAttribute("default");
		form.getInputByName("_.user").setValueAttribute("test");
		form.getInputByName("_.password").setValueAttribute("******");
		form.getInputByName("_.region").setValueAttribute("region");

		form.getInputByName("_.pollingStatus").setValueAttribute("1");
		form.getInputByName("_.timeoutProcess").setValueAttribute("600");

		form.getInputByName("_.pathHot").setValueAttribute(
				pathHot.substring(0, pathHot.lastIndexOf("/")));
		form.getInputByName("_.extHot").setValueAttribute("yaml");

		form.getInputByName("fromDirEnv").click();
		form.getInputByName("_.pathEnv").setValueAttribute(
				pathEnv.substring(0, pathEnv.lastIndexOf("/")));
		form.getInputByName("_.extEnv").setValueAttribute("yaml");
		form.getInputByName("_.defaultEnv").setValueAttribute("default.yaml");

		j.submit(form);
	}

}
