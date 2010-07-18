package org.designup.picsou.server.persistence.direct;

import org.designup.picsou.client.SerializableDeltaGlobSerializer;
import org.designup.picsou.client.SerializableGlobSerializer;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.server.model.SerializableGlobType;
import org.designup.picsou.server.model.ServerDelta;
import org.designup.picsou.server.persistence.prevayler.AccountDataManager;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.*;
import org.globsframework.utils.exceptions.EOFIOFailure;
import org.globsframework.utils.exceptions.IOFailure;
import org.globsframework.utils.exceptions.InvalidData;
import org.globsframework.utils.exceptions.InvalidState;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;
import org.globsframework.utils.serialization.YANBuffereInputStream;
import org.prevayler.implementation.PrevaylerDirectory;

import java.io.*;
import java.nio.channels.FileLock;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;

public class DirectAccountDataManager implements AccountDataManager {

  private Map<Integer, DurableOutputStream> outputStreamMap = new HashMap<Integer, DurableOutputStream>();
  private String prevaylerPath;
  private boolean inMemory;
  private int countFileNotToDelete = 6;
  private RandomAccessFile streamLock;
  private FileLock lock;
  public static final String LATEST_VERSION = "T1";

  private class TransactionInfo {
    public long transactionId;
    public long timestamp;

    public TransactionInfo(long transactionId, long timestamp) {
      this.transactionId = transactionId;
      this.timestamp = timestamp;
    }
  }

  public DirectAccountDataManager(String prevaylerPath, boolean inMemory) {
    this.prevaylerPath = prevaylerPath;
    this.inMemory = inMemory;
    if (inMemory) {
      return;
    }
    File file = new File(prevaylerPath, "app.lock");
    file.getParentFile().mkdirs();
    try {
      streamLock = new RandomAccessFile(file, "rw");
      lock = streamLock.getChannel().tryLock();
    }
    catch (IOException e) {
      streamLock = null;
      throw new InvalidState(prevaylerPath);
    }
    if (lock == null) {
      streamLock = null;
      throw new InvalidState("Repository already in use: " + prevaylerPath);
    }
  }

  void setCountFileNotToDelete(int countFileNotToDelete) {
    this.countFileNotToDelete = countFileNotToDelete;
  }

  synchronized public void getUserData(SerializedOutput output, Integer userId) {
    MapOfMaps<String, Integer, SerializableGlobType> globs = new MapOfMaps<String, Integer, SerializableGlobType>();
    TransactionInfo transactionInfo = readData(userId, globs);
    outputStreamMap.put(userId, new DurableOutputStream(this, transactionInfo.transactionId, userId));
    SerializableGlobSerializer.serialize(output, globs);
  }

  private TransactionInfo readData(Integer userId, MapOfMaps<String, Integer, SerializableGlobType> globs) {
    String path = getPath(userId);
    File file1 = new File(path);
    if (!file1.exists()) {
      file1.mkdirs();
    }
    final PrevaylerDirectory prevaylerDirectory = new PrevaylerDirectory(path);
    File file = prevaylerDirectory.latestSnapshot();
    long snapshotVersion = 1;
    TransactionInfo transactionInfo = new TransactionInfo(snapshotVersion, System.currentTimeMillis());
    if (file != null) {
      snapshotVersion = PrevaylerDirectory.snapshotVersion(file);
      ReadOnlyAccountDataManager.SnapshotInfo snapshotInfo = readSnapshot(globs, file);
      if (snapshotInfo != null) {
        transactionInfo = new TransactionInfo(snapshotVersion, snapshotInfo.timestamp);
      }
    }
    TransactionInfo nextTransactionVersion = transactionInfo;
    File journalFile = prevaylerDirectory.findInitialJournalFile(snapshotVersion);
    if (journalFile != null) {
      try {
        DirectAccountDataManager.TransactionInfo info =
          readFrom(globs, snapshotVersion, journalFile, prevaylerDirectory);
        if (info != null) {
          nextTransactionVersion = info;
        }
      }
      catch (FileNotFoundException e) {
      }
    }
    return nextTransactionVersion;
  }

