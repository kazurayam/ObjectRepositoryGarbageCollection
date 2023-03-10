package com.kazurayam.ks.testobject

import static org.junit.Assert.*

import org.junit.Before
import org.junit.FixMethodOrder;
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.runners.MethodSorters;

import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.Files

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(JUnit4.class)

public class ExtendedObjectRepositoryTest {

	private ExtendedObjectRepository instance

	@Before
	void setup() {
		Path objectRepositoryDir = Paths.get(".").resolve("Object Repository")
		instance = new ExtendedObjectRepository(objectRepositoryDir)
	}

	@Test
	void test_listTestObjectId_default() {
		String json = instance.listTestObjectId()
		println '********** test_listTestObjectId_default **********'
		println json
		//
		List list = instance.listTestObjectIdRaw()
		assertTrue( list.size() > 0 )
		assertTrue( json.contains("a_Make Appointment"))
	}

	@Test
	void test_listTestObjectId_byString() {
		String pattern = "button_"
		String json = instance.listTestObjectId(pattern, false)
		println '********** test_listTestObjectId_byString **********'
		println json
		List list = instance.listTestObjectIdRaw(pattern, true)
		assertTrue( list.size() > 0 )
		assertTrue(json.contains("button_Login"))
	}

	@Test
	void test_listTestObjectId_byRegex() {
		String pattern = "button_(\\w+)"
		String json = instance.listTestObjectId(pattern, true)
		println '********** test_listTestObjectId_byRegex **********'
		println json
		//
		List list = instance.listTestObjectIdRaw(pattern, true)
		assertTrue( list.size() > 0 )
		assertTrue(json.contains("button_Login"))
	}

	@Test
	void test_listGist_default() {
		String pattern = ""
		Boolean isRegex = false
		String json = instance.listGist(pattern, isRegex)
		println "********** test_listGist_default *********"
		println json
		List<Map<String, String>> result =
				instance.listGistRaw(pattern, isRegex)
		assertTrue( result.size() > 0 )
		assertTrue("json should contain 'a_Make Appointment'", json.contains("a_Make Appointment"))
	}

	@Test
	void test_listGistRaw_arg_string() {
		String pattern = "button_"
		Boolean isRegex = false
		String json = instance.listGistRaw(pattern, isRegex)
		println "********** test_listGistRaw_arg_string *********"
		println json
		List<Map<String, String>> result =
				instance.listGistRaw(pattern, isRegex)
		assertTrue( result.size() > 0 )
		assertTrue(json.contains("button_Login"))
	}

	@Test
	void test_getAllTestObjectIds() {
		Set<TestObjectId> allTOI = instance.getAllTestObjectIds()
		println "********** test_getAllTestObjectIds **********"
		allTOI.forEach({ toi ->
			println toi
			assertNotNull(toi.value())
			assertNotEquals("", toi.value())
		})
	}
	
	@Test
	void test_reverseLookupRaw() {
		Map<Locator, Set<TestObjectGist>> result = instance.reverseLookupRaw()
		// TODO
	}
	
	@Test
	void test_reverseLookup() {
		String json = instance.reverseLookup()
		println "********** test_reverseLookup **********"
		println json
	}
}
