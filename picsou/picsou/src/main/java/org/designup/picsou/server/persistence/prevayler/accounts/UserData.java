package org.designup.picsou.server.persistence.prevayler.accounts;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.model.*;
import org.crossbowlabs.globs.model.delta.DeltaGlob;
import org.crossbowlabs.globs.model.utils.GlobBuilder;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.globs.utils.exceptions.InvalidData;
import org.crossbowlabs.globs.utils.exceptions.UnexpectedApplicationState;
import org.crossbowlabs.globs.utils.serialization.SerializedInput;
import org.crossbowlabs.globs.utils.serialization.SerializedOutput;
import org.designup.picsou.model.*;
import org.designup.picsou.server.model.*;
import org.designup.picsou.server.persistence.prevayler.CustomSerializable;
import org.designup.picsou.server.persistence.prevayler.CustomSerializableFactory;

import java.util.List;

public class UserData implements CustomSerializable {
  private static final byte V1 = 1;
  private int lastTransactionId = 1;
  private int lastCategoryId = 1000;
  private int lastBankId = 1;
  private int lastTransactionToCategoryId = 1;
  private int lastImportId = 1;
  private int lastLabelToCategory = 1;
  private int lastAccountId = 1;
  private GlobList accounts = new GlobList();
  private GlobList transactions = new GlobList();
  private GlobList banks = new GlobList();
  private GlobList transactionToCategories = new GlobList();
  private GlobList labelToCategories = new GlobList();
  private GlobList imports = new GlobList();
  private GlobList categories = new GlobList();
  private static final String USER_DATA = "UserData";

  public void getUserData(SerializedOutput output) {
    output.write(accounts.size() + transactions.size() + banks.size() + transactionToCategories.size()
                 + labelToCategories.size() + imports.size() + categories.size());
    for (Glob account : accounts) {
      output.writeGlob(account);
    }
    for (Glob transaction : transactions) {
      output.writeGlob(transaction);
    }
    for (Glob bank : banks) {
      output.writeGlob(bank);
    }
    for (Glob transactionToCategory : transactionToCategories) {
      output.writeGlob(transactionToCategory);
    }
    for (Glob labelToCategory : labelToCategories) {
      output.writeGlob(labelToCategory);
    }
    for (Glob transactionImport : imports) {
      output.writeGlob(transactionImport);
    }
    for (Glob category : categories) {
      output.writeGlob(category);
    }
  }

  public GlobList getUserData() {
    GlobList globs = new GlobList();
    globs.addAll(accounts);
    globs.addAll(transactions);
    globs.addAll(banks);
    globs.addAll(transactionToCategories);
    globs.addAll(labelToCategories);
    globs.addAll(imports);
    globs.addAll(categories);
    return globs;
  }

  public Object getNextId(String globTypeName, Integer count) {
    if (Transaction.TYPE.getName().equals(globTypeName)) {
      lastTransactionId += count;
      return lastTransactionId - count;
    }
    if (Bank.TYPE.getName().equals(globTypeName)) {
      lastBankId += count;
      return lastBankId - count;
    }
    if (Account.TYPE.getName().equals(globTypeName)) {
      lastAccountId += count;
      return lastAccountId - count;
    }
    if (Category.TYPE.getName().equals(globTypeName)) {
      lastCategoryId += count;
      return lastCategoryId - count;
    }
    if (TransactionToCategory.TYPE.getName().equals(globTypeName)) {
      lastTransactionToCategoryId += count;
      return lastTransactionToCategoryId - count;
    }
    if (LabelToCategory.TYPE.getName().equals(globTypeName)) {
      lastLabelToCategory += count;
      return lastLabelToCategory - count;
    }
    if (TransactionImport.TYPE.getName().equals(globTypeName)) {
      lastImportId += count;
      return lastImportId - count;
    }
    throw new UnexpectedApplicationState("id on " + globTypeName + " not legal");
  }

  public String getSerializationName() {
    return USER_DATA;
  }

  public void read(SerializedInput input, Directory directory) {
    byte version = input.readByte();
    switch (version) {
      case V1:
        readV1(input);
        break;
      default:
        throw new InvalidData("Unable to read version " + version);
    }
  }

