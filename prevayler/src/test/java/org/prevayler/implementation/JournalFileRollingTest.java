//Prevayler(TM) - The Free-Software Prevalence Layer.
//Copyright (C) 2001-2004 Klaus Wuestefeld
//This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//Contributions: Justin Sampson.

package org.prevayler.implementation;

import java.io.IOException;
import java.util.Collections;
import java.util.Arrays;

import org.prevayler.Prevayler;
import org.prevayler.PrevaylerFactory;
import org.prevayler.foundation.*;

public class JournalFileRollingTest extends FileIOTest {

	private Prevayler _prevayler;

	public void testFileRolling() throws Exception {

		crashRecover(""); //There is nothing to recover at first. A new system will be created.

		append("a","a");
		crashRecover("a");

		append("b","ab"); //Starts new journal (2).
		append("c","abc");
		append("d","abcd"); //Starts new journal (4).
		append("e","abcde");
		append("f","abcdef");  //Starts new journal (6).
		crashRecover("abcdef");
		
		append("g","abcdefg"); //Starts new journal (7).
		snapshot();
		append("h","abcdefgh");
		append("i","abcdefghi");  //Starts new journal (9).
		append("j","abcdefghij");
		crashRecover("abcdefghij");

		_prevayler.close();
		deleteFromTestDirectory("0000000000000000001.journal");
		deleteFromTestDirectory("0000000000000000002.journal");
		deleteFromTestDirectory("0000000000000000004.journal");
		deleteFromTestDirectory("0000000000000000006.journal");
		deleteFromTestDirectory("0000000000000000007.journal");
		deleteFromTestDirectory("0000000000000000007.snapshot");
		deleteFromTestDirectory("0000000000000000009.journal");
		assertEquals(Collections.EMPTY_LIST,
				Arrays.asList(new java.io.File(_testDirectory).list()));
	}

	private void crashRecover(String expectedResult) throws Exception {
		out("CrashRecovery.");
		if (_prevayler != null) _prevayler.close();
		PrevaylerFactory factory = new PrevaylerFactory();
		factory.configureJournalFileSizeThreshold(232);  //Enough to hold 2 transactions.
		factory.configureJournalFileAgeThreshold(0);  //Not being tested.
		factory.configurePrevalenceDirectory(_testDirectory);
		factory.configurePrevalentSystem(new AppendingSystem());
		_prevayler = factory.create();
		verify(expectedResult);
	}

	private void snapshot() throws IOException {
		out("Snapshot.");
		_prevayler.takeSnapshot();
	}


	private void append(String appendix, String expectedResult) throws Exception {
		out("Appending " + appendix);
		_prevayler.execute(new Appendix(appendix));
		verify(expectedResult);
	}


	private void verify(String expectedResult) {
		out("Expecting result: " + expectedResult);
		assertEquals(expectedResult, system().value());
	}


	private AppendingSystem system() {
		return (AppendingSystem)_prevayler.prevalentSystem();
	}

	private static void out(Object obj) {
		if (false) System.out.println(obj);   //Change this line to see what the test is doing.
	}

}