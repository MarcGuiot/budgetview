package org.designup.picsou.gui.budget;

import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.SortedSet;

public class BalanceDialog {
  private PicsouDialog dialog;
  private BalancePanel balancePanel;

  public BalanceDialog(GlobRepository repository, Directory directory) {

    balancePanel = new BalancePanel(repository, directory);
    dialog = PicsouDialog.create(directory.get(JFrame.class), directory);
    dialog.setPanelAndButton(balancePanel.getPanel(), new AbstractAction(Lang.get("close")) {
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
      }
    });
  }

  public void show(SortedSet<Integer> monthId) {
    if (monthId.isEmpty()){
      return;
    }
    balancePanel.setMonth(monthId);
    dialog.pack();
    dialog.showCentered();
  }
}
