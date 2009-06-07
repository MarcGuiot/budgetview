package org.designup.picsou.model;

import org.designup.picsou.server.serialization.PicsouGlobSerializer;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.*;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.index.MultiFieldNotUniqueIndex;
import org.globsframework.metamodel.index.NotUniqueIndex;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.*;
import static org.globsframework.model.Key.create;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

public class Transaction {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Target(Month.class)
  @Required
  public static LinkField MONTH; // yyyymm format
  @Required
  public static IntegerField DAY; // Starts at 1

  @Target(Month.class)
  @Required
  public static LinkField BANK_MONTH; // yyyymm format
  @Required
  public static IntegerField BANK_DAY; // Starts at 1

  @Required
  public static DoubleField AMOUNT;

  public static DoubleField SUMMARY_POSITION;

  public static DoubleField ACCOUNT_POSITION;

  @NamingField
  public static StringField LABEL;

  public static StringField ORIGINAL_LABEL;

  public static StringField NOTE;

  public static StringField OFX_CHECK_NUM;

  public static StringField OFX_NAME;

  public static StringField OFX_MEMO;

  public static StringField QIF_M;

  public static StringField QIF_P;

  public static StringField BANK_TRANSACTION_TYPE;  //QIF : N et ofx : TRNTYPE

  public static BooleanField IS_OFX;

  public static StringField LABEL_FOR_CATEGORISATION;

  /** @deprecated */
  @Target(Category.class)
  @DefaultInteger(0)
  public static LinkField CATEGORY;

  @Target(TransactionType.class)
  @Required
  public static LinkField TRANSACTION_TYPE;

  @Target(Account.class)
  public static LinkField ACCOUNT;

  @Target(TransactionImport.class)
  public static LinkField IMPORT;

  public static BooleanField SPLIT;

  @Target(Transaction.class)
  public static LinkField SPLIT_SOURCE;

  public static IntegerField DAY_BEFORE_SHIFT;

  @Target(Series.class)
  @Required()
  @DefaultInteger(1)
  public static LinkField SERIES;

  @Target(SubSeries.class)
  public static LinkField SUB_SERIES;

  @DefaultBoolean(false)
  public static BooleanField PLANNED;

  @DefaultBoolean(false)
  public static BooleanField MIRROR;

  @DefaultBoolean(false)
  public static BooleanField CREATED_BY_SERIES;

  @Target(Transaction.class)
  public static LinkField NOT_IMPORTED_TRANSACTION;

  public static NotUniqueIndex LABEL_FOR_CATEGORISATION_INDEX;

  public static MultiFieldNotUniqueIndex SERIES_INDEX;

  static {
    GlobTypeLoader loader = GlobTypeLoader.init(Transaction.class, "transaction");
    loader.defineNonUniqueIndex(LABEL_FOR_CATEGORISATION_INDEX, LABEL_FOR_CATEGORISATION);
    loader.defineMultiFieldNotUniqueIndex(SERIES_INDEX, SERIES, MONTH);
  }

  public static int fullDate(Glob transaction) {
    return transaction.get(MONTH) * 100 + transaction.get(DAY);
  }

  public static int fullBankDate(Glob transaction) {
    return transaction.get(BANK_MONTH) * 100 + transaction.get(BANK_DAY);
  }

  public static boolean isSplitPart(Glob transaction) {
    return transaction.get(SPLIT_SOURCE) != null;
  }

  public static boolean isSplitSource(Glob transaction) {
    Boolean isSplit = transaction.get(SPLIT);
    return isSplit != null && Boolean.TRUE.equals(isSplit);
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
    return transaction.get(Transaction.PLANNED);
  }

  public static String anonymise(String labelOrNote) {
    String[] strings = labelOrNote.split(" ");
    StringBuilder builder = new StringBuilder();
    for (String string : strings) {
      int i = string.length() - 1;
      boolean noDigits = true;
      while (i >= 0) {
        char c = string.charAt(i);
        if (c >= '0' && c <= '9') {
          noDigits = false;
          break;
        }
        i--;
      }
      if (noDigits) {
        builder.append(" ").append(string);
      }
    }
    return builder.toString().replaceAll("  *", " ").trim();
  }

