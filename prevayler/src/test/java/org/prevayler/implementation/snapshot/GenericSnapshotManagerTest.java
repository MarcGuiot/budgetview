package org.prevayler.implementation.snapshot;

import org.prevayler.Prevayler;
import org.prevayler.PrevaylerFactory;
import org.prevayler.foundation.FileIOTest;
import org.prevayler.foundation.serialization.JavaSerializer;
import org.prevayler.foundation.serialization.Serializer;
import org.prevayler.implementation.AppendTransaction;

import java.io.File;
import java.io.IOException;

public class GenericSnapshotManagerTest extends FileIOTest {

	public void testNoExistingSnapshot() throws IOException, ClassNotFoundException {
		Prevayler prevayler = createPrevayler("snapshot", new JavaSerializer());
		assertEquals("initial", prevayler.prevalentSystem().toString());
	}

	public void testRoundtripJava() throws IOException, ClassNotFoundException {
		checkRoundtrip("snapshot", new JavaSerializer());
	}


	private void checkRoundtrip(String suffix, Serializer serializer) throws IOException, ClassNotFoundException {
		Prevayler first = createPrevayler(suffix, serializer);
		appendTakeSnapshotAndClose(first);

		checkSnapshotAndDeleteJournal("0000000000000000002." + suffix, "0000000000000000001.journal");

		Prevayler second = createPrevayler(suffix, serializer);
		assertEquals("initial one two", second.prevalentSystem().toString());
		second.close();
	}

	private Prevayler createPrevayler(String suffix, Serializer serializer) throws IOException,
			ClassNotFoundException {
		PrevaylerFactory factory = new PrevaylerFactory();
		factory.configurePrevalentSystem(new StringBuffer("initial"));
		factory.configurePrevalenceDirectory(_testDirectory);
		factory.configureSnapshotSerializer(suffix, serializer);
		return factory.create();
	}

	private void appendTakeSnapshotAndClose(Prevayler prevayler) throws IOException {
		prevayler.execute(new AppendTransaction(" one"));
		prevayler.execute(new AppendTransaction(" two"));
		prevayler.takeSnapshot();
		prevayler.close();
	}

	private void checkSnapshotAndDeleteJournal(String snapshot, String journal) {
		assertTrue(new File(_testDirectory, snapshot).exists());
		deleteFromTestDirectory(journal);
	}

}