  String getPath(Integer userId) {
    return prevaylerPath + "/" + userId.toString();
  }

  private TransactionInfo readFrom(MapOfMaps<String, Integer, SerializableGlobType> globs, long snapshotVersion,
                                   File nextTransactionFile, PrevaylerDirectory prevaylerDirectory) throws FileNotFoundException {
    long version = PrevaylerDirectory.journalVersion(nextTransactionFile);
    File file = nextTransactionFile;
    InputStream inputStream = new YANBuffereInputStream(new FileInputStream(file));
    TransactionInfo transactionInfo = new TransactionInfo(version, System.currentTimeMillis());
    while (true) {
      try {
        SerializedInput serializedInput =
          SerializedInputOutputFactory.init(inputStream);
        TransactionInfo newTransactionInfo = readJournalVersion(serializedInput);
        if (newTransactionInfo == null || newTransactionInfo.transactionId != version) {
          throw new InvalidState(Lang.get("data.load.error.journal", version, 
                                          Dates.toTimestampString(new Date(transactionInfo.timestamp))));
        }
        MultiMap<String, ServerDelta> map = SerializableDeltaGlobSerializer.deserialize(serializedInput);
        if (version >= snapshotVersion) {
          ReadOnlyAccountDataManager.apply(globs, map);
        }
        transactionInfo = newTransactionInfo;
        version++;
      }
      catch (InvalidData ex) {
        throw new InvalidState(Lang.get("data.load.error.journal", version,
                                        Dates.toTimestampString(new Date(transactionInfo.timestamp))), ex);
      }
      catch (EOFIOFailure e) {
        try {
          inputStream.close();
        }
        catch (IOException e1) {
        }
        File newfile = prevaylerDirectory.journalFile(version, "journal");
        if (newfile.equals(file)) {
          PrevaylerDirectory.renameEmptyFile(file);
        }
        if (!newfile.exists()) {
          break;
        }
        file = newfile;
        inputStream = new YANBuffereInputStream(new FileInputStream(file));
      }
    }
    try {
      inputStream.close();
    }
    catch (Exception e) {
    }
    return new TransactionInfo(version < snapshotVersion ? snapshotVersion : version, transactionInfo.timestamp);
  }

  private TransactionInfo readJournalVersion(SerializedInput serializedInput) {
    String s = serializedInput.readJavaString();
    if (s.equals("Tr")) {
      return new TransactionInfo(serializedInput.readNotNullLong(), -1);
    }
    if (s.equals(LATEST_VERSION)) {
      long version = serializedInput.readNotNullLong();
      long timestamp = serializedInput.readNotNullLong();
      return new TransactionInfo(version, timestamp);
    }
    return null;
  }

  ReadOnlyAccountDataManager.SnapshotInfo readSnapshot(MapOfMaps<String, Integer, SerializableGlobType> globs, File file) {
    try {
      return ReadOnlyAccountDataManager.readSnapshot(globs, new FileInputStream(file));
    }
    catch (FileNotFoundException e) {
      return null;
    }
  }

  synchronized public void updateUserData(SerializedInput input, Integer userId) {
    long timestamp = System.currentTimeMillis();
    MultiMap<String, ServerDelta> data = SerializableDeltaGlobSerializer.deserialize(input);
    DurableOutputStream bufferedOutputStream = outputStreamMap.get(userId);
    if (bufferedOutputStream == null) {
      throw new InvalidState("read should be call before write");
    }
    bufferedOutputStream.write(data, timestamp);
  }

  public Integer getNextId(String globTypeName, Integer userId, Integer count) {
    return null;
  }

