package org.designup.picsou.server.persistence.direct;

import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializationInputChecker;
import org.globsframework.utils.serialization.DefaultSerializationInput;
import org.globsframework.utils.MultiMap;
import org.globsframework.utils.exceptions.EOFIOFailure;
import org.designup.picsou.server.model.ServerDelta;
import org.designup.picsou.server.model.ServerState;

import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class DumpJournal {
  private String[] files;

  public DumpJournal(String[] files) {
    this.files = files;
  }

  public static void main(String[] args) throws FileNotFoundException {
    DumpJournal journal = new DumpJournal(args);
    journal.dump();
  }

  private void dump() throws FileNotFoundException {
    for (String fileName : files) {
      File file = new File(fileName);
      SerializedInput serializedInput =
        SerializedInputOutputFactory.init(new BufferedInputStream(new FileInputStream(file)));
      try {
        long l = readJournalVersion(serializedInput);
        while (true){
          System.out.println("transaction : " + l);
          deserialize(serializedInput);
          try {
            l = readJournalVersion(serializedInput);
          }
          catch (EOFIOFailure e) {
            System.out.println("End of file");
            break;
          }
        }
      }
      catch (RuntimeException e) {
        System.out.println("error at : " +((DefaultSerializationInput)serializedInput).count);
        throw e;
      }
    }
  }

  private long readJournalVersion(SerializedInput serializedInput) {
    String s = serializedInput.readJavaString();
    if (!s.equals("Tr")) {
      System.err.println("Missing expected 'Tr'");
      throw new RuntimeException("Missing expected 'Tr'");
    }
    return serializedInput.readNotNullLong();
  }

  public MultiMap<String, ServerDelta> deserialize(SerializedInput serializedInput) {
    MultiMap<String, ServerDelta> multiMap = new MultiMap<String, ServerDelta>();
    int globTypeCount = serializedInput.readNotNullInt();
    System.out.println("GlobTypeCount : " + globTypeCount);
    while (globTypeCount > 0) {
      String globTypeName = serializedInput.readJavaString();
      int deltaGlobCount = serializedInput.readNotNullInt();
      System.out.println("For " + globTypeName + " " + deltaGlobCount + " changes.");
      while (deltaGlobCount > 0) {
        int state = serializedInput.readNotNullInt();
        ServerState deltaState = ServerState.get(state);
        ServerDelta delta = new ServerDelta(serializedInput.readNotNullInt());
        delta.setState(deltaState);
        if (deltaState == ServerState.CREATED || deltaState == ServerState.UPDATED) {
          delta.setVersion(serializedInput.readNotNullInt());
          delta.setData(serializedInput.readBytes());
        }
        multiMap.put(globTypeName, delta);
        deltaGlobCount--;
      }
      globTypeCount--;
    }
    return multiMap;
  }

}
