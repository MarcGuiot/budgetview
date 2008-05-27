package org.designup.picsou.gui.triggers;

import junit.framework.Assert;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.globs.utils.TablePrinter;
import org.designup.picsou.gui.model.GlobalStat;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.MasterCategory;

import java.util.*;

class GlobalStatChecker {
  private Map<Integer, double[]> categoryValues = new HashMap<Integer, double[]>();
  private final String[] HEADER = {"", "min expenses", "max expenses", "min income", "max income"};
  private GlobRepository repository;

  public GlobalStatChecker(GlobRepository repository) {
    this.repository = repository;
  }

  public GlobalStatChecker add(String categoryName, double minExpenses, double maxExpenses, double minIncome, double maxIncome) {
    Glob category = Category.findByName(categoryName, repository);
    categoryValues.put(category.get(Category.ID), new double[]{minExpenses, maxExpenses, minIncome, maxIncome});
    return this;
  }

  public GlobalStatChecker add(MasterCategory category, double minExpenses, double maxExpenses, double minIncome, double maxIncome) {
    categoryValues.put(category.getId(), new double[]{minExpenses, maxExpenses, minIncome, maxIncome});
    return this;
  }

  public void check() {
    Assert.assertEquals(getExpectedDescription(), getActualDescription());
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
      Object[] row = new Object[5];
      row[0] = Category.getName(categoryId, repository);
      for (int i = 1; i < 5; i++) {
        row[i] = 0.0;
      }
      rows.add(row);
    }

    return TablePrinter.print(HEADER, rows);
  }

  private String getActualDescription() {
    List<Object[]> rows = new ArrayList<Object[]>();
    for (Glob category : repository.getAll(Category.TYPE)) {
      Glob stat = repository.get(Key.create(GlobalStat.TYPE, category.get(Category.ID)));
      Object[] row = new Object[5];
      int i = 0;
      row[i++] = category.get(Category.NAME);
      row[i++] = stat.get(GlobalStat.MIN_EXPENSES);
      row[i++] = stat.get(GlobalStat.MAX_EXPENSES);
      row[i++] = stat.get(GlobalStat.MIN_INCOME);
      row[i++] = stat.get(GlobalStat.MAX_INCOME);
      rows.add(row);
    }
    return TablePrinter.print(HEADER, rows);
  }
}
