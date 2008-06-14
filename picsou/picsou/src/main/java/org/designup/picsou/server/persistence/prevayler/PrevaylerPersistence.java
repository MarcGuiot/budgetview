package org.designup.picsou.server.persistence.prevayler;

import org.designup.picsou.client.exceptions.BadPassword;
import org.designup.picsou.client.exceptions.IdentificationFailed;
import org.designup.picsou.client.exceptions.UserNotRegistered;
import org.designup.picsou.server.model.*;
import org.designup.picsou.server.session.Persistence;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.delta.DefaultDeltaGlob;
import org.globsframework.model.delta.DeltaGlob;
import org.globsframework.model.delta.DeltaState;
import org.globsframework.remote.RemoteExecutor;
import org.globsframework.utils.Log;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidData;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;
import org.globsframework.utils.serialization.Encoder;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrevaylerPersistence implements Persistence {
  private RootDataManager rootDataManager;
  private AccountDataManager accountDataManager;
  private Directory directory;

  public PrevaylerPersistence(AccountDataManager accountDataManager, RootDataManager rootDataManager,
                              Directory directory) {
    this.accountDataManager = accountDataManager;
    this.rootDataManager = rootDataManager;
    this.directory = directory;
  }

  public Glob identify(String name, byte[] cryptedPassword) {
    Glob user = rootDataManager.getUser(name);
    if (user == null) {
      throw new UserNotRegistered(name + " not registered");
    }
    if (!Arrays.equals(user.get(User.ENCRYPTED_PASSWORD), cryptedPassword)) {
      Log.write("For " + name + " bad password");
      throw new BadPassword(name + " not identified correctly");
    }
    return user;
  }

  public Integer confirmUser(String b64LinkInfo) throws IdentificationFailed {
    Glob hiddenUser = rootDataManager.getHiddenUser(b64LinkInfo);
    if (hiddenUser == null) {
      throw new InvalidData("User recognized but no hiddenUser associated");
    }
    return hiddenUser.get(HiddenUser.USER_ID);
  }

  public UserInfo createUser(String name, boolean isRegisteredUser, byte[] cryptedPassword, byte[] linkInfo, byte[] cryptedLinkInfo) {
    return rootDataManager.createUserAndHiddenUser(name, isRegisteredUser, cryptedPassword, linkInfo, cryptedLinkInfo);
  }

  public void delete(String name, byte[] cryptedPassword, byte[] linkInfo, byte[] cryptedLinkInfo, Integer userId) {
    accountDataManager.delete(userId);
  }

  public Glob getUser(String name) {
    return rootDataManager.getUser(name);
  }

  public Glob getHiddenUser(byte[] cryptedLinkInfo) {
    return rootDataManager.getHiddenUser(Encoder.b64Decode(cryptedLinkInfo));
  }

  public GlobList getHiddenGlob(GlobType type, Integer userId) {
    return getFiltered(userId, type);
  }

  public void close() {
    rootDataManager.close();
    accountDataManager.close();
  }

  public void close(Integer userId) {
    accountDataManager.close(userId);
  }

  public void takeSnapshot(Integer userId) {
    accountDataManager.takeSnapshot(userId);
  }

  private GlobList getFiltered(int userId, GlobType globType) {
    GlobList list = accountDataManager.getUserData(userId);
    for (java.util.Iterator it = list.iterator(); it.hasNext();) {
      Glob glob = (Glob)it.next();
      if (glob.getType() != globType) {
        it.remove();
      }
    }
    return list;
  }

  public void getData(SerializedOutput output, Integer userId) {
    accountDataManager.getUserData(output, userId);
  }

  public void updateData(SerializedInput input, SerializedOutput output, Integer userId) {
    GlobRequestBuilder globRequestBuilder = new GlobRequestBuilder(userId);
    RemoteExecutor executor = new RemoteExecutor(directory.get(GlobModel.class), globRequestBuilder);
    executor.execute(SerializedInputOutputFactory.init(input.readBytes()));

    accountDataManager.updateUserData(globRequestBuilder.getDelta(), userId);
  }

  public Integer getNextId(String globTypeName, Integer count, Integer userId) {
    return accountDataManager.getNextId(globTypeName, userId, count);
  }

  public void init(Directory directory) {
  }

  private static class GlobRequestBuilder implements RemoteExecutor.RequestBuilder {
    private Integer userId;
    private List<DeltaGlob> delta = new ArrayList<DeltaGlob>();

    public GlobRequestBuilder(Integer userId) {
      this.userId = userId;
    }

    public List<DeltaGlob> getDelta() {
      return delta;
    }

    public RemoteExecutor.UpdateRequest getUpdate(GlobType globType, FieldValues fieldConstraint) {
      UpdateGlobRequest request = new UpdateGlobRequest(globType, fieldConstraint, userId);
      delta.add(request.getDeltaGlob());
      return request;
    }

    public RemoteExecutor.CreateRequest getCreate(GlobType globType, FieldValues fieldValues) {
      CreateGlobRequest request = new CreateGlobRequest(globType, fieldValues, userId);
      delta.add(request.getDeltaGlob());
      return request;
    }

    public RemoteExecutor.DeleteRequest getDelete(GlobType globType, FieldValues fieldValues) {
      DeleteGlobRequest request = new DeleteGlobRequest(globType, fieldValues, userId);
      delta.add(request.getDeltaGlob());
      return request;
    }

    private static class CreateGlobRequest extends GlobRequest implements RemoteExecutor.CreateRequest {
      public CreateGlobRequest(GlobType globType, FieldValues values, Integer userId) {
        super(globType, values, userId, DeltaState.CREATED);
      }

      public void create() {
      }
    }

    private static class UpdateGlobRequest extends GlobRequest implements RemoteExecutor.UpdateRequest {

      public UpdateGlobRequest(GlobType globType, FieldValues values, Integer userId) {
        super(globType, values, userId, DeltaState.UPDATED);
      }

      public void update() {
      }
    }

    private static class DeleteGlobRequest extends GlobRequest implements RemoteExecutor.DeleteRequest {
      public DeleteGlobRequest(GlobType globType, FieldValues values, Integer userId) {
        super(globType, values, userId, DeltaState.DELETED);
      }

      public void delete() {
      }
    }

    private static class GlobRequest implements RemoteExecutor.Request, HiddenServerTypeVisitor {
      private DeltaGlob deltaGlob;
      private GlobType globType;
      private FieldValues values;
      private Integer userId;
      private Key key;

      public GlobRequest(GlobType globType, FieldValues values, Integer userId, DeltaState state) {
        this.globType = globType;
        this.values = values;
        this.userId = userId;
        HiddenServerTypeVisitor.Visitor.safeVisit(globType, this);
        deltaGlob = new DefaultDeltaGlob(key);
        deltaGlob.setState(state);
      }

      public void update(Field field, Object value) {
        deltaGlob.setObject(field, value);
      }

      public DeltaGlob getDeltaGlob() {
        return deltaGlob;
      }

      public void visitHiddenTransaction() throws Exception {
        key = KeyBuilder.init(HiddenTransaction.ID, values.get(HiddenTransaction.ID))
          .setValue(HiddenTransaction.HIDDEN_USER_ID, userId).get();
      }

      public void visitHiddenBank() throws Exception {
        key = KeyBuilder.init(HiddenBank.ID, values.get(HiddenBank.ID))
          .setValue(HiddenBank.HIDDEN_USER_ID, userId).get();
      }

      public void visitHiddenAccount() throws Exception {
        key = KeyBuilder.init(HiddenAccount.ID, values.get(HiddenAccount.ID))
          .setValue(HiddenAccount.HIDDEN_USER_ID, userId).get();
      }

      public void visitHiddenTransactionToCategory() throws Exception {
        key = KeyBuilder
          .init(HiddenTransactionToCategory.CATEGORY_ID, values.get(HiddenTransactionToCategory.CATEGORY_ID))
          .setValue(HiddenTransactionToCategory.TRANSACTION_ID, values.get(HiddenTransactionToCategory.TRANSACTION_ID))
          .setValue(HiddenTransactionToCategory.HIDDEN_USER_ID, userId)
          .get();
      }

      public void visitHiddenLabelToCategory() throws Exception {
        key = KeyBuilder
          .init(HiddenLabelToCategory.ID, values.get(HiddenLabelToCategory.ID))
          .setValue(HiddenLabelToCategory.HIDDEN_USER_ID, userId)
          .get();
      }

      public void visitHiddenImport() throws Exception {
        key = KeyBuilder
          .init(HiddenImport.ID, values.get(HiddenImport.ID))
          .add(HiddenImport.HIDDEN_USER_ID, userId)
          .get();
      }

      public void visitHiddenCategory() throws Exception {
        key = KeyBuilder
          .init(HiddenCategory.ID, values.get(HiddenCategory.ID))
          .add(HiddenCategory.HIDDEN_USER_ID, userId)
          .get();
      }

      public void visitOther() throws Exception {
        throw new UnexpectedApplicationState(globType.getName() + " not managed");
      }
    }
  }
}
