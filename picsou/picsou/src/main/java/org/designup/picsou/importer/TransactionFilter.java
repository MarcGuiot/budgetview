package org.designup.picsou.importer;

import org.designup.picsou.model.Transaction;
import com.budgetview.shared.utils.Amounts;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.collections.MultiMap;
import org.globsframework.utils.Strings;

import static org.globsframework.model.utils.GlobMatchers.and;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;
import static org.globsframework.model.utils.GlobMatchers.isNotNull;
import static org.globsframework.utils.Utils.equal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TransactionFilter {

  public GlobList loadTransactions(ReadOnlyGlobRepository referenceRepository,
                                   GlobRepository targetRepository,
                                   GlobList transactionToFilter,
                                   Integer accountId) {

    return loadTransactionsToCreate(targetRepository, referenceRepository, transactionToFilter, accountId);
  }

  private GlobList loadTransactionsToCreate(GlobRepository targetRepository,
                                            ReadOnlyGlobRepository referenceRepository,
                                            GlobList transactionToFilter,
                                            Integer accountId) {
    GlobList importedTransactions = transactionToFilter.sortSelf(TransactionComparator.ASCENDING_BANK_SPLIT_AFTER);
    if (importedTransactions.isEmpty()) {
      return GlobList.EMPTY;
    }
    MultiMap<Integer, Glob> accountsByTransaction = new MultiMap<Integer, Glob>();
    for (Glob transaction : importedTransactions) {
      if (transaction.get(Transaction.ACCOUNT) == null) {
        accountsByTransaction.put(accountId, transaction);
      }
      else {
        accountsByTransaction.put(transaction.get(Transaction.ACCOUNT), transaction);
      }
    }
    GlobList newTransactions = new GlobList(importedTransactions.size());
    for (Integer transactionAccountId : accountsByTransaction.keySet()) {
      GlobList actualTransactions = referenceRepository
        .getAll(Transaction.TYPE, and(fieldEquals(Transaction.ORIGINAL_ACCOUNT, transactionAccountId),
                                      isNotNull(Transaction.IMPORT)))
        .sortSelf(TransactionComparator.ASCENDING_BANK_SPLIT_AFTER);
      if (actualTransactions.isEmpty()) {
        return importedTransactions;
      }
      if (firstIsAfterLast(importedTransactions, actualTransactions) ||
          firstIsAfterLast(actualTransactions, importedTransactions)) {
        return importedTransactions;
      }
      newTransactions.addAll(
        new TransactionChecker(importedTransactions.toArray(), actualTransactions.toArray()).getNewTransactions());
      Map<Key, Glob> transactions = new HashMap<Key, Glob>();
      for (Glob newTransaction : newTransactions) {
        transactions.put(newTransaction.getKey(), newTransaction);
      }
      for (Glob importedTransaction : importedTransactions) {
        if (!transactions.containsKey(importedTransaction.getKey())) {
          targetRepository.delete(importedTransaction);
        }
      }
    }
    return newTransactions;
  }

  private boolean firstIsAfterLast(GlobList list1, GlobList list2) {
    return Transaction.fullBankDate(list1.get(0)) >
           Transaction.fullBankDate(list2.get(list2.size() - 1));
  }

  static class TransactionChecker {
    private Glob[] sortedImported;
    private Glob[] sortedActual;
    private MultiMap<Integer, Glob> actualSplitedTransaction = new MultiMap<Integer, Glob>();
    private MultiMap<Integer, Glob> importedSplitedTransaction = new MultiMap<Integer, Glob>();

    public TransactionChecker(Glob[] sortedImported, Glob[] sortedActual) {
      this.sortedImported = sortedImported;
      this.sortedActual = sortedActual;
      for (Glob glob : sortedImported) {
        if (Transaction.isSplitPart(glob)) {
          importedSplitedTransaction.put(glob.get(Transaction.SPLIT_SOURCE), glob);
        }
      }
      for (Glob glob : sortedActual) {
        if (Transaction.isSplitPart(glob)) {
          actualSplitedTransaction.put(glob.get(Transaction.SPLIT_SOURCE), glob);
        }
      }
    }

    private GlobList getNewTransactions() {
      GlobList transactionsToAdd = new GlobList();
      int importedIndex = 0;
      int actualIndex = 0;
      while (importedIndex < sortedImported.length) {
        if (sortedImported[importedIndex].get(Transaction.SPLIT_SOURCE) != null) {
          importedIndex++;
        }
        else {
          long importedDate = Transaction.fullBankDate(sortedImported[importedIndex]);
          long actualDate = Transaction.fullBankDate(sortedActual[actualIndex]);
          if (importedDate < actualDate) {
            transactionsToAdd.add(sortedImported[importedIndex]);
            if (Transaction.isSplitSource(sortedImported[importedIndex])) {
              transactionsToAdd.addAll(importedSplitedTransaction
                .get(sortedImported[importedIndex].get(Transaction.ID)));
            }
            importedIndex++;
          }
          else if (importedDate == actualDate) {
            computeTransactionOnSameDay(importedDate, transactionsToAdd, importedIndex, actualIndex);
            while (importedIndex < sortedImported.length && importedDate == Transaction.fullBankDate(sortedImported[importedIndex])) {
              importedIndex++;
            }
            while (actualIndex < sortedActual.length && actualDate == Transaction.fullBankDate(sortedActual[actualIndex])) {
              actualIndex++;
            }
          }
          else {
            actualIndex++;
          }
          if (actualIndex >= sortedActual.length) {
            while (importedIndex < sortedImported.length) {
              transactionsToAdd.add(sortedImported[importedIndex]);
              importedIndex++;
            }
            return transactionsToAdd;
          }
        }
      }
      return transactionsToAdd;
    }

    private int computeTransactionOnSameDay(long date, GlobList transactionsToAdd, int importedIndex, int actualIndex) {
      int actualTransactionForCurrentDay = actualIndex;
      GlobList currentTransactions = new GlobList();
      while (actualTransactionForCurrentDay < sortedActual.length
             && Transaction.fullBankDate(sortedActual[actualTransactionForCurrentDay]) == date) {
        Glob e = sortedActual[actualTransactionForCurrentDay];
        if (!Transaction.isSplitPart(e)) {
          currentTransactions.add(e);
        }
        actualTransactionForCurrentDay++;
      }

      int importedTransactionForCurrentDay = importedIndex;
      GlobList importedTransactions = new GlobList();
      while (importedTransactionForCurrentDay < sortedImported.length
             && Transaction.fullBankDate(sortedImported[importedTransactionForCurrentDay]) == date) {
        Glob e = sortedImported[importedTransactionForCurrentDay];
        if (!Transaction.isSplitPart(e)) {
          importedTransactions.add(e);
        }
        importedTransactionForCurrentDay++;
      }

      for (Iterator it = importedTransactions.iterator(); it.hasNext();) {
        Glob transaction = (Glob)it.next();
        if (findAndRemoveIfEqual(currentTransactions, transaction)) {
          it.remove();
        }
      }

      if (!importedTransactions.isEmpty() && !currentTransactions.isEmpty()) {
        removeNearest(importedTransactions, currentTransactions);
      }
      for (Glob transaction : importedTransactions) {
        transactionsToAdd.add(transaction);
        if (Transaction.isSplitSource(transaction)) {
          transactionsToAdd.addAll(this.importedSplitedTransaction.get(transaction.get(Transaction.ID)));
        }
      }
      transactionsToAdd.addAll(importedTransactions);
      if (!currentTransactions.isEmpty()) {
        // on pourrait demander de verifier ces operations qui sont absentes dans la source importée.
      }
      return actualIndex;
    }

    private void removeNearest(GlobList importedTransactions, GlobList currentTransactions) {
      for (Iterator it1 = importedTransactions.iterator(); it1.hasNext();) {
        Glob transaction = (Glob)it1.next();
        for (Iterator it = currentTransactions.iterator(); it.hasNext();) {
          Glob currentTransaction = (Glob)it.next();
          if (currentTransaction.get(Transaction.TRANSACTION_TYPE).equals(transaction.get(Transaction.TRANSACTION_TYPE))
              && Amounts.equal(currentTransaction.get(Transaction.AMOUNT),
                               transaction.get(Transaction.AMOUNT))
              && isLabelNear(currentTransaction, transaction)) {
            it1.remove();
            it.remove();
            break;
          }
        }
      }
      // retry without transactionType
      for (Iterator it1 = importedTransactions.iterator(); it1.hasNext();) {
        Glob transaction = (Glob)it1.next();
        for (Iterator it = currentTransactions.iterator(); it.hasNext();) {
          Glob currentTransaction = (Glob)it.next();
          if (Amounts.equal(currentTransaction.get(Transaction.AMOUNT), transaction.get(Transaction.AMOUNT))
              && isLabelNear(currentTransaction, transaction)) {
            it1.remove();
            it.remove();
            break;
          }
        }
      }
    }

    private boolean isLabelNear(Glob transaction1, Glob transaction2) {
      String[] label1 = fullLabel(transaction1).split(" ");
      String[] label2 = fullLabel(transaction2).split(" ");
      double count = 0;
      for (String s : label1) {
        for (int i = 0; i < label2.length; i++) {
          String s1 = label2[i];
          if (s1 != null && isLabelNear(s, s1)) {
            label2[i] = null;
            count++;
            break;
          }
        }
      }
      double i = Math.min(label1.length, label2.length) + 1;
      double ratio = count / i;
      return ratio >= 0.5;
    }

    private boolean isLabelNear(String label1, String label2) {
      double count = 0;
      byte[] tab2 = label2.getBytes();
      for (byte ch : label1.getBytes()) {
        for (int i = 0; i < tab2.length; i++) {
          byte b = tab2[i];
          if (b == ch) {
            count++;
            tab2[i] = -1;
            break;
          }
        }
      }
      double i = Math.min(label1.length(), label2.length()) + 1;
      double ratio = count / i;
      return ratio > 0.5;
    }

    private String fullLabel(Glob transaction) {
      return Strings.join(transaction.get(Transaction.OFX_MEMO), transaction.get(Transaction.OFX_NAME),
                                   transaction.get(Transaction.OFX_CHECK_NUM), transaction.get(Transaction.QIF_M),
                                   transaction.get(Transaction.QIF_P));
    }

    private boolean findAndRemoveIfEqual(GlobList actual, Glob importedTransaction) {
      for (Iterator<Glob> iterator = actual.iterator(); iterator.hasNext();) {
        Glob glob = iterator.next();
        if (areEqual(glob, importedTransaction)) {
          iterator.remove();
          return true;
        }
      }
      return false;
    }

    private boolean areEqual(Glob actualTransaction, Glob importedTransaction) {
      return equal(actualTransaction.get(Transaction.ORIGINAL_LABEL),
                   importedTransaction.get(Transaction.ORIGINAL_LABEL))
             && Amounts.equal(getAmount(actualTransaction, this.actualSplitedTransaction),
                              getAmount(importedTransaction, this.importedSplitedTransaction));
    }

    private Double getAmount(Glob transaction, MultiMap<Integer, Glob> transactions) {
      if (!Transaction.isSplitSource(transaction)) {
        return transaction.get(Transaction.AMOUNT);
      }
      Integer transactionId = transaction.get(Transaction.ID);
      double amount = transaction.get(Transaction.AMOUNT);
      List<Glob> splitedTransaction = transactions.get(transactionId);
      for (Glob glob : splitedTransaction) {
        amount += glob.get(Transaction.AMOUNT);
      }
      return amount;
    }
  }
}
