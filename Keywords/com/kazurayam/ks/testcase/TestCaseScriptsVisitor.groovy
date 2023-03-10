package com.kazurayam.ks.testcase

import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

/**
 * This class visits the <projectDir>/Scripts directory recursively 
 * to make a list of Paths of *.groovy files, which is Katalon's Test Case scripts
 * 
 * @author kazurayam
 */
public class TestCaseScriptsVisitor extends SimpleFileVisitor<Path> {

	private Path scriptsDir
	private List<Path> groovyFiles

	TestCaseScriptsVisitor(Path scriptsDir) {
		Objects.requireNonNull(scriptsDir)
		assert Files.exists(scriptsDir)
		this.scriptsDir = scriptsDir.toAbsolutePath().normalize()
		groovyFiles = new ArrayList<Path>()
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
		if ( !Files.isDirectory(file) && file.getFileName().toString().endsWith(".groovy")) {
			groovyFiles.add(file.toAbsolutePath().normalize())
		}
		return FileVisitResult.CONTINUE;
	}

	Path getScriptsDir() {
		return scriptsDir
	}

	List<Path> getGroovyFiles() {
		List<Path> result = new ArrayList()
		groovyFiles.forEach { p ->
			result.add(p)
		}
		Collections.sort(result)
		return result
	}

	List<TestCaseId> getTestCaseIdList() {
		List<TestCaseId> list = new ArrayList<>()
		getGroovyFiles().forEach ({ groovyFile ->
			TestCaseId id = new TestCaseId(scriptsDir, groovyFile)
			list.add(id)
		})
		return list
	}
}
