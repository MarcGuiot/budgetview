package org.designup.picsou.client.http;

import org.designup.picsou.client.exceptions.BadPassword;
import org.designup.picsou.client.exceptions.UserAlreadyExists;
import org.designup.picsou.client.exceptions.UserNotRegistered;
import org.designup.picsou.client.local.LocalClientTransport;
import org.designup.picsou.functests.FunctionalTestCase;
import org.designup.picsou.model.*;
import org.designup.picsou.server.model.*;
import org.designup.picsou.server.model.User;
import org.designup.picsou.server.session.Persistence;
import org.globsframework.model.*;
import org.globsframework.model.delta.DefaultChangeSet;
import org.globsframework.model.impl.DefaultGlobRepository;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.utils.Functor;
import org.globsframework.utils.TestUtils;
import org.globsframework.utils.exceptions.InvalidState;

public class EncrypterToTransportServerAccessTest extends FunctionalTestCase {

  private GlobRepository init(final EncrypterToTransportServerAccess createClientCategorizer) {
    GlobRepository repository = GlobRepositoryBuilder.createEmpty();
    repository.addChangeListener(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        createClientCategorizer.applyChanges(changeSet, repository);
      }
    });
    return repository;
  }

  public void testInitConnection() throws Exception {
    Glob glob = createUser("name", "password", createServerAccess());
    assertEquals("name", glob.get(User.NAME));
    getHiddenUser(glob);
  }

  public void testAddAndModifyTransaction() throws Exception {
    EncrypterToTransportServerAccess serverAccess = createServerAccess();
    Glob glob = createUser("name", "password", serverAccess);
    assertEquals("name", glob.get(User.NAME));
    Glob hiddenGlob = getHiddenUser(glob);
    Integer userId = hiddenGlob.get(HiddenUser.USER_ID);
    Glob expected = getATransaction();

    GlobRepository repository = init(serverAccess);
    repository.create(expected.getKey(), expected.toArray());

    GlobList transactions = getHiddenTransaction(userId);
    assertEquals(1, transactions.size());

    GlobList result = serverAccess.getUserData(new DefaultChangeSet());
    assertEquals(1, result.size());
    assertEquals(expected.get(Transaction.CATEGORY), result.get(0).get(Transaction.CATEGORY));
  }

  public void testCreateAndModifyAccount() throws Exception {
    EncrypterToTransportServerAccess categorizer = createServerAccess();
    Glob glob = createUser("name", "password", categorizer);
    assertEquals("name", glob.get(User.NAME));
    Glob hiddenGlob = getHiddenUser(glob);
    Integer userId = hiddenGlob.get(HiddenUser.USER_ID);

    GlobRepository repository = init(categorizer);

    Glob expected = repository.create(Key.create(Account.TYPE, 123),
                                      FieldValue.value(Account.BANK_ENTITY, 1),
                                      FieldValue.value(Account.NUMBER, "main account"),
                                      FieldValue.value(Account.BALANCE, 100.),
                                      FieldValue.value(Account.BRANCH_ID, 2));
    {
      GlobList accounts = getAccount(userId);
      assertEquals(1, accounts.size());
      assertEquals(123, accounts.get(0).get(HiddenAccount.ID).intValue());
      Glob actualAccount = categorizer.getUserData(new DefaultChangeSet()).get(0);
      assertEquals(1, actualAccount.get(Account.BANK_ENTITY).intValue());
      assertEquals(123, actualAccount.get(Account.ID).intValue());
      assertEquals(2, actualAccount.get(Account.BRANCH_ID).intValue());
      assertEquals(100.0, actualAccount.get(Account.BALANCE), 0.1);
    }
    {
      repository.update(expected.getKey(), Account.BALANCE, -122.);
      GlobList accounts = getAccount(userId);
      assertEquals(1, accounts.size());
      Glob actualAccount = categorizer.getUserData(new DefaultChangeSet()).get(0);
      assertEquals(-122.0, actualAccount.get(Account.BALANCE), 0.1);
    }
  }

  public void testAddBank() throws Exception {
    EncrypterToTransportServerAccess categorizer = createServerAccess();
    Glob glob = createUser("name", "password", categorizer);
    Glob hiddenGlob = getHiddenUser(glob);
    Integer userId = hiddenGlob.get(HiddenUser.USER_ID);
    Glob expected = GlobBuilder.init(org.designup.picsou.model.Bank.TYPE)
      .set(Bank.ID, 12).get();

    GlobRepository repository = init(categorizer);

    {
      repository.create(expected.getKey(), expected.toArray());
      GlobList banks = getBank(userId);
      assertEquals(1, banks.size());
      assertEquals(12, banks.get(0).get(HiddenBank.ID).intValue());
      Glob actualBank = categorizer.getUserData(new DefaultChangeSet()).get(0);
      assertEquals(12, actualBank.get(Bank.ID).intValue());
    }
  }

  private Glob getATransaction() {
    return GlobBuilder.init(Transaction.TYPE)
      .set(Transaction.ID, 1).set(Transaction.LABEL, "some info").set(Transaction.AMOUNT, 100.0)
      .set(Transaction.CATEGORY, 3).get();
  }

  public void testCannotCreateTheSameUserTwice() throws Exception {
    createUser("name", "password", createServerAccess());
    TestUtils.assertFails(new Functor() {
      public void run() throws Exception {
        createUser("name", "other password", createServerAccess());
      }
    }, UserAlreadyExists.class);
  }

  public void testConnectFailsWithBadPassword() throws Exception {
    createUser("name", "password", createServerAccess());
    final EncrypterToTransportServerAccess categorizer = createServerAccess();
    TestUtils.assertFails(new Functor() {
      public void run() throws Exception {
        categorizer.initConnection("name", "other password".toCharArray(), false);
      }
    }, BadPassword.class);
  }

  public void testConnectFailsIfNotRecognized() throws Exception {
    final EncrypterToTransportServerAccess categorizer = createServerAccess();
    TestUtils.assertFails(new Functor() {
      public void run() throws Exception {
        categorizer.initConnection("other Name", "other password".toCharArray(), false);
      }
    }, UserNotRegistered.class);
  }

  public void testAllOnUnknownUser() throws Exception {
    final EncrypterToTransportServerAccess categorizer = createServerAccess();
    TestUtils.assertFails(new Functor() {
      public void run() throws Exception {
        categorizer.getUserData(new DefaultChangeSet());
      }
    }, InvalidState.class);

    TestUtils.assertFails(new Functor() {
      public void run() throws Exception {
        categorizer.applyChanges(new DefaultChangeSet(), new DefaultGlobRepository());
      }
    }, InvalidState.class);
  }

  public void testWithCategory() throws Exception {
    EncrypterToTransportServerAccess serverAccess = createServerAccess();
    Glob glob = createUser("name", "password", serverAccess);
    assertEquals("name", glob.get(User.NAME));
    Glob hiddenGlob = getHiddenUser(glob);
    Integer userId = hiddenGlob.get(HiddenUser.USER_ID);

    Glob expected = GlobBuilder.init(Category.TYPE)
      .set(Category.ID, 1).set(Category.NAME, "category name").set(Category.MASTER, 3).get();

    GlobRepository repository = init(serverAccess);
    repository.create(expected.getKey(), expected.toArray());

    GlobList transactions = getHiddenCateory(userId);
    assertEquals(1, transactions.size());

    GlobList result = serverAccess.getUserData(new DefaultChangeSet());
    assertEquals(1, result.size());
    assertEquals(expected.get(Category.NAME), result.get(0).get(Category.NAME));

    repository.update(Key.create(Category.TYPE, 1), Category.NAME, "other name");

    GlobList updateResult = serverAccess.getUserData(new DefaultChangeSet());
    assertEquals(1, updateResult.size());
    assertEquals("other name", updateResult.get(0).get(Category.NAME));
  }

  public void testGetNextId() throws Exception {
    EncrypterToTransportServerAccess serverAccess = createServerAccess();
    Glob glob = createUser("name", "password", serverAccess);
    assertEquals(1000, serverAccess.getNextId(Category.TYPE.getName(), 3));
    assertEquals(1003, serverAccess.getNextId(Category.TYPE.getName(), 2));

    assertEquals(1, serverAccess.getNextId(Transaction.TYPE.getName(), 3));
    assertEquals(4, serverAccess.getNextId(Transaction.TYPE.getName(), 2));

    assertEquals(1, serverAccess.getNextId(TransactionImport.TYPE.getName(), 3));
    assertEquals(4, serverAccess.getNextId(TransactionImport.TYPE.getName(), 2));

    assertEquals(1, serverAccess.getNextId(Account.TYPE.getName(), 3));
    assertEquals(4, serverAccess.getNextId(Account.TYPE.getName(), 2));

    assertEquals(1, serverAccess.getNextId(Bank.TYPE.getName(), 3));
    assertEquals(4, serverAccess.getNextId(Bank.TYPE.getName(), 2));

  }

  private Glob getHiddenUser(Glob glob) {
    PasswordBasedEncryptor passwordBasedEncryptor =
      new PasswordBasedEncryptor(EncrypterToTransportServerAccess.salt, "password".toCharArray(), 20);
    byte[] cryptedLinkInfo = passwordBasedEncryptor.encrypt(glob.get(User.LINK_INFO));
    Persistence persistence = directory.get(Persistence.class);
    return persistence.getHiddenUser(cryptedLinkInfo);
  }

  private GlobList getBank(Integer userId) {
    Persistence persistence = directory.get(Persistence.class);
    return persistence.getHiddenGlob(HiddenBank.TYPE, userId);
  }

  private GlobList getHiddenTransaction(int userId) {
    Persistence persistence = directory.get(Persistence.class);
    return persistence.getHiddenGlob(HiddenTransaction.TYPE, userId);
  }

  private GlobList getHiddenCateory(int userId) {
    Persistence persistence = directory.get(Persistence.class);
    return persistence.getHiddenGlob(HiddenCategory.TYPE, userId);
  }

  private GlobList getHiddenTransactionToCategory(int userId) {
    Persistence persistence = directory.get(Persistence.class);
    return persistence.getHiddenGlob(HiddenTransactionToCategory.TYPE, userId);
  }

  private Glob createUser(String name, String password, EncrypterToTransportServerAccess serverAccess) {
    serverAccess.createUser(name, password.toCharArray());
    Persistence persistence = directory.get(Persistence.class);
    return persistence.getUser(name);
  }

  private EncrypterToTransportServerAccess createServerAccess() {
    LocalClientTransport dummyClentTransport = new LocalClientTransport(directory);
    return new EncrypterToTransportServerAccess(dummyClentTransport);
  }

  private GlobList getAccount(Integer userId) {
    Persistence persistence = directory.get(Persistence.class);
    return persistence.getHiddenGlob(HiddenAccount.TYPE, userId);
  }

}
