package com.arkea.jenkins.openstack.heat.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.arkea.jenkins.openstack.AbstractTest;
import com.arkea.jenkins.openstack.ToolUtils;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
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
public class HOTPlayerSettingsMultiTenantsTest extends AbstractTest {

	@Test
	public void testMultiTenants() throws Exception {

		HtmlPage configPage = j.createWebClient().goTo("configure");
		HtmlForm form = configPage.getFormByName("config");

		ToolUtils.getButton(form, "Add a new Project(Tenant) configuration")
				.click();
		List<HtmlInput> projects = form.getInputsByName("_.project");
		projects.get(1).setValueAttribute("projectTest2");
		List<HtmlInput> urls = form.getInputsByName("_.url");
		urls.get(1).setValueAttribute("http://openstack2.com");
		List<HtmlInput> v3s = form.getInputsByName("v3");
		v3s.get(1).click();
		List<HtmlInput> domains = form.getInputsByName("_.domain");
		domains.get(1).setValueAttribute("default2");
		List<HtmlInput> users = form.getInputsByName("_.user");
		users.get(1).setValueAttribute("test2");
		List<HtmlInput> passwords = form.getInputsByName("_.password");
		passwords.get(1).setValueAttribute("******");

		j.submit(form);

		form = configPage.getFormByName("config");
		projects = form.getInputsByName("_.project");
		urls = form.getInputsByName("_.url");
		v3s = form.getInputsByName("v3");
		domains = form.getInputsByName("_.domain");
		users = form.getInputsByName("_.user");
		passwords = form.getInputsByName("_.password");
		List<HtmlInput> regions = form.getInputsByName("_.region");
		assertEquals("projectTest", projects.get(0).getValueAttribute());
		assertEquals("projectTest2", projects.get(1).getValueAttribute());
		assertEquals("http://openstack.com", urls.get(0).getValueAttribute());
		assertEquals("http://openstack2.com", urls.get(1).getValueAttribute());
		assertEquals("on", v3s.get(0).getValueAttribute());
		assertEquals("on", v3s.get(1).getValueAttribute());
		assertEquals("default", domains.get(0).getValueAttribute());
		assertEquals("default2", domains.get(1).getValueAttribute());
		assertEquals("test", users.get(0).getValueAttribute());
		assertEquals("test2", users.get(1).getValueAttribute());
		assertTrue("Password is not null ?",
				!Strings.isNullOrEmpty(passwords.get(0).getValueAttribute()));
		assertTrue("Password is not null ?",
				!Strings.isNullOrEmpty(passwords.get(1).getValueAttribute()));
		assertTrue("Region is not null ?",
				!Strings.isNullOrEmpty(regions.get(0).getValueAttribute()));
		assertTrue("Region is null ?",
				Strings.isNullOrEmpty(regions.get(1).getValueAttribute()));

	}
}