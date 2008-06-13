package org.designup.picsou.importer.analyzer;

import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.utils.MultiMap;
import org.designup.picsou.model.Bank;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionType;
import static org.designup.picsou.model.TransactionType.PRELEVEMENT;
import static org.designup.picsou.model.TransactionType.VIREMENT;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

public class DefaultTransactionAnalyzer implements TransactionAnalyzer {
  private MultiMap<Integer, TransactionTypeFinalizer> exclusiveFinalizers =
    new MultiMap<Integer, TransactionTypeFinalizer>();
  private List<TransactionTypeFinalizer> finalizers = new ArrayList<TransactionTypeFinalizer>();

  public void processTransactions(Integer bankId, List<Glob> transactions,
                                  GlobRepository globRepository, String dateFormat) {
    SimpleDateFormat format = new SimpleDateFormat(dateFormat);
    List<TransactionTypeFinalizer> finalizerList = exclusiveFinalizers.get(bankId);
    finalizerList = (finalizerList == null ? Collections.<TransactionTypeFinalizer>emptyList() : finalizerList);
    for (Glob transaction : transactions) {
      processTransaction(transaction, globRepository, finalizerList, format);
    }
  }

  private void processTransaction(Glob transaction, GlobRepository globRepository,
                                  List<TransactionTypeFinalizer> transactionTypeFinalizers, SimpleDateFormat format) {
    for (TransactionTypeFinalizer finalizer : transactionTypeFinalizers) {
      if (finalizer.processTransaction(transaction, globRepository, format)) {
        break;
      }
    }
    for (TransactionTypeFinalizer finalizer : finalizers) {
      finalizer.processTransaction(transaction, globRepository, format);
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
                           final Integer matcherGroupForLabel, final Integer matcherGroupForDate,
                           Glob glob) {
    AbstractRegexpTransactionTypeFinalizer finalizer = new AbstractRegexpTransactionTypeFinalizer(regexp) {
      protected void setTransactionType(Glob transaction, GlobRepository globRepository, Matcher matcher, SimpleDateFormat format) {
        String label = matcher.group(matcherGroupForLabel);
        setTransactionType(transaction, globRepository, type, label,
                           matcher.group(matcherGroupForDate), format);
      }
    };
    exclusiveFinalizers.put(glob.get(Bank.ID), finalizer);
  }

  public void addExclusive(String regexp, final TransactionType type, final Integer matcherGroupForLabel, Glob bank) {
    AbstractRegexpTransactionTypeFinalizer finalizer = new AbstractRegexpTransactionTypeFinalizer(regexp) {
      protected void setTransactionType(Glob transaction, GlobRepository globRepository, Matcher matcher, SimpleDateFormat format) {
        setTransactionType(transaction, globRepository, type, matcher.group(matcherGroupForLabel));
      }
    };
    exclusiveFinalizers.put(bank.get(Bank.ID), finalizer);
  }

  public void addExclusive(String regexp, final TransactionType type, Glob bank) {

    AbstractRegexpTransactionTypeFinalizer finalizer = new AbstractRegexpTransactionTypeFinalizer(regexp) {
      protected void setTransactionType(Glob transaction, GlobRepository globRepository, Matcher matcher, SimpleDateFormat format) {
        setTransactionType(transaction, globRepository, type, matcher.group());
      }
    };
    exclusiveFinalizers.put(bank.get(Bank.ID), finalizer);
  }

  public void add(TransactionTypeFinalizer finalizer) {
    finalizers.add(finalizer);
  }
}


