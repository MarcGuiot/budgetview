package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.OccasionalSeriesStat;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.PicsouTestCase;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.utils.GlobBuilder;

public class OccasionalSeriesStatTriggerTest extends PicsouTestCase {
  protected void setUp() throws Exception {
    super.setUp();
    repository.addTrigger(new OccasionalSeriesStatTrigger());
  }

  public void testCreatingAndDeletingTransactions() throws Exception {
    repository.create(Month.TYPE, value(Month.ID, 200808));
    listener.assertNoChanges(OccasionalSeriesStat.TYPE);
    assertTrue(repository.getAll(OccasionalSeriesStat.TYPE).isEmpty());

    Glob transaction1 = createTransaction(200808, Series.OCCASIONAL_SERIES_ID, MasterCategory.FOOD, 10.0);
    listener.assertLastChangesEqual(OccasionalSeriesStat.TYPE,
                                    "<create type='occasionalSeriesStat' month='200808' " +
                                    "        category='" + MasterCategory.FOOD.getId() + "' " +
                                    "        amount='10.0'/>");

    Glob transaction2 = createTransaction(200808, Series.OCCASIONAL_SERIES_ID, MasterCategory.FOOD, 20.0);
    listener.assertLastChangesEqual(OccasionalSeriesStat.TYPE,
                                    "<update type='occasionalSeriesStat' month='200808' " +
                                    "        category='" + MasterCategory.FOOD.getId() + "' " +
                                    "        amount='30.0' _amount='10.0'/>");

    repository.delete(transaction2.getKey());
    listener.assertLastChangesEqual(OccasionalSeriesStat.TYPE,
                                    "<update type='occasionalSeriesStat' month='200808' " +
                                    "        category='" + MasterCategory.FOOD.getId() + "' " +
                                    "        amount='10.0' _amount='30.0'/>");

    repository.delete(transaction1.getKey());
    listener.assertLastChangesEqual(OccasionalSeriesStat.TYPE,
                                    "<delete type='occasionalSeriesStat' month='200808' " +
                                    "        category='" + MasterCategory.FOOD.getId() + "' " +
                                    "        _amount='10.0'/>");
  }

  public void testIgnoresOtherSeries() throws Exception {
    createTransaction(200808, 12345, MasterCategory.FOOD, 200.0);
    listener.assertNoChanges(OccasionalSeriesStat.TYPE);
  }

  public void testIgnoresPlannedTransations() throws Exception {
    Glob transaction = repository.create(Transaction.TYPE,
                                         value(Transaction.MONTH, 200808),
                                         value(Transaction.SERIES, 12345),
                                         value(Transaction.CATEGORY, MasterCategory.FOOD.getId()),
                                         value(Transaction.AMOUNT, 10.0),
                                         value(Transaction.PLANNED, true));
    listener.assertNoChanges(OccasionalSeriesStat.TYPE);

    repository.update(transaction.getKey(), value(Transaction.AMOUNT, 5.0));
    listener.assertNoChanges(OccasionalSeriesStat.TYPE);

    repository.delete(transaction.getKey());
    listener.assertNoChanges(OccasionalSeriesStat.TYPE);
  }

  public void testUpdatingTransactionAmount() throws Exception {
    Glob transaction = createTransaction(200808, Series.OCCASIONAL_SERIES_ID, MasterCategory.FOOD, 10.0);
    listener.assertLastChangesEqual(OccasionalSeriesStat.TYPE,
                                    "<create type='occasionalSeriesStat' month='200808' " +
                                    "        category='" + MasterCategory.FOOD.getId() + "' " +
                                    "        amount='10.0'/>");

    repository.update(transaction.getKey(), value(Transaction.AMOUNT, 5.0));
    listener.assertLastChangesEqual(OccasionalSeriesStat.TYPE,
                                    "<update type='occasionalSeriesStat' month='200808' " +
                                    "        category='" + MasterCategory.FOOD.getId() + "' " +
                                    "        amount='5.0' _amount='10.0'/>");
  }

