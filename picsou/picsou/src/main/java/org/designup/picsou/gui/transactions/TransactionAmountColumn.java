package org.designup.picsou.gui.transactions;

import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class TransactionAmountColumn extends AbstractTransactionEditor {
  private GlobStringifier amountStringifier;
  private Icon splitOn;
  private Icon splitOff;
  private Icon splitRoll;
  private SplitTransactionAction splitTransactionAction;

  public TransactionAmountColumn(GlobTableView view, TransactionRendererColors transactionRendererColors,
                                 DescriptionService descriptionService, GlobRepository repository, Directory directory) {
    super(view, transactionRendererColors, descriptionService, repository, directory);
    amountStringifier = descriptionService.getStringifier(Transaction.AMOUNT);
    splitOn = iconLocator.get("split_on.png");
    splitOff = iconLocator.get("split_off.png");
    splitRoll = iconLocator.get("split_roll.png");
    splitTransactionAction = new SplitTransactionAction(repository, directory);
  }

  protected Component getComponent(Glob transaction) {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.add(Box.createHorizontalGlue());
    addAmount(transaction, repository, panel);
    panel.add(Box.createRigidArea(new Dimension(2, 0)));
    panel.add(createSplitButton(transaction));
    panel.add(Box.createRigidArea(new Dimension(3, 0)));
    rendererColors.setTransactionBackground(panel, isSelected, row);
    return panel;
  }

  private JButton createSplitButton(final Glob transaction) {
    JButton splitButton = new JButton();
    splitButton.setAction(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        tableView.getComponent().requestFocus();
        selectionService.select(transaction);
        splitTransactionAction.actionPerformed(e);
      }
    });
    if (Transaction.isSplit(transaction)) {
      Gui.setIcons(splitButton, splitOn, splitOn, splitOn);
    }
    else {
      Gui.setIcons(splitButton, splitOff, splitRoll, splitRoll);
    }
    Gui.configureIconButton(splitButton, "Split", new Dimension(13, 13));
    return splitButton;
  }

  private void addAmount(Glob transaction, GlobRepository globRepository, JPanel panel) {
    JLabel amount = createLabel(amountStringifier.toString(transaction, globRepository), Color.WHITE, Color.BLACK);
    amount.setName("amount");
    panel.add(amount);
    addTotalAmount(transaction, panel);
  }

  private void addTotalAmount(Glob transaction, JPanel panel) {
    GlobList splittedTransactions = getSplittedTransactions(transaction);
    if (!splittedTransactions.isEmpty()) {
      double total = 0;
      for (Glob glob : splittedTransactions) {
        total += glob.get(Transaction.AMOUNT);
      }
      String totalAmount = stringifyNumber(total, repository);
      panel.add(createLabel(" (" + totalAmount + ")", Color.LIGHT_GRAY, Color.GRAY));
    }
  }

  private GlobList getSplittedTransactions(Glob transaction) {
    GlobList splittedTransactions = new GlobList();
    if (Transaction.isSplitSource(transaction)) {
      splittedTransactions.add(transaction);
      splittedTransactions.addAll(repository.findLinkedTo(transaction, Transaction.SPLIT_SOURCE));
    }
    else if (Transaction.isSplitPart(transaction)) {
      Glob initialTransaction = repository.findLinkTarget(transaction, Transaction.SPLIT_SOURCE);
      splittedTransactions.add(initialTransaction);
      splittedTransactions.addAll(repository.findLinkedTo(initialTransaction, Transaction.SPLIT_SOURCE));
    }
    return splittedTransactions;
  }

  private String stringifyNumber(double value, GlobRepository globRepository) {
    Glob globForNumber = GlobBuilder.init(Transaction.TYPE).set(Transaction.AMOUNT, value).get();
    return amountStringifier.toString(globForNumber, globRepository);
  }
}
