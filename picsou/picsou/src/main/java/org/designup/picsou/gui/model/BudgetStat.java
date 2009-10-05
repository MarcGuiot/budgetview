package org.designup.picsou.gui.model;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultDouble;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

public class BudgetStat {
  public static GlobType TYPE;

  @Key
  @Target(Month.class)
  public static LinkField MONTH;

  @DefaultDouble(0.0)
  public static DoubleField BEGIN_OF_MONTH_ACCOUNT_POSITION;

  @DefaultDouble(0.0)
  public static DoubleField END_OF_MONTH_ACCOUNT_POSITION;

  @DefaultDouble(0.0)
  public static DoubleField MONTH_BALANCE;

  public static DoubleField LAST_KNOWN_ACCOUNT_POSITION;

  public static IntegerField LAST_KNOWN_ACCOUNT_POSITION_DAY;

  @DefaultDouble(0.0)
  public static DoubleField INCOME;

  @DefaultDouble(0.0)
  public static DoubleField INCOME_REMAINING;

  @DefaultDouble(0.0)
  public static DoubleField INCOME_PLANNED;

  @DefaultDouble(0.0)
  public static DoubleField INCOME_SUMMARY;

  @DefaultDouble(0.0)
  public static DoubleField EXPENSE;

  @DefaultDouble(0.0)
  public static DoubleField EXPENSE_REMAINING;

  @DefaultDouble(0.0)
  public static DoubleField EXPENSE_PLANNED;

  @DefaultDouble(0.0)
  public static DoubleField EXPENSE_SUMMARY;

  @DefaultDouble(0.0)
  public static DoubleField RECURRING;

  @DefaultDouble(0.0)
  public static DoubleField RECURRING_REMAINING;

  @DefaultDouble(0.0)
  public static DoubleField RECURRING_PLANNED;

  @DefaultDouble(0.0)
  public static DoubleField RECURRING_SUMMARY;

  @DefaultDouble(0.0)
  public static DoubleField ENVELOPES;

  @DefaultDouble(0.0)
  public static DoubleField ENVELOPES_REMAINING;

  @DefaultDouble(0.0)
  public static DoubleField ENVELOPES_PLANNED;

  @DefaultDouble(0.0)
  public static DoubleField ENVELOPES_SUMMARY;

  @DefaultDouble(0.0)
  public static DoubleField SPECIAL;

  @DefaultDouble(0.0)
  public static DoubleField SPECIAL_REMAINING;

  @DefaultDouble(0.0)
  public static DoubleField SPECIAL_PLANNED;

  @DefaultDouble(0.0)
  public static DoubleField SPECIAL_SUMMARY;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_REMAINING;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_PLANNED;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_SUMMARY;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_IN;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_IN_REMAINING;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_IN_PLANNED;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_IN_SUMMARY;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_OUT;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_OUT_REMAINING;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_OUT_PLANNED;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_OUT_SUMMARY;

  @DefaultDouble(0.0)
  public static DoubleField UNCATEGORIZED;

  @DefaultDouble(0.0)
  public static DoubleField UNCATEGORIZED_ABS;

  public static DoubleField[] INCOME_FIELDS = {INCOME, INCOME_REMAINING};

  public static DoubleField[] EXPENSE_FIELDS = {ENVELOPES, ENVELOPES_REMAINING,
                                                SPECIAL, SPECIAL_REMAINING, SAVINGS, SAVINGS_REMAINING, UNCATEGORIZED};

  static {
    GlobTypeLoader.init(BudgetStat.class);
  }

  public static DoubleField getRemaining(BudgetArea budgetArea) {
    switch (budgetArea) {
      case ENVELOPES:
        return ENVELOPES_REMAINING;
      case INCOME:
        return INCOME_REMAINING;
      case SAVINGS:
        return SAVINGS_REMAINING;
      case RECURRING:
        return RECURRING_REMAINING;
      case SPECIAL:
        return SPECIAL_REMAINING;
    }
    throw new UnexpectedApplicationState(budgetArea.getName() + " not managed");
  }

  public static DoubleField getObserved(BudgetArea budgetArea) {
    switch (budgetArea) {
      case ENVELOPES:
        return ENVELOPES;
      case INCOME:
        return INCOME;
      case SAVINGS:
        return SAVINGS;
      case RECURRING:
        return RECURRING;
      case SPECIAL:
        return SPECIAL;
    }
    throw new UnexpectedApplicationState(budgetArea.getName() + " not managed");
  }

  public static DoubleField getPlanned(BudgetArea budgetArea) {
    switch (budgetArea) {
      case ENVELOPES:
        return ENVELOPES_PLANNED;
      case INCOME:
        return INCOME_PLANNED;
      case SAVINGS:
        return SAVINGS_PLANNED;
      case RECURRING:
        return RECURRING_PLANNED;
      case SPECIAL:
        return SPECIAL_PLANNED;
    }
    throw new UnexpectedApplicationState(budgetArea.getName() + " not managed");
  }

  public static DoubleField getSummary(BudgetArea budgetArea) {
    switch (budgetArea) {
      case ENVELOPES:
        return ENVELOPES_SUMMARY;
      case INCOME:
        return INCOME_SUMMARY;
      case SAVINGS:
        return SAVINGS_SUMMARY;
      case RECURRING:
        return RECURRING_SUMMARY;
      case SPECIAL:
        return SPECIAL_SUMMARY;
    }
    throw new UnexpectedApplicationState(budgetArea.getName() + " not managed");
  }
}