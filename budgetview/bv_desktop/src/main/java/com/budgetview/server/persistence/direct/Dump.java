package com.budgetview.server.persistence.direct;

import com.budgetview.server.persistence.prevayler.users.PRootDataManager;
import com.budgetview.server.model.ServerState;
import org.globsframework.utils.exceptions.EOFIOFailure;
import org.globsframework.utils.serialization.DefaultSerializationInput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.YANBuffereInputStream;

import java.io.*;

public class Dump {

  public static void main(String[] args) throws FileNotFoundException {

    if (true) {
      PRootDataManager.main(args);
      return;
    }

    Dump dump = new Dump();
    for (String arg : args) {
      if (arg.endsWith("journal")) {
        System.out.println("Journal " + arg);
        dump.dumpJournal(new File(arg));
      }
      if (arg.endsWith("snapshot")) {
        System.out.println("Snapshot " + arg);
        dump.dumpSnapshot(new File(arg));
      }
    }
  }

  private void dumpSnapshot(File file) throws FileNotFoundException {
    InputStream inputStream = new YANBuffereInputStream(new FileInputStream(file));
    SerializedInput serializedInput = SerializedInputOutputFactory.init(inputStream);
    String version = serializedInput.readJavaString();
    if ("2".equals(version)) {
      deserializeSnapshot(serializedInput);
    }
  }

  private void dumpJournal(File file) throws FileNotFoundException {
    SerializedInput serializedInput =
      SerializedInputOutputFactory.init(new YANBuffereInputStream(new FileInputStream(file)));
    try {
      long l = readJournalVersion(serializedInput);
      while (true) {
        System.out.println("transaction : " + l);
        deserializeJournal(serializedInput);
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
      System.out.println("error at : " + ((DefaultSerializationInput)serializedInput).count);
      throw e;
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

  public void deserializeJournal(SerializedInput serializedInput) {
    int globTypeCount = serializedInput.readNotNullInt();
    while (globTypeCount > 0) {
      String globTypeName = serializedInput.readJavaString();
      int deltaGlobCount = serializedInput.readNotNullInt();
      while (deltaGlobCount > 0) {
        int state = serializedInput.readNotNullInt();
        ServerState deltaState = ServerState.get(state);
        int id = serializedInput.readNotNullInt();
        if (deltaState == ServerState.CREATED || deltaState == ServerState.UPDATED) {
          int version = serializedInput.readNotNullInt();
          byte[] data = serializedInput.readBytes();
        }
        System.out.println(globTypeName + "." + id + " : " + deltaState.toString());
        deltaGlobCount--;
      }
      globTypeCount--;
    }
  }

  public static void deserializeSnapshot(SerializedInput serializedInput) {
    int globTypeCount = serializedInput.readNotNullInt();
    while (globTypeCount > 0) {
      String globTypeName = serializedInput.readJavaString();
      int deltaGlobCount = serializedInput.readNotNullInt();
      while (deltaGlobCount > 0) {
        int id = serializedInput.readNotNullInt();
        int version = serializedInput.readNotNullInt();
        byte[] data = serializedInput.readBytes();
        deltaGlobCount--;
        System.out.println(globTypeName + "." + id);
      }
      globTypeCount--;
    }
  }
}
