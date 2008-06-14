package org.designup.picsou.client.http;

import org.designup.picsou.client.AllocationLearningService;
import org.designup.picsou.model.*;
import org.designup.picsou.server.model.*;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.Key;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;

public class HiddenServerTypeVisitorDecoder implements HiddenServerTypeVisitor {
  private GlobList globs;
  private Glob hiddenGlob;
  private PasswordBasedEncryptor passwordBasedEncryptor;
  private MutableChangeSet mutableChangeSet;

  public HiddenServerTypeVisitorDecoder(PasswordBasedEncryptor passwordBasedEncryptor, int globCount, MutableChangeSet changeSet) {
    this.passwordBasedEncryptor = passwordBasedEncryptor;
    globs = new GlobList(globCount);
    mutableChangeSet = changeSet;
  }

  public ChangeSet getMutableChangeSet() {
    return mutableChangeSet;
  }

  public void visitHiddenTransaction() throws Exception {
    GlobBuilder builder = GlobBuilder.init(Transaction.TYPE);
    byte[] bytes = hiddenGlob.get(HiddenTransaction.ENCRYPTED_INFO);
    Integer transactionType = hiddenGlob.get(HiddenTransaction.TRANSACTION_TYPE_ID);
    builder.set(Transaction.ID, hiddenGlob.get(HiddenTransaction.ID));
    updateTransaction(builder, bytes, transactionType);
    globs.add(builder.get());
  }

  public void visitHiddenBank() throws Exception {
    globs.add(GlobBuilder.init(Bank.TYPE)
      .set(Bank.ID, hiddenGlob.get(HiddenBank.ID))
      .set(Bank.DOWNLOAD_URL, hiddenGlob.get(HiddenBank.DOWNLOAD_URL))
      .set(Bank.NAME, hiddenGlob.get(HiddenBank.NAME)).get());
  }

  public void visitHiddenAccount() throws Exception {
    GlobBuilder builder = GlobBuilder.init(Account.TYPE);
    byte[] bytes = hiddenGlob.get(HiddenAccount.CRYPTED_INFO);
    builder.set(Account.ID, hiddenGlob.get(HiddenAccount.ID));
    updateAccount(builder, bytes);
    globs.add(builder.get());
  }

  public void visitHiddenTransactionToCategory() throws Exception {
    globs.add(GlobBuilder.init(TransactionToCategory.TYPE)
      .set(TransactionToCategory.CATEGORY,
           hiddenGlob.get(HiddenTransactionToCategory.CATEGORY_ID))
      .set(TransactionToCategory.TRANSACTION,
           hiddenGlob.get(HiddenTransactionToCategory.TRANSACTION_ID)).get());
  }

  public void visitHiddenLabelToCategory() throws Exception {
    GlobBuilder builder = GlobBuilder.init(LabelToCategory.TYPE);
    globs.add(builder
      .set(LabelToCategory.ID, hiddenGlob.get(HiddenLabelToCategory.ID))
      .set(LabelToCategory.COUNT, hiddenGlob.get(HiddenLabelToCategory.COUNT))
      .set(LabelToCategory.CATEGORY, hiddenGlob.get(HiddenLabelToCategory.CATEGORY))
      .set(LabelToCategory.LABEL,
           SerializedInputOutputFactory.init(
             passwordBasedEncryptor.decrypt(hiddenGlob.get(HiddenLabelToCategory.HIDDEN_LABEL))).readString())
      .get());
  }

  public void visitHiddenImport() throws Exception {
    GlobBuilder builder = GlobBuilder.init(TransactionImport.TYPE);
    builder.set(TransactionImport.ID, hiddenGlob.get(HiddenImport.ID));
    byte[] bytes = hiddenGlob.get(HiddenImport.CRYPTED_INFO);
    updateImport(builder, bytes);
    globs.add(builder.get());
  }

