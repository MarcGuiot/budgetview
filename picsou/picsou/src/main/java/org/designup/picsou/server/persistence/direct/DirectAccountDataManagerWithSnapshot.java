package org.designup.picsou.server.persistence.direct;

import org.designup.picsou.client.SerializableDeltaGlobSerializer;
import org.designup.picsou.client.SerializableGlobSerializer;
import org.designup.picsou.server.model.SerializableGlobType;
import org.designup.picsou.server.persistence.prevayler.AccountDataManager;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.delta.DeltaGlob;
import org.globsframework.model.delta.DeltaState;
import org.globsframework.model.utils.GlobBuilder;
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

public class DirectAccountDataManagerWithSnapshot implements AccountDataManager {
  private Map<Integer, DurableOutputStream> outputStreamMap = new HashMap<Integer, DurableOutputStream>();
  private String prevaylerPath;
  private boolean inMemory;

  public DirectAccountDataManagerWithSnapshot(String prevaylerPath, boolean inMemory) {
    this.prevaylerPath = prevaylerPath;
    this.inMemory = inMemory;
  }

  public void getUserData(SerializedOutput output, Integer userId) {
    MapOfMaps<String, Integer, Glob> globs = new MapOfMaps<String, Integer, Glob>();
    readData(userId, globs);
    SerializableGlobSerializer serializableGlobSerializer = new SerializableGlobSerializer();
    serializableGlobSerializer.serialize(output, globs);
  }

  private long readData(Integer userId, MapOfMaps<String, Integer, Glob> globs) {
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
      readSnapshot(file, globs);
    }
    File journalFile = prevaylerDirectory.findInitialJournalFile(snapshotVersion);
    if (journalFile == null) {
      outputStreamMap.put(userId, new DurableOutputStream(snapshotVersion, userId));
    }
    else {
      try {
        snapshotVersion = readFrom(globs, snapshotVersion, journalFile, prevaylerDirectory);
        outputStreamMap.put(userId, new DurableOutputStream(snapshotVersion, userId));
      }
      catch (FileNotFoundException e) {
      }
    }
    return snapshotVersion;
  }

  private String getPath(Integer userId) {
    return prevaylerPath + "/" + userId.toString();
  }

  private long readFrom(MapOfMaps<String, Integer, Glob> globs, long snapshotVersion,
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
        MultiMap<String, DeltaGlob> map = serializableDeltaGlobSerializer.deserialize(serializedInput);
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

  private void apply(MapOfMaps<String, Integer, Glob> globs, MultiMap<String, DeltaGlob> map) {
    for (Map.Entry<String, List<DeltaGlob>> stringListEntry : map.values()) {
      Map<Integer, Glob> globToMerge = globs.get(stringListEntry.getKey());
      for (DeltaGlob deltaGlob : stringListEntry.getValue()) {
        if (deltaGlob.getState() == DeltaState.DELETED) {
          globToMerge.remove(deltaGlob.get(SerializableGlobType.ID));
        }
        else {
//          globToMerge.put(deltaGlob.get(SerializableGlobType.ID), deltaGlob);
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

  private void readSnapshot(File file, MapOfMaps<String, Integer, Glob> globs) {
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

  /*
   format is :
     count
     globTypeName
       id
       value
  */
  private void readVersion2(SerializedInput serializedInput,
                            MapOfMaps<String, Integer, Glob> globs) {
    try {
      int count;
      count = serializedInput.readNotNullInt();
      if (count == 0) {
        return;
      }
      String globTypeName = serializedInput.readString();
      while (count != 0) {
        int id = serializedInput.readNotNullInt();
        GlobBuilder globBuilder = GlobBuilder.init(SerializableGlobType.TYPE)
          .set(SerializableGlobType.ID, id)
          .set(SerializableGlobType.GLOB_TYPE_NAME, globTypeName)
          .set(SerializableGlobType.DATA, serializedInput.readBytes());
        globs.put(globTypeName, id, globBuilder.get());
        count--;
      }
    }
    catch (UnexpectedApplicationState e) {
    }
  }

  public void updateUserData(SerializedInput input, Integer userId) {
    SerializableDeltaGlobSerializer serializableDeltaGlobSerializer = new SerializableDeltaGlobSerializer();
    MultiMap<String, DeltaGlob> map = serializableDeltaGlobSerializer.deserialize(input);
    DurableOutputStream bufferedOutputStream = outputStreamMap.get(userId);
    if (bufferedOutputStream == null) {
      throw new RuntimeException("read should be call before write");
    }
    bufferedOutputStream.write(map);
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
    MapOfMaps<String, Integer, Glob> globs = new MapOfMaps<String, Integer, Glob>();
    long transactionId = readData(userId, globs);
    writeSnapshot(userId, transactionId, globs);
  }

  private void writeSnapshot(Integer userId, long version, MapOfMaps<String, Integer, Glob> globs) {
    PrevaylerDirectory directory = new PrevaylerDirectory(getPath(userId));
//    File tempFile = directory.createTempFile("snapshot" + version + "temp", "generatingSnapshot");
//
//    writeSnapshot(prevalentSystem, tempFile);
//
//    File permanent = snapshotFile(version);
//    permanent.delete();
//    if (!tempFile.renameTo(permanent)) throw new IOFailure(
//        "Temporary snapshot file generated: " + tempFile + "\nUnable to rename it permanently to: " + permanent);
  }

  private class DurableOutputStream {
    private long nextTransactionId;
    private OutputStream outputStream;
    private PrevaylerDirectory prevaylerDirectory;
    private FileDescriptor fd;

    public DurableOutputStream(long nextTransactionId, Integer userId) {
      this.nextTransactionId = nextTransactionId;
      prevaylerDirectory = new PrevaylerDirectory(getPath(userId));
    }

    public void write(MultiMap<String, DeltaGlob> deltaGlob) {
      if (inMemory) {
        return;
      }
      try {
        if (outputStream == null) {
          File file = prevaylerDirectory.journalFile(nextTransactionId, "journal");
          FileOutputStream stream = new FileOutputStream(file);
          fd = stream.getFD();
          outputStream = new BufferedOutputStream(stream);
        }
        SerializableDeltaGlobSerializer serializableDeltaGlobSerializer = new SerializableDeltaGlobSerializer();
        SerializedOutput serializedOutput = SerializedInputOutputFactory.init(outputStream);
        serializedOutput.writeString("Tr");
        serializedOutput.write(nextTransactionId);
        serializableDeltaGlobSerializer.serialize(serializedOutput, deltaGlob);
        outputStream.flush();
        fd.sync();
        nextTransactionId++;
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}