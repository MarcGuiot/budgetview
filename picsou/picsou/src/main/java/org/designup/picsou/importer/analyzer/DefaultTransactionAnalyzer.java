package org.designup.picsou.importer.analyzer;

import org.designup.picsou.model.Bank;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionType;
import static org.designup.picsou.model.TransactionType.PRELEVEMENT;
import static org.designup.picsou.model.TransactionType.VIREMENT;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.MultiMap;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

public class DefaultTransactionAnalyzer implements TransactionAnalyzer {
  private MultiMap<Integer, TransactionTypeFinalizer> exclusiveFinalizers =
    new MultiMap<Integer, TransactionTypeFinalizer>();
  private List<TransactionTypeFinalizer> finalizers = new ArrayList<TransactionTypeFinalizer>();

  public void processTransactions(Integer bankId, List<Glob> transactions, GlobRepository repository) {
    List<TransactionTypeFinalizer> finalizerList = exclusiveFinalizers.get(bankId);
    finalizerList = (finalizerList == null ? Collections.<TransactionTypeFinalizer>emptyList() : finalizerList);
    for (Glob transaction : transactions) {
      processTransaction(transaction, repository, finalizerList);
    }
  }

  private void processTransaction(Glob transaction, GlobRepository repository,
                                  List<TransactionTypeFinalizer> transactionTypeFinalizers) {
    for (TransactionTypeFinalizer finalizer : transactionTypeFinalizers) {
      if (finalizer.processTransaction(transaction, repository)) {
        break;
      }
    }
    for (TransactionTypeFinalizer finalizer : finalizers) {
      finalizer.processTransaction(transaction, repository);
    }
    if (transaction.get(Transaction.TRANSACTION_TYPE) == null) {
      setDefaultType(transaction, repository);
    }
  }

  private void setDefaultType(Glob transaction, GlobRepository repository) {
    double amount = transaction.get(Transaction.AMOUNT);
    TransactionType type =
      amount < 0 ? PRELEVEMENT : VIREMENT;
    repository.setTarget(transaction.getKey(),
                             Transaction.TRANSACTION_TYPE,
                             type.getGlob().getKey());
  }

  public void addExclusive(String regexp, final TransactionType type,
                           final String labelRegexp, final Integer matcherGroupForDate, final String dateFormat,
                           Glob bank) {
    AbstractRegexpTransactionTypeFinalizer finalizer = new AbstractRegexpTransactionTypeFinalizer(regexp) {
      protected void setTransactionType(Glob transaction, GlobRepository repository, Matcher matcher) {
        String date = matcher.group(matcherGroupForDate);
        String label = getLabel(matcher, labelRegexp);
        setTransactionType(transaction, repository, type, label, date, new SimpleDateFormat(dateFormat));
      }
    };
    exclusiveFinalizers.put(bank.get(Bank.ID), finalizer);
  }

  public void addExclusive(String regexp, final TransactionType type, final String labelRegexp, Glob bank) {
    AbstractRegexpTransactionTypeFinalizer finalizer = new AbstractRegexpTransactionTypeFinalizer(regexp) {
      protected void setTransactionType(Glob transaction, GlobRepository globRepository, Matcher matcher) {
        setTransactionType(transaction, globRepository, type, getLabel(matcher, labelRegexp));
      }
    };
    exclusiveFinalizers.put(bank.get(Bank.ID), finalizer);
  }

  public void addExclusive(String regexp, final TransactionType type, Glob bank) {

    AbstractRegexpTransactionTypeFinalizer finalizer = new AbstractRegexpTransactionTypeFinalizer(regexp) {
      protected void setTransactionType(Glob transaction, GlobRepository globRepository, Matcher matcher) {
        setTransactionType(transaction, globRepository, type, matcher.group());
      }
    };
    exclusiveFinalizers.put(bank.get(Bank.ID), finalizer);
  }

  public void add(TransactionTypeFinalizer finalizer) {
    finalizers.add(finalizer);
  }

  private String getLabel(Matcher matcher, String labelRegexp) {
    return matcher.replaceAll(labelRegexp);
  }
}


