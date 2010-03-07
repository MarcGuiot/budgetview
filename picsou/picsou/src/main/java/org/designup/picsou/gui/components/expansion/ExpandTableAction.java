package org.designup.picsou.gui.components.expansion;

import org.designup.picsou.utils.Lang;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ExpandTableAction extends AbstractAction {
  private TableExpansionModel expansionModel;

  public ExpandTableAction(TableExpansionModel expansionModel) {
    super(Lang.get("expand"));
    this.expansionModel = expansionModel;
  }

  public void actionPerformed(ActionEvent e) {
    expansionModel.expandAll();
  }
}
