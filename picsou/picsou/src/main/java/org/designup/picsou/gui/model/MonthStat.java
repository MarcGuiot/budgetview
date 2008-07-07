package org.designup.picsou.gui.model;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.Month;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultDouble;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.KeyBuilder;
import org.globsframework.utils.exceptions.InvalidParameter;

public class MonthStat {
  public static GlobType TYPE;

  @Key
  @Target(Month.class)
  public static IntegerField MONTH;
  @Key
  @Target(Category.class)
  public static LinkField CATEGORY;
  @Key
  @Target(Account.class)
  public static LinkField ACCOUNT;

  @DefaultDouble(0.0)
  public static DoubleField TOTAL_SPENT;
  @DefaultDouble(0.0)
  public static DoubleField TOTAL_RECEIVED;

  @DefaultDouble(0.0)
  public static DoubleField INCOME_SPENT;
  @DefaultDouble(0.0)
  public static DoubleField INCOME_RECEIVED;

  @DefaultDouble(0.0)
  public static DoubleField SPENT_RECURRING;
  @DefaultDouble(0.0)
  public static DoubleField RECEIVED_RECURRING;

  @DefaultDouble(0.0)
  public static DoubleField SPENT_ENVELOP;
  @DefaultDouble(0.0)
  public static DoubleField RECEIVED_ENVELOP;

  @DefaultDouble(0.0)
  public static DoubleField SPENT_OCCASIONAL;
  @DefaultDouble(0.0)
  public static DoubleField RECEIVED_OCCASIONAL;

  @DefaultDouble(0.0)
  public static DoubleField DISPENSABLE;


  static {
    GlobTypeLoader.init(MonthStat.class);
  }

  public static org.globsframework.model.Key getKey(Integer month, Integer categoryId, int accountId) {
    return KeyBuilder.init(MONTH, month)
      .setValue(CATEGORY, categoryId)
      .setValue(ACCOUNT, accountId)
      .get();
  }

  public static DoubleField getSpent(BudgetArea bugdetArea) {
    switch (bugdetArea) {
      case RECURRING_EXPENSES:
        return SPENT_RECURRING;
      case EXPENSES_ENVELOPE:
        return SPENT_ENVELOP;
      case OCCASIONAL_EXPENSES:
        return SPENT_OCCASIONAL;
      case INCOME:
        return INCOME_SPENT;
      default:
        throw new InvalidParameter("budget area not managed");
    }
  }

  public static DoubleField getReceived(BudgetArea bugdetArea) {
    switch (bugdetArea) {
      case RECURRING_EXPENSES:
        return RECEIVED_RECURRING;
      case EXPENSES_ENVELOPE:
        return RECEIVED_ENVELOP;
      case OCCASIONAL_EXPENSES:
        return RECEIVED_OCCASIONAL;
      case INCOME:
        return INCOME_RECEIVED;
      default:
        throw new InvalidParameter("budget area not managed");
    }
  }
}
