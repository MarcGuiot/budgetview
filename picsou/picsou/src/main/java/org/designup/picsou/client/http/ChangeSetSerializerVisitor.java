package org.designup.picsou.client.http;

import org.crossbowlabs.globs.model.*;
import org.crossbowlabs.globs.remote.SerializedRemoteAccess;
import org.crossbowlabs.globs.utils.serialization.SerializedByteArrayOutput;
import org.crossbowlabs.globs.utils.serialization.SerializedOutput;
import org.designup.picsou.model.*;
import org.designup.picsou.server.model.*;

class ChangeSetSerializerVisitor implements ChangeSetVisitor {
  private final SerializedRemoteAccess.ChangeVisitor globChangeVisitor;
  private final GlobRepository globRepository;
  private PasswordBasedEncryptor passwordBasedEncryptor;
  public static final int CURRENT_VERSION_FOR_TRANSACTION = 3;
  public static final int V2 = 2;
  public static final int V1 = 1;
  public static final int CURRENT_VERSION_FOR_ACCOUNT = 1;
  public static final int CURRENT_VERSION_FOR_IMPORT = 1;
  public static final int CURRENT_VERSION_FOR_CATEGORY = 1;


  public ChangeSetSerializerVisitor(SerializedRemoteAccess.ChangeVisitor globChangeVisitor, GlobRepository globRepository,
                                    PasswordBasedEncryptor passwordBasedEncryptor) {
    this.globChangeVisitor = globChangeVisitor;
    this.globRepository = globRepository;
    this.passwordBasedEncryptor = passwordBasedEncryptor;
  }

  public void visitCreation(Key key, FieldValues values) throws Exception {
    ServerTypeVisitor.Visitor.safeVisit(key, values, new ServerTypeVisitorCoder(globChangeVisitor));
  }

  public void visitUpdate(Key key, FieldValues values) throws Exception {
    if (key.getGlobType() == Transaction.TYPE) {
      FieldValues fieldValues = createHiddenTransactionForUpdate(key, values, globRepository);
      globChangeVisitor.visitUpdate(
        KeyBuilder.init(HiddenTransaction.ID, key.get(Transaction.ID))
          .add(HiddenTransaction.HIDDEN_USER_ID, -1).get(),
        fieldValues);
    }
    else if (key.getGlobType() == Bank.TYPE) {
      globChangeVisitor.visitUpdate(
        KeyBuilder.init(HiddenBank.ID, key.get(Bank.ID))
          .add(HiddenBank.HIDDEN_USER_ID, -1).get(),
        FieldValuesBuilder.init().get());
    }
    else if (key.getGlobType() == Account.TYPE) {
      FieldValues fieldValues = createHiddenAccountForUpdate(key, values, globRepository);
      globChangeVisitor.visitUpdate(
        KeyBuilder.init(HiddenAccount.ID, key.get(Account.ID))
          .add(HiddenAccount.HIDDEN_USER_ID, -1).get(),
        fieldValues);
    }
    else if (key.getGlobType() == TransactionImport.TYPE) {
      FieldValues fieldValues = createHiddenImportForUpdate(key, values, globRepository);
      globChangeVisitor.visitUpdate(
        KeyBuilder.init(HiddenImport.ID, key.get(TransactionImport.ID))
          .add(HiddenImport.HIDDEN_USER_ID, -1).get(),
        fieldValues);
    }
    else if (key.getGlobType() == Category.TYPE) {
      FieldValues fieldValues = createHiddenCategoryForUpdate(key, values, globRepository);
      globChangeVisitor.visitUpdate(
        KeyBuilder.init(HiddenCategory.ID, key.get(Category.ID))
          .add(HiddenCategory.HIDDEN_USER_ID, -1).get(),
        fieldValues);
    }
  }

