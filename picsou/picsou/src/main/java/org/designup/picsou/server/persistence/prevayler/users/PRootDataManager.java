package org.designup.picsou.server.persistence.prevayler.users;

import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.utils.Log;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.globs.utils.exceptions.GlobsException;
import org.crossbowlabs.globs.utils.exceptions.UnexpectedApplicationState;
import org.designup.picsou.server.persistence.prevayler.CustomSerializablePolicy;
import org.designup.picsou.server.persistence.prevayler.DefaultSerializer;
import org.designup.picsou.server.persistence.prevayler.RootDataManager;
import org.designup.picsou.server.persistence.prevayler.categories.GetAssociatedCategory;
import org.designup.picsou.server.persistence.prevayler.categories.RegisterAssociatedCategory;
import org.designup.picsou.server.session.Persistence;
import org.prevayler.Prevayler;
import org.prevayler.PrevaylerFactory;
import org.prevayler.Query;
import org.prevayler.foundation.serialization.Serializer;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

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

  public List<Persistence.CategoryInfo> getAssociatedCategory(List<String> infos, Integer userId) {
    try {
      return (List<Persistence.CategoryInfo>)prevayler.execute(new GetAssociatedCategory(infos));
    }
    catch (GlobsException e) {
      throw e;
    }
    catch (Exception e) {
      throw new UnexpectedApplicationState(e);
    }
  }

  public void registerCategory(String info, int categoryId, Integer userId) {
    try {
      prevayler.execute(new RegisterAssociatedCategory(info, categoryId));
    }
    catch (GlobsException e) {
      throw e;
    }
    catch (Exception e) {
      throw new UnexpectedApplicationState(e);
    }
  }
}
