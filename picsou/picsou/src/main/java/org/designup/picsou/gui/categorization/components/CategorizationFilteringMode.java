package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.gui.utils.Matchers;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionImport;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobFieldsComparator;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import java.util.*;

import static org.globsframework.model.utils.GlobMatchers.*;
import static org.globsframework.model.utils.GlobMatchers.isFalse;

public enum CategorizationFilteringMode {
  ALL(1),
  SELECTED_MONTHS(2),
  LAST_IMPORTED_FILE(3),
  UNCATEGORIZED(4),
  UNCATEGORIZED_SELECTED_MONTHS(5),
  UNRECONCILED(6);

  private int id;

  CategorizationFilteringMode(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public GlobMatcher getMatcher(GlobRepository repository,
                                SelectionService selectionService,
                                Collection<Key> categorizedTransactions,
                                Set<Key> reconciledTransactions) {
    switch (this) {

      case ALL:
        return GlobMatchers.ALL;

      case SELECTED_MONTHS: {
        Set<Integer> selectedMonthIds = selectionService.getSelection(Month.TYPE).getValueSet(Month.ID);
        return fieldIn(Transaction.BUDGET_MONTH, selectedMonthIds);
      }

      case LAST_IMPORTED_FILE:
        GlobList imports = repository.getAll(TransactionImport.TYPE)
          .sort(new GlobFieldsComparator(TransactionImport.IMPORT_DATE, true, TransactionImport.ID, true));
        if (imports.isEmpty()) {
          return GlobMatchers.NONE;
        }
        return fieldEquals(Transaction.IMPORT, imports.getLast().get(TransactionImport.ID));

      case UNCATEGORIZED:
        return or(fieldEquals(Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID),
                  keyIn(categorizedTransactions));

      case UNCATEGORIZED_SELECTED_MONTHS: {
        Set<Integer> selectedMonthIds = selectionService.getSelection(Month.TYPE).getValueSet(Month.ID);
        if (selectedMonthIds.isEmpty()) {
          return GlobMatchers.NONE;
        }
        return
          or(and(fieldEquals(Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID),
                 fieldIn(Transaction.BUDGET_MONTH, selectedMonthIds)),
             keyIn(categorizedTransactions));
      }

      case UNRECONCILED: {
        return Matchers.unreconciled(reconciledTransactions);
      }
    }
    throw new UnexpectedApplicationState(name());
  }

  public String toString() {
    return Lang.get("categorization.filtering." + Strings.toNiceLowerCase(name()));
  }

  public static CategorizationFilteringMode[] getValues(boolean showReconciliation) {
    if (showReconciliation) {
      return values();
    }
    else {
      List<CategorizationFilteringMode> modes = new ArrayList<CategorizationFilteringMode>();
      modes.addAll(Arrays.asList(values()));
      modes.remove(UNRECONCILED);
      return modes.toArray(new CategorizationFilteringMode[modes.size()]);
    }
  }

  public static Object get(Integer modeId) {
    for (CategorizationFilteringMode mode : values()) {
      if (mode.getId() == modeId) {
        return mode;
      }
    }
    return null;
  }
}
