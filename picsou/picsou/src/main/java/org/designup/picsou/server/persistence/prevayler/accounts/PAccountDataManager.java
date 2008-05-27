package org.designup.picsou.server.persistence.prevayler.accounts;

import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.delta.DeltaGlob;
import org.crossbowlabs.globs.utils.Log;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.globs.utils.exceptions.GlobsException;
import org.crossbowlabs.globs.utils.exceptions.UnexpectedApplicationState;
import org.crossbowlabs.globs.utils.serialization.SerializedOutput;
import org.designup.picsou.server.persistence.prevayler.AccountDataManager;
import org.designup.picsou.server.persistence.prevayler.CustomSerializablePolicy;
import org.designup.picsou.server.persistence.prevayler.DefaultSerializer;
import org.designup.picsou.server.persistence.prevayler.categories.RegisterAssociatedCategory;
import org.prevayler.Prevayler;
import org.prevayler.PrevaylerFactory;
import org.prevayler.Transaction;
import org.prevayler.foundation.serialization.Serializer;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PAccountDataManager implements AccountDataManager {
  private Map<Integer, Prevayler> userIdToPrevayler = new HashMap<Integer, Prevayler>();
  private String root;
  private boolean inMemory;
  private Directory directory;

  public PAccountDataManager(String path, Directory directory, boolean inMemory) {
    this.directory = directory;
    this.root = path;
    this.inMemory = inMemory;
  }

  public void getUserData(SerializedOutput output, Integer userId) {
    try {
      getPrevayler(userId).execute(new GetSerializedUserDataTransaction(output));
    }
    catch (GlobsException e) {
      throw e;
    }
    catch (Exception e) {
      throw new UnexpectedApplicationState(e);
    }
  }

  public GlobList getUserData(Integer userId) {
    try {
      return (GlobList) getPrevayler(userId).execute(new GetUserDataTransaction());
    }
    catch (GlobsException e) {
      throw e;
    }
    catch (Exception e) {
      throw new UnexpectedApplicationState(e);
    }
  }

  synchronized public void close() {
    for (Prevayler prevayler : userIdToPrevayler.values()) {
      try {
        prevayler.close();
      }
      catch (IOException e) {
        Log.write("close fail ", e);
      }
    }
  }

  synchronized public void close(Integer userId) {
    Prevayler prevayler = userIdToPrevayler.remove(userId);
    if (prevayler != null) {
      try {
        prevayler.close();
      }
      catch (IOException e) {
        Log.write("close fail ", e);
      }
    }
  }

  public void takeSnapshot(Integer userId) {
    try {
      getPrevayler(userId).takeSnapshot();
    }
    catch (GlobsException e) {
      throw e;
    }
    catch (Exception e) {
      throw new UnexpectedApplicationState(e);
    }
  }

  public void updateUserData(List<DeltaGlob> deltaGlobs, Integer userId) {
    try {
      getPrevayler(userId).execute(new ApplyUserDataTransaction(deltaGlobs));
    }
    catch (GlobsException e) {
      throw e;
    }
    catch (Exception e) {
      throw new UnexpectedApplicationState(e);
    }
  }

  public Integer getNextId(String globTypeName, Integer userId, Integer count) {
    try {
      return (Integer) getPrevayler(userId).execute(new GetNextId(globTypeName, count));
    }
    catch (GlobsException e) {
      throw e;
    }
    catch (Exception e) {
      throw new UnexpectedApplicationState(e);
    }
  }

  public void delete(Integer userId) {
    String pathToPrevayler = root + "/usersData/" + userId;
    File file = new File(pathToPrevayler);
    file.renameTo(new File("../deleted/"));
    getPrevayler(userId).execute(new Transaction() {
      public void executeOn(Object object, Date date) {
        ((UserData) object).delete();
      }
    });
  }

  public Prevayler getPrevayler(Integer userId) {
    synchronized (this) {
      Prevayler prevayler = userIdToPrevayler.get(userId);
      if (prevayler != null) {
        return prevayler;
      }
      String pathToPrevayler = root + "/usersData/" + userId;
      File file = new File(pathToPrevayler);
      if (!file.exists()) {
        file.mkdirs();
      }
      try {
        prevayler = getPrevayler(pathToPrevayler);
      }
      catch (Exception e) {
        throw new UnexpectedApplicationState("for userId " + userId, e);
      }

      userIdToPrevayler.put(userId, prevayler);
      return prevayler;
    }
  }

  private Prevayler getPrevayler(String pathToPrevayler) throws IOException, ClassNotFoundException {
    PrevaylerFactory prevaylerFactory = new PrevaylerFactory();
    prevaylerFactory.configurePrevalenceDirectory(pathToPrevayler);
    CustomSerializablePolicy serializablePolicy = initSerializerPolicy();
    Serializer serializer = new DefaultSerializer(serializablePolicy);
    prevaylerFactory.configureJournalSerializer("journal", serializer);
    prevaylerFactory.configureSnapshotSerializer("snapshot", serializer);
    prevaylerFactory.configurePrevalentSystem(new UserData());
    prevaylerFactory.configureTransientMode(inMemory);
    return prevaylerFactory.create();
  }

  private CustomSerializablePolicy initSerializerPolicy() {
    CustomSerializablePolicy serializablePolicy = new CustomSerializablePolicy(directory);
    serializablePolicy.registerFactory(ApplyUserDataTransaction.getFactory());
    serializablePolicy.registerFactory(GetNextId.getFactory());
    serializablePolicy.registerFactory(RegisterAssociatedCategory.getFactory());
    serializablePolicy.registerFactory(UserData.getFactory());
    return serializablePolicy;
  }
}