  public void visitHiddenCategory() throws Exception {
    GlobBuilder builder = GlobBuilder.init(Category.TYPE);
    builder.set(Category.ID, hiddenGlob.get(HiddenCategory.ID));
    byte[] bytes = hiddenGlob.get(HiddenCategory.CRYPTED_INFO);
    updateCategory(builder, bytes);
    globs.add(builder.get());
  }

  public void visitOther() throws Exception {
  }

  public void set(Glob glob) {
    hiddenGlob = glob;
  }

  private int updateTransaction(GlobBuilder builder, byte[] bytes, Integer transactionType) {
    byte[] decrypted = passwordBasedEncryptor.decrypt(bytes);
    SerializedInput input = SerializedInputOutputFactory.init(decrypted);
    Integer version = input.readNotNullInt();
    if (version == ChangeSetSerializerVisitor.V1) {
      updateTransactionV1(builder, input, transactionType);
      return ChangeSetSerializerVisitor.V1;
    }
    if (version == ChangeSetSerializerVisitor.V2) {
      updateTransactionV2(builder, input, transactionType);
      return ChangeSetSerializerVisitor.V2;
    }
    if (version == ChangeSetSerializerVisitor.CURRENT_VERSION_FOR_TRANSACTION) {
      updateTransactionV3(builder, input);
      return ChangeSetSerializerVisitor.CURRENT_VERSION_FOR_TRANSACTION;
    }
    return -1;
  }

  private void updateTransactionV1(GlobBuilder builder, SerializedInput input, Integer transactionType) {
    builder.set(Transaction.ORIGINAL_LABEL, input.readString());
    String label = input.readString();
    builder.set(Transaction.LABEL, label);
    String note = input.readString();
    builder.set(Transaction.NOTE, note);
    builder.set(Transaction.MONTH, input.readInteger());
    builder.set(Transaction.DAY, input.readInteger());
    builder.set(Transaction.AMOUNT, input.readDouble());
    builder.set(Transaction.ACCOUNT, input.readInteger());
    builder.set(Transaction.CATEGORY, input.readInteger());
    builder.set(Transaction.SPLIT, input.readBoolean());
    builder.set(Transaction.SPLIT_SOURCE, input.readInteger());
    builder.set(Transaction.DISPENSABLE, input.readBoolean());
    if (label != null || note != null) {
      builder.set(Transaction.LABEL_FOR_CATEGORISATION,
                  AllocationLearningService.anonymise(note, label, transactionType));
      Glob glob = builder.get();
      Key key = glob.getKey();
      mutableChangeSet.processUpdate(key, Transaction.LABEL_FOR_CATEGORISATION,
                                     glob.get(Transaction.LABEL_FOR_CATEGORISATION));
      mutableChangeSet.processUpdate(key, Transaction.TRANSACTION_TYPE, transactionType);
    }
  }

  private void updateTransactionV2(GlobBuilder builder, SerializedInput input, Integer transactionType) {
    builder.set(Transaction.ORIGINAL_LABEL, input.readString());
    builder.set(Transaction.LABEL, input.readString());
    builder.set(Transaction.LABEL_FOR_CATEGORISATION, input.readString());
    builder.set(Transaction.NOTE, input.readString());
    builder.set(Transaction.MONTH, input.readInteger());
    builder.set(Transaction.DAY, input.readInteger());
    builder.set(Transaction.AMOUNT, input.readDouble());
    builder.set(Transaction.ACCOUNT, input.readInteger());
    builder.set(Transaction.CATEGORY, input.readInteger());
    builder.set(Transaction.SPLIT, input.readBoolean());
    builder.set(Transaction.SPLIT_SOURCE, input.readInteger());
    builder.set(Transaction.DISPENSABLE, input.readBoolean());
    builder.set(Transaction.TRANSACTION_TYPE, transactionType);
    mutableChangeSet.processUpdate(builder.get().getKey(), Transaction.TRANSACTION_TYPE, transactionType);
  }

