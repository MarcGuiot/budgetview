package com.budgetview.model;

import com.budgetview.model.util.TypeLoader;
import com.budgetview.shared.model.Provider;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.*;
import org.globsframework.model.Glob;

public class ImportedTransaction {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static StringField DATE;

  public static StringField BANK_DATE;

  public static DoubleField AMOUNT;

  public static StringField NOTE;

  public static StringField SIMPLE_LABEL;

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

  @Target(Provider.class)
  public static LinkField PROVIDER;

  public static IntegerField PROVIDER_ACCOUNT_ID;

  public static IntegerField PROVIDER_TRANSACTION_ID;

  public static BooleanField DELETED;

  static {
    TypeLoader.init(ImportedTransaction.class, "importedTransaction");
  }

  public static ImportType getImportType(Glob transaction) {
    return ImportType.get(transaction.get(IMPORT_TYPE));
  }
}