package org.designup.picsou.gui.transactions;

import org.crossbowlabs.globs.gui.views.GlobTableView;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.model.format.GlobStringifier;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionToCategory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

class TransactionCategoryColumn extends AbstractTransactionEditor {
  private GlobStringifier categoryStringifier;

  private boolean isCategoryNone;
  private boolean isMultiCategorized;

  private Icon addIcon;
  private Icon addRolloverIcon;
  private Icon selectIcon;
  private Icon selectRolloverIcon;
  private CategoryChooserAction categoryChooserAction;

  TransactionCategoryColumn(CategoryChooserAction action, GlobTableView view,
                            TransactionRendererColors transactionRendererColors,
                            DescriptionService descriptionService,
                            GlobRepository repository, Directory directory) {
    super(view, transactionRendererColors, descriptionService, repository, directory);
    categoryChooserAction = action;
    categoryStringifier = descriptionService.getStringifier(Category.TYPE);
    addIcon = iconLocator.get("add.png");
    addRolloverIcon = iconLocator.get("addrollover.png");
    selectIcon = iconLocator.get("select.png");
    selectRolloverIcon = iconLocator.get("selectrollover.png");
  }

  protected Component getComponent(Glob transaction) {
    isCategoryNone = Transaction.hasNoCategory(transaction);
    isMultiCategorized = TransactionToCategory.hasCategories(transaction, repository);
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    addCategorySelector(panel, transaction);
    if (!isCategoryNone || isMultiCategorized) {
      addCategoriesToPanel(panel, transaction);
    }
    else {
      panel.add(createLabel(" ", Color.WHITE, Color.BLACK));
    }
    if (isCategoryNone || isMultiCategorized) {
      if (isSelected) {
        panel.setBackground(rendererColors.getSelectionErrorBgColor());
      }
      else {
        panel.setBackground((row % 2) == 0 ?
                            rendererColors.getEvenErrorBgColor() :
                            rendererColors.getOddErrorBgColor());
      }
    }
    return panel;
  }

  private void addCategorySelector(JPanel panel, final Glob transaction) {
    final JButton chooseCategoryButton = new JButton();
    chooseCategoryButton.setAction(new AbstractAction() {
      public void actionPerformed(ActionEvent event) {
        tableView.getComponent().requestFocus();
        selectTransactionIfNeeded(transaction);
        categoryChooserAction.actionPerformed(event);
      }
    });
    Gui.setIcons(chooseCategoryButton, addIcon, addRolloverIcon, addRolloverIcon);
    Gui.configureIconButton(chooseCategoryButton, "Add", new Dimension(13, 13));
    panel.add(chooseCategoryButton);
    panel.add(Box.createRigidArea(new Dimension(3, 0)));
  }

  private void selectTransactionIfNeeded(Glob transaction) {
    int[] selectedRows = tableView.getComponent().getSelectedRows();
    for (int selectedRow : selectedRows) {
      if (selectedRow == row) {
        return;
      }
    }
    selectionService.select(transaction);
  }

  private void addCategoriesToPanel(JPanel panel, final Glob transaction) {
    if (!isCategoryNone) {
      final Glob category = repository.get(Key.create(Category.TYPE, transaction.get(Transaction.CATEGORY)));
      addCategoryLabel(category, repository, panel);
    }
    else {
      GlobList categories = TransactionToCategory.getCategories(transaction, repository)
        .sort(categoryStringifier.getComparator(repository));
      for (final Glob category : categories) {
        final JLabel label = addCategoryLabel(category, repository, panel);
        Gui.setRolloverColor(label, rendererColors.getRolloverCategoryColor());
        label.addMouseListener(new MouseAdapter() {
          public void mouseReleased(MouseEvent e) {
            Transaction.setCategory(transaction, category, repository);
          }
        });
        panel.add(Box.createRigidArea(new Dimension(2, 0)));
        panel.add(createSelectButton(new AbstractAction() {
          public void actionPerformed(ActionEvent event) {
            Transaction.setCategory(transaction, category, repository);
          }
        }));
        panel.add(Box.createRigidArea(new Dimension(3, 0)));
      }
    }
    rendererColors.setTransactionBackground(panel, isSelected, row);
  }

  private JLabel addCategoryLabel(Glob category, GlobRepository globRepository, JPanel panel) {
    String categoryToDisplay = categoryStringifier.toString(category, globRepository);
    JLabel label = createLabel(categoryToDisplay, Color.WHITE, Color.BLACK);
    panel.add(label);
    return label;
  }

  private JButton createSelectButton(AbstractAction action) {
    JButton selectButton = new JButton(action);
    Gui.setIcons(selectButton,
                 selectIcon,
                 selectRolloverIcon,
                 selectRolloverIcon);
    Gui.configureIconButton(selectButton, "Select", new Dimension(13, 13));
    return selectButton;
  }
}
