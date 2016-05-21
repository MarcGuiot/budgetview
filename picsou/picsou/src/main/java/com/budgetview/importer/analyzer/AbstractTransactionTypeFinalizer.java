package com.budgetview.importer.analyzer;

import com.budgetview.model.Transaction;
import com.budgetview.model.Month;
import com.budgetview.model.TransactionType;
import org.globsframework.metamodel.fields.StringField;
import static org.globsframework.model.FieldValue.value;

import org.globsframework.model.FieldValue;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.Log;
import org.globsframework.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
      try {
        return Utils.replace(labelRegexp, placementRegexp, fileContentValue.replace("$", "\\$"), newLabel);
      }
      catch (RuntimeException e) {
        Log.write("erreur label : " + labelRegexp + " placement :" + placementRegexp + " file content : " + fileContentValue
                  + " newlabel " + newLabel, e);
        return fileContentValue;
      }
    }
    else if (labelRegexp != null) {
      return null;
    }
    return newLabel;
  }

  protected void setTransactionType(Glob transaction,
                                    GlobRepository repository,
                                    TransactionType transactionType,
                                    String date,
                                    SimpleDateFormat format, StringField field, String label) {
    Key key = transaction.getKey();
    int transactionTypeId = transactionType.getId();
    if (date == null) {
      repository.update(key, FieldValue.value(Transaction.TRANSACTION_TYPE, transactionTypeId),
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
