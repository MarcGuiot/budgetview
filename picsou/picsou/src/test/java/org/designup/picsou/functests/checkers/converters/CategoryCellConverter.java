package org.designup.picsou.functests.checkers.converters;

import org.uispec4j.*;
import org.uispec4j.Window;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.GlobList;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionType;
import org.designup.picsou.model.TransactionToCategory;
import org.designup.picsou.gui.utils.PicsouFrame;
import org.designup.picsou.gui.utils.CategoryStringifier;
import org.designup.picsou.gui.categories.CategoryComparator;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.functests.checkers.TransactionChecker;

import javax.swing.*;
import java.awt.*;

public class CategoryCellConverter implements TableCellValueConverter {
  private GlobRepository repository;
  private CategoryStringifier categoryStringifier = new CategoryStringifier();
  private CategoryComparator categoryComparator;

  public CategoryCellConverter(Window window) {
    Container container = window.getAwtComponent();
    if (container instanceof PicsouFrame) {
      PicsouFrame frame = (PicsouFrame)container;
      this.repository = frame.getRepository();
    }
    else if (container instanceof PicsouDialog) {
      PicsouFrame frame = (PicsouFrame)container.getParent();
      this.repository = frame.getRepository();
    }
    this.categoryComparator = new CategoryComparator(repository, categoryStringifier);
  }

  public Object getValue(int row, int column,
                         Component renderedComponent, Object modelObject) {
    org.uispec4j.Panel panel = new org.uispec4j.Panel((JPanel)renderedComponent);
    UIComponent[] categoryLabels = panel.getUIComponents(TextBox.class);

    StringBuilder builder = new StringBuilder();
    Glob transaction = (Glob)modelObject;
    Integer transactionType = transaction.get(Transaction.TRANSACTION_TYPE);
    builder.append("(");
    builder.append(TransactionType.getType(transactionType).getName());
    builder.append(")");
    
    if (categoryLabels.length == 1) {
      TextBox label = (TextBox)categoryLabels[0];
      String text = label.getText();
      if (text.equals(TransactionChecker.TO_CATEGORIZE)) {
        stringifyCategories(transaction, builder);
      }
      else {
        builder.append(text);
      }
    }
    return builder.toString().trim();
  }

  private void stringifyCategories(Glob transaction, StringBuilder builder) {
    GlobList categories = TransactionToCategory.getCategories(transaction, repository);
    if (categories.isEmpty()) {
      builder.append(TransactionChecker.TO_CATEGORIZE);
      return ;
    }

    categories.sort(categoryComparator);
    int index = 0;
    for (Glob category : categories) {
      if (index++ > 0) {
        builder.append(", ");
      }
      builder.append(categoryStringifier.toString(category, repository));
    }
  }
}
