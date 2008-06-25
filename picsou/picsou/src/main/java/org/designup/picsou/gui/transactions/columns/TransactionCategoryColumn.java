package org.designup.picsou.gui.transactions.columns;

import org.designup.picsou.gui.transactions.categorization.CategoryChooserAction;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionToCategory;
import org.designup.picsou.utils.Lang;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TransactionCategoryColumn extends AbstractTransactionEditor {
  private GlobStringifier categoryStringifier;

  private boolean isCategoryNone;

  private Icon addIcon;
  private Icon addRolloverIcon;
  private Icon selectIcon;
  private Icon selectRolloverIcon;
  private CategoryChooserAction categoryChooserAction;

  public TransactionCategoryColumn(CategoryChooserAction action, GlobTableView view,
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

  protected Component getComponent(final Glob transaction) {
    isCategoryNone = Transaction.hasNoCategory(transaction);
    boolean multiCategorized = TransactionToCategory.hasCategories(transaction, repository);
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    addCategorySelector(panel, transaction);
    if (!isCategoryNone && !multiCategorized) {
      Glob category = repository.get(Key.create(Category.TYPE, transaction.get(Transaction.CATEGORY)));
      addCategoryLabel(category, repository, panel);
      rendererColors.setTransactionBackground(panel, isSelected, row);
    }
    else {
      JLabel label = createLabel(Lang.get("category.assignement.required"), Color.RED, Color.RED);
      panel.add(label);
      Gui.setRolloverColor(label, rendererColors.getRolloverCategoryColor());
      label.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          openCategoryChooser(transaction);
        }
      });
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
        openCategoryChooser(transaction);
      }
    });
    Gui.setIcons(chooseCategoryButton, addIcon, addRolloverIcon, addRolloverIcon);
    Gui.configureIconButton(chooseCategoryButton, "Add", new Dimension(13, 13));
    panel.add(chooseCategoryButton);
    panel.add(Box.createRigidArea(new Dimension(3, 0)));
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

  private void addCategoriesToPanel(JPanel panel, final Glob transaction) {
    if (!isCategoryNone) {
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
