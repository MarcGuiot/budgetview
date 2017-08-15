package com.budgetview.persistence.direct;

import com.budgetview.client.serialization.GlobCollectionSerializer;
import com.budgetview.client.serialization.SerializableDeltaGlobSerializer;
import com.budgetview.desktop.Application;
import com.budgetview.persistence.prevayler.AccountDataManager;
import com.budgetview.session.serialization.SerializedDelta;
import com.budgetview.session.serialization.SerializedGlob;
import com.budgetview.utils.Lang;
import org.globsframework.utils.Dates;
import org.globsframework.utils.Files;
import org.globsframework.utils.Log;
import org.globsframework.utils.collections.MapOfMaps;
import org.globsframework.utils.collections.MultiMap;
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
import java.util.*;

public class DirectAccountDataManager implements AccountDataManager {

  public static final String LOCK_FILE_NAME = "app.lock";
  private Map<Integer, DurableOutputStream> outputStreamMap = new HashMap<Integer, DurableOutputStream>();
  private String prevaylerPath;
  private boolean inMemory;
  private int countFileNotToDelete = 10;
  private RandomAccessFile streamLock;
  private FileLock lock;
  public static final String LATEST_VERSION = "T1";

  private class TransactionInfo {
    public long transactionId;
    public long timestamp;
    public boolean isSnapshot;

    public TransactionInfo(long transactionId, long timestamp, boolean isSnapshot) {
      this.transactionId = transactionId;
      this.timestamp = timestamp;
      this.isSnapshot = isSnapshot;
    }
  }

  public DirectAccountDataManager(String prevaylerPath, boolean inMemory) {
    this.prevaylerPath = prevaylerPath;
    this.inMemory = inMemory;
    if (inMemory) {
      return;
    }
    File file = new File(prevaylerPath, LOCK_FILE_NAME);
    file.getParentFile().mkdirs();
    try {
      streamLock = new RandomAccessFile(file, "rw");
      lock = streamLock.getChannel().tryLock();
    }
    catch (Exception e) {
      streamLock = null;
      throw new InvalidState(prevaylerPath, e);
    }
    if (lock == null) {
      streamLock = null;
      throw new InvalidState("Repository already in use: " + prevaylerPath);
    }
  }

  public boolean hasChanged(Integer userId) {
    DurableOutputStream durableOutputStream = outputStreamMap.get(userId);
    if (durableOutputStream == null){
      return false;
    }
    else {
      return !durableOutputStream.checkIsLast();
    }
  }

  void setCountFileNotToDelete(int countFileNotToDelete) {
    this.countFileNotToDelete = countFileNotToDelete;
  }

  synchronized public void getUserData(SerializedOutput output, Integer userId) {
    MapOfMaps<String, Integer, SerializedGlob> globs = new MapOfMaps<String, Integer, SerializedGlob>();
    TransactionInfo transactionInfo = readData(userId, globs);
    outputStreamMap.put(userId, new DurableOutputStream(this, transactionInfo.transactionId, userId));
    GlobCollectionSerializer.serialize(output, globs);
  }

