package org.designup.picsou.gui.printing.actions;

import org.designup.picsou.gui.printing.dialog.PrintBudgetDialog;
import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.actions.MultiSelectionAction;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.util.SortedSet;

public class PrintBudgetAction extends MultiSelectionAction {

  private PrintBudgetDialog dialog;

  public PrintBudgetAction(GlobRepository repository, Directory directory) {
    super(Lang.get("print.menu"), Month.TYPE, repository, directory);
  }

  protected void processClick(GlobList months, GlobRepository repository, Directory directory) {
    if (dialog == null) {
      dialog = new PrintBudgetDialog(repository, directory);
    }
    SortedSet<Integer> selectedMonths = months.getSortedSet(Month.ID);
    dialog.show(selectedMonths);
  }
}
