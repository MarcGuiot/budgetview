package com.budgetview.desktop.utils.datacheck.check;

import com.budgetview.desktop.time.TimeService;
import com.budgetview.desktop.utils.datacheck.DataCheckReport;
import com.budgetview.desktop.utils.datacheck.utils.TransactionMonthRange;
import com.budgetview.model.CurrentMonth;
import com.budgetview.model.Month;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.collections.Range;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class MonthCheck {
  public static Range<Integer> allMonthsPresent(GlobRepository repository, DataCheckReport report) {
    GlobList months = repository.getAll(Month.TYPE).sort(Month.ID);
    if (months.size() == 0) {
      report.addFix("No month found - adding current month");
      months.add(repository.get(CurrentMonth.KEY));
    }

    int firstMonth = months.getFirst().get(Month.ID);
    int lastMonth = months.getLast().get(Month.ID);

    TransactionMonthRange allTransactionsRange = TransactionMonthRange.get(repository, GlobMatchers.ALL);
    if (firstMonth > allTransactionsRange.first()) {
      report.addError("Mising first month: " + allTransactionsRange.first());
      firstMonth = allTransactionsRange.first();
    }
    if (lastMonth < allTransactionsRange.last()) {
      report.addError("Mising last month: " + allTransactionsRange.last());
      lastMonth = allTransactionsRange.last();
    }

    java.util.List<Integer> monthsToCreate = new ArrayList<Integer>();

    // Pour s'assurer que le mois courant est bien dans la liste des mois.
    int now = TimeService.getCurrentMonth();
    if (firstMonth > now) {
      firstMonth = now;
    }
    if (now > lastMonth) {
      lastMonth = now;
    }

    // ---- Months sequence ----

    boolean nowFound = false;
    int currentMonth = firstMonth;
    Iterator<Glob> it = months.iterator();
    Glob actual = null;
    while (currentMonth <= lastMonth) {
      if (actual == null && it.hasNext()) {
        actual = it.next();
        if (now == actual.get(Month.ID)) {
          nowFound = true;
        }
      }
      if (actual == null || actual.get(Month.ID) != currentMonth) {
        monthsToCreate.add(currentMonth);
      }
      else {
        actual = null;
      }
      currentMonth = Month.next(currentMonth);
    }
    if (!nowFound) {
      report.addError("Missing current month: " + now);
    }
    if (!monthsToCreate.isEmpty()) {
      Collections.sort(monthsToCreate);
      report.addFix("Creating months:" + monthsToCreate);
      for (Integer monthId : monthsToCreate) {
        repository.create(Key.create(Month.TYPE, monthId));
      }
    }

    return new Range<Integer>(firstMonth, lastMonth);
  }
}
