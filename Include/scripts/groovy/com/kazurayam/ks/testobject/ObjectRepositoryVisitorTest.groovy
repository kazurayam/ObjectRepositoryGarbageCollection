package com.kazurayam.ks.testobject

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import com.kazurayam.ks.testobject.ObjectRepositoryVisitor

import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.Files

@RunWith(JUnit4.class)
class ObjectRepositoryVisitorTest {

	private static final Path objectRepository = Paths.get("./Object Repository")
	private ObjectRepositoryVisitor visitor

	@Test
	void testGetRsFiles() throws IOException {
		visitor = new ObjectRepositoryVisitor(objectRepository)
		Files.walkFileTree(objectRepository, visitor)
		List<Path> list = visitor.getRsFiles()
		assertTrue(list.size() > 0)
		/*
		 list.forEach { p ->
		 println p
		 }
		 */
	}

	@Test
	void testGetTestObjects() {
		visitor = new ObjectRepositoryVisitor(objectRepository)
		Files.walkFileTree(objectRepository, visitor)
		List<String> list = visitor.getTestObjectIdList()
		assertTrue(list.size() > 0)
		list.forEach { p ->
			println p
		}
	}
}