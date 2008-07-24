package org.designup.picsou.server.persistence.prevayler.users;

import org.designup.picsou.server.persistence.prevayler.CustomSerializablePolicy;
import org.designup.picsou.server.persistence.prevayler.DefaultSerializer;
import org.designup.picsou.server.persistence.prevayler.RootDataManager;
import org.designup.picsou.server.session.Persistence;
import org.globsframework.model.Glob;
import org.globsframework.utils.Log;
import org.globsframework.utils.T3uples;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.GlobsException;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;
import org.prevayler.Prevayler;
import org.prevayler.PrevaylerFactory;
import org.prevayler.Query;
import org.prevayler.foundation.serialization.Serializer;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class PRootDataManager implements RootDataManager {
  private Prevayler prevayler;
  private Directory directory;

  public PRootDataManager(String path, Directory directory, boolean inMemory) {
    this.directory = directory;
    PrevaylerFactory prevaylerFactory = new PrevaylerFactory();

    prevaylerFactory.configurePrevalenceDirectory(getPathToPrevayler(path));

    Serializer serializer = new DefaultSerializer(initSerializerPolicy());
    prevaylerFactory.configureJournalSerializer("journal", serializer);
    prevaylerFactory.configureSnapshotSerializer("snapshot", serializer);
    prevaylerFactory.configureTransientMode(inMemory);
    prevaylerFactory.configurePrevalentSystem(new PRootData());
    try {
      prevayler = prevaylerFactory.create();
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
    serializablePolicy.registerFactory(CreateUserAndHiddenUser.getFactory());
    serializablePolicy.registerFactory(DeleteUserAndHiddenUser.getFactory());
    serializablePolicy.registerFactory(Register.getFactory());
    serializablePolicy.registerFactory(PRootData.getFactory());
    return serializablePolicy;
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

  public void register(final byte[] mail, final byte[] signature) {
    prevayler.execute(new Register(mail, signature));
  }

  public Persistence.UserInfo createUserAndHiddenUser(String name, boolean isRegisteredUser,
                                                      byte[] cryptedPassword, byte[] linkInfo, byte[] cryptedLinkInfo) {
    try {
      Persistence.UserInfo userInfo = (Persistence.UserInfo)prevayler.execute(new CreateUserAndHiddenUser(name, isRegisteredUser, cryptedPassword,
                                                                                                          linkInfo, cryptedLinkInfo));
      prevayler.takeSnapshot();
      return userInfo;
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
    try {
      prevayler.close();
    }
    catch (IOException e) {
      Log.write("prevayler close fail", e);
    }
  }

  public T3uples<byte[], byte[], Long> getAccountInfo() {
    try {
      return (T3uples<byte[], byte[], Long>)prevayler.execute(new Query() {
        public Object query(Object prevalentSystem, Date executionTime) throws Exception {
          PRootData rootData = (PRootData)prevalentSystem;
          return new T3uples<byte[], byte[], Long>(rootData.getMail(), rootData.getSignature(), rootData.getCount());
        }
      });
    }
    catch (Exception e) {
      throw new UnexpectedApplicationState(e);
    }
  }
}