  public void testUpdatingTransactionSeries() throws Exception {
    Glob transaction = createTransaction(200808, 1, MasterCategory.FOOD, 10.0);
    listener.assertNoChanges(OccasionalSeriesStat.TYPE);

    repository.update(transaction.getKey(), value(Transaction.SERIES, Series.OCCASIONAL_SERIES_ID));
    listener.assertLastChangesEqual(OccasionalSeriesStat.TYPE,
                                    "<create type='occasionalSeriesStat' month='200808' " +
                                    "        category='" + MasterCategory.FOOD.getId() + "' " +
                                    "        amount='10.0'/>");

    repository.update(transaction.getKey(), value(Transaction.SERIES, 1));
    listener.assertLastChangesEqual(OccasionalSeriesStat.TYPE,
                                    "<delete type='occasionalSeriesStat' month='200808' " +
                                    "        category='" + MasterCategory.FOOD.getId() + "' " +
                                    "        _amount='10.0'/>");
  }

  public void testBalancingTransactions() throws Exception {
    createTransaction(200808, Series.OCCASIONAL_SERIES_ID, MasterCategory.FOOD, 10.0);
    createTransaction(200808, Series.OCCASIONAL_SERIES_ID, MasterCategory.FOOD, -10.0);
    listener.assertLastChangesEqual(OccasionalSeriesStat.TYPE,
                                    "<update type='occasionalSeriesStat' month='200808' " +
                                    "        category='" + MasterCategory.FOOD.getId() + "' " +
                                    "        amount='0.0' _amount='10.0'/>");
  }

  public void testTransactionAssignedToOccasionalButWithoutACategory() throws Exception {
    Glob transaction = createTransaction(200808, Series.OCCASIONAL_SERIES_ID, (Integer)null, 10.0);
    repository.update(transaction.getKey(), value(Transaction.SERIES, Series.OCCASIONAL_SERIES_ID));
    listener.assertLastChangesEqual(OccasionalSeriesStat.TYPE,
                                    "<create type='occasionalSeriesStat' month='200808' " +
                                    "        category='" + MasterCategory.NONE.getId() + "' " +
                                    "        amount='10.0'/>");
  }

  public void testUpdatingTransactionCategory() throws Exception {
    Glob transaction = createTransaction(200808, Series.OCCASIONAL_SERIES_ID, (Integer)null, 10.0);
    listener.assertLastChangesEqual(OccasionalSeriesStat.TYPE,
                                    "<create type='occasionalSeriesStat' month='200808' " +
                                    "        category='" + MasterCategory.NONE.getId() + "' " +
                                    "        amount='10.0'/>");

    repository.update(transaction.getKey(), value(Transaction.CATEGORY, MasterCategory.FOOD.getId()));
    listener.assertLastChangesEqual(OccasionalSeriesStat.TYPE,
                                    "<delete type='occasionalSeriesStat' month='200808' " +
                                    "        category='" + MasterCategory.NONE.getId() + "' " +
                                    "        _amount='10.0'/>" +
                                    "<create type='occasionalSeriesStat' month='200808' " +
                                    "        category='" + MasterCategory.FOOD.getId() + "' " +
                                    "        amount='10.0'/>");

    repository.update(transaction.getKey(), value(Transaction.CATEGORY, MasterCategory.HOUSE.getId()));
    listener.assertLastChangesEqual(OccasionalSeriesStat.TYPE,
                                    "<delete type='occasionalSeriesStat' month='200808' " +
                                    "        category='" + MasterCategory.FOOD.getId() + "' " +
                                    "        _amount='10.0'/>" +
                                    "<create type='occasionalSeriesStat' month='200808' " +
                                    "        category='" + MasterCategory.HOUSE.getId() + "' " +
                                    "        amount='10.0'/>");
  }

