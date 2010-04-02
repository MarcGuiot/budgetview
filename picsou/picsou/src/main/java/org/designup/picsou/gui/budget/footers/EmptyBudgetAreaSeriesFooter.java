package org.designup.picsou.gui.budget.footers;

import org.globsframework.model.Key;
import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import java.util.List;

public class EmptyBudgetAreaSeriesFooter implements BudgetAreaSeriesFooter {

  public void init(JEditorPane editorPane) {
    editorPane.setVisible(false);
    GuiUtils.revalidate(editorPane);
  }

  public void update(List<Key> displayedKeys) {
  }
}
