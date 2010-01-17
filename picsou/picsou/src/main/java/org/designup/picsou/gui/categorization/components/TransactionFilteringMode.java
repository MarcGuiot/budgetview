package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobFieldsComparator;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import java.util.Set;

public enum TransactionFilteringMode {
  ALL(1),
  SELECTED_MONTHS(2),
  LAST_IMPORTED_FILE(3),
  UNCATEGORIZED(4),
  UNCATEGORIZED_LAST_THREE_MONTHS(5);

  private int id;

  TransactionFilteringMode(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public GlobMatcher getMatcher(GlobRepository repository, SelectionService selectionService) {
    switch (this) {

      case ALL:
        return GlobMatchers.ALL;

      case SELECTED_MONTHS:
        Set<Integer> selectedMonthIds = selectionService.getSelection(Month.TYPE).getValueSet(Month.ID);
        return GlobMatchers.fieldIn(Transaction.BUDGET_MONTH, selectedMonthIds);

      case LAST_IMPORTED_FILE:
        GlobList imports = repository.getAll(TransactionImport.TYPE)
          .sort(new GlobFieldsComparator(TransactionImport.IMPORT_DATE, true, TransactionImport.ID, true));
        if (imports.isEmpty()) {
          return GlobMatchers.NONE;
        }
        return GlobMatchers.fieldEquals(Transaction.IMPORT, imports.getLast().get(TransactionImport.ID));

      case UNCATEGORIZED:
        return GlobMatchers.fieldEquals(Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID);

      case UNCATEGORIZED_LAST_THREE_MONTHS:
        Integer month = CurrentMonth.getCurrentMonth(repository);
        if (month == null) {
          return GlobMatchers.NONE;
        }
        return GlobMatchers.and(
          GlobMatchers.fieldEquals(Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID),
          GlobMatchers.fieldGreaterOrEqual(Transaction.MONTH, Month.previous(month, 2))
        );
    }
    throw new UnexpectedApplicationState(name());
  }

  public String toString() {
    return Lang.get("categorization.filtering." + Strings.toNiceLowerCase(name()));
  }

  public static Object get(Integer modeId) {
    for (TransactionFilteringMode mode : values()) {
      if (mode.getId() == modeId) {
        return mode;
      }
    }
    return null;
  }
}