  public void visitDeletion(Key key, FieldValues values) throws Exception {
    if (key.getGlobType() == Transaction.TYPE) {
      globChangeVisitor.visitDeletion(
        KeyBuilder.init(HiddenTransaction.ID, key.get(Transaction.ID))
          .add(HiddenTransaction.HIDDEN_USER_ID, -1).get(), values);
    }
    else if (key.getGlobType() == Account.TYPE) {
      globChangeVisitor.visitDeletion(
        KeyBuilder.init(HiddenAccount.ID, key.get(Account.ID))
          .add(HiddenAccount.HIDDEN_USER_ID, -1).get(), values);
    }
    else if (key.getGlobType() == Bank.TYPE) {
      globChangeVisitor.visitDeletion(
        KeyBuilder.init(HiddenBank.ID, key.get(Bank.ID))
          .add(HiddenBank.HIDDEN_USER_ID, -1).get(), values);
    }
    else if (key.getGlobType() == TransactionToCategory.TYPE) {
      globChangeVisitor.visitDeletion(
        KeyBuilder
          .init(HiddenTransactionToCategory.TRANSACTION_ID, key.get(TransactionToCategory.TRANSACTION))
          .add(HiddenTransactionToCategory.CATEGORY_ID, key.get(TransactionToCategory.CATEGORY))
          .add(HiddenTransactionToCategory.HIDDEN_USER_ID, -1).get(), values);
    }
    else if (key.getGlobType() == TransactionImport.TYPE) {
      globChangeVisitor.visitDeletion(
        KeyBuilder
          .init(HiddenImport.ID, key.get(TransactionImport.ID))
          .add(HiddenImport.HIDDEN_USER_ID, -1).get(), values);
    }
    else if (key.getGlobType() == Category.TYPE) {
      globChangeVisitor.visitDeletion(
        KeyBuilder
          .init(HiddenCategory.ID, key.get(Category.ID))
          .add(HiddenCategory.HIDDEN_USER_ID, -1).get(), values);
    }
  }

  private class ServerTypeVisitorCoder implements ServerTypeVisitor {
    private final SerializedRemoteAccess.ChangeVisitor globChangeVisitor;

    public ServerTypeVisitorCoder(SerializedRemoteAccess.ChangeVisitor globChangeVisitor) {
      this.globChangeVisitor = globChangeVisitor;
    }

    public void visitTransaction(Key key, FieldValues values) throws Exception {
      FieldValues globFieldValues = getHiddenTransaction(key, values);
      globChangeVisitor.visitCreation(
        KeyBuilder.init(HiddenTransaction.ID, key.get(Transaction.ID))
          .add(HiddenTransaction.HIDDEN_USER_ID, -1).get(), globFieldValues);
    }

    public void visitBank(Key key, FieldValues values) throws Exception {
      globChangeVisitor.visitCreation(
        KeyBuilder.init(HiddenBank.ID, key.get(Bank.ID))
          .add(HiddenBank.HIDDEN_USER_ID, -1).get(),
        FieldValuesBuilder.init().get());
    }

    public void visitAccount(Key key, FieldValues values) throws Exception {
      FieldValues fieldValues =
        getHiddenAccount(values)
          .set(HiddenAccount.ID, key.get(Account.ID)).get();

      globChangeVisitor.visitCreation(
        KeyBuilder.init(HiddenAccount.ID, key.get(Account.ID))
          .add(HiddenAccount.HIDDEN_USER_ID, -1).get(), fieldValues);
    }

    public void visitTransactionToCategory(Key key, FieldValues values) throws Exception {
      globChangeVisitor.visitCreation(KeyBuilder
        .init(HiddenTransactionToCategory.CATEGORY_ID, key.get(TransactionToCategory.CATEGORY))
        .add(HiddenTransactionToCategory.TRANSACTION_ID, key.get(TransactionToCategory.TRANSACTION))
        .add(HiddenTransactionToCategory.HIDDEN_USER_ID, -1)
        .get(), FieldValuesBuilder.init().get());
    }

