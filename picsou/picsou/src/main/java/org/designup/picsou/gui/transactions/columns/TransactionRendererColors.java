package org.designup.picsou.gui.transactions.columns;

import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionType;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.model.Glob;
import org.globsframework.utils.Utils;
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
  private Color categoryColor;

  public TransactionRendererColors(Directory directory) {
    ColorService colorService = directory.get(ColorService.class);
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

  public Color getSelectionBgColor() {
    return selectionBgColor;
  }

  public void update(Component component, boolean isSelected, Glob transaction, int row) {
    setForeground(component, isSelected, transaction);
    setBackground(component, isSelected, row);
  }

  public void setForeground(Component component, boolean isSelected, Glob transaction) {
    component.setForeground(getForeground(transaction, isSelected));
  }

  private Color getForeground(Glob transaction, boolean isSelected) {
    if (isSelected) {
      return transactionSelectedTextColor;
    }
    if (Transaction.isPlanned(transaction)) {
      return transactionPlannedTextColor;
    }
    return transactionTextColor;
  }

  public void setBackground(Component component, boolean isSelected, int row) {
    if (isSelected) {
      component.setBackground(getSelectionBgColor());
    }
    else {
      component.setBackground((row % 2) == 0 ? getEvenRowsBgColor() : getOddRowsBgColor());
    }
  }

}
