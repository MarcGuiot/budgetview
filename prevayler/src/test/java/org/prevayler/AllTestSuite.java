//Prevayler(TM) - The Free-Software Prevalence Layer.
//Copyright (C) 2001-2003 Klaus Wuestefeld
//This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

//Testing CVS

package org.prevayler;

import junit.framework.TestSuite;
import junit.framework.Test;

public class AllTestSuite {
	public static Test _suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(org.prevayler.implementation.Suite._suite());
		suite.addTest(org.prevayler.foundation.Suite._suite());
		return suite;
	}
}
