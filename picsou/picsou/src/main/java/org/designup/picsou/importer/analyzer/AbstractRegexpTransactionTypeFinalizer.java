package org.designup.picsou.importer.analyzer;

import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionType;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractRegexpTransactionTypeFinalizer implements TransactionTypeFinalizer {

  private Pattern pattern;
  private Pattern typeRegexp;

  public AbstractRegexpTransactionTypeFinalizer(String regexp, String typeRegexp) {
    if (typeRegexp != null) {
      this.typeRegexp = Pattern.compile(typeRegexp);
    }
    this.pattern = Pattern.compile(regexp);
  }

  public boolean processTransaction(Glob transaction, GlobRepository repository) {
    String label = transaction.get(Transaction.LABEL);
    if (label == null) {
      return true;
    }

    if (typeRegexp != null) {
      String bankType = transaction.get(Transaction.BANK_TRANSACTION_TYPE);
      if (bankType == null) {
        return false;
      }
      Matcher matcher = typeRegexp.matcher(bankType.toUpperCase());
      if (!matcher.matches()) {
        return false;
      }
    }

    String upperCaseLabel = label.toUpperCase().trim();

    Matcher matcher = pattern.matcher(upperCaseLabel);
    if (matcher.matches()) {
      setTransactionType(transaction, repository, matcher);
      return true;
    }
    return false;
  }

  protected abstract void setTransactionType(Glob transaction, GlobRepository repository, Matcher matcher);

  protected void setTransactionType(Glob transaction,
                                    GlobRepository repository,
                                    TransactionType transactionType,
                                    String label,
                                    String date,
                                    SimpleDateFormat format) {
    Key key = transaction.getKey();
    try {
      Date parsedDate = getDate(format, date);
      repository.update(key,
                        value(Transaction.TRANSACTION_TYPE, transactionType.getId()),
                        value(Transaction.MONTH, Month.getMonthId(parsedDate)),
                        value(Transaction.DAY, Month.getDay(parsedDate)),
                        value(Transaction.LABEL, label.trim()));
    }
    catch (ParseException e) {
      repository.update(key,
                        value(Transaction.TRANSACTION_TYPE, transactionType.getId()),
                        value(Transaction.LABEL, label.trim()));
    }
  }

  protected void setTransactionType(Glob transaction, GlobRepository repository, TransactionType transactionType,
                                    String label) {
    Key key = transaction.getKey();
    repository.update(key,
                      value(Transaction.TRANSACTION_TYPE, transactionType.getId()),
                      value(Transaction.LABEL, label.trim()));
  }

  private Date getDate(SimpleDateFormat format, String date) throws ParseException {
    return format.parse(date);
  }

  public String toString() {
    return pattern.toString();
  }
}
