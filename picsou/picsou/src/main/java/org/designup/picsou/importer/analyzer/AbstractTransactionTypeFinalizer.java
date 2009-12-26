package org.designup.picsou.importer.analyzer;

import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionType;
import org.globsframework.metamodel.fields.StringField;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractTransactionTypeFinalizer implements TransactionTypeFinalizer {
  static Pattern ALL = Pattern.compile(".*");

  String replace(Glob transaction, String newLabel, final StringField field,
                 Pattern labelRegexp, final Pattern placementRegexp) {
    String fileContentValue = transaction.get(field);
    if (fileContentValue != null) {
      if (labelRegexp == null) {
        labelRegexp = ALL;
      }
      return replace(newLabel, labelRegexp, placementRegexp, fileContentValue);
    }
    else if (labelRegexp != null) {
      return null;
    }
    return newLabel;
  }

  private String replace(String newLabel, Pattern sourcePattern, Pattern placementRegexp, String mValue) {
    Matcher matcher = sourcePattern.matcher(mValue);
    if (!matcher.matches()) {
      return null;
    }
    return replace(matcher, placementRegexp, newLabel);
  }

  protected String replace(Matcher groupMatcher, final Pattern placementRegexp, final String label) {
    StringBuffer buffer = new StringBuffer();
    Matcher matcher = placementRegexp.matcher(label);
    while (matcher.find()) {
      int group;
      try {
        group = Integer.parseInt(matcher.group(1));
        try {
          matcher.appendReplacement(buffer, groupMatcher.group(group));
        }
        catch (IndexOutOfBoundsException e) {
          Log.write("Missing group '" + group + "' on " + label + " for patern " +
                    placementRegexp.toString() + " and " + groupMatcher.pattern().toString());
          return null;
        }
      }
      catch (IndexOutOfBoundsException e) {
        Log.write("Pattern error : " + matcher.pattern().toString() + " on " + label);
        return null;
      }
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
    int transactionTypeId = transactionType.getId();
    if (transactionType == TransactionType.VIREMENT ||
        transactionType == TransactionType.PRELEVEMENT) {
      if (transaction.get(Transaction.AMOUNT) >= 0) {
        transactionTypeId = TransactionType.VIREMENT.getId();
      }
      else {
        transactionTypeId = TransactionType.PRELEVEMENT.getId();
      }
    }
    if (date == null) {
      repository.update(key, value(Transaction.TRANSACTION_TYPE, transactionTypeId),
                        value(field, label));
      return;
    }
    try {
      Date parsedDate = getDate(transaction, format, date);
      repository.update(key,
                        value(Transaction.TRANSACTION_TYPE, transactionTypeId),
                        value(field, label),
                        value(Transaction.MONTH, Month.getMonthId(parsedDate)),
                        value(Transaction.DAY, Month.getDay(parsedDate)),
                        value(Transaction.BUDGET_MONTH, Month.getMonthId(parsedDate)),
                        value(Transaction.BUDGET_DAY, Month.getDay(parsedDate)));
    }
    catch (ParseException e) {
      repository.update(key,
                        value(Transaction.TRANSACTION_TYPE, transactionTypeId),
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
