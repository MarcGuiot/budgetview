package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.gui.components.expansion.TableExpansionColumn;
import org.uispec4j.*;

import javax.swing.*;

public abstract class ExpandableTableChecker extends ViewChecker {
  public ExpandableTableChecker(Window window) {
    super(window);
  }

  protected abstract Table getTable();

  protected abstract Panel getPanel();

  protected abstract int getLabelColumnIndex();

  public void checkExpansionEnabled(String label, boolean enabled) {
    Table table = getTable();
    int row = table.getRowIndex(getLabelColumnIndex(), label);
    JButton button = (JButton)getTable().getSwingRendererComponentAt(row, 0);
    Assert.assertEquals(enabled,
                        (button.getIcon() != null) &&
                        (button.getIcon() != TableExpansionColumn.DISABLED_ICON));
  }

  public void checkExpanded(String label, boolean expanded) {
    Table table = getTable();
    int row = table.getRowIndex(getLabelColumnIndex(), label);
    JButton button = (JButton)table.getSwingRendererComponentAt(row, 0);
    if (expanded) {
      Assert.assertSame(TableExpansionColumn.EXPANDED_ICON, button.getIcon());
    }
    else {
      Assert.assertSame(TableExpansionColumn.COLLAPSED_ICON, button.getIcon());
    }
  }

  public void toggleExpansion(String label) {
    Table table = getTable();
    int row = table.getRowIndex(getLabelColumnIndex(), label);
    table.selectRow(row);
    JButton button = (JButton)getTable().getSwingEditorComponentAt(row, 0);
    new Button(button).click();
  }

  public void expandAll() {
    getPanel().getButton("expand").click();
  }

  public void collapseAll() {
    getPanel().getButton("collapse").click();
  }

  protected UIComponent findMainComponent(Window window) {
    return getTable();
  }
}
