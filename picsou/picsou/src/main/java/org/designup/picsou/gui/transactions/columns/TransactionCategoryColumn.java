package org.designup.picsou.gui.transactions.columns;

import org.designup.picsou.gui.transactions.categorization.CategoryChooserAction;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionToCategory;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.components.HyperlinkButton;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class TransactionCategoryColumn extends AbstractTransactionEditor {
  private GlobStringifier categoryStringifier;
  private HyperlinkButton rendererButton;
  private JPanel rendererPanel;
  private HyperlinkButton editorButton;
  private JPanel editorPanel;
  private CategoryChooserAction categoryChooserAction;
  private Glob transaction;

  public TransactionCategoryColumn(CategoryChooserAction categoryChooserAction, GlobTableView view,
                                   TransactionRendererColors transactionRendererColors,
                                   DescriptionService descriptionService,
                                   GlobRepository repository, Directory directory) {
    super(view, transactionRendererColors, descriptionService, repository, directory);
    this.categoryChooserAction = categoryChooserAction;

    categoryStringifier = descriptionService.getStringifier(Category.TYPE);
    rendererPanel = new JPanel();
    rendererPanel.setLayout(new BoxLayout(rendererPanel, BoxLayout.X_AXIS));
    rendererButton = new HyperlinkButton(new ForwardAction());
    rendererButton.setOpaque(false);
    rendererPanel.add(rendererButton);
    rendererPanel.add(Box.createHorizontalGlue());
    editorPanel = new JPanel();
    editorPanel.setLayout(new BoxLayout(editorPanel, BoxLayout.X_AXIS));
    editorPanel.getLayout();
    editorButton = new HyperlinkButton(new ForwardAction());
    editorButton.setOpaque(false);
    editorPanel.add(editorButton);
    editorPanel.add(Box.createHorizontalGlue());
  }

  protected Component getComponent(final Glob transaction, boolean render) {
    HyperlinkButton button;
    JPanel panel;
    if (render) {
      button = this.rendererButton;
      panel = this.rendererPanel;
    }
    else {
      this.transaction = transaction;
      button = this.editorButton;
      panel = this.editorPanel;
    }
    boolean categoryNone = Transaction.hasNoCategory(transaction);
    boolean multiCategorized = TransactionToCategory.hasCategories(transaction, repository);
    if (!categoryNone && !multiCategorized) {
      button.setForeground(Color.BLUE);
      Glob category = repository.get(Key.create(Category.TYPE, transaction.get(Transaction.CATEGORY)));
      button.setText(categoryStringifier.toString(category, repository));
    }
    else {
      button.setForeground(Color.RED);
      button.setText(Lang.get("category.assignement.required"));
    }
    rendererColors.setTransactionBackground(panel, isSelected, row);
    return panel;
  }

  class ForwardAction extends AbstractAction {

    public void actionPerformed(ActionEvent e) {
      openCategoryChooser(transaction);
    }
  }

  private void openCategoryChooser(Glob transaction) {
    tableView.getComponent().requestFocus();
    selectTransactionIfNeeded(transaction);
    categoryChooserAction.actionPerformed(null);
  }

  private void selectTransactionIfNeeded(Glob transaction) {
    GlobList selection = tableView.getCurrentSelection();
    if (!selection.contains(transaction)) {
      tableView.select(transaction);
    }
  }

}