  public static String anonymise(String note, String label, Integer transactionType) {
    String labelOrNote;
    if (TransactionType.getId(TransactionType.CHECK).equals(transactionType)
        || TransactionType.getId(TransactionType.WITHDRAWAL).equals(transactionType)
        || TransactionType.getId(TransactionType.DEPOSIT).equals(transactionType)) {
      labelOrNote = note;
    }
    else {
      labelOrNote = label;
    }
    if (labelOrNote == null || labelOrNote.length() == 0) {
      return null;
    }
    return anonymise(labelOrNote);
  }

  public static boolean isMirrorTransaction(FieldValues values) {
    return values.get(MIRROR);
  }

  public static boolean isCreatedBySeries(Glob transaction) {
    return transaction.get(CREATED_BY_SERIES);
  }

  public static String getLabel(boolean planned, Glob series) {
    if (planned) {
      return Series.getPlannedTransactionLabel(series.get(Series.ID), series);
    }
    else {
      return series.get(Series.NAME).toUpperCase();
    }
  }

  static public boolean isTransactionBeforeOrEqual(Glob transaction, int month, int day) {
    return (transaction.get(BANK_MONTH) < month || (transaction.get(BANK_MONTH) == month &&
    transaction.get(BANK_DAY) <= day));
  }

  public static class Serializer implements PicsouGlobSerializer {

    public int getWriteVersion() {
      return 7;
    }

    public byte[] serializeData(FieldValues fieldValues) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput output = serializedByteArrayOutput.getOutput();
      output.writeUtf8String(fieldValues.get(Transaction.ORIGINAL_LABEL));
      output.writeUtf8String(fieldValues.get(Transaction.LABEL));
      output.writeUtf8String(fieldValues.get(Transaction.LABEL_FOR_CATEGORISATION));
      output.writeUtf8String(fieldValues.get(Transaction.BANK_TRANSACTION_TYPE));
      output.writeUtf8String(fieldValues.get(Transaction.NOTE));
      output.writeInteger(fieldValues.get(Transaction.MONTH));
      output.writeInteger(fieldValues.get(Transaction.DAY));
      output.writeInteger(fieldValues.get(Transaction.BANK_MONTH));
      output.writeInteger(fieldValues.get(Transaction.BANK_DAY));
      output.writeDouble(fieldValues.get(Transaction.AMOUNT));
      output.writeDouble(fieldValues.get(Transaction.SUMMARY_POSITION));
      output.writeDouble(fieldValues.get(Transaction.ACCOUNT_POSITION));
      output.writeInteger(fieldValues.get(Transaction.ACCOUNT));
      output.writeInteger(fieldValues.get(Transaction.TRANSACTION_TYPE));
      output.writeBoolean(fieldValues.get(Transaction.SPLIT));
      output.writeInteger(fieldValues.get(Transaction.SPLIT_SOURCE));
      output.writeInteger(fieldValues.get(Transaction.DAY_BEFORE_SHIFT));
      output.writeInteger(fieldValues.get(Transaction.SERIES));
      output.writeInteger(fieldValues.get(Transaction.SUB_SERIES));
      output.writeBoolean(fieldValues.get(Transaction.PLANNED));
      output.writeBoolean(fieldValues.get(Transaction.MIRROR));
      output.writeBoolean(fieldValues.get(Transaction.CREATED_BY_SERIES));
      output.writeInteger(fieldValues.get(Transaction.NOT_IMPORTED_TRANSACTION));
      output.writeUtf8String(fieldValues.get(Transaction.OFX_CHECK_NUM));
      output.writeUtf8String(fieldValues.get(Transaction.OFX_MEMO));
      output.writeUtf8String(fieldValues.get(Transaction.OFX_NAME));
      output.writeUtf8String(fieldValues.get(Transaction.QIF_M));
      output.writeUtf8String(fieldValues.get(Transaction.QIF_P));
      output.writeBoolean(fieldValues.get(Transaction.IS_OFX));
      output.writeInteger(fieldValues.get(Transaction.IMPORT));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
      else if (version == 2) {
        deserializeDataV2(fieldSetter, data);
      }
      else if (version == 3) {
        deserializeDataV3(fieldSetter, data);
      }
      else if (version == 4) {
        deserializeDataV4(fieldSetter, data);
      }
      else if (version == 5) {
        deserializeDataV5(fieldSetter, data);
      }
      else if (version == 6) {
        deserializeDataV6(fieldSetter, data);
      }
      else if (version == 7) {
        deserializeDataV7(fieldSetter, data);
      }
    }