  public void testSubCategory() throws Exception {
    Glob foodSubcat = repository.create(Category.TYPE, value(Category.MASTER, MasterCategory.FOOD.getId()));
    Glob houseSubcat = repository.create(Category.TYPE, value(Category.MASTER, MasterCategory.HOUSE.getId()));

    Glob transaction = createTransaction(200808, Series.OCCASIONAL_SERIES_ID, foodSubcat.get(Category.ID), 10.0);
    listener.assertLastChangesEqual(OccasionalSeriesStat.TYPE,
                                    "<create type='occasionalSeriesStat' month='200808' " +
                                    "        category='" + MasterCategory.FOOD.getId() + "' " +
                                    "        amount='10.0'/>");

    repository.update(transaction.getKey(), value(Transaction.CATEGORY, houseSubcat.get(Category.ID)));
    listener.assertLastChangesEqual(OccasionalSeriesStat.TYPE,
                                    "<delete type='occasionalSeriesStat' month='200808' " +
                                    "        category='" + MasterCategory.FOOD.getId() + "' " +
                                    "        _amount='10.0'/>" +
                                    "<create type='occasionalSeriesStat' month='200808' " +
                                    "        category='" + MasterCategory.HOUSE.getId() + "' " +
                                    "        amount='10.0'/>");
  }

  public void testDeletingASubcategory() throws Exception {
    Glob subcat = repository.create(Category.TYPE, value(Category.MASTER, MasterCategory.FOOD.getId()));
    fail();
  }

  public void testUpdatingTransactionMonth() throws Exception {
    Glob transaction = createTransaction(200808, Series.OCCASIONAL_SERIES_ID, MasterCategory.FOOD, 10.0);

    repository.update(transaction.getKey(), value(Transaction.MONTH, 200809));
    listener.assertLastChangesEqual(OccasionalSeriesStat.TYPE,
                                    "<delete type='occasionalSeriesStat' month='200808' " +
                                    "        category='" + MasterCategory.FOOD.getId() + "' " +
                                    "        _amount='10.0'/>" +
                                    "<create type='occasionalSeriesStat' month='200809' " +
                                    "        category='" + MasterCategory.FOOD.getId() + "' " +
                                    "        amount='10.0'/>"
    );
  }

  public void testReset() throws Exception {
    createTransaction(200808, Series.OCCASIONAL_SERIES_ID, MasterCategory.FOOD, 10.0);

    Glob newTransaction = GlobBuilder.init(Transaction.TYPE,
                                           value(Transaction.ID, 1),
                                           value(Transaction.MONTH, 200809),
                                           value(Transaction.SERIES, Series.OCCASIONAL_SERIES_ID),
                                           value(Transaction.CATEGORY, MasterCategory.HOUSE.getId()),
                                           value(Transaction.AMOUNT, 20.0)).get();

    repository.reset(new GlobList(newTransaction), Transaction.TYPE);
    listener.assertLastChangesEqual(OccasionalSeriesStat.TYPE,
                                    "<delete type='occasionalSeriesStat' month='200808' " +
                                    "        category='" + MasterCategory.FOOD.getId() + "' " +
                                    "        _amount='10.0'/>" +
                                    "<create type='occasionalSeriesStat' month='200809' " +
                                    "        category='" + MasterCategory.HOUSE.getId() + "' " +
                                    "        amount='20.0'/>"
    );
  }

  private Glob createTransaction(int monthId, Integer seriesId, MasterCategory category, double amount) {
    return createTransaction(monthId, seriesId, category != null ? category.getId() : null, amount);
  }

  private Glob createTransaction(int monthId, Integer seriesId, Integer categoryId, double amount) {
    return repository.create(Transaction.TYPE,
                             value(Transaction.MONTH, monthId),
                             value(Transaction.SERIES, seriesId),
                             value(Transaction.CATEGORY, categoryId),
                             value(Transaction.AMOUNT, amount));
  }
}
