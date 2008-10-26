package org.designup.picsou.gui.transactions.columns;

import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionType;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.ImageLocator;
import org.globsframework.gui.views.LabelCustomizer;
import org.globsframework.model.Glob;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidParameter;

import javax.swing.*;
import java.awt.*;

public class TransactionTypeCustomizer implements LabelCustomizer {
  private Icon cashIcon;
  private Icon checkIcon;
  private Icon creditCardIcon;
  private Icon debitIcon;
  private Icon creditIcon;
  private Icon bankIcon;

  public TransactionTypeCustomizer(Directory directory) {
    ImageLocator locator = directory.get(ImageLocator.class);
    cashIcon = locator.get("cash.png");
    checkIcon = locator.get("check.png");
    creditCardIcon = locator.get("creditcard.png");
    debitIcon = locator.get("debit.png");
    creditIcon = locator.get("credit.png");
    bankIcon = locator.get("bank.png");
  }

  public void process(JLabel label, Glob transaction, boolean isSelected, boolean hasFocus, int row, int column) {
    Integer transactionTypeId = transaction.get(Transaction.TRANSACTION_TYPE);
    label.setText("");
    label.setHorizontalAlignment(JLabel.CENTER);
    label.setIcon(getIcon(transactionTypeId));
    label.setToolTipText(getName(transactionTypeId));
    label.setPreferredSize(new Dimension(28, 14));
  }

  private String getName(Integer transactionTypeId) {
    return Lang.get("transactionType." + TransactionType.getType(transactionTypeId).getName());
  }

  private Icon getIcon(Integer transactionTypeId) {
    if (transactionTypeId == null) {
      throw new InvalidParameter("Transaction type not defined");
    }
    switch (TransactionType.getType(transactionTypeId)) {
      case WITHDRAWAL:
        return cashIcon;
      case CHECK:
        return checkIcon;
      case CREDIT_CARD:
        return creditCardIcon;
      case VIREMENT:
      case DEPOSIT:
        return creditIcon;
      case PRELEVEMENT:
      case CREDIT:
        return debitIcon;
      case BANK_FEES:
        return bankIcon;
    }
    throw new InvalidParameter("Unknown transaction type: " + transactionTypeId);
  }
}
