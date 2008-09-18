package org.designup.picsou.gui.transactions.columns;

import org.globsframework.gui.views.GlobTableView;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class TransactionAmountColumn implements TableCellRenderer {
  private GlobStringifier amountStringifier;
  private TransactionRendererColors rendererColors;
  private GlobRepository repository;
  private JLabel label = new JLabel();

  public TransactionAmountColumn(GlobTableView view, DoubleField amountField, TransactionRendererColors transactionRendererColors,
                                 DescriptionService descriptionService, GlobRepository repository, Directory directory
  ) {
    this.repository = repository;
    amountStringifier = descriptionService.getStringifier(amountField);
    label.setName("amount");
    label.setOpaque(true);
    label.setHorizontalAlignment(SwingConstants.RIGHT);
    this.rendererColors = transactionRendererColors;
    label.setFont(view.getDefaultFont());
  }

  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Glob transaction = (Glob)value;
    label.setText(amountStringifier.toString(transaction, repository));
    rendererColors.update(label, isSelected, transaction, row);
    return label;
  }

}
