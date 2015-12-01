package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.gui.transactions.utils.TransactionMatchers;
import org.designup.picsou.model.Month;
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

public enum CategorizationFilteringMode {
  ALL(1),
  SELECTED_MONTHS(2),
  LAST_IMPORTED_FILE(3),
  UNCATEGORIZED(4),
  UNCATEGORIZED_SELECTED_MONTHS(5),
  MISSING_RECONCILIATION_ANNOTATION(6),
  TO_RECONCILE(7);

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
          .sortSelf(new GlobFieldsComparator(TransactionImport.IMPORT_DATE, true, TransactionImport.ID, true));
        if (imports.isEmpty()) {
          return GlobMatchers.NONE;
        }
        return fieldEquals(Transaction.IMPORT, imports.getLast().get(TransactionImport.ID));

      case UNCATEGORIZED:
        return or(TransactionMatchers.uncategorized(),
                  keyIn(categorizedTransactions));

      case UNCATEGORIZED_SELECTED_MONTHS: {
        Set<Integer> selectedMonthIds = selectionService.getSelection(Month.TYPE).getValueSet(Month.ID);
        if (selectedMonthIds.isEmpty()) {
          return GlobMatchers.NONE;
        }
        return
          or(TransactionMatchers.uncategorizedForMonths(selectedMonthIds),
             keyIn(categorizedTransactions));
      }

      case MISSING_RECONCILIATION_ANNOTATION: {
        return TransactionMatchers.missingReconciliationAnnotation(reconciledTransactions);
      }

      case TO_RECONCILE: {
        return TransactionMatchers.transactionsToReconcile();
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
      modes.remove(MISSING_RECONCILIATION_ANNOTATION);
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
