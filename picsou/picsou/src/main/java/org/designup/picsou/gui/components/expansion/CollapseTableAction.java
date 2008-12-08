package org.designup.picsou.gui.components.expansion;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CollapseTableAction extends AbstractAction {
  private TableExpansionModel expansionModel;

  public CollapseTableAction(TableExpansionModel expansionModel) {
    this.expansionModel = expansionModel;
  }

  public void actionPerformed(ActionEvent e) {
    expansionModel.collapseAll();
  }
}