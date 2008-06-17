package org.designup.picsou.server.persistence.prevayler.accounts;

import org.designup.picsou.functests.FunctionalTestCase;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.server.ServerDirectory;
import org.designup.picsou.server.model.HiddenTransaction;
import org.designup.picsou.server.model.HiddenTransactionToCategory;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.KeyBuilder;
import org.globsframework.model.delta.DefaultDeltaGlob;
import org.globsframework.model.delta.DeltaGlob;
import org.globsframework.model.delta.DeltaState;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;

import java.util.Collections;

public class PAccountDataManagerTest extends FunctionalTestCase {

  public void testUpdateData() throws Exception {
    Directory directory = ServerDirectory.getNewDirectory(url);
    {
      PAccountDataManager accountDataManager = new PAccountDataManager(url, directory, false);
      DeltaGlob decodedGlob = new DefaultDeltaGlob(KeyBuilder.init(HiddenTransaction.ID, 1)
        .add(HiddenTransaction.HIDDEN_USER_ID, 2).get());
      decodedGlob.setState(DeltaState.CREATED);
      decodedGlob.set(HiddenTransaction.ENCRYPTED_INFO, new byte[0]);
      decodedGlob.set(HiddenTransaction.HIDDEN_LABEL, "some info");

      accountDataManager.updateUserData(Collections.singletonList(decodedGlob), 2);

      SerializedByteArrayOutput output = new SerializedByteArrayOutput();
      accountDataManager.getUserData(output.getOutput(), 2);
      SerializedInput input = SerializedInputOutputFactory.init(output.toByteArray());
      assertEquals(1, input.readNotNullInt());
      Glob glob = input.readGlob(directory.get(GlobModel.class));
      assertEquals(0, glob.get(HiddenTransaction.ENCRYPTED_INFO).length);
      assertEquals("some info", glob.get(HiddenTransaction.HIDDEN_LABEL));
    }
    {
      PAccountDataManager accountDataManager = new PAccountDataManager(url, directory, false);
      GlobList list = accountDataManager.getUserData(2);
      assertEquals(1, list.size());

      DeltaGlob decodedGlob = new DefaultDeltaGlob(KeyBuilder.init(HiddenTransaction.ID, 1)
        .add(HiddenTransaction.HIDDEN_USER_ID, 2).get());
      decodedGlob.setState(DeltaState.UPDATED);
      decodedGlob.set(HiddenTransaction.TRANSACTION_TYPE_ID, new Integer(5));

      accountDataManager.updateUserData(Collections.singletonList(decodedGlob), 2);
    }
    {
      PAccountDataManager accountDataManager = new PAccountDataManager(url, directory, false);
      GlobList list = accountDataManager.getUserData(2);
      assertEquals(1, list.size());
      Glob glob = list.get(0);
      assertEquals(5, glob.get(HiddenTransaction.TRANSACTION_TYPE_ID).intValue());
    }
  }

  public void testAddTransactionToCategory() throws Exception {
    Directory directory = ServerDirectory.getNewDirectory(url);
    {
      PAccountDataManager accountDataManager = new PAccountDataManager(url, directory, true);
      DeltaGlob decodedGlob = new DefaultDeltaGlob(KeyBuilder.init(HiddenTransactionToCategory.HIDDEN_USER_ID, 2)
        .add(HiddenTransactionToCategory.CATEGORY_ID, 1)
        .add(HiddenTransactionToCategory.TRANSACTION_ID, 3)
        .get());

      decodedGlob.setState(DeltaState.CREATED);

      accountDataManager.updateUserData(Collections.singletonList(decodedGlob), 2);

      SerializedByteArrayOutput output = new SerializedByteArrayOutput();
      accountDataManager.getUserData(output.getOutput(), 2);
      SerializedInput input = SerializedInputOutputFactory.init(output.toByteArray());
      assertEquals(1, input.readNotNullInt());
      Glob glob = input.readGlob(directory.get(GlobModel.class));
      assertEquals(1, glob.get(HiddenTransactionToCategory.CATEGORY_ID).intValue());
      assertEquals(3, glob.get(HiddenTransactionToCategory.TRANSACTION_ID).intValue());
    }
  }

  public void testGetNextId() throws Exception {
    PAccountDataManager accountDataManager =
      new PAccountDataManager(url, ServerDirectory.getNewDirectory(url), true);
    assertEquals(1, accountDataManager.getNextId(Account.TYPE.getName(), 123, 99).intValue());
    assertEquals(100, accountDataManager.getNextId(Account.TYPE.getName(), 123, 3).intValue());
    assertEquals(103, accountDataManager.getNextId(Account.TYPE.getName(), 123, 10).intValue());

    assertEquals(1, accountDataManager.getNextId(Transaction.TYPE.getName(), 123, 99).intValue());
    assertEquals(100, accountDataManager.getNextId(Transaction.TYPE.getName(), 123, 3).intValue());
    assertEquals(103, accountDataManager.getNextId(Transaction.TYPE.getName(), 123, 3).intValue());
  }

}