  synchronized public void delete(Integer userId) {
    DurableOutputStream durableOutputStream = outputStreamMap.get(userId);
    if (durableOutputStream != null) {
      durableOutputStream.close();
    }
    String path = getPath(userId);
    File file = new File(path);
    if (file.exists()) {
      Files.deleteSubtree(file);
    }
  }

  synchronized public void close() {
    for (DurableOutputStream durableOutputStream : outputStreamMap.values()) {
      durableOutputStream.close();
    }
    outputStreamMap.clear();
    try {
      if (streamLock != null) {
        lock.release();
        streamLock.close();
        streamLock = null;
      }
    }
    catch (IOException e) {
      Log.write("stream close error", e);
    }
  }

  synchronized public void close(Integer userId) {
    DurableOutputStream durableOutputStream = outputStreamMap.get(userId);
    if (durableOutputStream != null) {
      outputStreamMap.remove(userId);
      durableOutputStream.close();
    }
  }

  synchronized public void takeSnapshot(Integer userId) {
    if (inMemory) {
      return;
    }
    PrevaylerDirectory directory = new PrevaylerDirectory(getPath(userId));
    MapOfMaps<String, Integer, SerializableGlobType> globs = new MapOfMaps<String, Integer, SerializableGlobType>();
    TransactionInfo transactionInfo = readData(userId, globs);
    try {
      writeSnapshot(transactionInfo.transactionId, globs, directory, transactionInfo.timestamp);
      long lastDeletedSnasphotId = directory.deletePreviousSnapshot(countFileNotToDelete);
      directory.deletePreviousJournal(lastDeletedSnasphotId);
    }
    catch (Exception e) {
      Log.write("for " + userId, e);
    }
  }

  synchronized public boolean restore(SerializedInput input, Integer userId) {
    DurableOutputStream durableOutputStream = outputStreamMap.get(userId);
    MapOfMaps<String, Integer, SerializableGlobType> data = new MapOfMaps<String, Integer, SerializableGlobType>();
    SerializableGlobSerializer.deserialize(input, data);
    try {
      writeSnapshot(durableOutputStream.getNextTransactionVersion(), data,
                    durableOutputStream.getPrevaylerDirectory(), System.currentTimeMillis());
    }
    catch (IOException e) {
      return false;
    }
    return true;
  }

  public boolean newData(Integer userId, SerializedInput input) {
    MapOfMaps<String, Integer, SerializableGlobType> globs = new MapOfMaps<String, Integer, SerializableGlobType>();
    TransactionInfo transactionInfo = readData(userId, globs);
    DurableOutputStream durableOutputStream = new DurableOutputStream(this, transactionInfo.transactionId, userId);
    outputStreamMap.put(userId, durableOutputStream);

    MapOfMaps<String, Integer, SerializableGlobType> data = new MapOfMaps<String, Integer, SerializableGlobType>();
    SerializableGlobSerializer.deserialize(input, data);
    try {
      writeSnapshot(durableOutputStream.getNextTransactionVersion(), data,
                    durableOutputStream.getPrevaylerDirectory(), System.currentTimeMillis());
    }
    catch (IOException e) {
      Log.write("in new data", e);
      return false;
    }
    return true;
  }

  synchronized private void writeSnapshot(long transactionId, MapOfMaps<String, Integer, SerializableGlobType> data,
                                          PrevaylerDirectory directory, long timestamp) throws IOException {
    File tempFile = directory.createTempFile("snapshot" + transactionId + "temp", "generatingSnapshot");

    ReadOnlyAccountDataManager.writeSnapshot(data, tempFile, null, PicsouApplication.JAR_VERSION, timestamp);

    File permanent = directory.snapshotFile(transactionId, "snapshot");
    permanent.delete();
    if (!tempFile.renameTo(permanent)) {
      throw new IOFailure("Temporary snapshot file generated: " + tempFile +
                          "\nUnable to rename it permanently to: " + permanent);
    }
  }

  public boolean isInMemory() {
    return inMemory;
  }
}
