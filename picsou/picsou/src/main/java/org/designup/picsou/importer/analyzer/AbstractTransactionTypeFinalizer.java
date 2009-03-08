package org.designup.picsou.importer.analyzer;

import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionType;
import org.globsframework.metamodel.fields.StringField;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractTransactionTypeFinalizer implements TransactionTypeFinalizer {

  static Pattern ALL = Pattern.compile(".*");

  String replace(Glob transaction, String newLabel, final StringField field,
                 Pattern sourcePattern, final Pattern targetPattern) {
    String mValue = transaction.get(field);
    if (mValue != null) {
      if (sourcePattern == null) {
        sourcePattern = ALL;
      }
      return replace(newLabel, sourcePattern, targetPattern, mValue);
    }
    else if (sourcePattern != null) {
      return null;
    }
    return newLabel;
  }

  protected String replace(String newLabel, Pattern sourcePattern, Pattern targetPattern, String mValue) {
    Matcher matcher = sourcePattern.matcher(mValue);
    if (!matcher.matches()) {
      return null;
    }
    return replace(matcher, targetPattern, newLabel);
  }

  protected String replace(Matcher groupMatcher, final Pattern regexp, final String label) {
    StringBuffer buffer = new StringBuffer();
    Matcher matcher = regexp.matcher(label);
    while (matcher.find()) {
      matcher.appendReplacement(buffer, groupMatcher.group(Integer.parseInt(matcher.group(1))));
    }
    matcher.appendTail(buffer);
    return buffer.toString();
  }

  protected void setTransactionType(Glob transaction,
                                    GlobRepository repository,
                                    TransactionType transactionType,
                                    String date,
                                    SimpleDateFormat format, StringField field, String label) {
    Key key = transaction.getKey();
    if (date == null) {
      repository.update(key, value(Transaction.TRANSACTION_TYPE, transactionType.getId()),
                        value(field, label));
      return;
    }
    try {
      Date parsedDate = getDate(transaction, format, date);
      repository.update(key,
                        value(Transaction.TRANSACTION_TYPE, transactionType.getId()),
                        value(field, label),
                        value(Transaction.MONTH, Month.getMonthId(parsedDate)),
                        value(Transaction.DAY, Month.getDay(parsedDate)));
    }
    catch (ParseException e) {
      repository.update(key,
                        value(Transaction.TRANSACTION_TYPE, transactionType.getId()),
                        value(field, label));
    }
  }

  private Date getDate(Glob transaction, SimpleDateFormat format, String dateString) throws ParseException {
    Date date = format.parse(dateString);
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    if (calendar.get(Calendar.YEAR) == 1970) {
      int bankMonthId = transaction.get(Transaction.BANK_MONTH);
      Calendar bankCalendar = Calendar.getInstance();
      bankCalendar.set(Month.toYear(bankMonthId),
                       Month.toMonth(bankMonthId) - 1,
                       transaction.get(Transaction.BANK_DAY));
      calendar.set(Calendar.YEAR, Month.toYear(bankMonthId));
      if (calendar.after(bankCalendar)) {
        calendar.set(Calendar.YEAR, Month.toYear(bankMonthId) - 1);
      }
      date = calendar.getTime();
    }
    return date;
  }

}
