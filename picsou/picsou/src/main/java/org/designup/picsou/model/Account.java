package org.designup.picsou.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.annotations.Target;
import org.crossbowlabs.globs.metamodel.fields.*;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;
import static org.crossbowlabs.globs.model.FieldValue.value;
import org.crossbowlabs.globs.model.GlobRepository;

public class Account {
  public static final String SUMMARY_ACCOUNT_NUMBER = null;
  public static final int SUMMARY_ACCOUNT_ID = -1;

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static StringField NUMBER;

  @Target(BankEntity.class)
  public static LinkField BANK_ENTITY;

  public static IntegerField BRANCH_ID;

  public static StringField NAME;
  public static DoubleField BALANCE;
  public static DateField UPDATE_DATE;
  public static BooleanField IS_CARD_ACCOUNT;

  static {
    GlobTypeLoader.init(Account.class);
  }

  public static void createSummary(GlobRepository repository) {
    repository.create(TYPE,
                      value(ID, SUMMARY_ACCOUNT_ID),
                      value(NUMBER, SUMMARY_ACCOUNT_NUMBER));
  }
}
