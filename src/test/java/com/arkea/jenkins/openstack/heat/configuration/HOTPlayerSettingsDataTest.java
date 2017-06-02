package com.arkea.jenkins.openstack.heat.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import hudson.util.Secret;

import java.util.Arrays;

import net.sf.json.JSONObject;

import org.junit.Test;

import com.arkea.jenkins.openstack.AbstractTest;
import com.arkea.jenkins.openstack.heat.HOTPlayerSettings;
import com.arkea.jenkins.openstack.heat.loader.LoaderFromDir;
import com.arkea.jenkins.openstack.heat.loader.LoaderHttpREST;
import com.arkea.jenkins.openstack.heat.utils.CheckDataUtils;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
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
 *         Class test the differents inputs in the global configuration
 *         for this plugin
 */
public class HOTPlayerSettingsDataTest extends AbstractTest {

	@Test
	public void testCheckData() throws Exception {

		HOTPlayerSettings hPS = new HOTPlayerSettings();

		// Test LoaderFromDir setting
		CheckDataUtils.checkFormException(hPS, "loader");
		hPS.setLoader(new LoaderFromDir("", "", null));
		CheckDataUtils.checkFormException(hPS, "pathHot");
		hPS.setLoader(new LoaderFromDir("/test/path/hot", "", null));
		CheckDataUtils.checkFormException(hPS, "extHot");
		hPS.setLoader(new LoaderFromDir("/test/path/hot", "extHot", null));
		JSONObject fromDirEnv = new JSONObject();
		fromDirEnv.put("pathEnv", "");
		fromDirEnv.put("extEnv", "");
		fromDirEnv.put("defaultEnv", "");
		hPS.setLoader(new LoaderFromDir("/test/path/hot", "extHot", fromDirEnv));
		CheckDataUtils.checkFormException(hPS, "pathEnv");
		fromDirEnv.put("pathEnv", "/test/path/env");
		hPS.setLoader(new LoaderFromDir("/test/path/hot", "extHot", fromDirEnv));
		CheckDataUtils.checkFormException(hPS, "extEnv");
		fromDirEnv.put("extEnv", "yaml");
		
		// Test LoaderFromDir setting
		hPS.setLoader(null);
		CheckDataUtils.checkFormException(hPS, "loader");
		hPS.setLoader(new LoaderHttpREST("", null));
		CheckDataUtils.checkFormException(hPS, "urlHot");
		hPS.setLoader(new LoaderHttpREST("urlHot", null));
		JSONObject httpRESTEnv = new JSONObject();
		httpRESTEnv.put("urlEnv", "");
		httpRESTEnv.put("defaultEnv", "");
		hPS.setLoader(new LoaderHttpREST("urlHot", httpRESTEnv));
		CheckDataUtils.checkFormException(hPS, "urlEnv");
		httpRESTEnv.put("urlEnv", "urlEnv");
		hPS.setLoader(new LoaderHttpREST("urlHot", httpRESTEnv));
		
		// Test Project setting
		CheckDataUtils
				.checkFormException(hPS, "Project(Tenant) configurations");
		ProjectOS projectOS = new ProjectOS("", "", false, "", "", null, "");
		hPS.setProjects(Arrays.asList(projectOS));
		CheckDataUtils.checkFormException(hPS, "project");
		projectOS.setProject("project");
		hPS.setProjects(Arrays.asList(projectOS));
		CheckDataUtils.checkFormException(hPS, "url");
		projectOS.setUrl("url");
		projectOS.setCheckV3(true);
		CheckDataUtils.checkFormException(hPS, "domain");
		projectOS.setDomain("default");
		hPS.setProjects(Arrays.asList(projectOS));
		CheckDataUtils.checkFormException(hPS, "user");
		projectOS.setUser("user");
		hPS.setProjects(Arrays.asList(projectOS));
		CheckDataUtils.checkFormException(hPS, "password");
		projectOS.setPassword(Secret.fromString("password"));
		hPS.setProjects(Arrays.asList(projectOS));
		CheckDataUtils.checkFormException(hPS, "password");

		// Test timers setting
		hPS.setPollingStatus(-1);
		CheckDataUtils.checkFormException(hPS, "pollingStatus");
		hPS.setPollingStatus(20);
		hPS.setTimeoutProcess(-1);
		CheckDataUtils.checkFormException(hPS, "timeoutProcess");

	}

	@Test
	public void testConfigurable() throws Exception {

		String pathHot = getClass().getResource(
				"/hot/parameters-template.yaml").getPath();
		String pathEnv = getClass().getResource("/env/default.yaml")
				.getPath();

		HtmlForm form = j.createWebClient().goTo("configure")
				.getFormByName("config");

		assertEquals(pathHot.substring(0, pathHot.lastIndexOf("/")), form
				.getInputByName("_.pathHot").getValueAttribute());
		assertEquals("yaml", form.getInputByName("_.extHot")
				.getValueAttribute());
		assertEquals(pathEnv.substring(0, pathEnv.lastIndexOf("/")), form
				.getInputByName("_.pathEnv").getValueAttribute());
		assertEquals("yaml", form.getInputByName("_.extEnv")
				.getValueAttribute());
		assertEquals("default.yaml", form.getInputByName("_.defaultEnv")
				.getValueAttribute());

		assertEquals("projectTest", form.getInputByName("_.project")
				.getValueAttribute());
		assertEquals("http://openstack.com", form.getInputByName("_.url")
				.getValueAttribute());
		assertEquals("on", form.getInputByName("v3").getValueAttribute());
		assertEquals("default", form.getInputByName("_.domain")
				.getValueAttribute());
		assertEquals("test", form.getInputByName("_.user").getValueAttribute());
		assertTrue("Password is not null ?", !Strings.isNullOrEmpty(form
				.getInputByName("_.password").getValueAttribute()));
		assertEquals("region", form.getInputByName("_.region").getValueAttribute());
		
		assertEquals("1", form.getInputByName("_.pollingStatus")
				.getValueAttribute());
		assertEquals("600", form.getInputByName("_.timeoutProcess")
				.getValueAttribute());
	}

}
