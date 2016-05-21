package com.budgetview.gui.importer.edition;

import com.budgetview.model.Account;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class AccountToAccountType {
  public static GlobType TYPE;


  @Key
  public static IntegerField ID;

  @Target(Account.class)
  public static LinkField FROM_ACCOUNT;

  @Target(Account.class)
  public static LinkField TO_ACCOUNT;


  static {
    GlobTypeLoader.init(AccountToAccountType.class);
  }

}
