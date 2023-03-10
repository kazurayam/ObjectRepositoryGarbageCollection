import com.kazurayam.ks.testobject.ObjectRepositoryExtension
import com.kms.katalon.core.testobject.ObjectRepository

/*
 * Test Caes/sampleUse/step4
 */

// modify com.kms.katalon.core.testobject.ObjectRepository class on the fly
ObjectRepositoryExtension.apply()

// step4: reverse Test Object Lookup by locator, full result
Map<String, Set<String>> result4 = ObjectRepository.reverseLookupRaw()

println "\n---------------- reverseLookup, full ------------------------"
result4.keySet().forEach { locator ->
	println "${locator}"
	Set<String> ids = result4.get(locator)
	ids.forEach { id -> println "\t${id}" }
	println ""
}
