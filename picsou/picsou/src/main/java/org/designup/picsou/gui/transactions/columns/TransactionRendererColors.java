package org.designup.picsou.gui.transactions.columns;

import com.budgetview.shared.utils.Amounts;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.views.CellPainter;
import org.globsframework.model.Glob;
import org.globsframework.utils.directory.Directory;

import java.awt.*;

public class TransactionRendererColors implements ColorChangeListener {

  private Color selectionBgColor;
  private Color evenRowsBgColor;
  private Color oddRowsBgColor;
  private Color transactionTextColor;
  private Color transactionTextPositiveColor;
  private Color transactionTextNegativeColor;
  private Color transactionSelectedTextColor;
  private Color transactionPlannedTextColor;
  private Color transactionLinkTextColor;
  private Color transactionErrorTextColor;
  private Color transactionReconciliationColor;
  private Color splitSourceColor;
  private Color splitChildColor;
  private ColorService colorService;
  private Integer splitGroupSourceId;

  public enum Mode {
    DEFAULT,
    POSITIVE,
    NEGATIVE
  }

  public TransactionRendererColors(Directory directory) {
    colorService = directory.get(ColorService.class);
    colorService.addListener(this);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    selectionBgColor = colorLocator.get("transactionTable.selected.bg");
    evenRowsBgColor = colorLocator.get("transactionTable.rows.even.bg");
    oddRowsBgColor = colorLocator.get("transactionTable.rows.odd.bg");
    transactionTextColor = colorLocator.get("transactionTable.text");
    transactionTextPositiveColor = colorLocator.get("transactionTable.text.positive");
    transactionTextNegativeColor = colorLocator.get("transactionTable.text.negative");
    transactionSelectedTextColor = colorLocator.get("transactionTable.text.selected");
    transactionPlannedTextColor = colorLocator.get("transactionTable.text.planned");
    transactionLinkTextColor = colorLocator.get("transactionTable.text.link");
    transactionErrorTextColor = colorLocator.get("transactionTable.text.error");
    transactionReconciliationColor = colorLocator.get("transactionTable.reconciliation");
    splitSourceColor = colorLocator.get("transactionTable.split.source.bg");
    splitChildColor = colorLocator.get("transactionTable.split.bg");
  }

  public Color getEvenRowsBgColor() {
    return evenRowsBgColor;
  }

  public Color getOddRowsBgColor() {
    return oddRowsBgColor;
  }

  public Color getTransactionTextColor() {
    return transactionTextColor;
  }

  public Color getTransactionSelectedTextColor() {
    return transactionSelectedTextColor;
  }

  public Color getTransactionPlannedTextColor() {
    return transactionPlannedTextColor;
  }

  public Color getTransactionErrorTextColor() {
    return transactionErrorTextColor;
  }

  public Color getTransactionReconciliationColor() {
    return transactionReconciliationColor;
  }

  public void update(Component component, boolean isSelected, Glob transaction, Mode mode, int row) {
    setForeground(component, isSelected, transaction, mode, false);
    setBackground(component, transaction, isSelected, row);
  }

  public void setForeground(Component component, boolean isSelected, Glob transaction, Mode mode, boolean isLink) {
    component.setForeground(getForeground(transaction, mode, isSelected, isLink));
  }

  private Color getForeground(Glob transaction, Mode mode, boolean isSelected, boolean isLink) {
    if (isSelected) {
      return transactionSelectedTextColor;
    }
    if (Transaction.isPlanned(transaction)) {
      return transactionPlannedTextColor;
    }
    if (isLink) {
      return transactionLinkTextColor;
    }
    switch (mode) {
      case POSITIVE:
        return transactionTextPositiveColor;
      case NEGATIVE:
        return transactionTextNegativeColor;
      case DEFAULT:
        // falls through
      default:
        return transactionTextColor;
    }
  }

  public void setBackground(Component component, Glob transaction, boolean isSelected, int row) {
    component.setBackground(getBackgroundColor(transaction, isSelected, row));
  }

  public CellPainter getBackgroundPainter() {
    return new CellPainter() {
      public void paint(Graphics g, Glob transaction, int row, int column, boolean isSelected, boolean hasFocus, int width, int height) {
        g.setColor(getBackgroundColor(transaction, isSelected, row));
        g.fillRect(0, 0, width, height);
      }
    };
  }

  private Color getBackgroundColor(Glob transaction, boolean isSelected, int row) {
    if (isSelected) {
      return selectionBgColor;
    }
    else if ((splitGroupSourceId != null)
             && (transaction != null)
             && transaction.get(Transaction.ID).equals(splitGroupSourceId)) {
      return splitSourceColor;
    }
    else if ((splitGroupSourceId != null)
             && (transaction != null)
             && splitGroupSourceId.equals(transaction.get(Transaction.SPLIT_SOURCE))) {
      return splitChildColor;
    }
    else {
      return (row % 2) == 0 ? getEvenRowsBgColor() : getOddRowsBgColor();
    }
  }

  public void setSplitGroupSourceId(Integer splitGroupSourceId) {
    this.splitGroupSourceId = splitGroupSourceId;
  }

  public void dispose() {
    colorService.removeListener(this);
  }

  public static TransactionRendererColors.Mode getMode(Double amount) {
    TransactionRendererColors.Mode mode;
    if (Amounts.isNullOrZero(amount)) {
      mode = TransactionRendererColors.Mode.DEFAULT;
    }
    else if (amount < 0) {
      mode = TransactionRendererColors.Mode.NEGATIVE;
    }
    else {
      mode = TransactionRendererColors.Mode.POSITIVE;
    }
    return mode;
  }
}
