package com.budgetview.desktop.categorization.reconciliation;

import com.budgetview.model.Transaction;
import junit.framework.TestCase;
import org.globsframework.model.Glob;
import org.globsframework.model.utils.GlobBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ReferenceTransactionComparatorTest extends TestCase {

  private int nextId = 1;

  public void testTransactionsWithTheReferenceLabelComeFirst() throws Exception {
    ReferenceTransactionComparator comparator = new ReferenceTransactionComparator();
    comparator.setCurrent(transaction("AUCHAN", -200.00, 201204, 10));

    Glob same = transaction("auchan", -200.00, 201205, 10);
    Glob other = transaction("FNAC", -200.00, 201205, 10);
    assertTrue(comparator.compare(same, other) < 0);
    assertTrue(comparator.compare(other, same) > 0);
  }

  public void testTwoTransactionsWithTheReferenceLabelAreOrderedConsistently() throws Exception {
    ReferenceTransactionComparator comparator = new ReferenceTransactionComparator();
    comparator.setCurrent(transaction("AUCHAN", -200.00, 201204, 10));

    // Même écart de montant et de date : seul le libellé, puis l'identifiant, les départagent.
    Glob first = transaction("AUCHAN", -200.00, 201204, 5);
    Glob second = transaction("AUCHAN", -200.00, 201204, 15);
    int direct = comparator.compare(first, second);
    int reverse = comparator.compare(second, first);
    assertTrue(direct != 0);
    assertEquals(-Integer.signum(direct), Integer.signum(reverse));
  }

  public void testNullReferenceLabel() throws Exception {
    ReferenceTransactionComparator comparator = new ReferenceTransactionComparator();
    comparator.setCurrent(transaction(null, -200.00, 201204, 10));

    Glob a = transaction("AUCHAN", -200.00, 201205, 10);
    Glob b = transaction(null, -200.00, 201205, 10);
    assertEquals(-Integer.signum(comparator.compare(a, b)), Integer.signum(comparator.compare(b, a)));
  }

  /**
   * Cas de PrintTest.testTablePagination : beaucoup d'égalités de montant et de date, plusieurs
   * opérations au libellé de référence. Avant correction, le tri levait de temps à autre
   * « Comparison method violates its general contract! ».
   */
  public void testRespectsTheComparatorContract() throws Exception {
    List<Glob> transactions = new ArrayList<Glob>();
    for (int i = 1; i <= 12; i++) {
      String label = "AUCHAN" + (i % 4);
      transactions.add(transaction(label, -200.00, 201203, 10));
      transactions.add(transaction(label, -200.00, 201205, 10));
      transactions.add(transaction(label, -250.00, 201204, 10));
    }
    transactions.add(transaction(null, -200.00, 201204, 10));

    ReferenceTransactionComparator comparator = new ReferenceTransactionComparator();
    comparator.setCurrent(transaction("auchan1", -200.00, 201204, 10));

    for (Glob a : transactions) {
      assertEquals(0, comparator.compare(a, a));
      for (Glob b : transactions) {
        int ab = Integer.signum(comparator.compare(a, b));
        assertEquals(a + " / " + b, -ab, Integer.signum(comparator.compare(b, a)));
        for (Glob c : transactions) {
          if (ab < 0 && comparator.compare(b, c) < 0) {
            assertTrue(a + " < " + b + " < " + c, comparator.compare(a, c) < 0);
          }
        }
      }
    }

    Random random = new Random(42);
    for (int round = 0; round < 200; round++) {
      Collections.shuffle(transactions, random);
      Collections.sort(transactions, comparator);
    }
  }

  private Glob transaction(String label, double amount, int month, int day) {
    return GlobBuilder.init(Transaction.TYPE)
      .set(Transaction.ID, nextId++)
      .set(Transaction.LABEL, label)
      .set(Transaction.AMOUNT, amount)
      .set(Transaction.POSITION_MONTH, month)
      .set(Transaction.POSITION_DAY, day)
      .get();
  }
}
