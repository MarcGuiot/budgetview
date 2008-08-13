package org.designup.picsou.triggers;

import junit.framework.Assert;
import org.designup.picsou.gui.model.MonthStat;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.Month;
import org.globsframework.metamodel.Field;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.KeyBuilder;
import org.globsframework.utils.TablePrinter;
import org.globsframework.utils.TestUtils;
import org.globsframework.utils.Utils;

import java.util.*;

class MonthStatChecker {
  private GlobRepository repository;
  private Field field;
  private int[] months;
  private Map<Integer, double[]> categoryValues = new HashMap<Integer, double[]>();
  private int accountId = Account.SUMMARY_ACCOUNT_ID;

  public MonthStatChecker(GlobRepository repository, Field field) {
    this.repository = repository;
    this.field = field;
  }

  public MonthStatChecker setMonths(int... months) {
    this.months = months;
    return this;
  }

  public MonthStatChecker setAccount(int accountId) {
    this.accountId = accountId;
    return this;
  }

  public MonthStatChecker add(String name, double... values) {
    Glob category = Category.findByName(name, repository);
    return add(name, category.get(Category.ID), values);
  }

  public MonthStatChecker add(MasterCategory category, double... values) {
    return add(category.getName(), category.getId(), values);
  }

  private MonthStatChecker add(String name, Integer categoryId, double... values) {
    if (values.length != months.length) {
      throw new RuntimeException("Expecting " + months.length + " values for category: " + name);
    }
    categoryValues.put(categoryId, values);
    return this;
  }

  public void check() {
    checkMonths();
    Assert.assertEquals(getExpectedDescription(), getActualDescription());
  }

  private void checkMonths() {
    TestUtils.assertEquals(repository.getAll(Month.TYPE).getSortedSet(Month.ID),
                           Utils.toBigInt(months));
  }

  private String getExpectedDescription() {
    List<Object[]> rows = new ArrayList<Object[]>();
    for (Map.Entry<Integer, double[]> entry : categoryValues.entrySet()) {
      double[] values = entry.getValue();
      Object[] row = new Object[values.length + 1];
      row[0] = Category.getName(entry.getKey(), repository);
      for (int i = 0; i < values.length; i++) {
        row[i + 1] = values[i];
      }
      rows.add(row);
    }

    Set<Integer> categories = new HashSet<Integer>();
    categories.addAll(repository.getAll(Category.TYPE).getValueSet(Category.ID));
    categories.removeAll(categoryValues.keySet());
    for (Integer categoryId : categories) {
      Object[] row = new Object[months.length + 1];
      row[0] = Category.getName(categoryId, repository);
      for (int i = 0; i < months.length; i++) {
        row[i + 1] = 0.0;
      }
      rows.add(row);
    }

    return TablePrinter.print(getHeaderRow(), rows);
  }

  private String getActualDescription() {
    List<Object[]> rows = new ArrayList<Object[]>();
    for (Glob category : repository.getAll(Category.TYPE)) {
      Object[] row = new Object[months.length + 1];
      Integer categoryId = category.get(Category.ID);
      row[0] = Category.getName(categoryId, repository);
      for (int i = 0; i < months.length; i++) {
        int month = months[i];
        Glob monthStat = getMonthStat(month, categoryId);
        row[i + 1] = monthStat.getValue(field);
      }
      rows.add(row);
    }
    return TablePrinter.print(getHeaderRow(), rows);
  }

  private Object[] getHeaderRow() {
    Object[] header = new Object[months.length + 1];
    header[0] = "";
    for (int i = 0; i < months.length; i++) {
      header[i + 1] = months[i];
    }
    return header;
  }

  private Glob getMonthStat(int month, Integer categoryId) {
    Key key =
      KeyBuilder.init(MonthStat.MONTH, month)
        .set(MonthStat.CATEGORY, categoryId)
        .set(MonthStat.ACCOUNT, accountId)
        .get();
    return repository.get(key);
  }

}
