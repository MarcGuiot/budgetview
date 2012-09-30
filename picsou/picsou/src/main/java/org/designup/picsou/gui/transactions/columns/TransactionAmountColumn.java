package org.designup.picsou.gui.transactions.columns;

import org.globsframework.gui.views.GlobTableView;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class TransactionAmountColumn implements TableCellRenderer {
  private GlobStringifier amountStringifier;
  private TransactionRendererColors rendererColors;
  private DoubleField amountField;
  private GlobRepository repository;
  private JLabel label = new JLabel();

  public TransactionAmountColumn(GlobTableView view,
                                 DoubleField amountField,
                                 TransactionRendererColors transactionRendererColors,
                                 DescriptionService descriptionService,
                                 GlobRepository repository) {
    this.amountField = amountField;
    this.repository = repository;
    amountStringifier = descriptionService.getStringifier(amountField);
    label.setName("amount");
    label.setOpaque(true);
    label.setHorizontalAlignment(SwingConstants.RIGHT);
    this.rendererColors = transactionRendererColors;
    label.setFont(view.getDefaultFont());
  }

  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    if (value == null){
      return null;
    }
    Glob transaction = (Glob)value;
    label.setText(amountStringifier.toString(transaction, repository));
    Double amount = transaction.get(amountField);
    rendererColors.update(label, isSelected, transaction,
                          TransactionRendererColors.getMode(amount), row);
    return label;
  }

  public GlobStringifier getStringifier() {
    return amountStringifier;
  }
}