  private void updateTransactionV3(GlobBuilder builder, SerializedInput input) {
    builder.set(Transaction.ORIGINAL_LABEL, input.readString());
    builder.set(Transaction.LABEL, input.readString());
    builder.set(Transaction.LABEL_FOR_CATEGORISATION, input.readString());
    builder.set(Transaction.NOTE, input.readString());
    builder.set(Transaction.MONTH, input.readInteger());
    builder.set(Transaction.DAY, input.readInteger());
    builder.set(Transaction.BANK_MONTH, input.readInteger());
    builder.set(Transaction.BANK_DAY, input.readInteger());
    builder.set(Transaction.AMOUNT, input.readDouble());
    builder.set(Transaction.ACCOUNT, input.readInteger());
    builder.set(Transaction.TRANSACTION_TYPE, input.readInteger());
    builder.set(Transaction.CATEGORY, input.readInteger());
    builder.set(Transaction.SPLIT, input.readBoolean());
    builder.set(Transaction.SPLIT_SOURCE, input.readInteger());
    builder.set(Transaction.DISPENSABLE, input.readBoolean());
  }

  private void updateAccount(GlobBuilder builder, byte[] bytes) {
    SerializedInput serializedInput = SerializedInputOutputFactory.init(passwordBasedEncryptor.decrypt(bytes));
    int serializedNumber = serializedInput.readNotNullInt();
    if (serializedNumber == ChangeSetSerializerVisitor.CURRENT_VERSION_FOR_ACCOUNT) {
      readAccountV1(builder, serializedInput);
    }
  }

  private void readAccountV1(GlobBuilder builder, SerializedInput serializedInput) {
    builder.set(Account.NUMBER, serializedInput.readString());
    builder.set(Account.BANK_ENTITY, serializedInput.readInteger());
    builder.set(Account.BRANCH_ID, serializedInput.readInteger());
    builder.set(Account.IS_CARD_ACCOUNT, serializedInput.readBoolean());
    builder.set(Account.NAME, serializedInput.readString());
    builder.set(Account.UPDATE_DATE, serializedInput.readDate());
    builder.set(Account.BALANCE, serializedInput.readDouble());
  }

  private void updateImport(GlobBuilder builder, byte[] bytes) {
    SerializedInput serializedInput = SerializedInputOutputFactory.init(passwordBasedEncryptor.decrypt(bytes));
    int serializedNumber = serializedInput.readNotNullInt();
    if (serializedNumber == ChangeSetSerializerVisitor.CURRENT_VERSION_FOR_IMPORT) {
      readImportV1(builder, serializedInput);
    }
  }

  private void readImportV1(GlobBuilder builder, SerializedInput serializedInput) {
    builder.set(TransactionImport.SOURCE, serializedInput.readString());
    builder.set(TransactionImport.IMPORT_DATE, serializedInput.readDate());
    builder.set(TransactionImport.LAST_TRANSACTION_DATE, serializedInput.readDate());
    builder.set(TransactionImport.BALANCE, serializedInput.readDouble());
  }

  private void updateCategory(GlobBuilder builder, byte[] bytes) {
    SerializedInput serializedInput = SerializedInputOutputFactory.init(passwordBasedEncryptor.decrypt(bytes));
    int serializedNumber = serializedInput.readNotNullInt();
    if (serializedNumber == ChangeSetSerializerVisitor.CURRENT_VERSION_FOR_CATEGORY) {
      readCategoryV1(builder, serializedInput);
    }
  }

  private void readCategoryV1(GlobBuilder builder, SerializedInput serializedInput) {
    builder.set(Category.MASTER, serializedInput.readInteger());
    builder.set(Category.NAME, serializedInput.readString());
    builder.set(Category.SYSTEM, serializedInput.readBoolean());
  }

  public GlobList getGlobs() {
    return globs;
  }
}
