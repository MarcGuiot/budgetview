package org.designup.picsou.gui.transactions.split;

import org.designup.picsou.gui.transactions.columns.AbstractTransactionEditor;
import org.designup.picsou.gui.transactions.columns.TransactionRendererColors;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.utils.Icons;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.model.*;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.DefaultChangeSetVisitor;
import org.globsframework.model.utils.GlobUtils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

class DeleteSplitTransactionColumn extends AbstractTransactionEditor {
  DeleteSplitTransactionColumn(final Glob initialTransaction, GlobTableView view, TransactionRendererColors transactionRendererColors,
                               DescriptionService descriptionService, GlobRepository repository, Directory directory) {
    super(view, transactionRendererColors, descriptionService, repository, directory);
    repository.addTrigger(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
        if (changeSet.containsChanges(Transaction.TYPE)) {
          changeSet.safeVisit(new DefaultChangeSetVisitor() {
            public void visitDeletion(Key key, FieldValues values) throws Exception {
              GlobUtils.add(initialTransaction, Transaction.AMOUNT, values.get(Transaction.AMOUNT), repository);
            }
          });
        }
      }
    });
  }

  protected Component getComponent(Glob transaction, boolean render) {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.add(Box.createRigidArea(new Dimension(3, 0)));
    addDeleteButton(panel, transaction);
    panel.add(Box.createRigidArea(new Dimension(3, 0)));
    rendererColors.setBackground(panel, transaction, isSelected, row);
    return panel;
  }

  private void addDeleteButton(JPanel panel, final Glob transaction) {
    final JButton deleteButton = new JButton();
    deleteButton.setAction(new AbstractAction() {
      public void actionPerformed(ActionEvent event) {
        tableView.getComponent().requestFocus();
        selectionService.select(transaction);
        repository.delete(transaction.getKey());
      }

      public boolean isEnabled() {
        return Transaction.isSplitPart(transaction);
      }
    });
    Gui.setIcons(deleteButton, Icons.DELETE_ICON, Icons.DELETE_ROLLOVER_ICON, Icons.DELETE_ROLLOVER_ICON);
    Gui.configureIconButton(deleteButton, "Delete", new Dimension(13, 13));
    panel.add(deleteButton);
  }
}
