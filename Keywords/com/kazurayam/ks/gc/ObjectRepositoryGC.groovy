package com.kazurayam.ks.gc

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import com.kazurayam.ks.gc.TCTOReference
import com.kazurayam.ks.testcase.ScriptsSearcher
import com.kazurayam.ks.testcase.TestCaseId
import com.kazurayam.ks.testcase.TestCaseScriptsVisitor
import com.kazurayam.ks.testcase.TextSearchResult
import com.kazurayam.ks.testobject.ExtendedObjectRepository
import com.kazurayam.ks.testobject.TestObjectGist
import com.kazurayam.ks.testobject.TestObjectId
import groovy.json.JsonOutput
import com.kms.katalon.core.configuration.RunConfiguration


/**
 * A sort of "Garbage Collector" for the "Object Repository" of Katalon Studio.
 * It reports list of Test Objects with metadata which Test Case scripts use the Test Object. 
 * This class can report a list of "unused" Test Objects, which I call "garbages".
 * 
 * This class just compiles a report. It does not actually remove or change Test Objects at all.
 * 
 * @author kazurayam
 */
class ObjectRepositoryGC {

	private static final projectDir = Paths.get(".")

	private Path   objrepoDir // non null
	private Path   scriptsDir // non null
	private String objrepoSubpath // can be null
	private String scriptsSubpath // can be null

	private Database db
	private Set<TestObjectId> allTestObjectIds

	private ObjectRepositoryGC(Builder builder) {
		this.objrepoDir = builder.objrepoDir.toAbsolutePath().normalize()
		this.scriptsDir = builder.scriptsDir.toAbsolutePath().normalize()
		this.objrepoSubpath = builder.objrepoSubpath
		this.scriptsSubpath = builder.scriptsSubpath
		this.scan()
	}

	/*
	 * This method will scan the "Object Repository" folder and the "Scripts" folder.
	 * to create an instance of Database internally and fill it with information found 
	 * out of the directories.
	 * You can retrieve the Database by calling "db()" method.
	 * You can retrieve an Garbage Collection plan by calling "xref()" method, in which you can
	 * find a list of "garbage" Test Objects which are not used by any of the Test Cases.
	 */
	public void scan() {

		this.db = new Database()

		// scan the Object Repository directory to make a list of TestObjectGists
		ExtendedObjectRepository extOR = new ExtendedObjectRepository(objrepoDir, objrepoSubpath)
		allTestObjectIds = extOR.getAllTestObjectIds()
		List<TestObjectGist> gistList = extOR.listGistRaw("", false)

		// scan the Scripts directory to make a list of TestCaseIds
		TestCaseScriptsVisitor testCaseScriptsVisitor = new TestCaseScriptsVisitor(scriptsDir)
		Path targetDir = (scriptsSubpath != null) ? scriptsDir.resolve(scriptsSubpath) : scriptsDir
		Files.walkFileTree(targetDir, testCaseScriptsVisitor)
		List<TestCaseId> testCaseIdList = testCaseScriptsVisitor.getTestCaseIdList()

		// Iterate over the list of TestCaseIds.
		// Read the TestCase script, check if it contains any references to the TestObjects.
		// If true, record the reference into the database
		ScriptsSearcher scriptSearcher = new ScriptsSearcher(scriptsDir, scriptsSubpath)
		testCaseIdList.forEach { testCaseId ->
			gistList.forEach { gist ->
				TestObjectId testObjectId = gist.testObjectId()
				List<TextSearchResult> textSearchResultList =
						scriptSearcher.searchIn(testCaseId, testObjectId.value(), false)
				textSearchResultList.forEach { textSearchResult ->
					TCTOReference reference = new TCTOReference(testCaseId, textSearchResult, gist)
					db.add(reference)
				}
			}
		}

	}

	Database db() {
		return db
	}


	/**
	 * 
	 */
	Map<TestObjectId, Set<TCTOReference>> resolveRaw() {
		Set<TestObjectId> allTestObjectIds = db.getAllTestObjectIdsContained()
		Map<TestObjectId, Set<TCTOReference>> result = new TreeMap<>()
		allTestObjectIds.forEach { testObjectId ->
			result.put(testObjectId, db.findTCTOReferencesOf(testObjectId))
		}
		return result
	}

