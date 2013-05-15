package org.designup.picsou.utils;

import org.designup.picsou.model.Transaction;
import org.globsframework.model.FieldValue;
import org.globsframework.model.Glob;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.TestUtils;

import java.util.*;

public class TransactionComparatorTest extends PicsouTestCase {

  public void testSplitComparator() throws Exception {
    Glob o0 = repository.create(Key.create(Transaction.TYPE, -1),
                                FieldValue.value(Transaction.BANK_DAY, 1),
                                FieldValue.value(Transaction.BANK_MONTH, 3),
                                FieldValue.value(Transaction.AMOUNT, 3.));
    Glob o1 = createAccount1(11);
    Glob o2 = createSplitAccount1(10, 11);
    Glob o3 = createSplitAccount1(15, 11);
    Glob o4 = createSplitAccount1(16, 11);
    Glob o5 = createAccount1(14);
    Glob o6 = createSplitAccount1(12, 14);
    Glob o7 = createSplitAccount1(13, 14);
    Glob o8 = createSplitAccount1(18, 14);
    Glob o9 = createAccount1(21);
    Glob o10 = createAccount1(22);
    Glob o11 = createPlanned(19);
    Glob o12 = createPlanned(20);
    Glob o13 = repository.create(Key.create(Transaction.TYPE, 23),
                                 FieldValue.value(Transaction.BANK_DAY, 1),
                                 FieldValue.value(Transaction.PLANNED, true),
                                 FieldValue.value(Transaction.BANK_MONTH, 1),
                                 FieldValue.value(Transaction.AMOUNT, 1.));

    check(TransactionComparator.ASCENDING_BANK,
          o13, o2, o3, o4, o1, o6, o7, o8, o5, o9, o10, o11, o12, o0);
    check(TransactionComparator.ASCENDING_BANK_SPLIT_AFTER,
          o13, o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11, o12, o0);
    check(TransactionComparator.DESCENDING_BANK_SPLIT_AFTER,
          o0, o12, o11, o10, o9, o5, o8, o7, o6, o1, o4, o3, o2, o13);

    createAccount2(1);
    createSplitAccount2(0, 1);
    createSplitAccount2(5, 1);
    createSplitAccount2(6, 1);
    createAccount2(4);
    createSplitAccount2(2, 4);
    createSplitAccount2(3, 4);
    createSplitAccount2(8, 4);
    createAccount2(31);
    createAccount2(32);
    createPlannedAccount2(9);
    createPlannedAccount2(30);

    Glob[] globs = repository.getSorted(Transaction.TYPE, TransactionComparator.ASCENDING_BANK, GlobMatchers.ALL);
    int pos = 0;
    for (Glob glob : globs) {
      assertEquals(pos, Arrays.binarySearch(globs, glob, TransactionComparator.ASCENDING_BANK));
      pos++;
    }
  }

  public void testDeffered() throws Exception {
    Glob g1 = createAtPosition(1, 200901, 200812);
    Glob g2 = createAtPosition(2, 200901, 200901);
    Glob g3 = createAtPosition(3, 200901, 200812);
    Glob g4 = createAtPosition(4, 200901, 200901);
    Glob g5 = createAtPosition(5, 200902, 200901);
    check(TransactionComparator.ASCENDING_ACCOUNT, g1, g3, g2, g4, g5);
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

  private Glob createSplitAccount1(int id, int sourceId) {
    return repository.create(Key.create(Transaction.TYPE, id),
                             FieldValue.value(Transaction.SPLIT_SOURCE, sourceId),
                             FieldValue.value(Transaction.BANK_DAY, 1),
                             FieldValue.value(Transaction.ACCOUNT, 1),
                             FieldValue.value(Transaction.BANK_MONTH, 2),
                             FieldValue.value(Transaction.AMOUNT, 2.));
  }


  private Glob createAccount1(int id) {
    return repository.create(Key.create(Transaction.TYPE, id),
                             FieldValue.value(Transaction.ACCOUNT, 1),
                             FieldValue.value(Transaction.BANK_DAY, 1),
                             FieldValue.value(Transaction.BANK_MONTH, 2),
                             FieldValue.value(Transaction.AMOUNT, 10.));
  }

  private Glob createAtPosition(int id, int monthPosition, int monthBank) {
    return repository.create(Key.create(Transaction.TYPE, id),
                             FieldValue.value(Transaction.ACCOUNT, 1),
                             FieldValue.value(Transaction.POSITION_DAY, 26),
                             FieldValue.value(Transaction.POSITION_MONTH, monthPosition),
                             FieldValue.value(Transaction.BANK_DAY, 13),
                             FieldValue.value(Transaction.BANK_MONTH, monthBank),
                             FieldValue.value(Transaction.AMOUNT, 10.)
    );
  }

  private Glob createSplitAccount2(int id, int sourceId) {
    return repository.create(Key.create(Transaction.TYPE, id),
                             FieldValue.value(Transaction.SPLIT_SOURCE, sourceId),
                             FieldValue.value(Transaction.ACCOUNT, 2),
                             FieldValue.value(Transaction.BANK_DAY, 1),
                             FieldValue.value(Transaction.BANK_MONTH, 2),
                             FieldValue.value(Transaction.AMOUNT, 2.));
  }

  private Glob createAccount2(int id) {
    return repository.create(Key.create(Transaction.TYPE, id),
                             FieldValue.value(Transaction.ACCOUNT, 2),
                             FieldValue.value(Transaction.BANK_DAY, 1),
                             FieldValue.value(Transaction.BANK_MONTH, 2),
                             FieldValue.value(Transaction.AMOUNT, 2.));
  }

  private Glob createPlanned(int id) {
    return repository.create(Key.create(Transaction.TYPE, id),
                             FieldValue.value(Transaction.ACCOUNT, 1),
                             FieldValue.value(Transaction.BANK_DAY, 1),
                             FieldValue.value(Transaction.PLANNED, true),
                             FieldValue.value(Transaction.BANK_MONTH, 2),
                             FieldValue.value(Transaction.AMOUNT, 2.));
  }

  private Glob createPlannedAccount2(int id) {
    return repository.create(Key.create(Transaction.TYPE, id),
                             FieldValue.value(Transaction.ACCOUNT, 2),
                             FieldValue.value(Transaction.BANK_DAY, 1),
                             FieldValue.value(Transaction.PLANNED, true),
                             FieldValue.value(Transaction.BANK_MONTH, 2),
                             FieldValue.value(Transaction.AMOUNT, 2.));
  }
}