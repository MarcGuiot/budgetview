package org.designup.picsou.gui.transactions.columns;

import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
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
  private Color categoryColor;
  private ColorService colorService;

  public TransactionRendererColors(Directory directory) {
    colorService = directory.get(ColorService.class);
    colorService.addListener(this);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    selectionBgColor = colorLocator.get(PicsouColors.TRANSACTION_SELECTED_BG);
    evenRowsBgColor = colorLocator.get(PicsouColors.TRANSACTION_EVEN_ROWS_BG);
    oddRowsBgColor = colorLocator.get(PicsouColors.TRANSACTION_ODD_ROWS_BG);
    rolloverCategoryColor = colorLocator.get(PicsouColors.ROLLOVER_CATEGORY_LABEL);
    categoryColor = colorLocator.get(PicsouColors.CATEGORY_LABEL);
    transactionTextColor = colorLocator.get(PicsouColors.TRANSACTION_TEXT);
    transactionSelectedTextColor = colorLocator.get(PicsouColors.TRANSACTION_SELECTED_TEXT);
    transactionPlannedTextColor = colorLocator.get(PicsouColors.TRANSACTION_TEXT_PLANNED);
    transactionLinkTextColor = colorLocator.get(PicsouColors.TRANSACTION_TEXT_LINK);
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

  public Color getTransactionSelectedTextColor() {
    return transactionSelectedTextColor;
  }

  public Color getTransactionPlannedTextColor() {
    return transactionPlannedTextColor;
  }

  public void update(Component component, boolean isSelected, Glob transaction, int row) {
    setForeground(component, isSelected, transaction, false);
    setBackground(component, isSelected, row);
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

  public void setBackground(Component component, boolean isSelected, int row) {
    if (isSelected) {
      component.setBackground(selectionBgColor);
    }
    else {
      component.setBackground((row % 2) == 0 ? getEvenRowsBgColor() : getOddRowsBgColor());
    }
  }

  protected void finalize() throws Throwable {
    super.finalize();
    colorService.removeListener(this);
  }
}
