package com.budgetview.analytics.functors;

import com.budgetview.analytics.model.LogEntry;
import com.budgetview.analytics.model.LogEntryType;
import com.budgetview.analytics.model.User;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.utils.Strings;

import java.util.Date;

import static com.budgetview.analytics.utils.Days.daysBetween;
import static org.globsframework.model.FieldValue.value;

public class UserEntriesFunctor implements GlobFunctor {

  public void run(Glob entry, GlobRepository repository) throws Exception {
    Glob user = repository.findLinkTarget(entry, LogEntry.USER);
    if (user == null) {
      System.out.println("!! Entry without user: " + GlobPrinter.toString(entry));
      return;
    }

    String email = entry.get(LogEntry.EMAIL);
    if (Strings.isNotEmpty(email)) {
      repository.update(user.getKey(), User.EMAIL, email);
    }

    Date date = entry.get(LogEntry.DATE);
    LogEntryType entryType = LogEntryType.get(entry.get(LogEntry.ENTRY_TYPE));
    switch (entryType) {
      case NEW_USER:
      case KNOWN_USER:
      case LICENCE_CHECK:
      case DIFFERENT_CODE:
        updateDates(user, date, repository);
        break;
      case PURCHASE:
        if (user.get(User.PURCHASE_DATE) != null && date.after(user.get(User.PURCHASE_DATE))) {
          break;
        }
        repository.update(user.getKey(),
                          value(User.PURCHASE_DATE, date),
                          value(User.PING_COUNT_ON_PURCHASE, user.get(User.PING_COUNT)),
                          value(User.DAYS_BEFORE_PURCHASE, getDaysBeforePurchase(user, date)));
        updateDates(user, date, repository);
        break;
      default:
    }

    switch (entryType) {
      case NEW_USER:
      case KNOWN_USER:
      case LICENCE_CHECK:
        Integer pingCount = user.get(User.PING_COUNT);
        int newPingCount = pingCount != null ? pingCount + 1 : 1;
        repository.update(user.getKey(), User.PING_COUNT, newPingCount);
      default:
    }
  }

  private int getDaysBeforePurchase(Glob user, Date date) {
    Date firstDate = user.get(User.FIRST_DATE);
    if (firstDate == null) {
      return 0;
    }
    return daysBetween(firstDate, date);
  }

  private void updateDates(Glob user, Date date, GlobRepository repository) {
    Date firstDate = user.get(User.FIRST_DATE);
    if ((firstDate == null) || firstDate.after(date)) {
      repository.update(user.getKey(), User.FIRST_DATE, date);
    }

    Date lastDate = user.get(User.LAST_DATE);
    if ((lastDate == null) || lastDate.before(date)) {
      repository.update(user.getKey(), User.LAST_DATE, date);
    }
  }
}
