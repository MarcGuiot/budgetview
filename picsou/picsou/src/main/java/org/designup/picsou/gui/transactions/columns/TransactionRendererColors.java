package org.designup.picsou.gui.transactions.columns;

import org.designup.picsou.gui.utils.PicsouColors;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.utils.directory.Directory;

import java.awt.*;

public class TransactionRendererColors implements ColorChangeListener {

  private Color selectionBgColor;
  private Color evenRowsBgColor;
  private Color oddRowsBgColor;
  private Color evenErrorBgColor;
  private Color oddErrorBgColor;
  private Color selectionErrorBgColor;
  private Color rolloverCategoryColor;
  private Color transactionTextColor;
  private Color transactionSelectedTextColor;
  private Color categoryColor;

  public TransactionRendererColors(Directory directory) {
    ColorService colorService = directory.get(ColorService.class);
    colorService.addListener(this);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    selectionBgColor = colorLocator.get(PicsouColors.TRANSACTION_SELECTED_BG);
    evenRowsBgColor = colorLocator.get(PicsouColors.TRANSACTION_EVEN_ROWS_BG);
    oddRowsBgColor = colorLocator.get(PicsouColors.TRANSACTION_ODD_ROWS_BG);
    selectionErrorBgColor = colorLocator.get(PicsouColors.TRANSACTION_SELECTED_ERROR_BG);
    evenErrorBgColor = colorLocator.get(PicsouColors.TRANSACTION_EVEN_ERROR_BG);
    oddErrorBgColor = colorLocator.get(PicsouColors.TRANSACTION_ODD_ERROR_BG);
    rolloverCategoryColor = colorLocator.get(PicsouColors.ROLLOVER_CATEGORY_LABEL);
    categoryColor = colorLocator.get(PicsouColors.CATEGORY_LABEL);
    transactionTextColor = colorLocator.get(PicsouColors.TRANSACTION_TEXT);
    transactionSelectedTextColor = colorLocator.get(PicsouColors.TRANSACTION_SELECTED_TEXT);
  }

  public Color getEvenErrorBgColor() {
    return evenErrorBgColor;
  }

  public Color getEvenRowsBgColor() {
    return evenRowsBgColor;
  }

  public Color getOddErrorBgColor() {
    return oddErrorBgColor;
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

  public Color getSelectionBgColor() {
    return selectionBgColor;
  }

  public Color getSelectionErrorBgColor() {
    return selectionErrorBgColor;
  }

  public Color getTransactionTextColor() {
    return transactionTextColor;
  }

  public Color getTransactionSelectedTextColor() {
    return transactionSelectedTextColor;
  }

  public void setTransactionBackground(Component component, boolean isSelected, int row) {
    if (isSelected) {
      component.setBackground(getSelectionBgColor());
    }
    else {
      component.setBackground((row % 2) == 0 ? getEvenRowsBgColor() : getOddRowsBgColor());
    }
  }
}
