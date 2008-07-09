package org.designup.picsou.server.persistence.direct;

import org.designup.picsou.client.SerializableDeltaGlobSerializer;
import org.designup.picsou.client.SerializableGlobSerializer;
import org.designup.picsou.server.model.SerializableGlobType;
import org.designup.picsou.server.model.ServerDelta;
import org.designup.picsou.server.model.ServerState;
import org.designup.picsou.server.persistence.prevayler.AccountDataManager;
import org.globsframework.model.GlobList;
import org.globsframework.utils.MapOfMaps;
import org.globsframework.utils.MultiMap;
import org.globsframework.utils.exceptions.EOFIOFailure;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;
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

  public DirectAccountDataManager(String prevaylerPath, boolean inMemory) {
    this.prevaylerPath = prevaylerPath;
    this.inMemory = inMemory;
  }

  public void getUserData(SerializedOutput output, Integer userId) {
    MapOfMaps<String, Integer, SerializableGlobType> globs = readData(userId);
    SerializableGlobSerializer serializableGlobSerializer = new SerializableGlobSerializer();
    serializableGlobSerializer.serialize(output, globs);
  }

  private MapOfMaps<String, Integer, SerializableGlobType> readData(Integer userId) {
    String path = getPath(userId);
    File file1 = new File(path);
    if (!file1.exists()) {
      file1.mkdirs();
    }
    final PrevaylerDirectory prevaylerDirectory = new PrevaylerDirectory(path);
    File file = prevaylerDirectory.latestSnapshot();
    long snapshotVersion = 1;
    MapOfMaps<String, Integer, SerializableGlobType> globs = new MapOfMaps<String, Integer, SerializableGlobType>();
    if (file != null) {
      snapshotVersion = PrevaylerDirectory.snapshotVersion(file);
      globs = readSnapshot(file);
    }
    File journalFile = prevaylerDirectory.findInitialJournalFile(snapshotVersion);
    if (journalFile == null) {
      outputStreamMap.put(userId, new DurableOutputStream(1, userId));
    }
    else {
      try {
        long nextTransactionVersion = readFrom(globs, snapshotVersion, journalFile, prevaylerDirectory);
        outputStreamMap.put(userId, new DurableOutputStream(nextTransactionVersion, userId));
      }
      catch (FileNotFoundException e) {
      }
    }
    return globs;
  }

  private String getPath(Integer userId) {
    return prevaylerPath + "/" + userId.toString();
  }

  private long readFrom(MapOfMaps<String, Integer, SerializableGlobType> globs, long snapshotVersion,
                        File nextTransactionFile, PrevaylerDirectory prevaylerDirectory) throws FileNotFoundException {
    long version = snapshotVersion;
    File file = nextTransactionFile;
    BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
    while (true) {
      try {
        SerializedInput serializedInput =
          SerializedInputOutputFactory.init(inputStream);
        long readVersion = readJournalVersion(serializedInput);
        if (readVersion != version) {
          throw new RuntimeException("error while reading journal file");
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

  private MapOfMaps<String, Integer, SerializableGlobType> readSnapshot(File file) {
    try {
      SerializedInput serializedInput =
        SerializedInputOutputFactory.init(new BufferedInputStream(new FileInputStream(file)));
      String version = serializedInput.readString();
      if ("2".equals(version)) {
        return readVersion2(serializedInput);
      }
    }
    catch (FileNotFoundException e) {
    }
    return new MapOfMaps<String, Integer, SerializableGlobType>();
  }

  /*
   format is :
     count
     globTypeName
       id
       value
  */
  private MapOfMaps<String, Integer, SerializableGlobType> readVersion2(SerializedInput serializedInput) {
    try {
      MapOfMaps<String, Integer, SerializableGlobType> globMapOfMaps = new MapOfMaps<String, Integer, SerializableGlobType>();
      int count;
      count = serializedInput.readNotNullInt();
      if (count == 0) {
        return globMapOfMaps;
      }
      String globTypeName = serializedInput.readString();
      while (count != 0) {
        int id = serializedInput.readNotNullInt();
        SerializableGlobType data = new SerializableGlobType();
        data.setId(id);
        data.setGlobTypeName(globTypeName);
        data.setData(serializedInput.readBytes());
        globMapOfMaps.put(globTypeName, id, data);
        count--;
      }
    }
    catch (UnexpectedApplicationState e) {
    }
    return new MapOfMaps<String, Integer, SerializableGlobType>();
  }

  public void updateUserData(SerializedInput input, Integer userId) {
    SerializableDeltaGlobSerializer serializableDeltaGlobSerializer = new SerializableDeltaGlobSerializer();
    MultiMap<String, ServerDelta> data = serializableDeltaGlobSerializer.deserialize(input);
    DurableOutputStream bufferedOutputStream = outputStreamMap.get(userId);
    if (bufferedOutputStream == null) {
      throw new RuntimeException("read should be call before write");
    }
    bufferedOutputStream.write(data);
  }

  public Integer getNextId(String globTypeName, Integer userId, Integer count) {
    return null;
  }

  public void delete(Integer userId) {
  }

  public GlobList getUserData(Integer userId) {
    return null;
  }

  public void close() {
  }

  public void close(Integer userId) {
  }

  public void takeSnapshot(Integer userId) {
    MapOfMaps<String, Integer, SerializableGlobType> stringIntegerGlobMapOfMaps = readData(userId);
    PrevaylerDirectory prevaylerDirectory = new PrevaylerDirectory(getPath(userId));
//    File file = prevaylerDirectory.snapshotFile();

//    readAndWriteTransaction(userId);
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
        throw new RuntimeException(e);
      }
    }
  }
}
