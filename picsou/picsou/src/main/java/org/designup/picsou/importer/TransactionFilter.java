package org.designup.picsou.importer;

import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.util.Amounts;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.MultiMap;
import static org.globsframework.utils.Utils.equal;

import java.util.HashMap;
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
    GlobList importedTransactions = transactionToFilter.sort(TransactionComparator.ASCENDING_BANK_SPLIT_AFTER);
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
        .getAll(Transaction.TYPE, GlobMatchers.fieldEquals(Transaction.ACCOUNT, transactionAccountId))
        .sort(TransactionComparator.ASCENDING_BANK_SPLIT_AFTER);
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
          targetRepository.delete(importedTransaction.getKey());
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
            if (findAndSwapWithCurrentIfEqual(sortedActual, actualIndex, sortedImported[importedIndex])) {
              actualIndex++;
            }
            else {
              transactionsToAdd.add(sortedImported[importedIndex]);
              if (Transaction.isSplitSource(sortedImported[importedIndex])) {
                transactionsToAdd.addAll(importedSplitedTransaction
                  .get(sortedImported[importedIndex].get(Transaction.ID)));
              }
            }
            importedIndex++;
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

    private boolean findAndSwapWithCurrentIfEqual(Glob[] actual, int actualIndex, Glob importedTransaction) {
      int first = actualIndex;
      while (actualIndex < actual.length
             && Transaction.fullBankDate(actual[actualIndex]) == Transaction.fullBankDate(importedTransaction)) {
        if (areEqual(actual[actualIndex], importedTransaction)) {
          Glob tmp = actual[first];
          actual[first] = actual[actualIndex];
          actual[actualIndex] = tmp;
          return true;
        }
        actualIndex++;
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
