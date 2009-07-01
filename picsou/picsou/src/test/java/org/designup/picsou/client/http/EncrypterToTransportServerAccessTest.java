package org.designup.picsou.client.http;

import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.client.exceptions.BadPassword;
import org.designup.picsou.client.exceptions.UserAlreadyExists;
import org.designup.picsou.client.exceptions.UserNotRegistered;
import org.designup.picsou.client.local.LocalClientTransport;
import org.designup.picsou.functests.FunctionalTestCase;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.model.*;
import org.designup.picsou.server.ServerDirectory;
import org.designup.picsou.server.model.User;
import org.designup.picsou.server.session.Persistence;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.delta.DefaultChangeSet;
import org.globsframework.model.impl.DefaultGlobRepository;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.utils.Functor;
import org.globsframework.utils.TestUtils;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidState;

import java.util.HashMap;
import java.util.Map;

public class EncrypterToTransportServerAccessTest extends FunctionalTestCase {
  protected Directory directory;
  private ServerDirectory serverDirectory;
  private boolean inMemory = false;
  protected String url;


  protected void setUp() throws Exception {
    super.setUp();
    url = initServerEnvironment(inMemory);
    directory.add(new ConfigService("1", 1L, 1L, null));
    directory.add(PasswordBasedEncryptor.class, new RedirectPasswordBasedEncryptor());
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    serverDirectory = null;
    directory = null;
    url = null;
  }

  public String initServerEnvironment(boolean inMemory) throws Exception {
    String prevaylerPath = createPrevaylerRepository();
    serverDirectory = new ServerDirectory(prevaylerPath, inMemory);
    directory = serverDirectory.getServiceDirectory();
    return prevaylerPath;
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

    Glob user = createUser("name", "password", serverAccess);
    assertEquals("name", user.get(User.NAME));

    Glob expectedTransaction = getATransaction();

    serverAccess.getUserData(new DefaultChangeSet(), new DummyIdUpdater());

    GlobRepository repository = init(serverAccess);
    repository.create(expectedTransaction.getKey(), expectedTransaction.toArray());

    DummyIdUpdater update = new DummyIdUpdater();
    GlobList result = serverAccess.getUserData(new DefaultChangeSet(), update);
    assertEquals((int)update.ids.get(Transaction.ID), 1);
    assertEquals(1, result.size());
    assertEquals(expectedTransaction.get(Transaction.AMOUNT), result.get(0).get(Transaction.AMOUNT));
  }

  public void testCreateAndModifyAccount() throws Exception {
    EncrypterToTransportServerAccess serverAccess = createServerAccess();
    Glob glob = createUser("name", "password", serverAccess);
    serverAccess.getUserData(new DefaultChangeSet(), new DummyIdUpdater());
    assertEquals("name", glob.get(User.NAME));

    GlobRepository repository = init(serverAccess);

    Glob expected = repository.create(Key.create(Account.TYPE, 123),
                                      value(Account.BANK_ENTITY, 1),
                                      value(Account.NUMBER, "main account"),
                                      value(Account.POSITION, 100.),
                                      value(Account.BRANCH_ID, 2));
    {
      Glob actualAccount = serverAccess.getUserData(new DefaultChangeSet(), new DummyIdUpdater()).get(0);
      assertEquals(1, actualAccount.get(Account.BANK_ENTITY).intValue());
      assertEquals(123, actualAccount.get(Account.ID).intValue());
      assertEquals(2, actualAccount.get(Account.BRANCH_ID).intValue());
      assertEquals(100.0, actualAccount.get(Account.POSITION), 0.1);
    }
    {
      repository.update(expected.getKey(), Account.POSITION, -122.);
      Glob actualAccount = serverAccess.getUserData(new DefaultChangeSet(), new DummyIdUpdater()).get(0);
      assertEquals(-122.0, actualAccount.get(Account.POSITION), 0.1);
    }
  }

  public void testAddBank() throws Exception {
    EncrypterToTransportServerAccess serverAccess = createServerAccess();

    Glob glob = createUser("name", "password", serverAccess);
    serverAccess.getUserData(new DefaultChangeSet(), new DummyIdUpdater());
    Glob expected = GlobBuilder.init(org.designup.picsou.model.Bank.TYPE)
      .set(Bank.ID, 12).get();

    GlobRepository repository = init(serverAccess);

    {
      repository.create(expected.getKey(), expected.toArray());
      Glob actualBank = serverAccess.getUserData(new DefaultChangeSet(), new DummyIdUpdater()).get(0);
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
        categorizer.getUserData(new DefaultChangeSet(), new DummyIdUpdater());
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
    serverAccess.getUserData(new DefaultChangeSet(), new DummyIdUpdater());

    assertEquals("name", glob.get(User.NAME));

    Glob expected = GlobBuilder.init(Category.TYPE)
      .set(Category.ID, 1).set(Category.NAME, "category name").set(Category.MASTER, 3).get();

    GlobRepository repository = init(serverAccess);
    repository.create(expected.getKey(), expected.toArray());

    GlobList result = serverAccess.getUserData(new DefaultChangeSet(), new DummyIdUpdater());
    assertEquals(1, result.size());
    assertEquals(expected.get(Category.NAME), result.get(0).get(Category.NAME));

    repository.update(Key.create(Category.TYPE, 1), Category.NAME, "other name");

    DummyIdUpdater update = new DummyIdUpdater();
    GlobList updateResult = serverAccess.getUserData(new DefaultChangeSet(), update);
    assertEquals(1, updateResult.size());
    assertEquals("other name", updateResult.get(0).get(Category.NAME));
  }

  private Glob getHiddenUser(Glob glob) {
    PasswordBasedEncryptor passwordBasedEncryptor =
      new MD5PasswordBasedEncryptor(EncrypterToTransportServerAccess.salt, "password".toCharArray(), 20);
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

  private static class DummyIdUpdater implements ServerAccess.IdUpdater {
    public Map<IntegerField, Integer> ids = new HashMap<IntegerField, Integer>();

    public void update(IntegerField field, Integer lastAllocatedId) {
      ids.put(field, lastAllocatedId);
    }
  }
}
