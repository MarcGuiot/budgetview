package com.budgetview.android.checkers;

import com.budgetview.shared.model.*;
import com.xtremelabs.robolectric.shadows.ShadowContentValues;
import junit.framework.Assert;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;
import org.globsframework.model.Key;

import java.util.HashMap;
import java.util.Map;

import static org.globsframework.model.FieldValue.value;

public class LoadBuilder {

  private GlobRepository tempRepository;
  private Integer currentAccountId = null;
  private Glob currentSeriesValues = null;
  private int accountSequenceIndex = 0;
  private int transactionSequenceIndex = 0;
  private Map<String, Glob> seriesEntitiesByName = new HashMap<String, Glob>();

  LoadBuilder() {
    tempRepository = GlobRepositoryBuilder.createEmpty();
    for (BudgetArea budgetArea : BudgetArea.values()) {
      tempRepository.create(BudgetAreaEntity.TYPE,
                            value(BudgetAreaEntity.ID, budgetArea.id),
                            value(BudgetAreaEntity.LABEL, budgetArea.name().toLowerCase()));
    }
    seriesEntitiesByName.put("Uncategorized",
                             tempRepository.create(SeriesEntity.TYPE,
                                                   value(SeriesEntity.BUDGET_AREA, BudgetArea.UNCATEGORIZED.id),
                                                   value(SeriesEntity.NAME, "Uncategorized")));
  }

  public LoadBuilder addMainAccount(String accountName, Integer positionMonth, Integer positionDay, double position) {
    Glob account = tempRepository.create(AccountEntity.TYPE,
                                         value(AccountEntity.LABEL, accountName),
                                         value(AccountEntity.POSITION, position),
                                         value(AccountEntity.POSITION_MONTH, positionMonth),
                                         value(AccountEntity.POSITION_DAY, positionDay),
                                         value(AccountEntity.ACCOUNT_TYPE, AccountEntity.ACCOUNT_TYPE_MAIN),
                                         value(AccountEntity.SEQUENCE_NUMBER, accountSequenceIndex++));
    currentAccountId = account.get(AccountEntity.ID);
    return this;
  }

  public LoadBuilder addIncomeSeries(String name, int monthId, Double planned) {
    return doAddSeries(BudgetArea.INCOME, name, monthId, planned);
  }

  public LoadBuilder addRecurringSeries(String name, int monthId, Double planned) {
    return doAddSeries(BudgetArea.RECURRING, name, monthId, planned);
  }

  public LoadBuilder addVariableSeries(String name, int monthId, Double planned) {
    return doAddSeries(BudgetArea.VARIABLE, name, monthId, planned);
  }

  public void addExtrasSeries(String name, int monthId, Double planned) {
    doAddSeries(BudgetArea.EXTRAS, name, monthId, planned);
  }

  public void addSavingsSeries(String name, int monthId, Double planned) {
    doAddSeries(BudgetArea.SAVINGS, name, monthId, planned);
  }

  public void addUncategorizedSeries(String name, int monthId, Double planned) {
    doAddSeries(BudgetArea.UNCATEGORIZED, "Uncategorized", monthId, planned);
  }

  private LoadBuilder doAddSeries(BudgetArea budgetArea, String name, int monthId, Double planned) {
    Glob seriesEntity = findOrCreateSeriesEntity(name, budgetArea);
    currentSeriesValues = tempRepository.create(SeriesValues.TYPE,
                                                value(SeriesValues.BUDGET_AREA, budgetArea.id),
                                                value(SeriesValues.SERIES_ENTITY, seriesEntity.get(SeriesEntity.ID)),
                                                value(SeriesValues.MONTH, monthId),
                                                value(SeriesValues.AMOUNT, 0.00),
                                                value(SeriesValues.PLANNED_AMOUNT, planned),
                                                value(SeriesValues.OVERRUN_AMOUNT, null),
                                                value(SeriesValues.REMAINING_AMOUNT, planned));
    return this;
  }

  private Glob findOrCreateSeriesEntity(String name, BudgetArea budgetArea) {
    Glob entity = seriesEntitiesByName.get(name);
    if (entity == null) {
      entity = tempRepository.create(SeriesEntity.TYPE,
                                     value(SeriesEntity.NAME, name),
                                     value(SeriesEntity.BUDGET_AREA, budgetArea.id));
      seriesEntitiesByName.put(name, entity);
    }
    return entity;
  }

