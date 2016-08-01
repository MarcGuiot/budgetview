package com.budgetview.gui.printing.actions;

import com.budgetview.gui.printing.dialog.PrintBudgetDialog;
import com.budgetview.model.Month;
import com.budgetview.utils.Lang;
import org.globsframework.gui.actions.MultiSelectionAction;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.util.SortedSet;

public class PrintBudgetAction extends MultiSelectionAction {

  public PrintBudgetAction(GlobRepository repository, Directory directory) {
    super(Month.TYPE, repository, directory);
  }

  protected String getLabel(GlobList selection) {
    return Lang.get("print.menu");
  }

  protected void processClick(GlobList months, GlobRepository repository, Directory directory) {
    PrintBudgetDialog dialog = new PrintBudgetDialog(repository, directory);
    SortedSet<Integer> selectedMonths = months.getSortedSet(Month.ID);
    dialog.show(selectedMonths);
  }
}
