package com.budgetview.io.importer.analyzer;

import com.budgetview.model.ImportType;
import com.budgetview.model.PreTransactionTypeMatcher;
import com.budgetview.model.Transaction;
import com.budgetview.model.TransactionType;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;

import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

public class QifTransactionFinalizer extends AbstractTransactionTypeFinalizer {
  private static final Pattern M_REGEXP = Pattern.compile("\\{M\\.([0-9]+)\\}");
  private static final Pattern P_REGEXP = Pattern.compile("\\{P\\.([0-9]+)\\}");
  private static final Pattern TYPE_REGEXP = Pattern.compile("\\{TYPE\\.([0-9]+)\\}");
  private Pattern mRegexp;
  private Pattern pRegexp;
  private Pattern typeRegexp;
  private String label;
  private StringField field;
  private String date;
  private SimpleDateFormat format;
  private TransactionType transactionType;

  public QifTransactionFinalizer(String mValue, String pValue, String label, StringField field,
                                 String type, String date, String format, TransactionType transactionType) {
    this.field = field;
    this.typeRegexp = type != null ? Pattern.compile(type) : null;
    this.date = date;
    this.format = format != null ? new SimpleDateFormat(format) : null;
    this.transactionType = transactionType;
    this.mRegexp = mValue != null ? Pattern.compile(mValue) : null;
    this.pRegexp = pValue != null ? Pattern.compile(pValue) : null;
    this.label = label;
  }

  public boolean processTransaction(Glob transaction, GlobRepository repository) {
    if (Transaction.getImportType(transaction) != ImportType.QIF) {
      return false;
    }

    String newLabel = label;

    newLabel = replace(transaction, newLabel, Transaction.BANK_TRANSACTION_TYPE, typeRegexp, TYPE_REGEXP);
    if (newLabel == null) {
      return false;
    }

    newLabel = replace(transaction, newLabel, Transaction.QIF_M, mRegexp, M_REGEXP);
    if (newLabel == null) {
      return false;
    }

    newLabel = replace(transaction, newLabel, Transaction.QIF_P, pRegexp, P_REGEXP);
    if (newLabel == null) {
      return false;
    }

    String replacedDate = null;
    if (date != null) {
      replacedDate = replace(transaction, date, Transaction.QIF_M, mRegexp, M_REGEXP);
      replacedDate = replace(transaction, replacedDate, Transaction.QIF_P, pRegexp, P_REGEXP);
    }

    setTransactionType(transaction, repository, transactionType, replacedDate, format, field, newLabel);
    return true;
  }

  public static boolean isOfType(Glob matcher) {
    return matcher.get(PreTransactionTypeMatcher.QIF_M) != null ||
           matcher.get(PreTransactionTypeMatcher.QIF_P) != null ||
           check(matcher, PreTransactionTypeMatcher.LABEL) ||
           check(matcher, PreTransactionTypeMatcher.ORIGINAL_LABEL) ||
           check(matcher, PreTransactionTypeMatcher.GROUP_FOR_DATE);
  }

  private static boolean check(Glob matcher, StringField labelField) {
    if (matcher.get(labelField) != null) {
      if (M_REGEXP.matcher(matcher.get(labelField)).find()) {
        return true;
      }
      if (P_REGEXP.matcher(matcher.get(labelField)).find()) {
        return true;
      }
    }
    return false;
  }

}
