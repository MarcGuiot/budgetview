package org.designup.picsou.importer.analyzer;

import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionType;
import static org.designup.picsou.model.TransactionType.PRELEVEMENT;
import static org.designup.picsou.model.TransactionType.VIREMENT;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class DefaultTransactionAnalyzer implements TransactionAnalyzer {
  private List<TransactionTypeFinalizer> exclusiveFinalizers = new ArrayList<TransactionTypeFinalizer>();
  private List<TransactionTypeFinalizer> finalizers = new ArrayList<TransactionTypeFinalizer>();

  public void processTransactions(GlobList transactions, GlobRepository globRepository) {
    for (Glob transaction : transactions) {
      processTransaction(transaction, globRepository);
    }
  }

  private void processTransaction(Glob transaction, GlobRepository globRepository) {
    for (TransactionTypeFinalizer finalizer : exclusiveFinalizers) {
      if (finalizer.processTransaction(transaction, globRepository)) {
        break;
      }
    }
    for (TransactionTypeFinalizer finalizer : finalizers) {
      finalizer.processTransaction(transaction, globRepository);
    }
    if (transaction.get(Transaction.TRANSACTION_TYPE) == null) {
      setDefaultType(transaction, globRepository);
    }
  }

  private void setDefaultType(Glob transaction, GlobRepository globRepository) {
    double amount = transaction.get(Transaction.AMOUNT);
    TransactionType type =
      amount < 0 ? PRELEVEMENT : VIREMENT;
    globRepository.setTarget(transaction.getKey(),
                             Transaction.TRANSACTION_TYPE,
                             type.getGlob().getKey());
  }

  public void addExclusive(String regexp, final TransactionType type,
                  final Integer matcherGroupForLabel, final Integer matcherGroupForDate) {
    exclusiveFinalizers.add(new AbstractRegexpTransactionTypeFinalizer(regexp) {
      protected void setTransactionType(Glob transaction, GlobRepository globRepository, Matcher matcher) {
        String label = matcher.group(matcherGroupForLabel);
        setTransactionType(transaction, globRepository, type, label,
                           matcher.group(matcherGroupForDate)
        );
      }
    });
  }

  public void addExclusive(String regexp, final TransactionType type, Integer matcherGroupForLabel) {
    exclusiveFinalizers.add(new AbstractRegexpTransactionTypeFinalizer(regexp) {
      protected void setTransactionType(Glob transaction, GlobRepository globRepository, Matcher matcher) {
        setTransactionType(transaction, globRepository, type, matcher.group(1));
      }
    });
  }

  public void addExclusive(String regexp, final TransactionType type) {
    exclusiveFinalizers.add(new AbstractRegexpTransactionTypeFinalizer(regexp) {
      protected void setTransactionType(Glob transaction, GlobRepository globRepository, Matcher matcher) {
        setTransactionType(transaction, globRepository, type, matcher.group());
      }
    });
  }

  public void add(TransactionTypeFinalizer finalizer) {
    finalizers.add(finalizer);
  }
}


