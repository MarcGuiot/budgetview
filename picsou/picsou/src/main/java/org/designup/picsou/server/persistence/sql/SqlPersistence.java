package org.designup.picsou.server.persistence.sql;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobModel;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.model.FieldValues;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.remote.RemoteExecutor;
import org.crossbowlabs.globs.remote.impl.DefaultCreateRequest;
import org.crossbowlabs.globs.remote.impl.DefaultDeleteRequest;
import org.crossbowlabs.globs.remote.impl.DefaultUpdateRequest;
import org.crossbowlabs.globs.sqlstreams.CreateBuilder;
import org.crossbowlabs.globs.sqlstreams.SqlConnection;
import org.crossbowlabs.globs.sqlstreams.SqlService;
import org.crossbowlabs.globs.sqlstreams.constraints.Constraint;
import org.crossbowlabs.globs.sqlstreams.constraints.Constraints;
import org.crossbowlabs.globs.sqlstreams.drivers.jdbc.DbGlobIdGenerator;
import org.crossbowlabs.globs.streams.GlobStream;
import org.crossbowlabs.globs.streams.accessors.BlobAccessor;
import org.crossbowlabs.globs.streams.accessors.IntegerAccessor;
import org.crossbowlabs.globs.utils.Ref;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.globs.utils.exceptions.UnexpectedApplicationState;
import org.crossbowlabs.globs.utils.serialization.Encoder;
import org.crossbowlabs.globs.utils.serialization.SerializedInput;
import org.crossbowlabs.globs.utils.serialization.SerializedInputOutputFactory;
import org.crossbowlabs.globs.utils.serialization.SerializedOutput;
import org.designup.picsou.client.exceptions.IdentificationFailed;
import org.designup.picsou.client.exceptions.UserAlreadyExists;
import org.designup.picsou.server.model.*;
import org.designup.picsou.server.persistence.prevayler.RootDataManager;
import org.designup.picsou.server.session.Persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SqlPersistence implements Persistence {
  private SqlService sqlService;
  protected Directory directory;

  public SqlPersistence(SqlService sqlService, Directory directory) {
    this.sqlService = sqlService;
    this.directory = directory;
  }

  public RootDataManager.UserInfo createUser(String name, boolean isRegisteredUser, byte[] cryptedPassword, byte[] linkInfo, byte[] cryptedLinkInfo) {
    SqlConnection sqlConnection = sqlService.getDb();
    try {
      Ref<BlobAccessor> linkInfoAccessor = new Ref<BlobAccessor>();
      GlobStream userStream = sqlConnection.getQueryBuilder(User.TYPE, Constraints.equal(User.NAME, name))
        .select(User.LINK_INFO, linkInfoAccessor)
        .getQuery().execute();
      if (userStream.next()) {
//        byte[] value = linkInfoAccessor.get().getValue();
//        if (value != null && value.length == 0) {
        throw new UserAlreadyExists("User '" + name + "' already registered");
//        }
      }
//      else {
//        throw new UserNotRegisteredException("User '" + name + "' not registered");
//      }
      GlobStream hiddenUserStream =
        sqlConnection.getQueryBuilder(HiddenUser.TYPE,
                                      Constraints.equal(HiddenUser.ENCRYPTED_LINK_INFO,
                                                        Encoder.b64Decode(cryptedLinkInfo)))
          .getQuery().execute();
      if (hiddenUserStream.next()) {
        throw new IdentificationFailed("Duplicate info");
      }
      boolean userIdAllocated = false;
      int userId = 0;
      while (!userIdAllocated) {
        userId = (int) (Math.random() * Integer.MAX_VALUE);
        hiddenUserStream = sqlConnection.getQueryBuilder(HiddenUser.TYPE, Constraints.equal(HiddenUser.USER_ID, userId))
          .getQuery().execute();
        if (!hiddenUserStream.next()) {
          userIdAllocated = true;
        }
      }

      sqlConnection.getCreateBuilder(User.TYPE)
        .set(User.NAME, name)
        .set(User.ENCRYPTED_PASSWORD, cryptedPassword)
        .set(User.LINK_INFO, linkInfo)
        .getRequest()
        .run();
      sqlConnection.getCreateBuilder(HiddenUser.TYPE)
        .set(HiddenUser.ENCRYPTED_LINK_INFO, Encoder.b64Decode(cryptedLinkInfo))
        .set(HiddenUser.USER_ID, userId)
        .getRequest()
        .run();
      sqlConnection.commitAndClose();
      return new RootDataManager.UserInfo(userId, false);
    }
    finally {
      sqlConnection.commitAndClose();
    }
  }

  public void getData(SerializedOutput output, Integer userId) {
    SqlConnection sqlConnection = sqlService.getDb();
    try {
      List<Glob> globs = new ArrayList<Glob>();
      globs.addAll(sqlConnection.getQueryBuilder(HiddenBank.TYPE)
        .selectAll()
        .getQuery()
        .executeAsGlobs());
      globs.addAll(sqlConnection.getQueryBuilder(HiddenAccount.TYPE,
                                                 Constraints.equal(HiddenAccount.HIDDEN_USER_ID, userId))
        .selectAll()
        .getQuery()
        .executeAsGlobs());
      globs.addAll(sqlConnection
        .getQueryBuilder(HiddenTransaction.TYPE, Constraints.equal(HiddenTransaction.HIDDEN_USER_ID, userId))
        .selectAll()
        .getQuery()
        .executeAsGlobs());
      output.write(globs.size());
      for (Glob hiddenTransaction : globs) {
        output.writeGlob(hiddenTransaction);
      }
    }
    finally {
      sqlConnection.commitAndClose();
    }
  }

  public void updateData(SerializedInput input, SerializedOutput output, Integer userId) {
    SqlConnection sqlConnection = sqlService.getDb();
    RemoteExecutor executor =
      new RemoteExecutor(directory.get(GlobModel.class),
                         new HiddenTransactionOnlyRequestBuilder(sqlConnection, userId));
    try {
      executor.execute(SerializedInputOutputFactory.init(input.readBytes()));
    }
    finally {
      sqlConnection.commitAndClose();
    }
  }

  public Glob identify(String name, byte[] cryptedPassword) throws IdentificationFailed {
    SqlConnection sqlConnection = sqlService.getDb();
    try {
      Ref<BlobAccessor> linkInfoRef = new Ref<BlobAccessor>();
      Ref<BlobAccessor> expectedCryptedPassword = new Ref<BlobAccessor>();
      GlobStream globStream = sqlConnection.getQueryBuilder(User.TYPE,
                                                            Constraints.equal(User.NAME, name))
        .select(User.LINK_INFO, linkInfoRef)
        .select(User.ENCRYPTED_PASSWORD, expectedCryptedPassword)
        .getQuery().execute();
      if (globStream.next()) {
        if (!Arrays.equals(cryptedPassword, expectedCryptedPassword.get().getValue())) {
          throw new IdentificationFailed("Bad password");
        }
      }
      else {
        throw new IdentificationFailed("Unknown user");
      }
      return null;
//      return linkInfoRef.get().getValue();
    }
    finally {
      sqlConnection.commitAndClose();
    }
  }

  public Integer confirmUser(String b64LinkInfo) throws IdentificationFailed {
    Ref<IntegerAccessor> refId = new Ref<IntegerAccessor>();
    SqlConnection sqlConnection = sqlService.getDb();
    try {
      GlobStream globStream = sqlConnection.getQueryBuilder(HiddenUser.TYPE,
                                                            Constraints.equal(HiddenUser.ENCRYPTED_LINK_INFO, b64LinkInfo))
        .select(HiddenUser.USER_ID, refId)
        .getQuery().execute();
      if (!globStream.next()) {
        throw new IdentificationFailed("User not associated");
      }
      return refId.get().getInteger();
    }
    finally {
      sqlConnection.commitAndClose();
    }
  }

  public Integer getNextId(String tableName, Integer count, final Integer userId) {
    SqlConnection sqlConnection = sqlService.getDb();
    try {
      DbGlobIdGenerator globIdGenerator = new DbGlobIdGenerator(ReservedId.TYPE,
                                                                ReservedId.TABLE_NAME,
                                                                ReservedId.LAST_RESERVED_ID, sqlService) {

        protected void addAdditionalInfo(CreateBuilder builder) {
          builder.set(ReservedId.HIDDEN_USER, userId);
        }

        protected Constraint getAdditionalConstraint() {
          return Constraints.equal(ReservedId.HIDDEN_USER, userId);
        }
      };
      return globIdGenerator.getNextId(tableName, count);
    }
    finally {
      sqlConnection.commitAndClose();
    }
  }

  public void delete(String name, byte[] cryptedPassword, byte[] linkInfo, byte[] cryptedLinkInfo, Integer userId) {
  }

  public Glob getUser(String name) {
    return sqlService.getDb().getQueryBuilder(User.TYPE).selectAll().getQuery().executeUnique();
  }

  public Glob getHiddenUser(byte[] cryptedLinkInfo) {
    return sqlService.getDb().getQueryBuilder(HiddenUser.TYPE,
                                              Constraints.equal(HiddenUser.ENCRYPTED_LINK_INFO,
                                                                Encoder.b64Decode(cryptedLinkInfo)))
      .selectAll().getQuery().executeUnique();
  }

  public GlobList getHiddenGlob(GlobType type, Integer userId) {
    IntegerField userFieldId;
    if (type == HiddenTransaction.TYPE) {
      userFieldId = HiddenTransaction.HIDDEN_USER_ID;
    }
    else if (type == HiddenBank.TYPE) {
      userFieldId = HiddenBank.HIDDEN_USER_ID;
    }
    else if (type == HiddenTransactionToCategory.TYPE) {
      userFieldId = HiddenTransactionToCategory.HIDDEN_USER_ID;
    }
    else if (type == HiddenAccount.TYPE) {
      userFieldId = HiddenAccount.HIDDEN_USER_ID;
    }
    else {
      throw new UnexpectedApplicationState("glob Type " + type.getName() + " not legal");
    }
    return sqlService.getDb()
      .getQueryBuilder(type, Constraints.equal(userFieldId, userId))
      .selectAll()
      .getQuery()
      .executeAsGlobs();
  }

  public void close() {
  }

  public void close(Integer userId) {
  }

  public void takeSnapshot(Integer userId) {
  }

  private static class NULLUpdateRequest implements RemoteExecutor.UpdateRequest {
    public void update() {
    }

    public void update(Field field, Object value) {
    }
  }

  private static class NULLCreateRequest implements RemoteExecutor.CreateRequest {
    public void create() {
    }

    public void update(Field field, Object value) {
    }
  }

  private static class NULLDeleteRequest implements RemoteExecutor.DeleteRequest {
    public void delete() {
    }
  }

  private class HiddenTransactionOnlyRequestBuilder implements RemoteExecutor.RequestBuilder {
    private final SqlConnection sqlConnection;
    private Integer userId;

    public HiddenTransactionOnlyRequestBuilder(SqlConnection sqlConnection, Integer userId) {
      this.userId = userId;
      this.sqlConnection = sqlConnection;
    }

    public RemoteExecutor.UpdateRequest getUpdate(GlobType globType, FieldValues fieldValues) {
      if (globType == HiddenTransaction.TYPE) {
        return new DefaultUpdateRequest(sqlConnection, globType, createTransactionConstraintForUpdate(fieldValues, userId));
      }
      else if (globType == HiddenAccount.TYPE) {
        return new DefaultUpdateRequest(sqlConnection, globType, createAccountConstraintForUpdate(fieldValues, userId));
      }
      else {
        return new NULLUpdateRequest();
      }
    }

    public RemoteExecutor.CreateRequest getCreate(GlobType globType, FieldValues fieldValues) {
      if (HiddenTransaction.TYPE == globType) {
        return new DefaultCreateRequestWithUserId(globType, fieldValues, HiddenTransaction.HIDDEN_USER_ID);
      }
      else if (HiddenAccount.TYPE == globType) {
        return new DefaultCreateRequestWithUserId(globType, fieldValues, HiddenAccount.HIDDEN_USER_ID);
      }
      else if (HiddenBank.TYPE == globType) {
        return new DefaultCreateRequestWithUserId(globType, fieldValues, HiddenBank.HIDDEN_USER_ID);
      }
      else if (HiddenTransactionToCategory.TYPE == globType) {
        return new DefaultCreateRequestWithUserId(globType, fieldValues, HiddenTransactionToCategory.HIDDEN_USER_ID);
      }
      else {
        return new NULLCreateRequest();
      }
    }

    public RemoteExecutor.DeleteRequest getDelete(GlobType globType, FieldValues valuesConstraint) {
      if (globType != HiddenTransaction.TYPE) {
        return new NULLDeleteRequest();
      }
      Constraint constraint = Constraints.and(Constraints.equal(HiddenTransaction.ID, valuesConstraint.get(HiddenTransaction.ID)),
                                              Constraints.equal(HiddenTransaction.HIDDEN_USER_ID, userId));
      return new DefaultDeleteRequest(sqlConnection, globType, constraint);
    }

    private class DefaultCreateRequestWithUserId extends DefaultCreateRequest {
      private final IntegerField hiddenUserIdField;

      public DefaultCreateRequestWithUserId(GlobType globType, FieldValues fieldValues,
                                            IntegerField hiddenUserIdField) {
        super(HiddenTransactionOnlyRequestBuilder.this.sqlConnection, globType, fieldValues, true);
        this.hiddenUserIdField = hiddenUserIdField;
      }

      public void create() {
        update(hiddenUserIdField, userId);
        super.create();
      }
    }
  }

  private Constraint createAccountConstraintForUpdate(FieldValues fieldValues, Integer userId) {
    return Constraints.and(Constraints.equal(HiddenAccount.ID, fieldValues.get(HiddenAccount.ID)),
                           Constraints.equal(HiddenAccount.HIDDEN_USER_ID, userId));
  }

  private Constraint createTransactionConstraintForUpdate(FieldValues fieldValues, Integer userId) {
    return Constraints.and(Constraints.equal(HiddenTransaction.ID, fieldValues.get(HiddenTransaction.ID)),
                           Constraints.equal(HiddenTransaction.HIDDEN_USER_ID, userId));
  }


}
