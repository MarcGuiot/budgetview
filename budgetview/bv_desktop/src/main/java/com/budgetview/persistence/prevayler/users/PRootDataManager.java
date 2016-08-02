package com.budgetview.persistence.prevayler.users;

import com.budgetview.persistence.prevayler.DefaultSerializer;
import com.budgetview.persistence.prevayler.RootDataManager;
import com.budgetview.persistence.prevayler.CustomSerializablePolicy;
import com.budgetview.session.states.Persistence;
import com.budgetview.client.exceptions.RemoteException;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.GlobsException;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;
import org.prevayler.Prevayler;
import org.prevayler.PrevaylerFactory;
import org.prevayler.Query;
import org.prevayler.foundation.serialization.Serializer;
import org.prevayler.implementation.PrevaylerDirectory;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class PRootDataManager implements RootDataManager {
  private Prevayler prevayler;
  private Directory directory;
  private String pathToPrevaylerDirectory;
  private static final int SNASPHOT_TO_PRESERVE = 3;

  public PRootDataManager(String path, Directory directory, boolean inMemory) {
    this.directory = directory;
    PrevaylerFactory prevaylerFactory = new PrevaylerFactory();

    pathToPrevaylerDirectory = getPathToPrevayler(path);
    prevaylerFactory.configurePrevalenceDirectory(pathToPrevaylerDirectory);
    prevaylerFactory.configureTransactionFiltering(false);
    Serializer serializer = new DefaultSerializer(initSerializerPolicy());
    prevaylerFactory.configureJournalSerializer("journal", serializer);
    prevaylerFactory.configureSnapshotSerializer("snapshot", serializer);
    prevaylerFactory.configureTransientMode(inMemory);
    prevaylerFactory.configurePrevalentSystem(new PRootData());
    try {
      prevayler = prevaylerFactory.create();
      Object result = prevayler.execute(new Query() {
        public Object query(Object prevalentSystem, Date executionTime) throws Exception {
          return ((PRootData)prevalentSystem).getId();
        }
      });
      if (result == null) {
        prevayler.execute(InitialRepoIdTransaction.create());
      }
    }
    catch (RuntimeException e){
      throw e;
    }
    catch (Exception e) {
      throw new UnexpectedApplicationState(e);
    }
  }

  private String getPathToPrevayler(String url) {
    String pathToPrevayler = url + "/users";
    File file = new File(pathToPrevayler);
    if (!file.exists()) {
      file.mkdirs();
    }
    return pathToPrevayler;
  }

  private CustomSerializablePolicy initSerializerPolicy() {
    CustomSerializablePolicy serializablePolicy = new CustomSerializablePolicy(directory);
    serializablePolicy.registerFactory(RenameUserAndHiddenUser.getFactory());
    serializablePolicy.registerFactory(AllocateNewUserId.getFactory());
    serializablePolicy.registerFactory(CreateUserAndHiddenUser.getFactory());
    serializablePolicy.registerFactory(DeleteUserAndHiddenUser.getFactory());
    serializablePolicy.registerFactory(GetAndUpdateCount.getFactory());
    serializablePolicy.registerFactory(InitialRepoIdTransaction.getFactory());
    serializablePolicy.registerFactory(Register.getFactory());
    serializablePolicy.registerFactory(PRootData.getFactory());
    serializablePolicy.registerFactory(SetDownloadedVersion.getFactory());
    serializablePolicy.registerFactory(SetLang.getFactory());
    serializablePolicy.registerFactory(SetJarVersion.getFactory());
    return serializablePolicy;
  }

  public GlobList getLocalUsers() {
    try {
      return (GlobList)prevayler.execute(new Query() {
        public Object query(Object prevalentSystem, Date executionTime) throws Exception {
          return ((PRootData)prevalentSystem).getLocalUsers();
        }
      });
    }
    catch (Exception e) {
      throw new UnexpectedApplicationState(e);
    }
  }

  public Integer allocateNewUserId(String name) {
    try {
      Integer newId =
        (Integer)prevayler.execute(new AllocateNewUserId(name));
      return newId;
    }
    catch (GlobsException e) {
      throw e;
    }
    catch (Exception e) {
      throw new UnexpectedApplicationState(pathToPrevaylerDirectory, e);
    }
  }

  public Glob getHiddenUser(final String linkInfo) {
    try {
      return (Glob)prevayler.execute(new Query() {
        public Object query(Object prevalentSystem, Date executionTime) throws Exception {
          return ((PRootData)prevalentSystem).getHiddenUser(linkInfo);
        }
      });
    }
    catch (Exception e) {
      throw new UnexpectedApplicationState(e);
    }
  }

  public Glob getUser(final String name) {
    try {
      return (Glob)prevayler.execute(new Query() {
        public Object query(Object prevalentSystem, Date executionTime) throws Exception {
          return ((PRootData)prevalentSystem).getUser(name);
        }
      });
    }
    catch (Exception e) {
      throw new UnexpectedApplicationState(e);
    }
  }

  public void register(final byte[] mail, final byte[] signature, String activationCode) {
    prevayler.execute(new Register(mail, signature, activationCode));
  }

  public Persistence.UserInfo createUserAndHiddenUser(String name, boolean autoLog, boolean isRegisteredUser,
                                          byte[] cryptedPassword, byte[] linkInfo,
                                          byte[] cryptedLinkInfo, Integer userId) {
    try {
      Persistence.UserInfo userInfo =
        (Persistence.UserInfo)prevayler.execute(new CreateUserAndHiddenUser(name, autoLog, isRegisteredUser, cryptedPassword,
                                                                            linkInfo, cryptedLinkInfo, userId));
      prevayler.takeSnapshot();
      return userInfo;
    }
    catch (GlobsException e) {
      throw e;
    }
    catch (Exception e) {
      throw new UnexpectedApplicationState(pathToPrevaylerDirectory, e);
    }
  }

  public void replaceUserAndHiddenUser(boolean autoLog, boolean isRegisteredUser,
                                                       String newName, byte[] newCryptedPassword,
                                                       byte[] newLinkInfo, byte[] newCryptedLinkInfo,
                                                       String name, byte[] linkInfo, byte[] cryptedLinkInfo,
                                                       Integer userId) {
    try {
      RemoteException error =
        (RemoteException)prevayler.execute(
          new RenameUserAndHiddenUser(autoLog, isRegisteredUser, newName, newCryptedPassword, newLinkInfo, newCryptedLinkInfo,
                                      name, linkInfo, cryptedLinkInfo, userId));
      if (error != null){
        throw error;
      }
      prevayler.takeSnapshot();
    }
    catch (GlobsException e) {
      throw e;
    }
    catch (Exception e) {
      throw new UnexpectedApplicationState(pathToPrevaylerDirectory, e);
    }
  }

  public void setDownloadedVersion(long version) {
    try {
      prevayler.execute(new SetDownloadedVersion(version));
      prevayler.takeSnapshot();
    }
    catch (GlobsException e) {
      throw e;
    }
    catch (Exception e) {
      throw new UnexpectedApplicationState(e);
    }
  }

  public void setLang(String lang) {
    try {
      prevayler.execute(new SetLang(lang));
      prevayler.takeSnapshot();
    }
    catch (GlobsException e) {
      throw e;
    }
    catch (Exception e) {
      throw new UnexpectedApplicationState(e);
    }
  }

  public void deleteUser(String name, byte[] cryptedLinkInfo) {
    try {
      prevayler.execute(new DeleteUserAndHiddenUser(name, cryptedLinkInfo));
      prevayler.takeSnapshot();
    }
    catch (GlobsException e) {
      throw e;
    }
    catch (Exception e) {
      throw new UnexpectedApplicationState(e);
    }
  }

  public void close() {
    if (prevayler == null) {
      return;
    }
    try {
      prevayler.takeSnapshot();
      PrevaylerDirectory directory = new PrevaylerDirectory(pathToPrevaylerDirectory);
      long lastTransactionId = directory.deletePreviousSnapshot(SNASPHOT_TO_PRESERVE);
      directory.deletePreviousJournal(lastTransactionId);
    }
    catch (IOException e) {
    }
    try {
      prevayler.close();
      prevayler = null;
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public RepoInfo getAndUpdateAccountInfo(long version) {
    try {
      RepoInfo execute = (RepoInfo)prevayler.execute(new GetAndUpdateCount());
      if (execute.getVersion() < version) {
        prevayler.execute(new SetJarVersion(version));
      }
      return execute;
    }
    catch (Exception e) {
      throw new UnexpectedApplicationState(e);
    }
  }

  private void dump() {
    try {
      prevayler.execute(new Query() {
        public Object query(Object prevalentSystem, Date executionTime) throws Exception {
          ((PRootData)prevalentSystem).dump();
          return null;
        }
      });
    }
    catch (Exception e) {
      throw new UnexpectedApplicationState(e);
    }
  }

  public static void main(String[] args) {
    PRootDataManager rootDataManager = new PRootDataManager(args[0], new DefaultDirectory(), false);
    rootDataManager.dump();
  }
}
