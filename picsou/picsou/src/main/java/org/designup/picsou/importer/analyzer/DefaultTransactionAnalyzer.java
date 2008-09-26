package org.designup.picsou.importer.analyzer;

import org.designup.picsou.model.Bank;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionType;
import static org.designup.picsou.model.TransactionType.*;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.MultiMap;
import org.globsframework.utils.Strings;

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

  public void addExclusive(String regexp, final TransactionType type, final String typeRegexp,
                           final String labelRegexp, final Integer matcherGroupForDate, final String dateFormat,
                           Glob bank) {
    AbstractRegexpTransactionTypeFinalizer finalizer = new AbstractRegexpTransactionTypeFinalizer(regexp, typeRegexp) {
      protected void setTransactionType(Glob transaction, GlobRepository repository, Matcher matcher) {
        String date = matcher.group(matcherGroupForDate);
        String label = getLabel(matcher, replaceBankType(labelRegexp, transaction));
        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
        setTransactionType(transaction, repository, type, label, date, format);
      }
    };
    exclusiveFinalizers.put(bank.get(Bank.ID), finalizer);
  }

  public void addExclusive(String regexp, final TransactionType type, final String typeRegexp,
                           final String labelRegexp, Glob bank) {
    AbstractRegexpTransactionTypeFinalizer finalizer = new AbstractRegexpTransactionTypeFinalizer(regexp, typeRegexp) {
      protected void setTransactionType(Glob transaction, GlobRepository globRepository, Matcher matcher) {
        String label = getLabel(matcher, replaceBankType(labelRegexp, transaction));
        setTransactionType(transaction, globRepository, type, label);
      }
    };
    exclusiveFinalizers.put(bank.get(Bank.ID), finalizer);
  }

  public void addExclusive(String regexp, final TransactionType type, final String typeRegexp, Glob bank) {

    AbstractRegexpTransactionTypeFinalizer finalizer = new AbstractRegexpTransactionTypeFinalizer(regexp, typeRegexp) {
      protected void setTransactionType(Glob transaction, GlobRepository globRepository, Matcher matcher) {
        String label = matcher.group();
        label = replaceBankType(label, transaction);
        setTransactionType(transaction, globRepository, type, label);
      }
    };
    exclusiveFinalizers.put(bank.get(Bank.ID), finalizer);
  }

  private String replaceBankType(String text, Glob transaction) {
    String bankType = transaction.get(Transaction.BANK_TRANSACTION_TYPE);
    if (Strings.isNotEmpty(bankType)) {
      return text.replace("$bankType", bankType);
    }
    return text;
  }

  public void add(TransactionTypeFinalizer finalizer) {
    finalizers.add(finalizer);
  }

  private String getLabel(Matcher matcher, String labelRegexp) {
    return matcher.replaceFirst(labelRegexp);
  }
}


