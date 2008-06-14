package org.designup.picsou.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.GlobRepository;

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
