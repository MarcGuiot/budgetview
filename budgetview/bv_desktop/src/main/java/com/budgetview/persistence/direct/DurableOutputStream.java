package com.budgetview.persistence.direct;

import com.budgetview.client.serialization.SerializableDeltaGlobSerializer;
import com.budgetview.session.serialization.SerializedDelta;
import org.globsframework.utils.Log;
import org.globsframework.utils.collections.MultiMap;
import org.globsframework.utils.exceptions.IOFailure;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;
import org.prevayler.implementation.PrevaylerDirectory;

import java.io.*;

class DurableOutputStream {
  private long nextTransactionVersion;
  private OutputStream outputStream;
  private PrevaylerDirectory prevaylerDirectory;
  private FileDescriptor fd;
  private DirectAccountDataManager manager;
  private long transactionIdAtCreation;

  public DurableOutputStream(DirectAccountDataManager manager, long nextTransactionVersion, Integer userId) {
    this.manager = manager;
    this.nextTransactionVersion = nextTransactionVersion;
    transactionIdAtCreation = nextTransactionVersion;
    prevaylerDirectory = new PrevaylerDirectory(manager.getPath(userId));
    try {
      prevaylerDirectory.produceDirectory();
    }
    catch (IOException e) {
      throw new IOFailure(e);
    }
  }

  public void write(MultiMap<String, SerializedDelta> data, long timestamp) {
    if (manager.isInMemory()) {
      return;
    }
    try {
      if (outputStream == null) {
        File file = prevaylerDirectory.journalFile(nextTransactionVersion, "journal");
        FileOutputStream stream = new FileOutputStream(file);
        fd = stream.getFD();
        outputStream = new BufferedOutputStream(stream);
      }
      SerializedOutput serializedOutput = SerializedInputOutputFactory.init(outputStream);
      serializedOutput.writeJavaString(DirectAccountDataManager.LATEST_VERSION);
      serializedOutput.write(nextTransactionVersion);
      serializedOutput.write(timestamp);
      SerializableDeltaGlobSerializer.serialize(serializedOutput, data);
      outputStream.flush();
      fd.sync();
      nextTransactionVersion++;
    }
    catch (IOException e) {
      throw new IOFailure(e);
    }
    catch (RuntimeException ex){
      Log.write("[Persistence] Error while writing data", ex);
      throw ex;
    }
  }

  public void close() {
    if (outputStream != null) {
      try {
        outputStream.close();
      }
      catch (IOException e) {
        Log.write("[Persistence] Stream close error", e);
      }
    }
  }

  public long getNextTransactionVersion() {
    return nextTransactionVersion;
  }

  public PrevaylerDirectory getPrevaylerDirectory() {
    return prevaylerDirectory;
  }

  public boolean checkIsLast() {
    File file = prevaylerDirectory.findInitialJournalFile(Integer.MAX_VALUE);
    return file == null || PrevaylerDirectory.journalVersion(file) <= transactionIdAtCreation;
  }
}