  private TransactionInfo readData(Integer userId, MapOfMaps<String, Integer, SerializedGlob> globs) {
    String path = getPath(userId);
    File file1 = new File(path);
    if (!file1.exists()) {
      file1.mkdirs();
    }
    final PrevaylerDirectory prevaylerDirectory = new PrevaylerDirectory(path);
    File file = prevaylerDirectory.latestSnapshot();
    long snapshotVersion = 1;
    TransactionInfo transactionInfo = new TransactionInfo(snapshotVersion, System.currentTimeMillis(), false);
    if (file != null) {
      snapshotVersion = PrevaylerDirectory.snapshotVersion(file);
      ReadOnlyAccountDataManager.SnapshotInfo snapshotInfo = readSnapshot(globs, file);
      if (snapshotInfo != null) {
        transactionInfo = new TransactionInfo(snapshotVersion, snapshotInfo.timestamp, true);
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

  private TransactionInfo readFrom(MapOfMaps<String, Integer, SerializedGlob> globs, long snapshotVersion,
                                   File nextTransactionFile, PrevaylerDirectory prevaylerDirectory) throws FileNotFoundException {
    long version = PrevaylerDirectory.journalVersion(nextTransactionFile);
    File file = nextTransactionFile;
    InputStream inputStream = new YANBuffereInputStream(new FileInputStream(file));
    TransactionInfo transactionInfo = new TransactionInfo(version, System.currentTimeMillis(), false);
    boolean isFromSnapshot = true;
    while (true) {
      try {
        SerializedInput serializedInput =
          SerializedInputOutputFactory.init(inputStream);
        TransactionInfo newTransactionInfo = readJournalVersion(serializedInput);
        if (newTransactionInfo == null || newTransactionInfo.transactionId != version) {
          throw new InvalidState(Lang.get("data.load.error.journal", version,
                                          Dates.toTimestampString(new Date(transactionInfo.timestamp))));
        }
        MultiMap<String, SerializedDelta> map = SerializableDeltaGlobSerializer.deserialize(serializedInput);
        if (version >= snapshotVersion) {
          ReadOnlyAccountDataManager.apply(globs, map);
          isFromSnapshot = false;
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
    return new TransactionInfo(version < snapshotVersion ? snapshotVersion : version,
                               transactionInfo.timestamp, isFromSnapshot);
  }

  private TransactionInfo readJournalVersion(SerializedInput serializedInput) {
    String s = serializedInput.readJavaString();
    if (s.equals("Tr")) {
      return new TransactionInfo(serializedInput.readNotNullLong(), -1, false);
    }
    if (s.equals(LATEST_VERSION)) {
      long version = serializedInput.readNotNullLong();
      long timestamp = serializedInput.readNotNullLong();
      return new TransactionInfo(version, timestamp, false);
    }
    return null;
  }

  ReadOnlyAccountDataManager.SnapshotInfo readSnapshot(MapOfMaps<String, Integer, SerializedGlob> globs, File file) {
    try {
      return ReadOnlyAccountDataManager.readSnapshot(globs, new FileInputStream(file));
    }
    catch (FileNotFoundException e) {
      return null;
    }
  }

  synchronized public void updateUserData(SerializedInput input, Integer userId) {
    long timestamp = System.currentTimeMillis();
    MultiMap<String, SerializedDelta> data = SerializableDeltaGlobSerializer.deserialize(input);
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
      Files.deleteWithSubtree(file);
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
      Log.write("[Persistence] Stream close error", e);
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
    close(userId);
    PrevaylerDirectory directory = new PrevaylerDirectory(getPath(userId));
    MapOfMaps<String, Integer, SerializedGlob> globs = new MapOfMaps<String, Integer, SerializedGlob>();
    TransactionInfo transactionInfo = readData(userId, globs);
    if (!transactionInfo.isSnapshot) {
      try {
        writeSnapshot(transactionInfo.transactionId, globs, directory, transactionInfo.timestamp);
        long lastDeletedSnasphotId = directory.deletePreviousSnapshot(countFileNotToDelete);
        directory.deletePreviousJournal(lastDeletedSnasphotId);
      }
      catch (Exception e) {
        Log.write("[Persistence] Snapshot creation error for " + userId, e);
      }
    }
    outputStreamMap.put(userId, new DurableOutputStream(this, transactionInfo.transactionId, userId));
  }

  synchronized public boolean restore(SerializedInput input, Integer userId) {
    DurableOutputStream durableOutputStream = outputStreamMap.get(userId);
    MapOfMaps<String, Integer, SerializedGlob> data = new MapOfMaps<String, Integer, SerializedGlob>();
    GlobCollectionSerializer.deserialize(input, data);
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
    MapOfMaps<String, Integer, SerializedGlob> globs = new MapOfMaps<String, Integer, SerializedGlob>();
    TransactionInfo transactionInfo = readData(userId, globs);
    DurableOutputStream durableOutputStream = new DurableOutputStream(this, transactionInfo.transactionId, userId);
    outputStreamMap.put(userId, durableOutputStream);

    MapOfMaps<String, Integer, SerializedGlob> data = new MapOfMaps<String, Integer, SerializedGlob>();
    GlobCollectionSerializer.deserialize(input, data);
    try {
      writeSnapshot(durableOutputStream.getNextTransactionVersion(), data,
                    durableOutputStream.getPrevaylerDirectory(), System.currentTimeMillis());
    }
    catch (IOException e) {
      Log.write("[Persistence] Error in new data", e);
      return false;
    }
    return true;
  }

  public List<SnapshotInfo> getSnapshotInfos(Integer userId) {
    PrevaylerDirectory directory = new PrevaylerDirectory(getPath(userId));
    List<SnapshotInfo> snapshotInfos = new ArrayList<SnapshotInfo>();
    File[] files = directory.getOrderedSnapshot();
    for (File file : files) {
      try {
        ReadOnlyAccountDataManager.SnapshotInfo snapshotInfo =
          ReadOnlyAccountDataManager.readSnapshot(null, new FileInputStream(file));
        snapshotInfos.add(new SnapshotInfo(snapshotInfo.timestamp, snapshotInfo.password,
                                           snapshotInfo.version, file.getName()));
      }
      catch (FileNotFoundException e) {
      }
    }
    return snapshotInfos;
  }

  public void getSnapshotData(Integer userId, String fileName, final SerializedOutput output) {
    String path = getPath(userId);
    File file1 = new File(path);
    if (!file1.exists()) {
      file1.mkdirs();
    }
    final PrevaylerDirectory prevaylerDirectory = new PrevaylerDirectory(path);
    File file = prevaylerDirectory.getFile(fileName);
    if (file != null) {
      MapOfMaps<String, Integer, SerializedGlob> globs = new MapOfMaps<String, Integer, SerializedGlob>();
      readSnapshot(globs, file);
      GlobCollectionSerializer.serialize(output, globs);
    }
  }

  synchronized private void writeSnapshot(long transactionId, MapOfMaps<String, Integer, SerializedGlob> data,
                                          PrevaylerDirectory directory, long timestamp) throws IOException {
    File tempFile = directory.createTempFile("snapshot" + transactionId + "temp", "generatingSnapshot");

    ReadOnlyAccountDataManager.writeSnapshot(data, tempFile, null, Application.JAR_VERSION, timestamp);

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

