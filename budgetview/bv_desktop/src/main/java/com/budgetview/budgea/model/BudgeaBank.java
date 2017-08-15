package com.budgetview.budgea.model;

import com.budgetview.model.Bank;
import com.budgetview.model.util.TypeLoader;
import com.budgetview.shared.model.Provider;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;

import static org.globsframework.model.utils.GlobMatchers.and;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class BudgeaBank {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Target(Bank.class)
  public static LinkField BANK;

  static {
    TypeLoader.init(BudgeaBank.class, "budgeaBank");
  }

  public static Glob findBudgetViewBank(int budgeaId, GlobRepository repository) {
    GlobList banks = repository.getAll(Bank.TYPE, and(fieldEquals(Bank.PROVIDER, Provider.BUDGEA.getId()),
                                                      fieldEquals(Bank.PROVIDER_ID, budgeaId)));
    return banks.isEmpty() ? null : banks.getFirst();
  }
}