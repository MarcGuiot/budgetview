package com.budgetview.gui.categorization.actions;

import com.budgetview.gui.card.NavigationService;
import com.budgetview.model.Transaction;
import com.budgetview.utils.Lang;
import org.globsframework.gui.actions.MultiSelectionAction;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

public class CategorizeTransactionsAction extends MultiSelectionAction {

  public CategorizeTransactionsAction(GlobRepository repository, Directory directory) {
    super(Transaction.TYPE, repository, directory);
  }

  protected String getLabel(GlobList selection) {
    return Lang.get("transaction.categorize.action");
  }

  protected void processSelection(GlobList selection) {
    Set<Boolean> plannedValues = selection.getAll(Transaction.TYPE).getValueSet(Transaction.PLANNED);
    setEnabled(!plannedValues.isEmpty() && !plannedValues.contains(Boolean.TRUE));
  }

  protected void processClick(GlobList selection, GlobRepository repository, Directory directory) {
    NavigationService navigation = directory.get(NavigationService.class);
    navigation.gotoCategorization(selectionService.getSelection(Transaction.TYPE), false);
  }
}
