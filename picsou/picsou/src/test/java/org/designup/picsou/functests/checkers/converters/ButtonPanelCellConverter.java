package org.designup.picsou.functests.checkers.converters;

import org.uispec4j.TableCellValueConverter;

import java.awt.*;

public class ButtonPanelCellConverter implements TableCellValueConverter {
  public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {
    org.uispec4j.Panel panel = new org.uispec4j.Panel((Container)renderedComponent);
    final org.uispec4j.Button button = panel.getButton();
    return button.getLabel();
  }
}
