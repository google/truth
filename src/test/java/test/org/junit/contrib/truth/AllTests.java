package test.org.junit.contrib.truth;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({FirstTest.class, ExtensionTest.class, ExpectTest.class})
public class AllTests {
	
}
