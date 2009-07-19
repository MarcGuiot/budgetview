package org.designup.picsou.server.persistence.direct;

import org.prevayler.implementation.PrevaylerDirectory;
import org.designup.picsou.server.model.ServerDelta;
import org.designup.picsou.client.SerializableDeltaGlobSerializer;
import org.globsframework.utils.MultiMap;
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

  public DurableOutputStream(DirectAccountDataManager manager, long nextTransactionVersion, Integer userId) {
    this.manager = manager;
    this.nextTransactionVersion = nextTransactionVersion;
    prevaylerDirectory = new PrevaylerDirectory(manager.getPath(userId));
  }

  public void write(MultiMap<String, ServerDelta> data) {
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
      SerializableDeltaGlobSerializer serializableDeltaGlobSerializer = new SerializableDeltaGlobSerializer();
      SerializedOutput serializedOutput = SerializedInputOutputFactory.init(outputStream);
      serializedOutput.writeJavaString("Tr");
      serializedOutput.write(nextTransactionVersion);
      SerializableDeltaGlobSerializer.serialize(serializedOutput, data);
      outputStream.flush();
      fd.sync();
      nextTransactionVersion++;
    }
    catch (IOException e) {
      throw new IOFailure(e);
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
}
