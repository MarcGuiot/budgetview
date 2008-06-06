package org.designup.picsou.functests.checkers.converters;

import org.uispec4j.TableCellValueConverter;
import org.uispec4j.UIComponent;
import org.uispec4j.TextBox;
import org.crossbowlabs.globs.model.Glob;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionType;

import javax.swing.*;
import java.awt.*;

public class CategoryCellConverter implements TableCellValueConverter {
  public Object getValue(int row, int column,
                         Component renderedComponent, Object modelObject) {
    org.uispec4j.Panel panel =
      new org.uispec4j.Panel((JPanel)renderedComponent);
    UIComponent[] categoryLabels = panel.getUIComponents(TextBox.class);
    int index = 0;
    StringBuilder builder = new StringBuilder();

    Integer transactionType = ((Glob)modelObject).get(Transaction.TRANSACTION_TYPE);
    builder.append("(");
    builder.append(TransactionType.getType(transactionType).getName());
    builder.append(")");
    for (int i = 0; i < categoryLabels.length; i++) {
      TextBox label = (TextBox)categoryLabels[i];
      if (index++ > 0) {
        builder.append(", ");
      }
      builder.append(label.getText());
    }
    return builder.toString().trim();
  }
}
