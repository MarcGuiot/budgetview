package org.designup.picsou.model;

import org.designup.picsou.server.serialization.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.*;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.index.NotUniqueIndex;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.*;
import static org.globsframework.model.Key.create;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.utils.Utils;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

public class Transaction {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Target(Month.class)
  public static LinkField MONTH; // yyyymm format
  public static IntegerField DAY; // Starts at 1

  @Target(Month.class)
  public static LinkField BANK_MONTH; // yyyymm format
  public static IntegerField BANK_DAY; // Starts at 1

  public static DoubleField AMOUNT;

  @NamingField
  public static StringField LABEL;

  public static StringField NOTE;
  public static StringField ORIGINAL_LABEL;
  public static BooleanField DISPENSABLE; // unused
  public static StringField LABEL_FOR_CATEGORISATION;

  @Target(Category.class)
  @DefaultInteger(0)
  public static LinkField CATEGORY;

  @Target(TransactionType.class)
  public static LinkField TRANSACTION_TYPE;

  @Target(Account.class)
  public static LinkField ACCOUNT;

  @Target(TransactionImport.class)
  public static LinkField IMPORT;

  public static BooleanField SPLIT;

  @Target(Transaction.class)
  public static LinkField SPLIT_SOURCE;

  @Target(Series.class)
  @Required()
  @DefaultInteger(1)
  public static LinkField SERIES;

  @DefaultBoolean(false)
  public static BooleanField PLANNED;

  public static NotUniqueIndex LABEL_FOR_CATEGORISATION_INDEX;

  public static NotUniqueIndex MONTH_INDEX;

  static {
    GlobTypeLoader loader = GlobTypeLoader.init(Transaction.class, "transaction");
    loader.defineNonUniqueIndex(LABEL_FOR_CATEGORISATION_INDEX, LABEL_FOR_CATEGORISATION);
    loader.defineNonUniqueIndex(MONTH_INDEX, MONTH);
  }

  public static int fullDate(Glob transaction) {
    return transaction.get(MONTH) * 100 + transaction.get(DAY);
  }

  public static int fullBankDate(Glob transaction) {
    return transaction.get(BANK_MONTH) * 100 + transaction.get(BANK_DAY);
  }

  public static String stringifyCategories(Glob transaction,
                                           GlobRepository repository,
                                           GlobStringifier categoryStringifier) {
    GlobList categories = getCategories(transaction, repository).sort(Category.ID);
    int index = 0;
    StringBuilder builder = new StringBuilder();
    for (Glob category : categories) {
      if (index++ > 0) {
        builder.append(", ");
      }
      builder.append(categoryStringifier.toString(category, repository));
    }
    return builder.toString();
  }

  public static boolean isCategorized(Glob transaction, GlobRepository repository) {
    return !hasNoCategory(transaction) || TransactionToCategory.hasCategories(transaction, repository);
  }

  public static boolean hasNoCategory(Glob transaction) {
    Integer categoryId = transaction.get(CATEGORY);
    return (categoryId == null) || Utils.equal(categoryId, Category.NONE);
  }

  public static GlobList getCategories(Glob transaction, GlobRepository repository) {
    GlobList categories = TransactionToCategory.getCategories(transaction.get(Transaction.ID), repository);
    if (!hasNoCategory(transaction)) {
      categories.add(0, repository.get(create(Category.TYPE, transaction.get(CATEGORY))));
    }
    return categories;
  }

  public static void setCategory(Glob transaction, Integer categoryId, GlobRepository repository) {
    repository.enterBulkDispatchingMode();

    try {
      repository.setTarget(transaction.getKey(), CATEGORY, create(Category.TYPE, categoryId));
      repository.update(transaction.getKey(), Transaction.SERIES, Series.OCCASIONAL_SERIES_ID);
      repository.delete(repository.findByIndex(TransactionToCategory.TRANSACTION_INDEX,
                                               TransactionToCategory.TRANSACTION,
                                               transaction.get(Transaction.ID)).getGlobs());
    }
    finally {
      repository.completeBulkDispatchingMode();
    }
  }

  public static void setCategory(Glob transaction, Glob category, GlobRepository repository) {
    setCategory(transaction, category.get(Category.ID), repository);
  }

