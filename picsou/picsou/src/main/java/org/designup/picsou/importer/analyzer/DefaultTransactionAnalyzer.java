package org.designup.picsou.importer.analyzer;

import org.designup.picsou.model.*;
import static org.designup.picsou.model.TransactionType.PRELEVEMENT;
import static org.designup.picsou.model.TransactionType.VIREMENT;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.MultiMap;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultTransactionAnalyzer implements TransactionAnalyzer {
  private MultiMap<Integer, TransactionTypeFinalizer> exclusiveFinalizers =
    new MultiMap<Integer, TransactionTypeFinalizer>();
  private List<TransactionTypeFinalizer> finalizers = new ArrayList<TransactionTypeFinalizer>();

  private MultiMap<Integer, ImportedTransactionTypeFinalizer> importedTransactionTypeFinalizersForOriginalLabel =
    new MultiMap<Integer, ImportedTransactionTypeFinalizer>();

  private MultiMap<Integer, ImportedTransactionTypeFinalizer> importedTransactionTypeFinalizersForLabel =
    new MultiMap<Integer, ImportedTransactionTypeFinalizer>();

  public void processTransactions(Integer bankId, List<Glob> transactions, GlobRepository repository) {
    List<TransactionTypeFinalizer> finalizerList = exclusiveFinalizers.get(bankId);
    finalizerList = (finalizerList == null ? Collections.<TransactionTypeFinalizer>emptyList() : finalizerList);
    for (Glob transaction : transactions) {
      processTransaction(transaction, repository, finalizerList);
    }
  }

  public void processImportedTransactions(GlobRepository repository, GlobList transactions) {

    for (Glob transaction : transactions) {
      Glob account = repository.get(Key.create(Account.TYPE, transaction.get(ImportedTransaction.ACCOUNT)));
      Glob bankEntity = repository.findLinkTarget(account, Account.BANK_ENTITY);
      Glob bank = repository.findLinkTarget(bankEntity, BankEntity.BANK);
      Integer id = Bank.GENERIC_BANK_ID;
      if (bank != null) {
        id = bank.get(Bank.ID);
      }
      boolean processed = false;
      boolean originalLabelProcessed = false;
      for (ImportedTransactionTypeFinalizer importedTransactionTypeFinalizer : importedTransactionTypeFinalizersForLabel.get(id)) {
        if (importedTransactionTypeFinalizer.processTransaction(transaction, repository)) {
          processed = true;
          break;
        }
      }
      for (ImportedTransactionTypeFinalizer importedTransactionTypeFinalizer : importedTransactionTypeFinalizersForOriginalLabel.get(id)) {
        if (importedTransactionTypeFinalizer.processTransaction(transaction, repository)) {
          originalLabelProcessed = true;
          break;
        }
      }

      if (transaction.get(ImportedTransaction.IS_OFX)) {
        String memo = transaction.get(ImportedTransaction.OFX_MEMO);
        String name = transaction.get(ImportedTransaction.OFX_NAME);
        String checkNum = transaction.get(ImportedTransaction.OFX_CHECK_NUM);
        String content;
        if (name == null && checkNum != null) {
          name = checkNum;
        }
        if (Utils.equal(name, memo)) {
          content = name;
        }
        else {
          content = Strings.join(name, memo);
        }

        if (!processed) {
          repository.update(transaction.getKey(), ImportedTransaction.LABEL, content.trim());
        }
        if (!originalLabelProcessed) {
          repository.update(transaction.getKey(), ImportedTransaction.ORIGINAL_LABEL,
                            content.trim());
        }
      }
      if (!transaction.get(ImportedTransaction.IS_OFX)) {
        String mValue = transaction.get(ImportedTransaction.QIF_M);
        String pValue = transaction.get(ImportedTransaction.QIF_P);
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
          repository.update(transaction.getKey(), ImportedTransaction.LABEL, value.trim());
        }
        if (!originalLabelProcessed) {
          repository.update(transaction.getKey(), ImportedTransaction.ORIGINAL_LABEL, value.trim());
        }
      }
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

  public void addOfx(String name, String memo, String num, String label, Glob bank) {
    importedTransactionTypeFinalizersForLabel.put(bank.get(Bank.ID),
                                                  new OfxImportedTransactionTypeFinalizer(name, memo, num, label, ImportedTransaction.LABEL));
  }

  public void addQif(String mValue, String pValue, String label, Glob bank) {
    importedTransactionTypeFinalizersForLabel.put(bank.get(Bank.ID),
                                                  new QifImportedTransactionTypeFinalizer(mValue, pValue, label, ImportedTransaction.LABEL));
  }

  public void addOriginalOfx(String name, String memo, String num, String label, Glob bank) {
    importedTransactionTypeFinalizersForOriginalLabel.put(bank.get(Bank.ID),
                                                          new OfxImportedTransactionTypeFinalizer(name, memo,
                                                                                                  num, label, ImportedTransaction.ORIGINAL_LABEL));
  }

  public void addOriginalQif(String mValue, String pValue, String label, Glob bank) {
    importedTransactionTypeFinalizersForOriginalLabel.put(bank.get(Bank.ID),
                                                          new QifImportedTransactionTypeFinalizer(mValue, pValue, label,
                                                                                                  ImportedTransaction.ORIGINAL_LABEL));
  }

  private static class OfxImportedTransactionTypeFinalizer implements ImportedTransactionTypeFinalizer {
    private static final Pattern NAME_REGEXP = Pattern.compile("\\{NAME.([0-9]+)\\}");
    private static final Pattern MEMO_REGEXP = Pattern.compile("\\{MEMO.([0-9]+)\\}");
    private static final Pattern CHECK_NUM_REGEXP = Pattern.compile("\\{CHECK_NUM.([0-9]+)\\}");
    private Pattern nameRegexp;
    private Pattern checkNumRegexp;
    private Pattern memoRegexp;
    private String label;
    private StringField field;

    public OfxImportedTransactionTypeFinalizer(String name, String memo, String num, String label,
                                               StringField field) {
      this.field = field;
      this.nameRegexp = name != null ? Pattern.compile(name) : null;
      this.checkNumRegexp = num != null ? Pattern.compile(num) : null;
      this.memoRegexp = memo != null ? Pattern.compile(memo) : null;
      this.label = label;
    }

    public boolean processTransaction(Glob transaction, GlobRepository repository) {
      if (!transaction.get(ImportedTransaction.IS_OFX)) {
        return false;
      }
      String newLabel = label;

      String name = transaction.get(ImportedTransaction.OFX_NAME);
      if (nameRegexp != null && name != null) {
        Matcher nameMatcher = nameRegexp.matcher(name);
        if (!nameMatcher.matches()) {
          return false;
        }
        newLabel = replace(nameMatcher, NAME_REGEXP, newLabel);
      }
      else if (name != null || nameRegexp != null) {
        return false;
      }

      String memo = transaction.get(ImportedTransaction.OFX_MEMO);
      if (memoRegexp != null && memo != null) {
        Matcher memoMatcher = memoRegexp.matcher(memo);
        if (!memoMatcher.matches()) {
          return false;
        }
        newLabel = replace(memoMatcher, MEMO_REGEXP, newLabel);
      }
      else if (memo != null || memoRegexp != null) {
        return false;
      }

      String checkNum = transaction.get(ImportedTransaction.OFX_CHECK_NUM);
      if (checkNumRegexp != null && checkNum != null) {
        Matcher checkNumMatcher = checkNumRegexp.matcher(checkNum);
        if (!checkNumMatcher.matches()) {
          return false;
        }
        newLabel = replace(checkNumMatcher, CHECK_NUM_REGEXP, newLabel);
      }
      else if (checkNum != null || checkNumRegexp != null) {
        return false;
      }

      repository.update(transaction.getKey(), field, newLabel.trim());
      return true;
    }

    private String replace(Matcher groupMatcher, final Pattern regexp, final String label) {
      StringBuffer buffer = new StringBuffer();
      Matcher matcher = regexp.matcher(label);
      while (matcher.find()) {
        matcher.appendReplacement(buffer, groupMatcher.group(Integer.parseInt(matcher.group(1))));
      }
      matcher.appendTail(buffer);
      return buffer.toString();
    }
  }

  private static class QifImportedTransactionTypeFinalizer implements ImportedTransactionTypeFinalizer {
    private static final Pattern M_REGEXP = Pattern.compile("\\{M.([0-9]+)\\}");
    private static final Pattern P_REGEXP = Pattern.compile("\\{P.([0-9]+)\\}");
    private Pattern mRegexp;
    private Pattern pRegexp;
    private String label;
    private StringField field;

    public QifImportedTransactionTypeFinalizer(String mValue, String pValue, String label, StringField field) {
      this.field = field;
      this.mRegexp = mValue != null ? Pattern.compile(mValue) : null;
      this.pRegexp = pValue != null ? Pattern.compile(pValue) : null;
      this.label = label;
    }

    public boolean processTransaction(Glob transaction, GlobRepository repository) {
      if (transaction.get(ImportedTransaction.IS_OFX)) {
        return false;
      }

      String newLabel = label;

      String mValue = transaction.get(ImportedTransaction.QIF_M);
      if (mRegexp != null && mValue != null) {
        Matcher matcher = mRegexp.matcher(mValue);
        if (!matcher.matches()) {
          return false;
        }
        newLabel = replace(matcher, M_REGEXP, newLabel);
      }
      else if (mRegexp != null || mValue != null) {
        return false;
      }

      String pValue = transaction.get(ImportedTransaction.QIF_P);
      if (pRegexp != null && pValue != null) {
        Matcher matcher = pRegexp.matcher(pValue);
        if (!matcher.matches()) {
          return false;
        }
        newLabel = replace(matcher, P_REGEXP, newLabel);
      }
      else if (pValue != null || pRegexp != null) {
        return false;
      }

      repository.update(transaction.getKey(), field, newLabel.trim());
      return true;
    }

    private String replace(Matcher groupMatcher, final Pattern regexp, final String label) {
      StringBuffer buffer = new StringBuffer();
      Matcher matcher = regexp.matcher(label);
      while (matcher.find()) {
        matcher.appendReplacement(buffer, groupMatcher.group(Integer.parseInt(matcher.group(1))));
      }
      matcher.appendTail(buffer);
      return buffer.toString();
    }
  }
}


