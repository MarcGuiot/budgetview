package com.budgetview.functests.checkers;

import com.budgetview.gui.components.expansion.TableExpansionColumn;
import junit.framework.Assert;
import org.globsframework.utils.Strings;
import org.uispec4j.*;

import javax.swing.*;

public abstract class ExpandableTableChecker<T extends ExpandableTableChecker> extends ViewChecker {
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
    Icon icon = button.getIcon();
    if (enabled) {
      Assert.assertTrue(Strings.toString(icon),
                        (icon == TableExpansionColumn.EXPANDED_ICON)          ||
                        (icon == TableExpansionColumn.COLLAPSED_ICON)         ||
                        (icon == TableExpansionColumn.EXPANDED_ICON_SELECTED) ||
                        (icon == TableExpansionColumn.COLLAPSED_ICON_SELECTED));
    }
    else {
      Assert.assertTrue(Strings.toString(icon),
                        (icon == null) ||
                        (icon == TableExpansionColumn.DISABLED_ICON));
    }
  }

  public void checkExpanded(String label, boolean expanded) {
    Table table = getTable();
    int row = table.getRowIndex(getLabelColumnIndex(), label);
    if (row < 0) {
      Assert.fail(label + " not found in table. Actual content:\n" + table.toString());
    }
    JButton button = (JButton)table.getSwingRendererComponentAt(row, 0);
    Icon icon = button.getIcon();
    if (expanded) {
      Assert.assertTrue(Strings.toString(icon), icon == TableExpansionColumn.EXPANDED_ICON || icon == TableExpansionColumn.EXPANDED_ICON_SELECTED);
    }
    else {
      Assert.assertTrue(Strings.toString(icon), icon == TableExpansionColumn.COLLAPSED_ICON || icon == TableExpansionColumn.COLLAPSED_ICON_SELECTED);
    }
  }

  public T toggleExpansion(String label) {
    Table table = getTable();
    int row = table.getRowIndex(getLabelColumnIndex(), label);
    table.selectRow(row);
    JButton button = (JButton)getTable().getSwingEditorComponentAt(row, 0);
    new Button(button).click();
    return (T)this;
  }

  protected UIComponent getMainComponent() {
    return getTable();
  }
}
