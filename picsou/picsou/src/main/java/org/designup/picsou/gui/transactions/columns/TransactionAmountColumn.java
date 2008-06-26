package org.designup.picsou.gui.transactions.columns;

import org.designup.picsou.model.Transaction;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class TransactionAmountColumn implements TableCellRenderer {
  private GlobStringifier amountStringifier;
  private JLabel amount;
  private JLabel splitPart;
  private JPanel panel;
  private TransactionRendererColors rendererColors;
  private GlobRepository repository;

  public TransactionAmountColumn(GlobTableView view, TransactionRendererColors transactionRendererColors,
                                 DescriptionService descriptionService, GlobRepository repository, Directory directory) {
    this.repository = repository;
    amountStringifier = descriptionService.getStringifier(Transaction.AMOUNT);
    panel = new JPanel();
    amount = new JLabel();
    splitPart = new JLabel();
    amount.setBackground(Color.WHITE);
    amount.setName("amount");
    splitPart.setName("");
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.add(Box.createHorizontalGlue());
    panel.add(splitPart);
    panel.add(amount);
    this.rendererColors = transactionRendererColors;
    amount.setFont(view.getDefaultFont());
    splitPart.setFont(view.getDefaultFont());
  }


  private void updateTotalAmount(Glob transaction, boolean selected) {
    GlobList splittedTransactions = getSplittedTransactions(transaction);
    if (!splittedTransactions.isEmpty()) {
      splitPart.setVisible(true);
      double total = 0;
      for (Glob glob : splittedTransactions) {
        total += glob.get(Transaction.AMOUNT);
      }
      String totalAmount = stringifyNumber(total, repository);
      splitPart.setText(" (" + totalAmount + ")");
      updateColor(splitPart, Color.LIGHT_GRAY, Color.GRAY, selected);
    }
    else {
      splitPart.setVisible(false);
    }
  }

  private void updateColor(JLabel label, Color selectionForeground, Color foreground, boolean isSelected) {
    label.setForeground(isSelected ? selectionForeground : foreground);

  }

  private GlobList getSplittedTransactions(Glob transaction) {
    GlobList splittedTransactions = new GlobList();
    if (Transaction.isSplitSource(transaction)) {
      splittedTransactions.add(transaction);
      splittedTransactions.addAll(repository.findLinkedTo(transaction, Transaction.SPLIT_SOURCE));
    }
    else if (Transaction.isSplitPart(transaction)) {
      Glob initialTransaction = repository.findLinkTarget(transaction, Transaction.SPLIT_SOURCE);
      splittedTransactions.add(initialTransaction);
      splittedTransactions.addAll(repository.findLinkedTo(initialTransaction, Transaction.SPLIT_SOURCE));
    }
    return splittedTransactions;
  }

  private String stringifyNumber(double value, GlobRepository globRepository) {
    Glob globForNumber = GlobBuilder.init(Transaction.TYPE).set(Transaction.AMOUNT, value).get();
    return amountStringifier.toString(globForNumber, globRepository);
  }

  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Glob transaction = (Glob)value;
    amount.setText(amountStringifier.toString(transaction, repository));
    updateColor(amount, Color.WHITE, Color.BLACK, isSelected);
    updateTotalAmount(transaction, isSelected);
    rendererColors.setTransactionBackground(panel, isSelected, row);
    return panel;
  }

}