    public void visitLabelToCategory(Key key, FieldValues values) throws Exception {
      SerializedByteArrayOutput outputStream = new SerializedByteArrayOutput();
      outputStream.getOutput()
        .writeString(values.get(LabelToCategory.LABEL));
      globChangeVisitor.visitCreation(
        KeyBuilder
          .init(HiddenLabelToCategory.ID, key.get(LabelToCategory.ID))
          .add(HiddenLabelToCategory.HIDDEN_USER_ID, -1).get(),
        FieldValuesBuilder.init()
          .set(HiddenLabelToCategory.CATEGORY, values.get(LabelToCategory.CATEGORY))
          .set(HiddenLabelToCategory.COUNT, values.get(LabelToCategory.COUNT))
          .set(HiddenLabelToCategory.HIDDEN_LABEL, passwordBasedEncryptor.encrypt(outputStream.toByteArray()))
          .get());
    }

    public void visitImport(Key key, FieldValues values) throws Exception {
      FieldValues globFieldValues = getHiddenImport(key, values);
      globChangeVisitor.visitCreation(
        KeyBuilder.init(HiddenImport.ID, key.get(TransactionImport.ID))
          .add(HiddenImport.HIDDEN_USER_ID, -1).get(), globFieldValues);
    }

    public void visitCategory(Key key, FieldValues values) throws Exception {
      FieldValues globFieldValues = getHiddenCategory(key, values);
      globChangeVisitor.visitCreation(
        KeyBuilder.init(HiddenCategory.ID, key.get(Category.ID))
          .add(HiddenCategory.HIDDEN_USER_ID, -1).get(), globFieldValues);
    }

    public void visitOther(Key key, FieldValues values) throws Exception {
    }
  }

  private FieldValues createHiddenImportForUpdate(Key key, FieldValues values, GlobRepository globRepository) {
    FieldValuesBuilder builder = new FieldValuesBuilder();
    builder.set(HiddenImport.ID, key.get(TransactionImport.ID));
    builder.set(HiddenImport.CRYPTED_INFO, getEncryptedImport(globRepository.get(key).getValues(true)));
    return builder.get();
  }

  private FieldValues createHiddenCategoryForUpdate(Key key, FieldValues values, GlobRepository globRepository) {
    FieldValuesBuilder builder = new FieldValuesBuilder();
    builder.set(HiddenCategory.ID, key.get(Category.ID));
    builder.set(HiddenCategory.CRYPTED_INFO, getEncryptedCategory(globRepository.get(key).getValues(true)));
    return builder.get();
  }

  private FieldValuesBuilder getHiddenAccount(FieldValues values) {
    FieldValuesBuilder fieldValuesBuilder = new FieldValuesBuilder();
    return fieldValuesBuilder
      .set(HiddenAccount.CRYPTED_INFO, getEncryptedAccountInfo(values));
  }

  private FieldValues createHiddenAccountForUpdate(Key key, FieldValues values, GlobRepository globRepository) {
    FieldValuesBuilder builder = new FieldValuesBuilder();
    builder.set(HiddenAccount.ID, key.get(Account.ID));
    builder.set(HiddenAccount.CRYPTED_INFO, getEncryptedAccountInfo(globRepository.get(key).getValues(true)));
    return builder.get();
  }

  private FieldValues createHiddenTransactionForUpdate(Key key, FieldValues values, GlobRepository globRepository) {
    FieldValuesBuilder builder = new FieldValuesBuilder();
    builder.set(HiddenTransaction.ENCRYPTED_INFO,
                getEncryptedTransactionInfo(globRepository.get(key).getValues(true)));
    return builder.get();
  }

  public FieldValues getHiddenTransaction(Key key, FieldValues values) {
    FieldValuesBuilder fieldValuesBuilder = new FieldValuesBuilder();
    return fieldValuesBuilder
      .set(HiddenTransaction.ID, values.get(Transaction.ID))
      .set(HiddenTransaction.ENCRYPTED_INFO, getEncryptedTransactionInfo(values)).get();
  }

  private FieldValues getHiddenImport(Key key, FieldValues values) {
    FieldValuesBuilder fieldValuesBuilder = new FieldValuesBuilder();
    return fieldValuesBuilder.set(HiddenImport.CRYPTED_INFO, getEncryptedImport(values)).get();
  }

