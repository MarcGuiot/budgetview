package org.designup.picsou.server.persistence.direct;

import org.designup.picsou.client.SerializableDeltaGlobSerializer;
import org.designup.picsou.client.SerializableGlobSerializer;
import org.designup.picsou.server.model.SerializableGlobType;
import org.designup.picsou.server.model.ServerDelta;
import org.designup.picsou.server.persistence.prevayler.AccountDataManager;
import org.designup.picsou.gui.PicsouApplication;
import org.globsframework.utils.Log;
import org.globsframework.utils.MapOfMaps;
import org.globsframework.utils.MultiMap;
import org.globsframework.utils.Files;
import org.globsframework.utils.exceptions.EOFIOFailure;
import org.globsframework.utils.exceptions.IOFailure;
import org.globsframework.utils.exceptions.InvalidState;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;
import org.globsframework.model.GlobList;
import org.prevayler.implementation.PrevaylerDirectory;

import java.io.*;
import java.nio.channels.FileLock;
import java.util.HashMap;
import java.util.Map;

public class DirectAccountDataManager implements AccountDataManager {

  private Map<Integer, DurableOutputStream> outputStreamMap = new HashMap<Integer, DurableOutputStream>();
  private String prevaylerPath;
  private boolean inMemory;
  private int countFileNotToDelete = 6;
  private RandomAccessFile stream;
  private FileLock lock;

  public DirectAccountDataManager(String prevaylerPath, boolean inMemory) {
    this.prevaylerPath = prevaylerPath;
    this.inMemory = inMemory;
    if (inMemory){
      return;
    }
    File file = new File(prevaylerPath, "app.lock");
    file.getParentFile().mkdirs();
    try {
      stream = new RandomAccessFile(file, "rw");
      lock = stream.getChannel().tryLock();
    }
    catch (IOException e) {
      stream = null;
      throw new InvalidState(prevaylerPath);
    }
    if (lock == null) {
      stream = null;
      throw new InvalidState("Repository already in use: " + prevaylerPath);
    }
  }

  void setCountFileNotToDelete(int countFileNotToDelete) {
    this.countFileNotToDelete = countFileNotToDelete;
  }

  synchronized public void getUserData(SerializedOutput output, Integer userId) {
    MapOfMaps<String, Integer, SerializableGlobType> globs = new MapOfMaps<String, Integer, SerializableGlobType>();
    long transactionId = readData(userId, globs);
    outputStreamMap.put(userId, new DurableOutputStream(this, transactionId, userId));
    SerializableGlobSerializer.serialize(output, globs);
  }

  private long readData(Integer userId, MapOfMaps<String, Integer, SerializableGlobType> globs) {
    String path = getPath(userId);
    File file1 = new File(path);
    if (!file1.exists()) {
      file1.mkdirs();
    }
    final PrevaylerDirectory prevaylerDirectory = new PrevaylerDirectory(path);
    File file = prevaylerDirectory.latestSnapshot();
    long snapshotVersion = 1;
    if (file != null) {
      snapshotVersion = PrevaylerDirectory.snapshotVersion(file);
      readSnapshot(globs, file);
    }
    long nextTransactionVersion = snapshotVersion;
    File journalFile = prevaylerDirectory.findInitialJournalFile(snapshotVersion);
    if (journalFile != null) {
      try {
        nextTransactionVersion = readFrom(globs, snapshotVersion, journalFile, prevaylerDirectory);
      }
      catch (FileNotFoundException e) {
      }
    }
    return nextTransactionVersion;
  }

  String getPath(Integer userId) {
    return prevaylerPath + "/" + userId.toString();
  }

  private long readFrom(MapOfMaps<String, Integer, SerializableGlobType> globs, long snapshotVersion,
                        File nextTransactionFile, PrevaylerDirectory prevaylerDirectory) throws FileNotFoundException {
    long version = PrevaylerDirectory.journalVersion(nextTransactionFile);
    File file = nextTransactionFile;
    BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
    while (true) {
      try {
        SerializedInput serializedInput =
          SerializedInputOutputFactory.init(inputStream);
        long readVersion = readJournalVersion(serializedInput);
        if (readVersion != version) {
          throw new InvalidState("error while reading journal file");
        }
        SerializableDeltaGlobSerializer serializableDeltaGlobSerializer = new SerializableDeltaGlobSerializer();
        MultiMap<String, ServerDelta> map = serializableDeltaGlobSerializer.deserialize(serializedInput);
        if (version >= snapshotVersion) {
          ReadOnlyAccountDataManager.apply(globs, map);
        }
        version++;
      }
      catch (EOFIOFailure e) {
        try {
          inputStream.close();
        }
        catch (IOException e1) {
        }
        File newfile = prevaylerDirectory.journalFile(version, "journal");
        if (newfile.equals(file)) {
          PrevaylerDirectory.renameUnusedFile(file);
        }
        if (!newfile.exists()) {
          break;
        }
        file = newfile;
        inputStream = new BufferedInputStream(new FileInputStream(file));
      }
    }
    try {
      inputStream.close();
    }
    catch (Exception e) {
    }
    return version;
  }

  private long readJournalVersion(SerializedInput serializedInput) {
    String s = serializedInput.readJavaString();
    if (!s.equals("Tr")) {
      return -1;
    }
    return serializedInput.readNotNullLong();
  }

  void readSnapshot(MapOfMaps<String, Integer, SerializableGlobType> globs, File file) {
    try {
      ReadOnlyAccountDataManager.readSnapshot(globs, new FileInputStream(file));
    }
    catch (FileNotFoundException e) {
    }
  }

  synchronized public void updateUserData(SerializedInput input, Integer userId) {
    SerializableDeltaGlobSerializer serializableDeltaGlobSerializer = new SerializableDeltaGlobSerializer();
    MultiMap<String, ServerDelta> data = serializableDeltaGlobSerializer.deserialize(input);
    DurableOutputStream bufferedOutputStream = outputStreamMap.get(userId);
    if (bufferedOutputStream == null) {
      throw new InvalidState("read should be call before write");
    }
    bufferedOutputStream.write(data);
  }

  public Integer getNextId(String globTypeName, Integer userId, Integer count) {
    return null;
  }

  synchronized public void delete(Integer userId) {
    DurableOutputStream durableOutputStream = outputStreamMap.get(userId);
    if (durableOutputStream != null){
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
    try {
      if (stream != null) {
        stream.close();
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
    try {
      if (stream != null) {
        stream.close();
      }
    }
    catch (IOException e) {
      Log.write("stream close error", e);
    }
  }

  synchronized public void takeSnapshot(Integer userId) {
    if (inMemory) {
      return;
    }
    PrevaylerDirectory directory = new PrevaylerDirectory(getPath(userId));
    MapOfMaps<String, Integer, SerializableGlobType> globs = new MapOfMaps<String, Integer, SerializableGlobType>();
    long transactionId = readData(userId, globs);
    try {
      writeSnapshot(transactionId, globs, directory);
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
                    durableOutputStream.getPrevaylerDirectory());
    }
    catch (IOException e) {
      return false;
    }
    return true;
  }

  synchronized private void writeSnapshot(long transactionId, MapOfMaps<String, Integer, SerializableGlobType> data,
                             PrevaylerDirectory directory) throws IOException {
    File tempFile = directory.createTempFile("snapshot" + transactionId + "temp", "generatingSnapshot");

    ReadOnlyAccountDataManager.writeSnapshot(data, tempFile, null, PicsouApplication.JAR_VERSION);

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
