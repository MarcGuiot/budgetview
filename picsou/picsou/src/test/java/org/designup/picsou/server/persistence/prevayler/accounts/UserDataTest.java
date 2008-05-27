package org.designup.picsou.server.persistence.prevayler.accounts;

import junit.framework.TestCase;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.GlobRepositoryBuilder;
import org.crossbowlabs.globs.model.GlobTestUtils;
import org.crossbowlabs.globs.model.KeyBuilder;
import org.crossbowlabs.globs.model.delta.DefaultDeltaGlob;
import org.crossbowlabs.globs.model.delta.DeltaGlob;
import org.crossbowlabs.globs.model.delta.DeltaState;
import org.crossbowlabs.globs.utils.serialization.SerializedByteArrayOutput;
import org.designup.picsou.model.*;
import org.designup.picsou.server.model.*;

import java.util.Arrays;

public class UserDataTest extends TestCase {

  public void testReadWrite() throws Exception {
    UserData expected = new UserData();
    DeltaGlob hiddenTransaction = new DefaultDeltaGlob(KeyBuilder.init(HiddenTransaction.ID, 1)
      .add(HiddenTransaction.HIDDEN_USER_ID, -1).get());
    hiddenTransaction.setState(DeltaState.CREATED);
    hiddenTransaction.set(HiddenTransaction.ENCRYPTED_INFO, "some Info".getBytes());

    DeltaGlob hiddenAccount = new DefaultDeltaGlob(KeyBuilder.init(HiddenAccount.ID, 3)
      .add(HiddenAccount.HIDDEN_USER_ID, -1).get());
    hiddenAccount.set(HiddenAccount.CRYPTED_INFO, "some Info".getBytes());
    hiddenAccount.setState(DeltaState.CREATED);

    DeltaGlob hiddenTransactionToCategory = new DefaultDeltaGlob(KeyBuilder.init(HiddenTransactionToCategory.CATEGORY_ID, 5)
      .add(HiddenTransactionToCategory.HIDDEN_USER_ID, -1)
      .add(HiddenTransactionToCategory.TRANSACTION_ID, 3).get());
    hiddenTransactionToCategory.setState(DeltaState.CREATED);

    DeltaGlob hiddenBank = new DefaultDeltaGlob(KeyBuilder.init(HiddenBank.ID, 8)
      .add(HiddenBank.HIDDEN_USER_ID, -1).get());
    hiddenBank.setState(DeltaState.CREATED);

    DeltaGlob hiddenLabel = new DefaultDeltaGlob(KeyBuilder.init(HiddenLabelToCategory.ID, 1)
      .add(HiddenLabelToCategory.HIDDEN_USER_ID, -1).get());
    hiddenLabel.setState(DeltaState.CREATED);
    hiddenLabel.set(HiddenLabelToCategory.CATEGORY, 3);
    hiddenLabel.set(HiddenLabelToCategory.COUNT, 1);
    hiddenLabel.set(HiddenLabelToCategory.HIDDEN_LABEL, "lab".getBytes());

    expected.apply(Arrays.asList(hiddenTransactionToCategory, hiddenTransaction, hiddenAccount, hiddenBank, hiddenLabel));

    assertEquals(1, expected.getNextId(Transaction.TYPE.getName(), 42));
    assertEquals(1, expected.getNextId(TransactionToCategory.TYPE.getName(), 32));
    assertEquals(1, expected.getNextId(LabelToCategory.TYPE.getName(), 4));
    assertEquals(1, expected.getNextId(Account.TYPE.getName(), 7));
    assertEquals(1, expected.getNextId(Bank.TYPE.getName(), 2));

    SerializedByteArrayOutput byteArrayOutput = new SerializedByteArrayOutput();
    expected.write(byteArrayOutput.getOutput(), null);

    UserData actual = new UserData();
    actual.read(byteArrayOutput.getInput(), null);

    assertEquals(43, actual.getNextId(Transaction.TYPE.getName(), 1));
    assertEquals(33, actual.getNextId(TransactionToCategory.TYPE.getName(), 1));
    assertEquals(5, actual.getNextId(LabelToCategory.TYPE.getName(), 1));
    assertEquals(8, actual.getNextId(Account.TYPE.getName(), 1));
    assertEquals(3, actual.getNextId(Bank.TYPE.getName(), 1));

    GlobRepository repository = GlobRepositoryBuilder.init()
      .add(TransactionType.values())
      .add(MasterCategory.createGlobs()).get();
    GlobTestUtils.assertEquals(expected.getUserData(), actual.getUserData(), repository);
  }
}