    private void deserializeDataV7(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(Transaction.ORIGINAL_LABEL, input.readUtf8String());
      fieldSetter.set(Transaction.LABEL, input.readUtf8String());
      fieldSetter.set(Transaction.LABEL_FOR_CATEGORISATION, input.readUtf8String());
      fieldSetter.set(Transaction.BANK_TRANSACTION_TYPE, input.readUtf8String());
      fieldSetter.set(Transaction.NOTE, input.readUtf8String());
      fieldSetter.set(Transaction.MONTH, input.readInteger());
      fieldSetter.set(Transaction.DAY, input.readInteger());
      fieldSetter.set(Transaction.BANK_MONTH, input.readInteger());
      fieldSetter.set(Transaction.BANK_DAY, input.readInteger());
      fieldSetter.set(Transaction.AMOUNT, input.readDouble());
      fieldSetter.set(Transaction.SUMMARY_POSITION, input.readDouble());
      fieldSetter.set(Transaction.ACCOUNT_POSITION, input.readDouble());
      fieldSetter.set(Transaction.ACCOUNT, input.readInteger());
      fieldSetter.set(Transaction.TRANSACTION_TYPE, input.readInteger());
      fieldSetter.set(Transaction.SPLIT, input.readBoolean());
      fieldSetter.set(Transaction.SPLIT_SOURCE, input.readInteger());
      fieldSetter.set(Transaction.DAY_BEFORE_SHIFT, input.readInteger());
      fieldSetter.set(Transaction.SERIES, input.readInteger());
      fieldSetter.set(Transaction.SUB_SERIES, input.readInteger());
      fieldSetter.set(Transaction.PLANNED, input.readBoolean());
      fieldSetter.set(Transaction.MIRROR, input.readBoolean());
      fieldSetter.set(Transaction.CREATED_BY_SERIES, input.readBoolean());
      fieldSetter.set(Transaction.NOT_IMPORTED_TRANSACTION, input.readInteger());
      fieldSetter.set(Transaction.OFX_CHECK_NUM, input.readUtf8String());
      fieldSetter.set(Transaction.OFX_MEMO, input.readUtf8String());
      fieldSetter.set(Transaction.OFX_NAME, input.readUtf8String());
      fieldSetter.set(Transaction.QIF_M, input.readUtf8String());
      fieldSetter.set(Transaction.QIF_P, input.readUtf8String());
      fieldSetter.set(Transaction.IS_OFX, input.readBoolean());
      fieldSetter.set(Transaction.IMPORT, input.readInteger());
    }

