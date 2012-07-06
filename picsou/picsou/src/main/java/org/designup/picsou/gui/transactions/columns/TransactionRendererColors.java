package org.designup.picsou.gui.transactions.columns;

import org.designup.picsou.gui.utils.ApplicationColors;
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
  private Color rolloverCategoryColor;
  private Color transactionTextColor;
  private Color transactionSelectedTextColor;
  private Color transactionPlannedTextColor;
  private Color transactionLinkTextColor;
  private Color transactionErrorTextColor;
  private Color transactionReconciliationColor;
  private Color splitSourceColor;
  private Color splitChildColor;
  private Color categoryColor;
  private ColorService colorService;
  private Integer splitGroupSourceId;

  public TransactionRendererColors(Directory directory) {
    colorService = directory.get(ColorService.class);
    colorService.addListener(this);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    selectionBgColor = colorLocator.get(ApplicationColors.TABLE_SELECTED_BG);
    evenRowsBgColor = colorLocator.get(ApplicationColors.TABLE_EVEN_ROWS_BG);
    oddRowsBgColor = colorLocator.get(ApplicationColors.TABLE_ODD_ROWS_BG);
    rolloverCategoryColor = colorLocator.get(ApplicationColors.CATEGORY_ROLLOVER_LABEL);
    categoryColor = colorLocator.get(ApplicationColors.CATEGORY_LABEL);
    transactionTextColor = colorLocator.get(ApplicationColors.TABLE_TEXT);
    transactionSelectedTextColor = colorLocator.get(ApplicationColors.TRANSACTION_SELECTED_TEXT);
    transactionPlannedTextColor = colorLocator.get(ApplicationColors.TRANSACTION_TEXT_PLANNED);
    transactionLinkTextColor = colorLocator.get(ApplicationColors.TRANSACTION_TEXT_LINK);
    transactionErrorTextColor = colorLocator.get(ApplicationColors.TABLE_TEXT_ERROR);
    transactionReconciliationColor = colorLocator.get(ApplicationColors.TRANSACTION_RECONCILIATION);
    splitSourceColor = colorLocator.get(ApplicationColors.TRANSACTION_SPLIT_SOURCE_BG);
    splitChildColor = colorLocator.get(ApplicationColors.TRANSACTION_SPLIT_BG);
  }

  public Color getEvenRowsBgColor() {
    return evenRowsBgColor;
  }

  public Color getOddRowsBgColor() {
    return oddRowsBgColor;
  }

  public Color getRolloverCategoryColor() {
    return rolloverCategoryColor;
  }

  public Color getCategoryColor() {
    return categoryColor;
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

  public void update(Component component, boolean isSelected, Glob transaction, int row) {
    setForeground(component, isSelected, transaction, false);
    setBackground(component, transaction, isSelected, row);
  }

  public void setForeground(Component component, boolean isSelected, Glob transaction, boolean isLink) {
    component.setForeground(getForeground(transaction, isSelected, isLink));
  }

  private Color getForeground(Glob transaction, boolean isSelected, boolean isLink) {
    if (isSelected) {
      return transactionSelectedTextColor;
    }
    if (Transaction.isPlanned(transaction)) {
      return transactionPlannedTextColor;
    }
    if (isLink) {
      return transactionLinkTextColor;
    }
    return transactionTextColor;
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

  public void dispose(){
    colorService.removeListener(this);
  }
}