  public static boolean isSplitPart(Glob transaction) {
    return transaction.get(SPLIT_SOURCE) != null;
  }

  public static boolean isSplitSource(Glob transaction) {
    return Boolean.TRUE.equals(transaction.get(SPLIT));
  }

  public static boolean isSplit(Glob transaction) {
    return isSplitSource(transaction) || isSplitPart(transaction);
  }

  public static double subtract(double initialValue, double amount) {
    return Math.rint(Math.signum(initialValue) * (Math.abs(initialValue) - Math.abs(amount)) * 100.0) / 100.0;
  }

  public static GlobList getSplittedTransactions(Glob transaction, GlobRepository repository) {
    GlobList splittedTransactions = new GlobList();
    if (isSplitSource(transaction)) {
      splittedTransactions.add(transaction);
      splittedTransactions.addAll(repository.findLinkedTo(transaction, SPLIT_SOURCE));
    }
    else if (isSplitPart(transaction)) {
      Glob initialTransaction = repository.findLinkTarget(transaction, SPLIT_SOURCE);
      splittedTransactions.add(initialTransaction);
      splittedTransactions.addAll(repository.findLinkedTo(initialTransaction, SPLIT_SOURCE));
    }
    return splittedTransactions;
  }

  public static boolean isPlanned(Glob transaction) {
    return Utils.equal(TransactionType.PLANNED.getId(), transaction.get(Transaction.TRANSACTION_TYPE));
  }

  public static class Serializer implements PicsouGlobSerializer {

    public byte[] serializeData(FieldValues fieldValues) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput output = serializedByteArrayOutput.getOutput();
      output.writeString(fieldValues.get(Transaction.ORIGINAL_LABEL));
      output.writeString(fieldValues.get(Transaction.LABEL));
      output.writeString(fieldValues.get(Transaction.LABEL_FOR_CATEGORISATION));
      output.writeString(fieldValues.get(Transaction.NOTE));
      output.writeInteger(fieldValues.get(Transaction.MONTH));
      output.writeInteger(fieldValues.get(Transaction.DAY));
      output.writeInteger(fieldValues.get(Transaction.BANK_MONTH));
      output.writeInteger(fieldValues.get(Transaction.BANK_DAY));
      output.writeDouble(fieldValues.get(Transaction.AMOUNT));
      output.writeInteger(fieldValues.get(Transaction.ACCOUNT));
      output.writeInteger(fieldValues.get(Transaction.TRANSACTION_TYPE));
      output.writeInteger(fieldValues.get(Transaction.CATEGORY));
      output.writeBoolean(fieldValues.get(Transaction.SPLIT));
      output.writeInteger(fieldValues.get(Transaction.SPLIT_SOURCE));
      output.writeBoolean(fieldValues.get(Transaction.DISPENSABLE));
      output.writeInteger(fieldValues.get(Transaction.SERIES));
      output.writeBoolean(fieldValues.get(Transaction.PLANNED));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(Transaction.ORIGINAL_LABEL, input.readString());
      fieldSetter.set(Transaction.LABEL, input.readString());
      fieldSetter.set(Transaction.LABEL_FOR_CATEGORISATION, input.readString());
      fieldSetter.set(Transaction.NOTE, input.readString());
      fieldSetter.set(Transaction.MONTH, input.readInteger());
      fieldSetter.set(Transaction.DAY, input.readInteger());
      fieldSetter.set(Transaction.BANK_MONTH, input.readInteger());
      fieldSetter.set(Transaction.BANK_DAY, input.readInteger());
      fieldSetter.set(Transaction.AMOUNT, input.readDouble());
      fieldSetter.set(Transaction.ACCOUNT, input.readInteger());
      fieldSetter.set(Transaction.TRANSACTION_TYPE, input.readInteger());
      fieldSetter.set(Transaction.CATEGORY, input.readInteger());
      fieldSetter.set(Transaction.SPLIT, input.readBoolean());
      fieldSetter.set(Transaction.SPLIT_SOURCE, input.readInteger());
      fieldSetter.set(Transaction.DISPENSABLE, input.readBoolean());
      fieldSetter.set(Transaction.SERIES, input.readInteger());
      fieldSetter.set(Transaction.PLANNED, input.readBoolean());
    }

    public int getWriteVersion() {
      return 1;
    }
  }
}