    private void deserializeDataV6(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(Transaction.ORIGINAL_LABEL, input.readUtf8String());
      fieldSetter.set(Transaction.LABEL, input.readUtf8String());
      fieldSetter.set(Transaction.LABEL_FOR_CATEGORISATION, input.readUtf8String());
      fieldSetter.set(Transaction.BANK_TRANSACTION_TYPE, input.readUtf8String());
      fieldSetter.set(Transaction.NOTE, input.readUtf8String());
      fieldSetter.set(Transaction.MONTH, input.readInteger());
      fieldSetter.set(Transaction.DAY, input.readInteger());
      fieldSetter.set(Transaction.BANK_MONTH, input.readInteger());
      fieldSetter.set(Transaction.BANK_DAY, input.readInteger());
      fieldSetter.set(Transaction.AMOUNT, input.readDouble());
      fieldSetter.set(Transaction.SUMMARY_POSITION, input.readDouble());
      fieldSetter.set(Transaction.ACCOUNT_POSITION, input.readDouble());
      fieldSetter.set(Transaction.ACCOUNT, input.readInteger());
      fieldSetter.set(Transaction.TRANSACTION_TYPE, input.readInteger());
      fieldSetter.set(Transaction.CATEGORY, input.readInteger());
      fieldSetter.set(Transaction.SPLIT, input.readBoolean());
      fieldSetter.set(Transaction.SPLIT_SOURCE, input.readInteger());
      fieldSetter.set(Transaction.DAY_BEFORE_SHIFT, input.readInteger());
      fieldSetter.set(Transaction.SERIES, input.readInteger());
      fieldSetter.set(Transaction.PLANNED, input.readBoolean());
      fieldSetter.set(Transaction.MIRROR, input.readBoolean());
      fieldSetter.set(Transaction.CREATED_BY_SERIES, input.readBoolean());
      fieldSetter.set(Transaction.NOT_IMPORTED_TRANSACTION, input.readInteger());
      fieldSetter.set(Transaction.OFX_CHECK_NUM, input.readUtf8String());
      fieldSetter.set(Transaction.OFX_MEMO, input.readUtf8String());
      fieldSetter.set(Transaction.OFX_NAME, input.readUtf8String());
      fieldSetter.set(Transaction.QIF_M, input.readUtf8String());
      fieldSetter.set(Transaction.QIF_P, input.readUtf8String());
      fieldSetter.set(Transaction.IS_OFX, input.readBoolean());
      fieldSetter.set(Transaction.IMPORT, input.readInteger());
    }

    private void deserializeDataV5(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(Transaction.ORIGINAL_LABEL, input.readUtf8String());
      fieldSetter.set(Transaction.LABEL, input.readUtf8String());
      fieldSetter.set(Transaction.LABEL_FOR_CATEGORISATION, input.readUtf8String());
      fieldSetter.set(Transaction.BANK_TRANSACTION_TYPE, input.readUtf8String());
      fieldSetter.set(Transaction.NOTE, input.readUtf8String());
      fieldSetter.set(Transaction.MONTH, input.readInteger());
      fieldSetter.set(Transaction.DAY, input.readInteger());
      fieldSetter.set(Transaction.BANK_MONTH, input.readInteger());
      fieldSetter.set(Transaction.BANK_DAY, input.readInteger());
      fieldSetter.set(Transaction.AMOUNT, input.readDouble());
      fieldSetter.set(Transaction.SUMMARY_POSITION, input.readDouble());
      fieldSetter.set(Transaction.ACCOUNT_POSITION, input.readDouble());
      fieldSetter.set(Transaction.ACCOUNT, input.readInteger());
      fieldSetter.set(Transaction.TRANSACTION_TYPE, input.readInteger());
      fieldSetter.set(Transaction.CATEGORY, input.readInteger());
      fieldSetter.set(Transaction.SPLIT, input.readBoolean());
      fieldSetter.set(Transaction.SPLIT_SOURCE, input.readInteger());
      fieldSetter.set(Transaction.SERIES, input.readInteger());
      fieldSetter.set(Transaction.PLANNED, input.readBoolean());
      fieldSetter.set(Transaction.MIRROR, input.readBoolean());
      fieldSetter.set(Transaction.CREATED_BY_SERIES, input.readBoolean());
      fieldSetter.set(Transaction.NOT_IMPORTED_TRANSACTION, input.readInteger());
      fieldSetter.set(Transaction.OFX_CHECK_NUM, input.readUtf8String());
      fieldSetter.set(Transaction.OFX_MEMO, input.readUtf8String());
      fieldSetter.set(Transaction.OFX_NAME, input.readUtf8String());
      fieldSetter.set(Transaction.QIF_M, input.readUtf8String());
      fieldSetter.set(Transaction.QIF_P, input.readUtf8String());
      fieldSetter.set(Transaction.IS_OFX, input.readBoolean());
      fieldSetter.set(Transaction.IMPORT, input.readInteger());
    }

