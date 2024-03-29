package com.budgetview.desktop.model;

import com.budgetview.model.Month;
import com.budgetview.model.util.TypeLoader;
import com.budgetview.shared.model.BudgetArea;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultDouble;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

public class BudgetStat {
  public static GlobType TYPE;

  @Key
  @Target(Month.class)
  public static LinkField MONTH;

  @DefaultDouble(0.0)
  public static DoubleField MONTH_BALANCE;

  @DefaultDouble(0.0)
  public static DoubleField INCOME;

  @DefaultDouble(0.0)
  public static DoubleField INCOME_POSITIVE_REMAINING;

  @DefaultDouble(0.0)
  public static DoubleField INCOME_NEGATIVE_REMAINING;

  @DefaultDouble(0.0)
  public static DoubleField INCOME_PLANNED;

  @DefaultDouble(0.0)
  public static DoubleField INCOME_SUMMARY;

  @DefaultDouble(0.0)
  public static DoubleField INCOME_POSITIVE_OVERRUN;

  @DefaultDouble(0.0)
  public static DoubleField INCOME_NEGATIVE_OVERRUN;

  @DefaultDouble(0.0)
  public static DoubleField EXPENSE;

  @DefaultDouble(0.0)
  public static DoubleField EXPENSE_REMAINING;

  @DefaultDouble(0.0)
  public static DoubleField EXPENSE_OVERRUN;

  @DefaultDouble(0.0)
  public static DoubleField EXPENSE_PLANNED;

  @DefaultDouble(0.0)
  public static DoubleField EXPENSE_SUMMARY;

  @DefaultDouble(0.0)
  public static DoubleField RECURRING;

  @DefaultDouble(0.0)
  public static DoubleField RECURRING_POSITIVE_REMAINING;

  @DefaultDouble(0.0)
  public static DoubleField RECURRING_NEGATIVE_REMAINING;

  @DefaultDouble(0.0)
  public static DoubleField RECURRING_PLANNED;

  @DefaultDouble(0.0)
  public static DoubleField RECURRING_SUMMARY;

  @DefaultDouble(0.0)
  public static DoubleField RECURRING_POSITIVE_OVERRUN;

  @DefaultDouble(0.0)
  public static DoubleField RECURRING_NEGATIVE_OVERRUN;

  @DefaultDouble(0.0)
  public static DoubleField VARIABLE;

  @DefaultDouble(0.0)
  public static DoubleField VARIABLE_POSITIVE_REMAINING;

  @DefaultDouble(0.0)
  public static DoubleField VARIABLE_NEGATIVE_REMAINING;

  @DefaultDouble(0.0)
  public static DoubleField VARIABLE_PLANNED;

  @DefaultDouble(0.0)
  public static DoubleField VARIABLE_SUMMARY;

  @DefaultDouble(0.0)
  public static DoubleField VARIABLE_POSITIVE_OVERRUN;

  @DefaultDouble(0.0)
  public static DoubleField VARIABLE_NEGATIVE_OVERRUN;

  @DefaultDouble(0.0)
  public static DoubleField EXTRAS;

  @DefaultDouble(0.0)
  public static DoubleField EXTRAS_POSITIVE_REMAINING;

  @DefaultDouble(0.0)
  public static DoubleField EXTRAS_NEGATIVE_REMAINING;

  @DefaultDouble(0.0)
  public static DoubleField EXTRAS_PLANNED;

  @DefaultDouble(0.0)
  public static DoubleField EXTRAS_SUMMARY;

  @DefaultDouble(0.0)
  public static DoubleField EXTRAS_POSITIVE_OVERRUN;

  @DefaultDouble(0.0)
  public static DoubleField EXTRAS_NEGATIVE_OVERRUN;

  @DefaultDouble(0.0)
  public static DoubleField OTHER;

  @DefaultDouble(0.0)
  public static DoubleField OTHER_POSITIVE_REMAINING;

  @DefaultDouble(0.0)
  public static DoubleField OTHER_NEGATIVE_REMAINING;

  @DefaultDouble(0.0)
  public static DoubleField OTHER_PLANNED;

  @DefaultDouble(0.0)
  public static DoubleField OTHER_SUMMARY;

  @DefaultDouble(0.0)
  public static DoubleField OTHER_POSITIVE_OVERRUN;

  @DefaultDouble(0.0)
  public static DoubleField OTHER_NEGATIVE_OVERRUN;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_POSITIVE_REMAINING;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_NEGATIVE_REMAINING;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_PLANNED;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_SUMMARY;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_POSITIVE_OVERRUN;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_NEGATIVE_OVERRUN;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_IN;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_IN_POSITIVE_REMAINING;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_IN_NEGATIVE_REMAINING;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_IN_PLANNED;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_IN_SUMMARY;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_IN_POSITIVE_OVERRUN;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_IN_NEGATIVE_OVERRUN;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_OUT_POSITIVE_REMAINING;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_OUT_NEGATIVE_REMAINING;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_OUT_PLANNED;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_OUT_SUMMARY;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_OUT_POSITIVE_OVERRUN;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_OUT_NEGATIVE_OVERRUN;

  @DefaultDouble(0.0)
  public static DoubleField UNCATEGORIZED;

