package com.budgetview.desktop.components.expansion;

import com.budgetview.utils.Lang;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CollapseTableAction extends AbstractAction {
  private TableExpansionModel expansionModel;

  public CollapseTableAction(TableExpansionModel expansionModel) {
    super(Lang.get("collapse"));
    this.expansionModel = expansionModel;
  }

  public void actionPerformed(ActionEvent e) {
    expansionModel.collapseAll();
  }
}