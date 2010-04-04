package org.designup.picsou.gui.budget;

import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.SortedSet;

public class PositionDialog {
  private PicsouDialog dialog;
  private PositionPanel positionPanel;

  public PositionDialog(GlobRepository repository, Directory directory) {

    dialog = PicsouDialog.create(directory.get(JFrame.class), directory);
    positionPanel = new PositionPanel(dialog, repository, directory);
    dialog.setPanelAndButton(positionPanel.getPanel(), new AbstractAction(Lang.get("close")) {
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
      }
    });
  }

  public void show(SortedSet<Integer> monthId) {
    if (monthId.isEmpty()){
      return;
    }
    positionPanel.setMonth(monthId);
    dialog.pack();
    dialog.showCentered();
  }
}