package com.budgetview.server.persistence.direct;

import org.globsframework.utils.collections.MultiMap;
import org.prevayler.implementation.PrevaylerDirectory;
import com.budgetview.server.model.ServerDelta;
import com.budgetview.client.SerializableDeltaGlobSerializer;
import org.globsframework.utils.Log;
import org.globsframework.utils.exceptions.IOFailure;
import org.globsframework.utils.serialization.SerializedOutput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;

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

  public void write(MultiMap<String, ServerDelta> data, long timestamp) {
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
      Log.write("Erreur while writing data", ex);
      throw ex;
    }
  }

  public void close() {
    if (outputStream != null) {
      try {
        outputStream.close();
      }
      catch (IOException e) {
        Log.write("stream close error", e);
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
