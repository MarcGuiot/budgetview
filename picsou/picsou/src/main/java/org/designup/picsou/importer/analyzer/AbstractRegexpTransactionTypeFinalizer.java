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
  public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yy");

  private Pattern pattern;

  public AbstractRegexpTransactionTypeFinalizer(String regexp) {
    this.pattern = Pattern.compile(regexp);
  }

  public boolean processTransaction(Glob transaction, GlobRepository globRepository, SimpleDateFormat format) {
    String label = transaction.get(Transaction.LABEL);
    if (label == null) {
      return true;
    }

    String upperCaseLabel = label.toUpperCase().trim();

    Matcher matcher = pattern.matcher(upperCaseLabel);
    if (matcher.matches()) {
      setTransactionType(transaction, globRepository, matcher, format);
      return true;
    }

    return false;
  }

  protected abstract void setTransactionType(Glob transaction, GlobRepository globRepository, Matcher matcher, SimpleDateFormat format);

  protected void setTransactionType(Glob transaction,
                                    GlobRepository globRepository,
                                    TransactionType transactionType,
                                    String label,
                                    String date, SimpleDateFormat format) {
    Key key = transaction.getKey();
    Date parsedDate = getDate(format, date);
    globRepository.update(key,
                          value(Transaction.TRANSACTION_TYPE, transactionType.getId()),
                          value(Transaction.MONTH, Month.getMonthId(parsedDate)),
                          value(Transaction.DAY, Month.getDay(parsedDate)),
                          value(Transaction.LABEL, label.trim()));
  }

  protected void setTransactionType(Glob transaction, GlobRepository globRepository, TransactionType transactionType,
                                    String label) {
    Key key = transaction.getKey();
    globRepository.update(key,
                          value(Transaction.TRANSACTION_TYPE, transactionType.getId()),
                          value(Transaction.LABEL, label.trim()));
  }

  private Date getDate(SimpleDateFormat format, String date) {
    try {
      return format.parse(date);
    }
    catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  public String toString() {
    return pattern.toString();
  }
}