    private void deserializeDataV4(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(Transaction.ORIGINAL_LABEL, input.readUtf8String());
      fieldSetter.set(Transaction.LABEL, input.readUtf8String());
      fieldSetter.set(Transaction.LABEL_FOR_CATEGORISATION, input.readUtf8String());
      fieldSetter.set(Transaction.BANK_TRANSACTION_TYPE, input.readUtf8String());
      fieldSetter.set(Transaction.NOTE, input.readUtf8String());
      fieldSetter.set(Transaction.MONTH, input.readInteger());
      fieldSetter.set(Transaction.DAY, input.readInteger());
      fieldSetter.set(Transaction.BANK_MONTH, input.readInteger());
      fieldSetter.set(Transaction.BANK_DAY, input.readInteger());
      fieldSetter.set(Transaction.AMOUNT, input.readDouble());
      fieldSetter.set(Transaction.SUMMARY_POSITION, input.readDouble());
      fieldSetter.set(Transaction.ACCOUNT_POSITION, input.readDouble());
      fieldSetter.set(Transaction.ACCOUNT, input.readInteger());
      fieldSetter.set(Transaction.TRANSACTION_TYPE, input.readInteger());
      fieldSetter.set(Transaction.CATEGORY, input.readInteger());
      fieldSetter.set(Transaction.SPLIT, input.readBoolean());
      fieldSetter.set(Transaction.SPLIT_SOURCE, input.readInteger());
      fieldSetter.set(Transaction.SERIES, input.readInteger());
      fieldSetter.set(Transaction.PLANNED, input.readBoolean());
      fieldSetter.set(Transaction.MIRROR, input.readBoolean());
      fieldSetter.set(Transaction.CREATED_BY_SERIES, input.readBoolean());
      fieldSetter.set(Transaction.NOT_IMPORTED_TRANSACTION, input.readInteger());
    }

    private void deserializeDataV3(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(Transaction.ORIGINAL_LABEL, input.readUtf8String());
      fieldSetter.set(Transaction.LABEL, input.readUtf8String());
      fieldSetter.set(Transaction.LABEL_FOR_CATEGORISATION, input.readUtf8String());
      fieldSetter.set(Transaction.BANK_TRANSACTION_TYPE, input.readUtf8String());
      fieldSetter.set(Transaction.NOTE, input.readUtf8String());
      fieldSetter.set(Transaction.MONTH, input.readInteger());
      fieldSetter.set(Transaction.DAY, input.readInteger());
      fieldSetter.set(Transaction.BANK_MONTH, input.readInteger());
      fieldSetter.set(Transaction.BANK_DAY, input.readInteger());
      fieldSetter.set(Transaction.AMOUNT, input.readDouble());
      fieldSetter.set(Transaction.SUMMARY_POSITION, input.readDouble());
      fieldSetter.set(Transaction.ACCOUNT_POSITION, input.readDouble());
      fieldSetter.set(Transaction.ACCOUNT, input.readInteger());
      fieldSetter.set(Transaction.TRANSACTION_TYPE, input.readInteger());
      fieldSetter.set(Transaction.CATEGORY, input.readInteger());
      fieldSetter.set(Transaction.SPLIT, input.readBoolean());
      fieldSetter.set(Transaction.SPLIT_SOURCE, input.readInteger());
      fieldSetter.set(Transaction.SERIES, input.readInteger());
      fieldSetter.set(Transaction.PLANNED, input.readBoolean());
      fieldSetter.set(Transaction.MIRROR, input.readBoolean());
      fieldSetter.set(Transaction.CREATED_BY_SERIES, input.readBoolean());
    }

