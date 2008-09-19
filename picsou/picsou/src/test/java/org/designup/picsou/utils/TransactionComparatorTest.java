package org.designup.picsou.utils;

import org.designup.picsou.model.Transaction;
import org.globsframework.model.FieldValue;
import org.globsframework.model.Glob;
import org.globsframework.model.Key;
import org.globsframework.utils.TestUtils;

import java.util.*;

public class TransactionComparatorTest extends PicsouTestCase {

  public void testSplitComparator() throws Exception {
    Glob o1 = create(1);
    Glob o2 = createSplit(0, 1);
    Glob o3 = createSplit(3, 1);
    Glob o4 = createSplit(5, 1);
    Glob o5 = create(4);
    Glob o6 = createSplit(2, 4);
    Glob o7 = createSplit(6, 4);
    Glob o8 = createSplit(8, 4);
    Glob o9 = create(11);
    Glob o10 = create(12);
    Glob o11 = createPlanned(9);
    Glob o12 = createPlanned(10);
    List<Glob> expectedOrder = Arrays.asList(o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11, o12);
    check(expectedOrder);
    check(expectedOrder);
    check(expectedOrder);
    check(expectedOrder);
    check(expectedOrder);
  }

  private void check(List<Glob> expectedOrder) {
    List<Glob> unorder = unorder(expectedOrder);
    Collections.sort(unorder, TransactionComparator.ASCENDING_BANK);
    TestUtils.assertEquals(expectedOrder, unorder);
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