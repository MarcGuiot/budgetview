package org.prevayler.implementation;

import java.io.File;

import org.prevayler.Prevayler;
import org.prevayler.PrevaylerFactory;
import org.prevayler.foundation.FileIOTest;

public class DeadKingDeepCopyTest extends FileIOTest {

	private Prevayler _prevayler;

	private String _prevalenceBase;

	public void tearDown() throws Exception {
		if (_prevayler != null) {
			_prevayler.close();
		}
		super.tearDown();
	}

	public void testDeadKing() throws Exception {
		newPrevalenceBase();
		crashRecover();

		append("a", "a");
		append("b", "ab");
		verify("ab");

		NondeterministicErrorTransaction.armBomb(2);
		try {
			_prevayler.execute(new NondeterministicErrorTransaction("c"));
			fail();
		} catch (Bomb expected) {
			assertEquals("BOOM!", expected.getMessage());
		}

		try {
			_prevayler.execute(new Appendix("rollback"));
			fail();
		} catch (RuntimeException expected) {
			assertEquals(RuntimeException.class, expected.getClass());
			assertEquals("Testing Rollback", expected.getMessage());
		}

		try {
			_prevayler.execute(new Appendix("z"));
			fail();
		} catch (ErrorInEarlierTransactionError expected) {
			assertEquals("Prevayler is no longer allowing access to the prevalent system due to an Error thrown from an earlier transaction.", expected.getMessage());
		}

		crashRecover();
		verify("abc");
	}

	private void crashRecover() throws Exception {
		if (_prevayler != null)
			_prevayler.close();
		_prevayler = PrevaylerFactory.createPrevayler(new AppendingSystem(), prevalenceBase());
	}

	private void append(String appendix, String expectedResult) throws Exception {
		_prevayler.execute(new Appendix(appendix));
		verify(expectedResult);
	}

	private void verify(String expectedResult) {
		assertEquals(expectedResult, system().value());
	}

	private AppendingSystem system() {
		return (AppendingSystem) _prevayler.prevalentSystem();
	}

	private String prevalenceBase() {
		return _prevalenceBase;
	}

	private void newPrevalenceBase() throws Exception {
		_prevalenceBase = _testDirectory + File.separator + System.currentTimeMillis();
	}

}
