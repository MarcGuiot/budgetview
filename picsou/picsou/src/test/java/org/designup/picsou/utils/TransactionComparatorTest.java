package org.designup.picsou.utils;

import org.designup.picsou.model.Transaction;
import org.globsframework.model.FieldValue;
import org.globsframework.model.Glob;
import org.globsframework.model.Key;
import org.globsframework.utils.TestUtils;

import java.util.*;

public class TransactionComparatorTest extends PicsouTestCase {

  public void testSplitComparator() throws Exception {
    Glob o0 = repository.create(Key.create(Transaction.TYPE, -1),
                                FieldValue.value(Transaction.BANK_DAY, 1),
                                FieldValue.value(Transaction.BANK_MONTH, 3));
    Glob o1 = create(1);
    Glob o2 = createSplit(0, 1);
    Glob o3 = createSplit(5, 1);
    Glob o4 = createSplit(6, 1);
    Glob o5 = create(4);
    Glob o6 = createSplit(2, 4);
    Glob o7 = createSplit(3, 4);
    Glob o8 = createSplit(8, 4);
    Glob o9 = create(11);
    Glob o10 = create(12);
    Glob o11 = createPlanned(9);
    Glob o12 = createPlanned(10);
    Glob o13 = repository.create(Key.create(Transaction.TYPE, 13),
                                 FieldValue.value(Transaction.BANK_DAY, 1),
                                 FieldValue.value(Transaction.PLANNED, true),
                                 FieldValue.value(Transaction.BANK_MONTH, 1));
    check(TransactionComparator.ASCENDING_BANK,
          o13, o2, o3, o4, o1, o6, o7, o8, o5, o9, o10, o11, o12, o0);
    check(TransactionComparator.ASCENDING_BANK_SPLIT_AFTER,
          o13, o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11, o12, o0);
    check(TransactionComparator.DESCENDING_BANK_SPLIT_AFTER,
          o0, o12, o11, o10, o9, o5, o8, o7, o6, o1, o4, o3, o2, o13);
  }

  private void check(TransactionComparator comparator, Glob... expectedOrder) {
    for (int i = 0; i < 5; i++) {
      List<Glob> unorder = unorder(Arrays.asList(expectedOrder));
      Collections.sort(unorder, comparator);
      TestUtils.assertEquals(Arrays.asList(expectedOrder), unorder);
    }
  }

  private List<Glob> unorder(List<Glob> order) {
    ArrayList<Glob> neworder = new ArrayList<Glob>(order);
    int i = 0;
    Random ramdom = new Random(System.currentTimeMillis());
    while (i < 30) {
      Glob removed = neworder.remove(Math.abs(ramdom.nextInt()) % order.size());
      neworder.add(Math.abs(ramdom.nextInt()) % order.size(), removed);
      i++;
    }
    return neworder;
  }

  private Glob createSplit(int id, int sourceId) {
    return repository.create(Key.create(Transaction.TYPE, id),
                             FieldValue.value(Transaction.SPLIT_SOURCE, sourceId),
                             FieldValue.value(Transaction.BANK_DAY, 1),
                             FieldValue.value(Transaction.BANK_MONTH, 2));
  }

  private Glob create(int id) {
    return repository.create(Key.create(Transaction.TYPE, id),
                             FieldValue.value(Transaction.BANK_DAY, 1),
                             FieldValue.value(Transaction.BANK_MONTH, 2));
  }

  private Glob createPlanned(int id) {
    return repository.create(Key.create(Transaction.TYPE, id),
                             FieldValue.value(Transaction.BANK_DAY, 1),
                             FieldValue.value(Transaction.PLANNED, true),
                             FieldValue.value(Transaction.BANK_MONTH, 2));
  }
}