  private void readV1(SerializedInput input) {
    lastBankId = input.readNotNullInt();
    lastTransactionId = input.readNotNullInt();
    lastCategoryId = input.readNotNullInt();
    lastTransactionToCategoryId = input.readNotNullInt();
    lastLabelToCategory = input.readNotNullInt();
    lastAccountId = input.readNotNullInt();
    lastImportId = input.readNotNullInt();
    int accountLength = input.readNotNullInt();
    accounts = new GlobList(accountLength);
    while (accountLength != 0) {
      accounts.add(HiddenAccount.read(input));
      accountLength--;
    }
    int transactionLength = input.readNotNullInt();
    transactions = new GlobList(transactionLength);
    while (transactionLength != 0) {
      transactions.add(HiddenTransaction.read(input));
      transactionLength--;
    }
    int transactionToCategoryLength = input.readNotNullInt();
    transactionToCategories = new GlobList(transactionToCategoryLength);
    while (transactionToCategoryLength != 0) {
      transactionToCategories.add(HiddenTransactionToCategory.read(input));
      transactionToCategoryLength--;
    }
    int labelToCategoryLength = input.readNotNullInt();
    labelToCategories = new GlobList(labelToCategoryLength);
    while (labelToCategoryLength != 0) {
      labelToCategories.add(HiddenLabelToCategory.read(input));
      labelToCategoryLength--;
    }
    int bankLength = input.readNotNullInt();
    banks = new GlobList(bankLength);
    while (bankLength != 0) {
      banks.add(HiddenBank.read(input));
      bankLength--;
    }
    int importLength = input.readNotNullInt();
    imports = new GlobList(importLength);
    while (importLength != 0) {
      imports.add(HiddenImport.read(input));
      importLength--;
    }
    int categoryLength = input.readNotNullInt();
    imports = new GlobList(categoryLength);
    while (categoryLength != 0) {
      imports.add(HiddenCategory.read(input));
      categoryLength--;
    }
  }

  public void write(SerializedOutput output, Directory directory) {
    output.writeByte(V1);
    output.write(lastBankId);
    output.write(lastTransactionId);
    output.write(lastCategoryId);
    output.write(lastTransactionToCategoryId);
    output.write(lastLabelToCategory);
    output.write(lastAccountId);
    output.write(lastImportId);
    output.write(accounts.size());
    for (Glob account : accounts) {
      HiddenAccount.write(output, account);
    }
    output.write(transactions.size());
    for (Glob transaction : transactions) {
      HiddenTransaction.write(output, transaction);
    }
    output.write(transactionToCategories.size());
    for (Glob transactionToCategory : transactionToCategories) {
      HiddenTransactionToCategory.write(output, transactionToCategory);
    }
    output.write(labelToCategories.size());
    for (Glob labelToCategory : labelToCategories) {
      HiddenLabelToCategory.write(output, labelToCategory);
    }
    output.write(banks.size());
    for (Glob bank : banks) {
      HiddenBank.write(output, bank);
    }
    output.write(imports.size());
    for (Glob transactionImport : imports) {
      HiddenImport.write(output, transactionImport);
    }
    output.write(categories.size());
    for (Glob category : categories) {
      HiddenCategory.write(output, category);
    }
  }

  public static CustomSerializableFactory getFactory() {
    return new Factory();
  }

  public void apply(List<DeltaGlob> deltaGlobs) {
    for (DeltaGlob deltaGlob : deltaGlobs) {
      deltaGlob.safeVisit(new ChangeSetVisitor() {
        public void visitCreation(Key key, FieldValues values) throws Exception {
          getList(key).add(GlobBuilder.init(key, values).get());
        }

        public void visitUpdate(Key key, FieldValues values) throws Exception {
          GlobList list = getList(key);
          for (java.util.Iterator it = list.iterator(); it.hasNext();) {
            Glob glob = (Glob) it.next();
            if (glob.getKey().equals(key)) {
              it.remove();
              final GlobBuilder builder = GlobBuilder.init(key, glob.getValues(true));
              values.apply(new FieldValues.Functor() {
                public void process(Field field, Object value) throws Exception {
                  builder.setObject(field, value);
                }
              });
              list.add(builder.get());
              return;
            }
          }
        }

        public void visitDeletion(Key key, FieldValues values) throws Exception {
          GlobList list = getList(key);
          for (java.util.Iterator it = list.iterator(); it.hasNext();) {
            Glob glob = (Glob) it.next();
            if (glob.getKey().equals(key)) {
              it.remove();
              return;
            }
          }
        }
      });
    }
  }

  private GlobList getList(Key key) {
    if (key.getGlobType() == HiddenTransaction.TYPE) {
      return transactions;
    }
    if (key.getGlobType() == HiddenAccount.TYPE) {
      return accounts;
    }
    if (key.getGlobType() == HiddenBank.TYPE) {
      return banks;
    }
    if (key.getGlobType() == HiddenTransactionToCategory.TYPE) {
      return transactionToCategories;
    }
    if (key.getGlobType() == HiddenLabelToCategory.TYPE) {
      return labelToCategories;
    }
    if (key.getGlobType() == HiddenImport.TYPE) {
      return imports;
    }
    if (key.getGlobType() == HiddenCategory.TYPE) {
      return categories;
    }
    throw new UnexpectedApplicationState(key.getGlobType().getName() + " not managed");
  }

  public void delete() {
    transactions.clear();
    accounts.clear();
    transactionToCategories.clear();
  }

  private static class Factory implements CustomSerializableFactory {

    public String getSerializationName() {
      return USER_DATA;
    }

    public CustomSerializable create() {
      return new UserData();
    }
  }
}
