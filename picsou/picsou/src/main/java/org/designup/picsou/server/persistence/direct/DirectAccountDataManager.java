package org.designup.picsou.server.persistence.direct;

import org.designup.picsou.client.SerializableDeltaGlobSerializer;
import org.designup.picsou.client.SerializableGlobSerializer;
import org.designup.picsou.server.model.SerializableGlobType;
import org.designup.picsou.server.model.ServerDelta;
import org.designup.picsou.server.model.ServerState;
import org.designup.picsou.server.persistence.prevayler.AccountDataManager;
import org.globsframework.utils.Log;
import org.globsframework.utils.MapOfMaps;
import org.globsframework.utils.MultiMap;
import org.globsframework.utils.exceptions.EOFIOFailure;
import org.globsframework.utils.exceptions.IOFailure;
import org.globsframework.utils.exceptions.InvalidState;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;
import org.prevayler.implementation.PrevaylerDirectory;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DirectAccountDataManager implements AccountDataManager {

  private Map<Integer, DurableOutputStream> outputStreamMap = new HashMap<Integer, DurableOutputStream>();
  private String prevaylerPath;
  private boolean inMemory;
  private int countFileNotToDelete = 2;

  public DirectAccountDataManager(String prevaylerPath, boolean inMemory) {
    this.prevaylerPath = prevaylerPath;
    this.inMemory = inMemory;
  }

  void setCountFileNotToDelete(int countFileNotToDelete) {
    this.countFileNotToDelete = countFileNotToDelete;
  }

  public void getUserData(SerializedOutput output, Integer userId) {
    MapOfMaps<String, Integer, SerializableGlobType> globs = new MapOfMaps<String, Integer, SerializableGlobType>();
    long transactionId = readData(userId, globs);
    outputStreamMap.put(userId, new DurableOutputStream(transactionId, userId));
    SerializableGlobSerializer serializableGlobSerializer = new SerializableGlobSerializer();
    serializableGlobSerializer.serialize(output, globs);
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
    long nextTransactionVersion = 1;
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

  private String getPath(Integer userId) {
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
          apply(globs, map);
        }
        version++;
      }
      catch (EOFIOFailure e) {
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
    return version;
  }

  private void apply(MapOfMaps<String, Integer, SerializableGlobType> globs, MultiMap<String, ServerDelta> map) {
    for (Map.Entry<String, List<ServerDelta>> stringListEntry : map.values()) {
      Map<Integer, SerializableGlobType> globToMerge = globs.get(stringListEntry.getKey());
      for (ServerDelta deltaGlob : stringListEntry.getValue()) {
        if (deltaGlob.getState() == ServerState.DELETED) {
          globToMerge.remove(deltaGlob.getId());
        }
        else {
          globToMerge.put(deltaGlob.getId(), new SerializableGlobType(stringListEntry.getKey(), deltaGlob));
        }
      }
    }
  }

  private long readJournalVersion(SerializedInput serializedInput) {
    String s = serializedInput.readString();
    if (!s.equals("Tr")) {
      return -1;
    }
    return serializedInput.readNotNullLong();
  }

  void readSnapshot(MapOfMaps<String, Integer, SerializableGlobType> globs, File file) {
    try {
      SerializedInput serializedInput =
        SerializedInputOutputFactory.init(new BufferedInputStream(new FileInputStream(file)));
      String version = serializedInput.readString();
      if ("2".equals(version)) {
        readVersion2(serializedInput, globs);
      }
    }
    catch (FileNotFoundException e) {
    }
  }

  private void readVersion2(SerializedInput serializedInput,
                            MapOfMaps<String, Integer, SerializableGlobType> data) {
    SerializableGlobSerializer serializer = new SerializableGlobSerializer();
    serializer.deserialize(serializedInput, data);
  }

  public void updateUserData(SerializedInput input, Integer userId) {
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

  public void delete(Integer userId) {
  }

  public void close() {
    for (DurableOutputStream durableOutputStream : outputStreamMap.values()) {
      durableOutputStream.close();
    }
  }

  public void close(Integer userId) {
    DirectAccountDataManager.DurableOutputStream durableOutputStream = outputStreamMap.get(userId);
    if (durableOutputStream != null) {
      outputStreamMap.remove(userId);
      durableOutputStream.close();
    }
  }

  public void takeSnapshot(Integer userId) {
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

  private void writeSnapshot(long transactionId, MapOfMaps<String, Integer, SerializableGlobType> data,
                             PrevaylerDirectory directory) throws IOException {
    File tempFile = directory.createTempFile("snapshot" + transactionId + "temp", "generatingSnapshot");

    writeSnapshot_V2(data, tempFile);

    File permanent = directory.snapshotFile(transactionId, "snapshot");
    permanent.delete();
    if (!tempFile.renameTo(permanent)) {
      throw new IOFailure("Temporary snapshot file generated: " + tempFile +
                          "\nUnable to rename it permanently to: " + permanent);
    }
  }

  private void writeSnapshot_V2(MapOfMaps<String, Integer, SerializableGlobType> data, File file) throws IOException {
    FileOutputStream outputStream = new FileOutputStream(file);
    SerializedOutput serializedOutput =
      SerializedInputOutputFactory.init(outputStream);
    serializedOutput.writeString("2");
    SerializableGlobSerializer serializer = new SerializableGlobSerializer();
    serializer.serialize(serializedOutput, data);
    outputStream.close();
  }

  private class DurableOutputStream {
    private long nextTransactionVersion;
    private OutputStream outputStream;
    private PrevaylerDirectory prevaylerDirectory;
    private FileDescriptor fd;

    public DurableOutputStream(long nextTransactionVersion, Integer userId) {
      this.nextTransactionVersion = nextTransactionVersion;
      prevaylerDirectory = new PrevaylerDirectory(getPath(userId));
    }

    public void write(MultiMap<String, ServerDelta> data) {
      if (inMemory) {
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
        serializedOutput.writeString("Tr");
        serializedOutput.write(nextTransactionVersion);
        serializableDeltaGlobSerializer.serialize(serializedOutput, data);
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
  }
}
