package org.designup.picsou.client.http;

import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.client.exceptions.BadPassword;
import org.designup.picsou.client.exceptions.UserAlreadyExists;
import org.designup.picsou.client.exceptions.UserNotRegistered;
import org.designup.picsou.client.local.LocalClientTransport;
import org.designup.picsou.functests.FunctionalTestCase;
import org.designup.picsou.model.*;
import org.designup.picsou.server.model.User;
import org.designup.picsou.server.session.Persistence;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.*;
import org.globsframework.model.delta.DefaultChangeSet;
import org.globsframework.model.impl.DefaultGlobRepository;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.utils.Functor;
import org.globsframework.utils.TestUtils;
import org.globsframework.utils.exceptions.InvalidState;

import java.util.HashMap;
import java.util.Map;

public class EncrypterToTransportServerAccessTest extends FunctionalTestCase {

  protected void setUp() throws Exception {
    setInMemory(false);
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    setInMemory(true);
  }

  private GlobRepository init(final EncrypterToTransportServerAccess createClientCategorizer) {
    GlobRepository repository = GlobRepositoryBuilder.createEmpty();
    repository.addChangeListener(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        createClientCategorizer.applyChanges(changeSet, repository);
      }
    });
    PicsouModel.get();
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
    Glob expected = getATransaction();
    serverAccess.getUserData(new DefaultChangeSet(), new DummyIdUpdate());

    GlobRepository repository = init(serverAccess);
    repository.create(expected.getKey(), expected.toArray());

    DummyIdUpdate update = new DummyIdUpdate();
    GlobList result = serverAccess.getUserData(new DefaultChangeSet(), update);
    assertEquals((int)update.ids.get(Transaction.ID), 1);
    assertEquals(1, result.size());
    assertEquals(expected.get(Transaction.CATEGORY), result.get(0).get(Transaction.CATEGORY));
  }

  public void testCreateAndModifyAccount() throws Exception {
    EncrypterToTransportServerAccess serverAccess = createServerAccess();
    Glob glob = createUser("name", "password", serverAccess);
    serverAccess.getUserData(new DefaultChangeSet(), new DummyIdUpdate());
    assertEquals("name", glob.get(User.NAME));

    GlobRepository repository = init(serverAccess);

    Glob expected = repository.create(Key.create(Account.TYPE, 123),
                                      FieldValue.value(Account.BANK_ENTITY, 1),
                                      FieldValue.value(Account.NUMBER, "main account"),
                                      FieldValue.value(Account.BALANCE, 100.),
                                      FieldValue.value(Account.BRANCH_ID, 2));
    {
      Glob actualAccount = serverAccess.getUserData(new DefaultChangeSet(), new DummyIdUpdate()).get(0);
      assertEquals(1, actualAccount.get(Account.BANK_ENTITY).intValue());
      assertEquals(123, actualAccount.get(Account.ID).intValue());
      assertEquals(2, actualAccount.get(Account.BRANCH_ID).intValue());
      assertEquals(100.0, actualAccount.get(Account.BALANCE), 0.1);
    }
    {
      repository.update(expected.getKey(), Account.BALANCE, -122.);
      Glob actualAccount = serverAccess.getUserData(new DefaultChangeSet(), new DummyIdUpdate()).get(0);
      assertEquals(-122.0, actualAccount.get(Account.BALANCE), 0.1);
    }
  }

  public void testAddBank() throws Exception {
    EncrypterToTransportServerAccess serverAccess = createServerAccess();

    Glob glob = createUser("name", "password", serverAccess);
    serverAccess.getUserData(new DefaultChangeSet(), new DummyIdUpdate());
    Glob expected = GlobBuilder.init(org.designup.picsou.model.Bank.TYPE)
      .set(Bank.ID, 12).get();

    GlobRepository repository = init(serverAccess);

    {
      repository.create(expected.getKey(), expected.toArray());
      Glob actualBank = serverAccess.getUserData(new DefaultChangeSet(), new DummyIdUpdate()).get(0);
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
    categorizer.connect();
    TestUtils.assertFails(new Functor() {
      public void run() throws Exception {
        categorizer.initConnection("name", "other password".toCharArray(), false);
      }
    }, BadPassword.class);
  }

  public void testConnectFailsIfNotRecognized() throws Exception {
    final EncrypterToTransportServerAccess categorizer = createServerAccess();
    categorizer.connect();
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
        categorizer.getUserData(new DefaultChangeSet(), new DummyIdUpdate());
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
    serverAccess.getUserData(new DefaultChangeSet(), new DummyIdUpdate());

    assertEquals("name", glob.get(User.NAME));

    Glob expected = GlobBuilder.init(Category.TYPE)
      .set(Category.ID, 1).set(Category.NAME, "category name").set(Category.MASTER, 3).get();

    GlobRepository repository = init(serverAccess);
    repository.create(expected.getKey(), expected.toArray());

    GlobList result = serverAccess.getUserData(new DefaultChangeSet(), new DummyIdUpdate());
    assertEquals(1, result.size());
    assertEquals(expected.get(Category.NAME), result.get(0).get(Category.NAME));

    repository.update(Key.create(Category.TYPE, 1), Category.NAME, "other name");

    DummyIdUpdate update = new DummyIdUpdate();
    GlobList updateResult = serverAccess.getUserData(new DefaultChangeSet(), update);
    assertEquals(1, updateResult.size());
    assertEquals("other name", updateResult.get(0).get(Category.NAME));
  }

  private Glob getHiddenUser(Glob glob) {
    PasswordBasedEncryptor passwordBasedEncryptor =
      new PasswordBasedEncryptor(EncrypterToTransportServerAccess.salt, "password".toCharArray(), 20);
    byte[] cryptedLinkInfo = passwordBasedEncryptor.encrypt(glob.get(User.LINK_INFO));
    Persistence persistence = directory.get(Persistence.class);
    return persistence.getHiddenUser(cryptedLinkInfo);
  }


  private Glob createUser(String name, String password, EncrypterToTransportServerAccess serverAccess) {
    serverAccess.connect();
    serverAccess.createUser(name, password.toCharArray());
    Persistence persistence = directory.get(Persistence.class);
    return persistence.getUser(name);
  }

  private EncrypterToTransportServerAccess createServerAccess() {
    LocalClientTransport dummyClentTransport = new LocalClientTransport(directory);
    return new EncrypterToTransportServerAccess(dummyClentTransport, directory);
  }

  private static class DummyIdUpdate implements ServerAccess.IdUpdate {
    public Map<IntegerField, Integer> ids = new HashMap<IntegerField, Integer>();

    public void update(IntegerField field, Integer lastAllocatedId) {
      ids.put(field, lastAllocatedId);
    }
  }
}
