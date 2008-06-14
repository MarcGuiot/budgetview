package org.designup.picsou.gui.transactions;

import org.designup.picsou.gui.utils.PicsouColors;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.ColorSource;
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

  public void colorsChanged(ColorSource colorSource) {
    selectionBgColor = colorSource.get(PicsouColors.TRANSACTION_SELECTED_BG);
    evenRowsBgColor = colorSource.get(PicsouColors.TRANSACTION_EVEN_ROWS_BG);
    oddRowsBgColor = colorSource.get(PicsouColors.TRANSACTION_ODD_ROWS_BG);
    selectionErrorBgColor = colorSource.get(PicsouColors.TRANSACTION_SELECTED_ERROR_BG);
    evenErrorBgColor = colorSource.get(PicsouColors.TRANSACTION_EVEN_ERROR_BG);
    oddErrorBgColor = colorSource.get(PicsouColors.TRANSACTION_ODD_ERROR_BG);
    rolloverCategoryColor = colorSource.get(PicsouColors.ROLLOVER_CATEGORY_LABEL);
    categoryColor = colorSource.get(PicsouColors.CATEGORY_LABEL);
    transactionTextColor = colorSource.get(PicsouColors.TRANSACTION_TEXT);
    transactionSelectedTextColor = colorSource.get(PicsouColors.TRANSACTION_SELECTED_TEXT);
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