  private byte[] getEncryptedImport(FieldValues values) {
    SerializedByteArrayOutput outputStream = new SerializedByteArrayOutput();
    writeImportV1(outputStream.getOutput(), values);
    return passwordBasedEncryptor.encrypt(outputStream.toByteArray());
  }

  private FieldValues getHiddenCategory(Key key, FieldValues values) {
    FieldValuesBuilder fieldValuesBuilder = new FieldValuesBuilder();
    return fieldValuesBuilder.set(HiddenCategory.CRYPTED_INFO, getEncryptedCategory(values)).get();
  }

  private byte[] getEncryptedCategory(FieldValues values) {
    SerializedByteArrayOutput outputStream = new SerializedByteArrayOutput();
    writeCategoryV1(outputStream.getOutput(), values);
    return passwordBasedEncryptor.encrypt(outputStream.toByteArray());
  }

  private byte[] getEncryptedAccountInfo(FieldValues accountValues) {
    SerializedByteArrayOutput outputStream = new SerializedByteArrayOutput();
    writeAccountV1(outputStream.getOutput(), accountValues);
    return passwordBasedEncryptor.encrypt(outputStream.toByteArray());
  }

  private byte[] getEncryptedTransactionInfo(FieldValues transaction) {
    SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
    SerializedOutput output = serializedByteArrayOutput.getOutput();
    output.write(CURRENT_VERSION_FOR_TRANSACTION);
    output.writeString(transaction.get(Transaction.ORIGINAL_LABEL));
    output.writeString(transaction.get(Transaction.LABEL));
    output.writeString(transaction.get(Transaction.LABEL_FOR_CATEGORISATION));
    output.writeString(transaction.get(Transaction.NOTE));
    output.writeInteger(transaction.get(Transaction.MONTH));
    output.writeInteger(transaction.get(Transaction.DAY));
    output.writeDouble(transaction.get(Transaction.AMOUNT));
    output.writeInteger(transaction.get(Transaction.ACCOUNT));
    output.writeInteger(transaction.get(Transaction.TRANSACTION_TYPE));
    output.writeInteger(transaction.get(Transaction.CATEGORY));
    output.writeBoolean(transaction.get(Transaction.SPLIT));
    output.writeInteger(transaction.get(Transaction.SPLIT_SOURCE));
    output.writeBoolean(transaction.get(Transaction.DISPENSABLE));
    return passwordBasedEncryptor.encrypt(serializedByteArrayOutput.toByteArray());
  }

  private String hideLabel(FieldValues values) {
    return values.get(Transaction.LABEL);
  }

  private void writeAccountV1(SerializedOutput outputStream, FieldValues accountValues) {
    outputStream.write(CURRENT_VERSION_FOR_ACCOUNT);
    outputStream.writeString(accountValues.get(Account.NUMBER));
    outputStream.writeInteger(accountValues.get(Account.BANK_ENTITY));
    outputStream.writeInteger(accountValues.get(Account.BRANCH_ID));
    outputStream.writeBoolean(accountValues.get(Account.IS_CARD_ACCOUNT));
    outputStream.writeString(accountValues.get(Account.NAME));
    outputStream.writeDate(accountValues.get(Account.UPDATE_DATE));
    outputStream.writeDouble(accountValues.get(Account.BALANCE));
  }

  private void writeImportV1(SerializedOutput outputStream, FieldValues values) {
    outputStream.write(CURRENT_VERSION_FOR_IMPORT);
    outputStream.writeString(values.get(TransactionImport.SOURCE));
    outputStream.writeDate(values.get(TransactionImport.IMPORT_DATE));
    outputStream.writeDate(values.get(TransactionImport.LAST_TRANSACTION_DATE));
    outputStream.writeDouble(values.get(TransactionImport.BALANCE));
  }

  private void writeCategoryV1(SerializedOutput outputStream, FieldValues values) {
    outputStream.write(CURRENT_VERSION_FOR_CATEGORY);
    outputStream.writeInteger(values.get(Category.MASTER));
    outputStream.writeString(values.get(Category.NAME));
    outputStream.writeBoolean(values.get(Category.SYSTEM));
  }


}
