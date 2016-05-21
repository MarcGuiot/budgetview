package com.budgetview.gui.transactions.columns;

import com.budgetview.model.Transaction;
import com.budgetview.shared.utils.Amounts;
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
  private String selectionBgKey;
  private String evenRowBgKey;
  private String oddRowsBgKey;
  private String transactionTextKey;
  private String transactionTextPositiveKey;
  private String transactionTextNegativeKey;
  private String transactionSelectedTextKey;
  private String transactionPlannedTextKey;
  private String transactionLinkTextKey;
  private String transactionErrorTextKey;
  private String transactionReconciliationKey;
  private String splitSourceKey;
  private String splitChildKey;

  public enum Mode {
    DEFAULT,
    POSITIVE,
    NEGATIVE
  }

  public TransactionRendererColors(String selectionBg, String evenRowBg, String oddRowBg, String text, String positiveText, String negativeText, String selectedText, String plannedText, String linkText, String errorText, String reconciliation, String splitSource, String splitChild, Directory directory) {
    colorService = directory.get(ColorService.class);
    colorService.addListener(this);
    this.selectionBgKey = selectionBg;
    this.evenRowBgKey = evenRowBg;
    this.oddRowsBgKey = oddRowBg;
    this.transactionTextKey = text;
    this.transactionTextPositiveKey = positiveText;
    this.transactionTextNegativeKey = negativeText;
    this.transactionSelectedTextKey = selectedText;
    this.transactionPlannedTextKey = plannedText;
    this.transactionLinkTextKey = linkText;
    this.transactionErrorTextKey = errorText;
    this.transactionReconciliationKey = reconciliation;
    this.splitSourceKey = splitSource;
    this.splitChildKey = splitChild;
  }

  public void colorsChanged(ColorLocator colorLocator) {
    selectionBgColor = colorLocator.get(selectionBgKey);
    evenRowsBgColor = colorLocator.get(evenRowBgKey);
    oddRowsBgColor = colorLocator.get(oddRowsBgKey);
    transactionTextColor = colorLocator.get(transactionTextKey);
    transactionTextPositiveColor = colorLocator.get(transactionTextPositiveKey);
    transactionTextNegativeColor = colorLocator.get(transactionTextNegativeKey);
    transactionSelectedTextColor = colorLocator.get(transactionSelectedTextKey);
    transactionPlannedTextColor = colorLocator.get(transactionPlannedTextKey);
    transactionLinkTextColor = colorLocator.get(transactionLinkTextKey);
    transactionErrorTextColor = colorLocator.get(transactionErrorTextKey);
    transactionReconciliationColor = colorLocator.get(transactionReconciliationKey);
    splitSourceColor = colorLocator.get(splitSourceKey);
    splitChildColor = colorLocator.get(splitChildKey);
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