  public void addTransactionToSeries(String label, int day, double amount) {
    Assert.assertNotNull("A series must be created first", currentSeriesValues);
    Assert.assertNotNull("An account must be created first", currentAccountId);
    tempRepository.create(TransactionValues.TYPE,
                          value(TransactionValues.SERIES, currentSeriesValues.get(SeriesValues.SERIES_ENTITY)),
                          value(TransactionValues.ACCOUNT, currentAccountId),
                          value(TransactionValues.LABEL, label),
                          value(TransactionValues.AMOUNT, amount),
                          value(TransactionValues.BANK_MONTH, currentSeriesValues.get(SeriesValues.MONTH)),
                          value(TransactionValues.BANK_DAY, day),
                          value(TransactionValues.PLANNED, false),
                          value(TransactionValues.SEQUENCE_NUMBER, transactionSequenceIndex++));

    Double newSeriesAmount = currentSeriesValues.get(SeriesValues.AMOUNT) + amount;
    Double seriesPlanned = currentSeriesValues.get(SeriesValues.PLANNED_AMOUNT);
    Double remaining = 0.00;
    Double overrun = 0.00;
    if (Math.signum(newSeriesAmount) == Math.signum(seriesPlanned)) {
      if (Math.abs(newSeriesAmount) < Math.abs(seriesPlanned)) {
        remaining = seriesPlanned - newSeriesAmount;
        overrun = 0.00;
      }
      else {
        remaining = 0.00;
        overrun = newSeriesAmount - seriesPlanned;
      }
    }
    else {
      Assert.fail("Not supported - tbd");
    }
    tempRepository.update(currentSeriesValues.getKey(),
                          value(SeriesValues.AMOUNT, newSeriesAmount),
                          value(SeriesValues.REMAINING_AMOUNT, remaining),
                          value(SeriesValues.OVERRUN_AMOUNT, overrun));
  }

  private void complete() {
    for (Glob seriesValues : tempRepository.getAll(SeriesValues.TYPE)) {
      Glob budgetAreaValues =
        tempRepository.findOrCreate(Key.create(BudgetAreaValues.BUDGET_AREA, seriesValues.get(SeriesValues.BUDGET_AREA),
                                               BudgetAreaValues.MONTH, seriesValues.get(SeriesValues.MONTH)),
                                    value(BudgetAreaValues.INITIALLY_PLANNED, 0.00),
                                    value(BudgetAreaValues.ACTUAL, 0.00),
                                    value(BudgetAreaValues.REMAINDER, 0.00),
                                    value(BudgetAreaValues.OVERRUN, 0.00));
      tempRepository.update(budgetAreaValues.getKey(),
                            value(BudgetAreaValues.INITIALLY_PLANNED,
                                  budgetAreaValues.get(BudgetAreaValues.INITIALLY_PLANNED) + seriesValues.get(SeriesValues.PLANNED_AMOUNT)),
                            value(BudgetAreaValues.ACTUAL,
                                  budgetAreaValues.get(BudgetAreaValues.ACTUAL) + seriesValues.get(SeriesValues.AMOUNT)),
                            value(BudgetAreaValues.REMAINDER,
                                  budgetAreaValues.get(BudgetAreaValues.REMAINDER) + seriesValues.get(SeriesValues.REMAINING_AMOUNT)),
                            value(BudgetAreaValues.OVERRUN,
                                  budgetAreaValues.get(BudgetAreaValues.OVERRUN) + seriesValues.get(SeriesValues.OVERRUN_AMOUNT))
      );
    }

    for (Integer monthId : tempRepository.getAll(SeriesValues.TYPE).getValueSet(SeriesValues.MONTH)) {
      tempRepository.create(Key.create(MonthEntity.TYPE, monthId));
    }
  }

  void apply(GlobRepository targetRepository) {
    complete();
    targetRepository.deleteAll();
    for (Glob glob : tempRepository.getAll()) {
      targetRepository.create(glob.getType(), glob.toArray());
    }
  }

  private enum BudgetArea {
    INCOME(0),
    RECURRING(1),
    VARIABLE(2),
    EXTRAS(3),
    SAVINGS(4),
    UNCATEGORIZED(-1);

    private final int id;

    private BudgetArea(int id) {
      this.id = id;
    }
  }
}
