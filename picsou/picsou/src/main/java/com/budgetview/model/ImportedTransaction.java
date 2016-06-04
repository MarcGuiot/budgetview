package com.budgetview.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.Glob;
import org.globsframework.utils.Utils;

public class ImportedTransaction {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static StringField DATE;

  public static StringField BANK_DATE;

  public static DoubleField AMOUNT;

  public static StringField NOTE;

  public static StringField OFX_CHECK_NUM;

  public static StringField OFX_NAME;

  public static StringField OFX_MEMO;

  public static StringField QIF_M;

  public static StringField QIF_P;

  public static StringField BANK_TRANSACTION_TYPE;  // N for qif, TRNTYPE for ofx.

  public static BooleanField IS_CARD;

  @Target(ImportType.class)
  public static IntegerField IMPORT_TYPE;

  @Target(RealAccount.class)
  public static LinkField ACCOUNT;

  @Target(ImportedSeries.class)
  public static LinkField SERIES;

  public static BooleanField SPLIT;

  @Target(ImportedTransaction.class)
  public static LinkField SPLIT_SOURCE;

  static {
    GlobTypeLoader.init(ImportedTransaction.class);
  }

  public static boolean isOfx(Glob importedTransaction) {
    return Utils.equal(ImportType.OFX.getId(), importedTransaction.get(IMPORT_TYPE));
  }

  public static boolean isQif(Glob importedTransaction) {
    return Utils.equal(ImportType.QIF.getId(), importedTransaction.get(IMPORT_TYPE));
  }

  public static ImportType getImportType(Glob transaction) {
    return ImportType.get(transaction.get(IMPORT_TYPE));
  }
}