package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionImport;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import java.util.Set;

public enum TransactionFilteringMode {
  ALL(1),
  SELECTED_MONTHS(2),
  LAST_IMPORTED_FILE(3),
  UNCATEGORIZED(4);

  TransactionFilteringMode(int id) {
  }

  public GlobMatcher getMatcher(GlobRepository repository, SelectionService selectionService) {
    switch (this) {

      case ALL:
        return GlobMatchers.ALL;

      case SELECTED_MONTHS:
        Set<Integer> selectedMonthIds = selectionService.getSelection(Month.TYPE).getValueSet(Month.ID);
        return GlobMatchers.fieldIn(Transaction.MONTH, selectedMonthIds);

      case LAST_IMPORTED_FILE:
        GlobList imports = repository.getAll(TransactionImport.TYPE).sort(TransactionImport.IMPORT_DATE);
        if (imports.isEmpty()) {
          return GlobMatchers.NONE;
        }
        return GlobMatchers.fieldEquals(Transaction.IMPORT, imports.getLast().get(TransactionImport.ID));

      case UNCATEGORIZED:
        return GlobMatchers.fieldEquals(Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID);
      
    }
    throw new UnexpectedApplicationState(name());
  }

  public String toString() {
    return Lang.get("categorization.filtering." + Strings.toNiceLowerCase(name()));
  }
}
