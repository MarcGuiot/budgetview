package org.designup.picsou.importer;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.*;
import org.crossbowlabs.globs.model.utils.GlobMatchers;
import org.crossbowlabs.globs.utils.MultiMap;
import static org.crossbowlabs.globs.utils.Utils.equal;
import org.designup.picsou.model.*;
import org.designup.picsou.triggers.SummaryAccountCreationTrigger;
import org.designup.picsou.utils.TransactionComparator;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TransactionFilter implements AccountFileImporter {
  private AccountFileImporter innerImporter;

  public TransactionFilter(AccountFileImporter accountFileImporter) {
    this.innerImporter = accountFileImporter;
  }

  public GlobList loadTransactions(Reader reader,
                                   ReadOnlyGlobRepository initialRepository,
                                   GlobRepository targetRepository) {
    GlobList createdTransactions = loadTransactionsToCreate(reader, targetRepository, initialRepository);

    SummaryAccountCreationTrigger.updateSummary(targetRepository);

    for (Glob transaction : createdTransactions) {
      Set<Integer> categoryIds =
        targetRepository.findByIndex(TransactionToCategory.TRANSACTION_INDEX, transaction.get(Transaction.ID))
          .getValueSet(TransactionToCategory.CATEGORY);
      TransactionToCategory.link(targetRepository,
                                 transaction.get(Transaction.ID),
                                 categoryIds.toArray(new Integer[categoryIds.size()]));
    }
    return createdTransactions;
  }

  private GlobList loadTransactionsToCreate(Reader reader, GlobRepository targetRepository,
                                            ReadOnlyGlobRepository initialRepository) {
    GlobList importedTransactions =
      innerImporter
        .loadTransactions(reader, initialRepository, targetRepository)
        .sort(TransactionComparator.ASCENDING);
    if (importedTransactions.isEmpty()) {
      return GlobList.EMPTY;
    }
    GlobList actualTransactions = initialRepository.getAll(Transaction.TYPE).sort(TransactionComparator.ASCENDING);
    if (actualTransactions.isEmpty()) {
      return importedTransactions;
    }
    if (firstIsAfterLast(importedTransactions, actualTransactions) ||
        firstIsAfterLast(actualTransactions, importedTransactions)) {
      return importedTransactions;
    }
    GlobList newTransactions = new TransactionChecker(importedTransactions.toArray(), actualTransactions.toArray())
      .getNewTransactions();
    Map<Key, Glob> transactions = new HashMap<Key, Glob>();
    for (Glob newTransaction : newTransactions) {
      transactions.put(newTransaction.getKey(), newTransaction);
    }
    for (Glob importedTransaction : importedTransactions) {
      if (!transactions.containsKey(importedTransaction.getKey())) {
        targetRepository.delete(importedTransaction.getKey());
      }
    }
    return newTransactions;
  }

  private void retrieveObjects(GlobType globType, GlobRepository source, GlobRepository target) {
    for (Glob glob : source.getAll(globType)) {
      Glob created = target.findOrCreate(glob.getKey());
      target.update(created.getKey(), glob.toArray());
    }
  }

  private boolean firstIsAfterLast(GlobList list1, GlobList list2) {
    return Transaction.fullDate(list1.get(0)) >
           Transaction.fullDate(list2.get(list2.size() - 1));
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
          long importedDate = Transaction.fullDate(sortedImported[importedIndex]);
          long actualDate = Transaction.fullDate(sortedActual[actualIndex]);
          if (importedDate < actualDate) {
            transactionsToAdd.add(sortedImported[importedIndex]);
            importedIndex++;
          }
          else if (importedDate == actualDate) {
            if (findAndSwapWithCurrentIfEqual(sortedActual, actualIndex, sortedImported[importedIndex])) {
              actualIndex++;
            }
            else {
              transactionsToAdd.add(sortedImported[importedIndex]);
              if (Transaction.isSplitPart(sortedImported[importedIndex])) {
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
             && Transaction.fullDate(actual[actualIndex]) == Transaction.fullDate(importedTransaction)) {
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
             && equal(getAmount(actualTransaction, this.actualSplitedTransaction),
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
