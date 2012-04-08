package org.designup.picsou.gui.printing.actions;

import org.designup.picsou.gui.printing.dialog.PrintDialog;
import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.actions.MultiSelectionAction;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class PrintAction extends MultiSelectionAction {

  private PrintDialog dialog;

  public PrintAction(GlobRepository repository, Directory directory) {
    super(Lang.get("print.menu"), Month.TYPE, repository, directory);
  }

  protected void process(GlobList months, GlobRepository repository, Directory directory) {
    if (dialog == null) {
      dialog = new PrintDialog(repository, directory);
    }
    Integer currentMonthId = months.getSortedSet(Month.ID).last();
    dialog.show(currentMonthId);
  }
}