    private void deserializeDataV2(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(Transaction.ORIGINAL_LABEL, input.readUtf8String());
      fieldSetter.set(Transaction.LABEL, input.readUtf8String());
      fieldSetter.set(Transaction.LABEL_FOR_CATEGORISATION, input.readUtf8String());
      fieldSetter.set(Transaction.BANK_TRANSACTION_TYPE, input.readUtf8String());
      fieldSetter.set(Transaction.NOTE, input.readUtf8String());
      fieldSetter.set(Transaction.MONTH, input.readInteger());
      fieldSetter.set(Transaction.DAY, input.readInteger());
      fieldSetter.set(Transaction.BANK_MONTH, input.readInteger());
      fieldSetter.set(Transaction.BANK_DAY, input.readInteger());
      Double amount = input.readDouble();
      fieldSetter.set(Transaction.AMOUNT, amount);
      fieldSetter.set(Transaction.SUMMARY_POSITION, input.readDouble());
      fieldSetter.set(Transaction.ACCOUNT_POSITION, input.readDouble());
      fieldSetter.set(Transaction.ACCOUNT, input.readInteger());
      Integer transactionType = input.readInteger();
      if (transactionType == TransactionType.PLANNED.getId()) {
        if (amount > 0) {
          fieldSetter.set(Transaction.TRANSACTION_TYPE, TransactionType.VIREMENT.getId());
        }
        else {
          fieldSetter.set(Transaction.TRANSACTION_TYPE, TransactionType.PRELEVEMENT.getId());
        }
      }
      else {
        fieldSetter.set(Transaction.TRANSACTION_TYPE, transactionType);
      }
      fieldSetter.set(Transaction.CATEGORY, input.readInteger());
      fieldSetter.set(Transaction.SPLIT, input.readBoolean());
      fieldSetter.set(Transaction.SPLIT_SOURCE, input.readInteger());
      fieldSetter.set(Transaction.SERIES, input.readInteger());
      fieldSetter.set(Transaction.PLANNED, input.readBoolean());
      fieldSetter.set(Transaction.CREATED_BY_SERIES, false);
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(Transaction.ORIGINAL_LABEL, input.readString());
      fieldSetter.set(Transaction.LABEL, input.readString());
      fieldSetter.set(Transaction.LABEL_FOR_CATEGORISATION, input.readString());
      fieldSetter.set(Transaction.BANK_TRANSACTION_TYPE, input.readString());
      fieldSetter.set(Transaction.NOTE, input.readString());
      fieldSetter.set(Transaction.MONTH, input.readInteger());
      fieldSetter.set(Transaction.DAY, input.readInteger());
      fieldSetter.set(Transaction.BANK_MONTH, input.readInteger());
      fieldSetter.set(Transaction.BANK_DAY, input.readInteger());
      Double amount = input.readDouble();
      fieldSetter.set(Transaction.AMOUNT, amount);
      fieldSetter.set(Transaction.SUMMARY_POSITION, input.readDouble());
      fieldSetter.set(Transaction.ACCOUNT_POSITION, input.readDouble());
      fieldSetter.set(Transaction.ACCOUNT, input.readInteger());
      Integer transactionType = input.readInteger();
      if (transactionType == TransactionType.PLANNED.getId()) {
        if (amount > 0) {
          fieldSetter.set(Transaction.TRANSACTION_TYPE, TransactionType.VIREMENT.getId());
        }
        else {
          fieldSetter.set(Transaction.TRANSACTION_TYPE, TransactionType.PRELEVEMENT.getId());
        }
      }
      else {
        fieldSetter.set(Transaction.TRANSACTION_TYPE, transactionType);
      }
      fieldSetter.set(Transaction.CATEGORY, input.readInteger());
      fieldSetter.set(Transaction.SPLIT, input.readBoolean());
      fieldSetter.set(Transaction.SPLIT_SOURCE, input.readInteger());
      fieldSetter.set(Transaction.SERIES, input.readInteger());
      fieldSetter.set(Transaction.PLANNED, input.readBoolean());
      fieldSetter.set(Transaction.CREATED_BY_SERIES, false);
    }
  }
}
