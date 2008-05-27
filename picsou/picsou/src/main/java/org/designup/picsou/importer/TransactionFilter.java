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
import java.util.List;
import java.util.Set;

public class TransactionFilter implements AccountFileImporter {
  private AccountFileImporter innerImporter;

  public TransactionFilter(AccountFileImporter accountFileImporter) {
    this.innerImporter = accountFileImporter;
  }

  public GlobList loadTransactions(Reader reader, GlobRepository repository) {
    GlobRepository tmpRepository =
      GlobRepositoryBuilder.init(repository.getIdGenerator())
        .add(repository.getAll(Bank.TYPE))
        .add(repository.getAll(Account.TYPE))
        .add(repository.getAll(Category.TYPE))
        .get();

    GlobList tmpTransactions = getTransactionsToCreate(tmpRepository, reader, repository);
    retrieveObjects(Account.TYPE, tmpRepository, repository);
    SummaryAccountCreationTrigger.updateSummary(repository);
    retrieveObjects(Bank.TYPE, tmpRepository, repository);
    retrieveCategories(repository, tmpRepository);

    GlobList createdTransactions = new GlobList();
    for (Glob tmpTransaction : tmpTransactions) {
      createdTransactions.add(repository.create(tmpTransaction.getKey(), tmpTransaction.toArray()));
      Set<Integer> categoryIds =
        tmpRepository.findByIndex(TransactionToCategory.TRANSACTION_INDEX,
                                  tmpTransaction.get(Transaction.ID))
          .getValueSet(TransactionToCategory.CATEGORY);
      TransactionToCategory.link(repository,
                                 tmpTransaction.get(Transaction.ID),
                                 categoryIds.toArray(new Integer[categoryIds.size()]));
    }
    return createdTransactions;
  }

  private void retrieveCategories(GlobRepository repository, GlobRepository tmpRepository) {
    for (Glob account : tmpRepository.getAll(Category.TYPE, GlobMatchers.isNotNull(Category.MASTER))) {
      Glob created = repository.findOrCreate(account.getKey());
      repository.update(created.getKey(), account.toArray());
    }
  }

  private GlobList getTransactionsToCreate(GlobRepository tempRepository, Reader reader, GlobRepository globRepository) {
    GlobList importedTransactions =
      innerImporter.loadTransactions(reader, tempRepository).sort(TransactionComparator.ASCENDING);
    if (importedTransactions.isEmpty()) {
      return GlobList.EMPTY;
    }
    GlobList actualTransactions = globRepository.getAll(Transaction.TYPE).sort(TransactionComparator.ASCENDING);
    if (actualTransactions.isEmpty()) {
      return importedTransactions;
    }
    if (firstIsAfterLast(importedTransactions, actualTransactions) ||
        firstIsAfterLast(actualTransactions, importedTransactions)) {
      return importedTransactions;
    }
    return new TransactionChecker(importedTransactions.toArray(), actualTransactions.toArray()).getNewTransactions();
  }

  private void retrieveObjects(GlobType globType, GlobRepository tempRepository, GlobRepository repository) {
    for (Glob glob : tempRepository.getAll(globType)) {
      Glob created = repository.findOrCreate(glob.getKey());
      repository.update(created.getKey(), glob.toArray());
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
                      getAmount(importedTransaction, this.importedSplitedTransaction))
             && equal(actualTransaction.get(Transaction.TRANSACTION_TYPE), importedTransaction.get(Transaction.TRANSACTION_TYPE));
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
