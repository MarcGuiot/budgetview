package com.budgetview.io.importer.analyzer;

import com.budgetview.model.Bank;
import com.budgetview.model.BankFormat;
import com.budgetview.model.Transaction;
import com.budgetview.model.TransactionType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;
import org.globsframework.utils.collections.MultiMap;

import java.util.ArrayList;
import java.util.List;

import static com.budgetview.model.TransactionType.PRELEVEMENT;
import static com.budgetview.model.TransactionType.VIREMENT;

public class DefaultTransactionAnalyzer implements TransactionAnalyzer {
  private List<TransactionTypeFinalizer> finalizers = new ArrayList<TransactionTypeFinalizer>();

  private MultiMap<Integer, TransactionTypeFinalizer> analyserForOriginalLabel =
    new MultiMap<Integer, TransactionTypeFinalizer>();

  private MultiMap<Integer, TransactionTypeFinalizer> analyserForLabel =
    new MultiMap<Integer, TransactionTypeFinalizer>();


  public void processTransactions(Integer bankId, List<Glob> transactions, GlobRepository repository) {
    Integer bankFormatId = repository.get(Key.create(Bank.TYPE, bankId)).get(Bank.BANK_FORMAT);
    for (Glob transaction : transactions) {
      boolean processed = false;
      boolean originalLabelProcessed = false;
      for (TransactionTypeFinalizer transactionTypeFinalizer : analyserForLabel.get(bankFormatId)) {
        if (transactionTypeFinalizer.processTransaction(transaction, repository)) {
          processed = true;
          break;
        }
      }
      for (TransactionTypeFinalizer finalizer : analyserForOriginalLabel.get(bankFormatId)) {
        if (finalizer.processTransaction(transaction, repository)) {
          originalLabelProcessed = true;
          break;
        }
      }

      String memo = transaction.get(Transaction.OFX_MEMO);
      String name = transaction.get(Transaction.OFX_NAME);
      String checkNum = transaction.get(Transaction.OFX_CHECK_NUM);
      if ((memo != null || name != null || checkNum != null) && (!processed || !originalLabelProcessed)) {
        String content;
        if (name == null && checkNum != null) {
          name = checkNum;
        }
        if (Utils.equal(name, memo)) {
          content = name;
        }
        else if (name != null && memo != null && name.contains(memo)) {
          content = name;
        }
        else if (name != null && memo != null && memo.contains(name)) {
          content = memo;
        }
        else {
          content = Strings.join(name, memo);
        }

        if (!processed && Strings.isNullOrEmpty(transaction.get(Transaction.LABEL))) {
          repository.update(transaction.getKey(), Transaction.LABEL, content.trim());
        }
        if (!originalLabelProcessed) {
          repository.update(transaction.getKey(), Transaction.ORIGINAL_LABEL, content.trim());
        }
      }
      String mValue = transaction.get(Transaction.QIF_M);
      String pValue = transaction.get(Transaction.QIF_P);
      if ((mValue != null || pValue != null) && (!processed || !originalLabelProcessed)) {
        String value = "";
        if (Strings.isNotEmpty(mValue)) {
          value = mValue;
        }
        else if (Strings.isNotEmpty(pValue)) {
          value = pValue;
        }
        if (!Strings.isNullOrEmpty(mValue) && !Strings.isNullOrEmpty(pValue)) {
          if (mValue.startsWith(pValue.substring(0, pValue.length() > 10 ? 10 : pValue.length()))) {
            value = mValue;
          }
          else {
            value = Strings.join(pValue, mValue);
          }
        }
        if (!processed) {
          repository.update(transaction.getKey(), Transaction.LABEL, value.trim());
        }
        if (!originalLabelProcessed) {
          repository.update(transaction.getKey(), Transaction.ORIGINAL_LABEL, value.trim());
        }
      }

      for (TransactionTypeFinalizer finalizer : finalizers) {
        finalizer.processTransaction(transaction, repository);
      }
      if (transaction.get(Transaction.TRANSACTION_TYPE) == null) {
        setDefaultType(transaction, repository);
      }
      if (transaction.get(Transaction.LABEL) == null) {
        repository.update(transaction.getKey(), Transaction.LABEL, "");
      }
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

  public void addOfx(String name, String memo, String num, String label, Glob bankFormat,
                     String type, String date, String format, TransactionType transactionType) {
    analyserForLabel.put(bankFormat.get(BankFormat.ID),
                         new OfxTransactionFinalizer(name, memo, num, label, Transaction.LABEL,
                                                     type, date, format, transactionType));
  }

  public void addQif(String mValue, String pValue, String label, Glob bankFormat,
                     String type, String date, String format, TransactionType transactionType) {
    analyserForLabel.put(bankFormat.get(BankFormat.ID),
                         new QifTransactionFinalizer(mValue, pValue, label, Transaction.LABEL,
                                                     type, date, format, transactionType));
  }

  public void addOriginalOfx(String name, String memo, String num, String label, Glob bankFormat, String type,
                             String date, String format, TransactionType transactionType) {
    analyserForOriginalLabel.put(bankFormat.get(BankFormat.ID),
                                 new OfxTransactionFinalizer(name, memo,
                                                             num, label, Transaction.ORIGINAL_LABEL, type, date, format, transactionType));
  }

  public void addOriginalQif(String mValue, String pValue, String label, Glob bankFormat, String type,
                             String date, String format, TransactionType transactionType) {
    analyserForOriginalLabel.put(bankFormat.get(BankFormat.ID),
                                 new QifTransactionFinalizer(mValue, pValue, label,
                                                             Transaction.ORIGINAL_LABEL, type, date, format, transactionType));
  }
}