	/**
	 *
	 */
	String resolve() {
		Map<TestObjectId, Set<TCTOReference>> resolved = this.resolveRaw()
		StringBuilder sb = new StringBuilder()
		sb.append("{")
		sb.append(JsonOutput.toJson("ObjectRepositoryGC#resolve"))
		sb.append(":")
		sb.append("[")
		String sep1 = ""
		resolved.keySet().forEach { testObjectId ->
			sb.append(sep1)
			sb.append("{")
			sb.append(JsonOutput.toJson("TestObjectId"))
			sb.append(":")
			sb.append(JsonOutput.toJson(testObjectId.value()))
			sb.append(",")
			sb.append(JsonOutput.toJson("TCTOReferences"))
			sb.append(":")
			sb.append("[")
			Set<TCTOReference> refs = resolved.get(testObjectId)
			String sep2 = ""
			refs.forEach { ref ->
				sb.append(sep2)
				sb.append(ref.toJson())
				sep2 = ","
			}
			sb.append("]")
			sb.append("}")
			sep1 = ","
		}
		sb.append("]")
		sb.append("}")
		return JsonOutput.prettyPrint(sb.toString())
	}

	/**
	 * 
	 */
	List<TestObjectId> garbagesRaw() {
		Set<TestObjectId> tmp = new TreeSet<>()
		Map<TestObjectId, Set<TCTOReference>> resolved = this.resolveRaw()
		// allTestObjectIds are set in the init() method
		allTestObjectIds.forEach { testObjectId ->
			Set<TCTOReference> value = resolved.get(testObjectId)
			if (value == null) {
				// Oh, this TestObject must be a garbage
				// as no Test Case uses this
				tmp.add(testObjectId)
			}
		}
		List<TestObjectId> result = new ArrayList<>()
		result.addAll(tmp)
		Collections.sort(result)
		return result
	}

	String garbages() {
		List<TestObjectId> garbages = garbagesRaw()
		StringBuilder sb = new StringBuilder()
		sb.append("{")
		sb.append(JsonOutput.toJson("ObjectRepositoryGC#garbages"))
		sb.append(":")
		sb.append("[")
		String sep = ""
		garbages.forEach { toi ->
			sb.append(sep)
			sb.append(toi.toJson())
			sep = ","
		}
		sb.append("]")
		sb.append("}")
		return JsonOutput.prettyPrint(sb.toString())
	}

	/**
	 * Joshua Bloch's Builder pattern in Effective Java
	 * 
	 * @author kazuarayam
	 */
	public static class Builder {

		private Path   objrepoDir // non null
		private Path   scriptsDir // non null

		private String objrepoSubpath // can be null
		private String scriptsSubpath // can be null

		Builder() {
			Path projectDir = Paths.get(RunConfiguration.getProjectDir())
			Path objrepoDir = projectDir.resolve("Object Repository")
			Path scriptsDir = projectDir.resolve("Scripts")
			init(objrepoDir, scriptsDir)
			
		}
		
		Builder(Path objrepoDir, Path scriptsDir) {
			init(objrepoDir, scriptsDir)
		}
		
		Builder(File objrepoDir, File scriptsDir) {
			init(objrepoDir.toPath(), scriptsDir.toPath())
		}
		
		private void init(Path objrepoDir, Path scriptsDir) {
			Objects.requireNonNull(objrepoDir)
			Objects.requireNonNull(scriptsDir)
			assert Files.exists(objrepoDir)
			assert Files.exists(scriptsDir)
			this.objrepoDir = objrepoDir
			this.scriptsDir = scriptsDir
		}

		Builder objrepoSubpath(String subpath) {
			Objects.requireNonNull(subpath)
			Path p = objrepoDir.resolve(subpath)
			assert Files.exists(p)
			this.objrepoSubpath = subpath
			return this
		}

		Builder scriptsSubpath(String subpath) {
			Objects.requireNonNull(subpath)
			Path p = scriptsDir.resolve(subpath)
			assert Files.exists(p)
			this.scriptsSubpath = subpath
			return this
		}

		ObjectRepositoryGC build() {
			return new ObjectRepositoryGC(this)
		}
	}

}
