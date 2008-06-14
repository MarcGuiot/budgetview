package org.designup.picsou.gui.transactions;

import org.designup.picsou.gui.categories.*;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.IconLocator;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.layout.Anchor;
import org.globsframework.gui.splits.layout.Fill;
import org.globsframework.gui.splits.layout.GridBagBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class CategoryChooserDialog implements ChangeSetListener {
  private GlobList selectedTransactions = GlobList.EMPTY;
  private HashMap<Integer, JLabel> categoryIdToJLabel = new HashMap<Integer, JLabel>();
  private ArrayList<JLabel> selectedCategories = new ArrayList<JLabel>();
  private CategoryChooserCallback callback;
  private TransactionRendererColors colors;
  private GlobRepository repository;
  private GlobStringifier stringifier;
  private PicsouDialog dialog;
  private JFrame mainFrame;
  private GlobList firstPartCategories = new GlobList();
  private GlobList secondPartCategories = new GlobList();
  private GlobList thirdPartCategories = new GlobList();
  private boolean needToRebuild = true;
  private CreateCategoryAction addCategoryAction;
  private DeleteCategoryAction deleteCategoryAction;
  private RenameCategoryAction renameCategoryAction;
  private ColorService colorService;
  private IconLocator iconLocator;
  private SelectionService selectionService;
  private JPanel contentPanel;
  protected Font masterFont;

  public CategoryChooserDialog(CategoryChooserCallback callback, TransactionRendererColors colors,
                               GlobRepository repository, Directory directory) {
    this.callback = callback;
    this.colors = colors;

    Directory localDirectory = new DefaultDirectory(directory);
    selectionService = new SelectionService();
    localDirectory.add(selectionService);

    this.repository = repository;

    this.masterFont = Gui.DEFAULT_TABLE_FONT_BOLD.deriveFont(14.0f);

    this.stringifier = localDirectory.get(DescriptionService.class).getStringifier(Category.TYPE);
    repository.addChangeListener(this);
    iconLocator = localDirectory.get(IconLocator.class);
    colorService = localDirectory.get(ColorService.class);
    addCategoryAction = new CreateCategoryAction(repository, localDirectory) {
      public JDialog getDialog(ActionEvent e) {
        return PicsouDialog.create(dialog);
      }
    };
    deleteCategoryAction = new DeleteCategoryAction(repository, localDirectory);
    renameCategoryAction = new RenameCategoryAction(repository, localDirectory) {
      public JDialog getDialog(ActionEvent e) {
        return PicsouDialog.create(dialog);
      }
    };
    mainFrame = localDirectory.get(JFrame.class);
  }

  public void show(GlobList selectedTransactions) {
    dialog = PicsouDialog.create(mainFrame, Lang.get("choose.category.title"));
    if (contentPanel == null || needToRebuild) {
      contentPanel = prepareDialogContent();
      needToRebuild = false;
    }
    dialog.getContentPane().add(contentPanel);
    dialog.pack();
    this.selectedTransactions = selectedTransactions;
    setSelectedCategories();

//    ColorServiceEditor.showInFrame(colorService, dialog);

    GuiUtils.showCentered(dialog);
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(Category.TYPE)) {
      if (dialog.isVisible()) {
        dialog.getContentPane().removeAll();
        dialog.getContentPane().add(prepareDialogContent());
        dialog.pack();
        setSelectedCategories();
      }
      else {
        needToRebuild = true;
      }
    }
  }

  public void globsReset(GlobRepository repository, java.util.List<GlobType> changedTypes) {
    needToRebuild = true;
  }

  private JPanel prepareDialogContent() {
    categoryIdToJLabel.clear();
    firstPartCategories.clear();
    secondPartCategories.clear();
    thirdPartCategories.clear();

    splitCategories();

    SplitsBuilder splitsBuilder = new SplitsBuilder(colorService, iconLocator);
    splitsBuilder.add("first", getCategoriesPanel(firstPartCategories));
    splitsBuilder.add("second", getCategoriesPanel(secondPartCategories));
    splitsBuilder.add("third", getCategoriesPanel(thirdPartCategories));
    splitsBuilder.add("close", new AbstractAction(Lang.get("close")) {
      public void actionPerformed(ActionEvent e) {
        close();
      }
    });

    return (JPanel)splitsBuilder.parse(getClass(), "/layout/categoryChooser.splits");
  }

  private JPanel getCategoriesPanel(GlobList categories) {
    GridBagBuilder bagBuilder = GridBagBuilder.init().setOpaque(false);
    int y = 0;
    for (Glob category : categories) {
      addCategoryPanel(bagBuilder, y++, category);
    }
    return bagBuilder.getPanel();
  }

  private void setSelectedCategories() {
    HashSet<Integer> categoryIdsForSelectedTransactions = new HashSet<Integer>();

    for (Glob transaction : selectedTransactions) {
      GlobList categories = Transaction.getCategories(transaction, repository);
      if (categories.size() == 0) {
        categoryIdsForSelectedTransactions.add(Category.NONE);
      }
      for (Glob category : categories) {
        categoryIdsForSelectedTransactions.add(category.get(Category.ID));
      }
    }

    unselectAllCategories();

    for (Integer categoryId : categoryIdsForSelectedTransactions) {
      JLabel label = categoryIdToJLabel.get(categoryId);
      label.setForeground(colors.getRolloverCategoryColor());
      selectedCategories.add(label);
    }
  }

  private void unselectAllCategories() {
    for (JLabel label : selectedCategories) {
      label.setForeground(colors.getCategoryColor());
    }
    selectedCategories.clear();
  }

  private void addCategoryPanel(GridBagBuilder builder, int y, final Glob category) {
    boolean isMaster = Category.isMaster(category);
    int leftMargin = isMaster ? 2 : 20;
    int topMargin = isMaster ? 20 : 0;
    final JPanel panel = Gui.createHorizontalBoxLayoutPanel();

    final JLabel label = createCategoryLabel(panel, category);
    panel.add(Box.createRigidArea(new Dimension(2, 0)));

    final CategoryButtonsPanel buttonsPanel = new CategoryButtonsPanel(category,
                                                                       label,
                                                                       panel,
                                                                       addCategoryAction,
                                                                       renameCategoryAction,
                                                                       deleteCategoryAction,
                                                                       selectionService);
    panel.add(buttonsPanel.getPanel());

    builder.add(panel, 0, y, 1, 1, 1, 0, Fill.NONE, Anchor.NORTHWEST, new Insets(topMargin, leftMargin, 0, 0));
  }

  private JLabel createCategoryLabel(JPanel panel, final Glob category) {
    String categoryName = stringifier.toString(category, repository);
    final JLabel label = new JLabel(categoryName);
    label.setName("label." + categoryName);
    label.setFont(Category.isMaster(category) ? masterFont : Gui.DEFAULT_TABLE_FONT);
    label.setHorizontalAlignment(JLabel.LEFT);

    categoryIdToJLabel.put(category.get(Category.ID), label);

    label.addMouseListener(new MouseAdapter() {
      public void mouseEntered(MouseEvent e) {
        selectionService.select(category);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.setForeground(colors.getRolloverCategoryColor());
      }

      public void mouseExited(MouseEvent e) {
        label.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        label.setForeground(selectedCategories.contains(label) ?
                            colors.getRolloverCategoryColor() :
                            colors.getCategoryColor());
      }

      public void mouseReleased(MouseEvent e) {
        close();
        callback.categorySelected(category);
      }
    });

    panel.add(label);
    return label;
  }

  private void close() {
    dialog.setVisible(false);
  }

  private void splitCategories() {
    GlobList allCategories = repository.getAll(Category.TYPE).sort(new CategoryComparator(repository, stringifier));
    int categoryCountByColumn = allCategories.size() / 3;
    int firstPartCount;
    int secondPartCount;
    boolean forceSecondPart = false;
    boolean forceThirdPart = false;
    for (Glob category : allCategories) {
      if (category.get(Category.ID).equals(Category.ALL)) {
        continue;
      }
      firstPartCount = firstPartCategories.size();
      if (firstPartCount < categoryCountByColumn) {
        firstPartCategories.add(category);
        continue;
      }
      if (forceSecondPart) {
        secondPartCount = secondPartCategories.size();
        if (secondPartCount < categoryCountByColumn) {
          secondPartCategories.add(category);
          continue;
        }
        if (forceThirdPart) {
          thirdPartCategories.add(category);
          continue;
        }
        if (!Category.isMaster(category)) {
          secondPartCategories.add(category);
        }
        else {
          thirdPartCategories.add(category);
          forceThirdPart = true;
        }
        continue;
      }
      if (!Category.isMaster(category)) {
        firstPartCategories.add(category);
      }
      else {
        secondPartCategories.add(category);
        forceSecondPart = true;
      }
    }
    firstPartCategories.sort(new CategoryComparator(repository, stringifier));
    secondPartCategories.sort(new CategoryComparator(repository, stringifier));
    thirdPartCategories.sort(new CategoryComparator(repository, stringifier));
  }
}
