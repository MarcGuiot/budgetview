package org.designup.picsou.functests.checkers.converters;

import org.uispec4j.TableCellValueConverter;

import javax.swing.*;
import java.awt.*;

public class AmountCellConverter implements TableCellValueConverter {
  public Object getValue(int row, int column,
                         Component renderedComponent, Object modelObject) {
    org.uispec4j.Panel panel = new org.uispec4j.Panel((JPanel)renderedComponent);
    return panel.getTextBox("amount").getText();
  }
}
