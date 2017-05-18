package com.arkea.jenkins.openstack.heat.build;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import hudson.model.Result;

import org.junit.Test;

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
public class DeleteCreateTest extends AbstractStackTest {

	@Test
	public void testCreate() {
		assertEquals(
				true,
				testPerform("deleteCreateDebug", "delete_create_complete",
						Result.SUCCESS, true, true,
						Arrays.asList("instance_ip", "192.168.1.19"),
						"delete_create_complete.json"));
	}

}
