package org.designup.picsou.gui.budget.dialogs;

import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.budget.wizard.BudgetBalancePage;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.gui.GlobsPanelBuilder;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.SortedSet;

public class BalanceDialog {
  private PicsouDialog dialog;
  private BudgetBalancePage balancePanel;

  public BalanceDialog(GlobRepository repository, Directory directory) {

    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/budget/dialogs/balanceDialog.splits", repository, directory);

    balancePanel = new BudgetBalancePage(repository, directory);
    balancePanel.init();

    builder.add("content", balancePanel.getPanel());
    dialog = PicsouDialog.create(directory.get(JFrame.class), directory);
    dialog.setPanelAndButton((JPanel)builder.load(), new AbstractAction(Lang.get("close")) {
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
      }
    });
  }

  public void show(SortedSet<Integer> monthId) {
    if (monthId.isEmpty()){
      return;
    }
    balancePanel.updateBeforeDisplay();
    dialog.pack();
    dialog.showCentered(true);
  }
}
