package org.designup.picsou.functests.checkers.converters;

import org.designup.picsou.functests.checkers.TransactionChecker;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.components.PicsouFrame;
import org.designup.picsou.gui.description.CategoryComparator;
import org.designup.picsou.gui.description.CategoryStringifier;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionToCategory;
import org.designup.picsou.model.TransactionType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.uispec4j.Button;
import org.uispec4j.TableCellValueConverter;
import org.uispec4j.UIComponent;
import org.uispec4j.Window;

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

  public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {

    StringBuilder builder = new StringBuilder();
    Glob transaction = (Glob)modelObject;
    Integer transactionType = transaction.get(Transaction.TRANSACTION_TYPE);
    builder.append("(");
    builder.append(TransactionType.getType(transactionType).getName());
    builder.append(")");

    org.uispec4j.Panel panel = new org.uispec4j.Panel((JPanel)renderedComponent);
    Button hyperlink = panel.getButton();
    String text = hyperlink.getLabel();
    if (text.equals(TransactionChecker.TO_CATEGORIZE)) {
      builder.append(TransactionChecker.TO_CATEGORIZE);
    }
    else {
      builder.append(text);
    }
    return builder.toString().trim();
  }
}