  @DefaultDouble(0.0)
  public static DoubleField UNCATEGORIZED_ABS;

  public static DoubleField[] EXPENSE_FIELDS;
  public static DoubleField[] EXPENSE_REMAINING_FIELDS;

  static {
    TypeLoader.init(BudgetStat.class, "budgetStat");
    EXPENSE_FIELDS = new DoubleField[]{VARIABLE, RECURRING, EXTRAS};
    EXPENSE_REMAINING_FIELDS = new DoubleField[]{VARIABLE_NEGATIVE_REMAINING, VARIABLE_POSITIVE_REMAINING,
                                                 RECURRING_NEGATIVE_REMAINING, RECURRING_POSITIVE_REMAINING,
                                                 EXTRAS_NEGATIVE_REMAINING, EXTRAS_POSITIVE_REMAINING};
  }

  public static DoubleField getPositiveRemaining(BudgetArea budgetArea) {
    switch (budgetArea) {
      case VARIABLE:
        return VARIABLE_POSITIVE_REMAINING;
      case INCOME:
        return INCOME_POSITIVE_REMAINING;
      case TRANSFER:
        return SAVINGS_POSITIVE_REMAINING;
      case RECURRING:
        return RECURRING_POSITIVE_REMAINING;
      case EXTRAS:
        return EXTRAS_POSITIVE_REMAINING;
      case OTHER:
        return OTHER_POSITIVE_REMAINING;
    }
    throw new UnexpectedApplicationState(budgetArea.getName() + " not managed");
  }

  public static DoubleField getNegativeRemaining(BudgetArea budgetArea) {
    switch (budgetArea) {
      case VARIABLE:
        return VARIABLE_NEGATIVE_REMAINING;
      case INCOME:
        return INCOME_NEGATIVE_REMAINING;
      case TRANSFER:
        return SAVINGS_NEGATIVE_REMAINING;
      case RECURRING:
        return RECURRING_NEGATIVE_REMAINING;
      case EXTRAS:
        return EXTRAS_NEGATIVE_REMAINING;
      case OTHER:
        return OTHER_NEGATIVE_REMAINING;
    }
    throw new UnexpectedApplicationState(budgetArea.getName() + " not managed");
  }

  public static DoubleField getObserved(BudgetArea budgetArea) {
    switch (budgetArea) {
      case VARIABLE:
        return VARIABLE;
      case INCOME:
        return INCOME;
      case TRANSFER:
        return SAVINGS;
      case RECURRING:
        return RECURRING;
      case EXTRAS:
        return EXTRAS;
      case OTHER:
        return OTHER;
    }
    throw new UnexpectedApplicationState(budgetArea.getName() + " not managed");
  }

  public static DoubleField getPlanned(BudgetArea budgetArea) {
    switch (budgetArea) {
      case VARIABLE:
        return VARIABLE_PLANNED;
      case INCOME:
        return INCOME_PLANNED;
      case TRANSFER:
        return SAVINGS_PLANNED;
      case RECURRING:
        return RECURRING_PLANNED;
      case EXTRAS:
        return EXTRAS_PLANNED;
      case OTHER:
        return OTHER_PLANNED;
    }
    throw new UnexpectedApplicationState(budgetArea.getName() + " not managed");
  }

  public static DoubleField getSummary(BudgetArea budgetArea) {
    switch (budgetArea) {
      case VARIABLE:
        return VARIABLE_SUMMARY;
      case INCOME:
        return INCOME_SUMMARY;
      case TRANSFER:
        return SAVINGS_SUMMARY;
      case RECURRING:
        return RECURRING_SUMMARY;
      case EXTRAS:
        return EXTRAS_SUMMARY;
      case OTHER:
        return OTHER_SUMMARY;
    }
    throw new UnexpectedApplicationState(budgetArea.getName() + " not managed");
  }

  public static DoubleField getPositiveOverrun(BudgetArea budgetArea) {
    switch (budgetArea) {
      case VARIABLE:
        return VARIABLE_POSITIVE_OVERRUN;
      case INCOME:
        return INCOME_POSITIVE_OVERRUN;
      case TRANSFER:
        return SAVINGS_POSITIVE_OVERRUN;
      case RECURRING:
        return RECURRING_POSITIVE_OVERRUN;
      case EXTRAS:
        return EXTRAS_POSITIVE_OVERRUN;
      case OTHER:
        return OTHER_POSITIVE_OVERRUN;
    }
    throw new UnexpectedApplicationState(budgetArea.getName() + " not managed");
  }

  public static DoubleField getNegativeOverrun(BudgetArea budgetArea) {
    switch (budgetArea) {
      case VARIABLE:
        return VARIABLE_NEGATIVE_OVERRUN;
      case INCOME:
        return INCOME_NEGATIVE_OVERRUN;
      case TRANSFER:
        return SAVINGS_NEGATIVE_OVERRUN;
      case RECURRING:
        return RECURRING_NEGATIVE_OVERRUN;
      case EXTRAS:
        return EXTRAS_NEGATIVE_OVERRUN;
      case OTHER:
        return OTHER_NEGATIVE_OVERRUN;
    }
    throw new UnexpectedApplicationState(budgetArea.getName() + " not managed");
  }
}