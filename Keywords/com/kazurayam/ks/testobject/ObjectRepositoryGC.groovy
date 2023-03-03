package com.kazurayam.ks.testobject

import java.nio.file.Path
import java.nio.file.Paths

class ObjectRepositoryGC {

	private static final projectDir = Paths.get(".")	
	
	private Path objectRepositoryDir
	private Path scriptsDir
	
	ObjectRepositoryGC() {
		this(projectDir.resolve("Object Repository"), projectDir.resolve("Scripts"))
	}	
	
	ObjectRepositoryGC(Path objectRepositoryDir, Path scriptsDir) {
		this.objectRepositoryDir = objectRepositoryDir
		this.scriptsDir = scriptsDir
	}
	
	void dryrun() {
		throw new RuntimeException("TODO")	
	}
	
	
}
