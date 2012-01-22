package org.designup.picsou.gui.printing.actions;

import org.designup.picsou.gui.printing.dialog.PrintDialog;
import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class PrintAction extends AbstractAction {

  private GlobRepository repository;
  private Directory directory;
  private PrintDialog dialog;
  public Integer currentMonthId;

  public PrintAction(GlobRepository repository, Directory directory) {
    super(Lang.get("print.menu"));
    this.repository = repository;
    this.directory = directory;
    directory.get(SelectionService.class).addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        GlobList months = selection.getAll(Month.TYPE);
        if (months.isEmpty()) {
          setEnabled(false);
          currentMonthId = null;
          return;
        }

        setEnabled(true);
        currentMonthId = months.getSortedSet(Month.ID).last();
      }
    }, Month.TYPE);
  }

  public void actionPerformed(ActionEvent actionEvent) {
    if (dialog == null) {
      dialog = new PrintDialog(repository, directory);
    }
    dialog.show(currentMonthId);
  }
}
