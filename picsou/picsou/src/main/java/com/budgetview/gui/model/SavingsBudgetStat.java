package com.budgetview.gui.model;

import com.budgetview.model.BudgetArea;
import com.budgetview.model.Account;
import com.budgetview.model.Month;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultDouble;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

public class SavingsBudgetStat {
  public static GlobType TYPE;

  @Key @Target(Month.class)
  public static LinkField MONTH;

  @Key @Target(Account.class)
  public static LinkField ACCOUNT;

  @DefaultDouble(0.0)
  public static DoubleField BEGIN_OF_MONTH_POSITION;

  @DefaultDouble(0.0)
  public static DoubleField END_OF_MONTH_POSITION;

  @DefaultDouble(0.0)
  public static DoubleField BALANCE;

  public static DoubleField LAST_KNOWN_ACCOUNT_POSITION;

  public static IntegerField LAST_KNOWN_POSITION_DAY;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_IN;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_IN_REMAINING;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_IN_OVERRUN;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_IN_PLANNED;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_OUT;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_OUT_REMAINING;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_OUT_OVERRUN;

  @DefaultDouble(0.0)
  public static DoubleField SAVINGS_OUT_PLANNED;

  static {
    GlobTypeLoader.init(SavingsBudgetStat.class);
  }

  public static DoubleField getObserved(BudgetArea budgetArea) {
    switch (budgetArea) {
      case TRANSFER:
        return SAVINGS_IN;
    }
    throw new UnexpectedApplicationState(budgetArea.getName() + " not managed");
  }

  public static DoubleField getPlanned(BudgetArea budgetArea) {
    switch (budgetArea) {
      case TRANSFER:
        return SAVINGS_IN_PLANNED;
    }
    throw new UnexpectedApplicationState(budgetArea.getName() + " not managed");
  }

  public static DoubleField getRemaining(BudgetArea budgetArea) {
    switch (budgetArea) {
      case TRANSFER:
        return SAVINGS_IN_REMAINING;
    }
    throw new UnexpectedApplicationState(budgetArea.getName() + " not managed");
  }

  public static DoubleField getOverrun(BudgetArea budgetArea) {
    switch (budgetArea) {
      case TRANSFER:
        return SAVINGS_IN_OVERRUN;
    }
    throw new UnexpectedApplicationState(budgetArea.getName() + " not managed");
  }

  public static Glob findSummary(int monthId, GlobRepository repository) {
    return repository.find(org.globsframework.model.Key.create(MONTH, monthId,
                                                               ACCOUNT, Account.SAVINGS_SUMMARY_ACCOUNT_ID));
  }

  public static Glob find(int monthId, int savingsAccountId, GlobRepository repository) {
    return repository.find(org.globsframework.model.Key.create(MONTH, monthId,
                                                               ACCOUNT, savingsAccountId));
  }
}