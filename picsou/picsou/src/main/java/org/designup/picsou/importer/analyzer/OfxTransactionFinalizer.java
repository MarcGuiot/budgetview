package org.designup.picsou.importer.analyzer;

import org.designup.picsou.model.PreTransactionTypeMatcher;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionType;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;

import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

public class OfxTransactionFinalizer extends AbstractTransactionTypeFinalizer {
  private static final Pattern NAME_REGEXP = Pattern.compile("\\{NAME\\.([0-9]+)\\}");
  private static final Pattern MEMO_REGEXP = Pattern.compile("\\{MEMO\\.([0-9]+)\\}");
  private static final Pattern CHECK_NUM_REGEXP = Pattern.compile("\\{NUM\\.([0-9]+)\\}");
  private static final Pattern TYPE_REGEXP = Pattern.compile("\\{TYPE\\.([0-9]+)\\}");
  private Pattern nameRegexp;
  private Pattern checkNumRegexp;
  private Pattern memoRegexp;
  private Pattern typeRegexp;
  private String label;
  private StringField field;
  private String date;
  private SimpleDateFormat format;
  private TransactionType transactionType;

  public OfxTransactionFinalizer(String name, String memo, String num, String label,
                                 StringField field, String type, String date, String format,
                                 TransactionType transactionType) {
    this.field = field;
    this.typeRegexp = type != null ? Pattern.compile(type) : null;
    this.date = date;
    this.format = format != null ? new SimpleDateFormat(format) : null;
    this.transactionType = transactionType;
    this.nameRegexp = name != null ? Pattern.compile(name) : null;
    this.checkNumRegexp = num != null ? Pattern.compile(num) : null;
    this.memoRegexp = memo != null ? Pattern.compile(memo) : null;
    this.label = label;
  }

  public boolean processTransaction(Glob transaction, GlobRepository repository) {
    if (!transaction.get(Transaction.IS_OFX)) {
      return false;
    }

    String newLabel = label;

    newLabel = replace(transaction, newLabel, Transaction.BANK_TRANSACTION_TYPE, typeRegexp, TYPE_REGEXP);
    if (newLabel == null) {
      return false;
    }

    newLabel = replace(transaction, newLabel, Transaction.OFX_NAME, nameRegexp, NAME_REGEXP);
    if (newLabel == null) {
      return false;
    }

    newLabel = replace(transaction, newLabel, Transaction.OFX_MEMO, memoRegexp, MEMO_REGEXP);
    if (newLabel == null) {
      return false;
    }

    newLabel = replace(transaction, newLabel, Transaction.OFX_CHECK_NUM, checkNumRegexp, CHECK_NUM_REGEXP);
    if (newLabel == null) {
      return false;
    }

    String replacedDate = null;
    if (date != null) {
      replacedDate = replace(transaction, date, Transaction.OFX_NAME, nameRegexp, NAME_REGEXP);
      replacedDate = replace(transaction, replacedDate, Transaction.OFX_MEMO, memoRegexp, MEMO_REGEXP);
    }

    setTransactionType(transaction, repository, transactionType, replacedDate, format, field, newLabel.trim());
    return true;
  }

  public static boolean isOfType(Glob matcher) {
    return matcher.get(PreTransactionTypeMatcher.OFX_CHECK_NUM) != null ||
           matcher.get(PreTransactionTypeMatcher.OFX_MEMO) != null ||
           matcher.get(PreTransactionTypeMatcher.OFX_NAME) != null ||
           check(matcher, PreTransactionTypeMatcher.LABEL) ||
           check(matcher, PreTransactionTypeMatcher.ORIGINAL_LABEL) ||
           check(matcher, PreTransactionTypeMatcher.GROUP_FOR_DATE);
  }

  private static boolean check(Glob matcher, StringField labelField) {
    if (matcher.get(labelField) != null) {
      if (NAME_REGEXP.matcher(matcher.get(labelField)).find()) {
        return true;
      }
      if (MEMO_REGEXP.matcher(matcher.get(labelField)).find()) {
        return true;
      }
      if (CHECK_NUM_REGEXP.matcher(matcher.get(labelField)).find()) {
        return true;
      }
    }
    return false;
  }
}
