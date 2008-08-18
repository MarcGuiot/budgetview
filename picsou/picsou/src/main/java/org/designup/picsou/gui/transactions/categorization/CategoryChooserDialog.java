package org.designup.picsou.gui.transactions.categorization;

import org.designup.picsou.gui.categories.actions.CreateCategoryAction;
import org.designup.picsou.gui.categories.actions.DeleteCategoryAction;
import org.designup.picsou.gui.categories.actions.RenameCategoryAction;
import org.designup.picsou.gui.categories.columns.CategoryButtonsPanel;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.description.CategoryComparator;
import org.designup.picsou.gui.transactions.columns.TransactionRendererColors;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.utils.PicsouMatchers;
import org.designup.picsou.model.Category;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.SplitsLoader;
import org.globsframework.gui.splits.layout.WrappedColumnLayout;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
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
import java.util.Set;

public class CategoryChooserDialog implements ChangeSetListener {
  private HashMap<Integer, JLabel> categoryIdToJLabel = new HashMap<Integer, JLabel>();
  private ArrayList<JLabel> selectedCategories = new ArrayList<JLabel>();
  private CategoryChooserCallback callback;
  private TransactionRendererColors colors;
  private Directory directory;
  private GlobRepository repository;
  private GlobStringifier stringifier;
  private PicsouDialog dialog;
  private CreateCategoryAction addCategoryAction;
  private DeleteCategoryAction deleteCategoryAction;
  private RenameCategoryAction renameCategoryAction;
  private SelectionService selectionService;
  protected Font masterFont;

  public CategoryChooserDialog(CategoryChooserCallback callback, TransactionRendererColors colors,
                               GlobRepository repository, Directory directory) {
    this.callback = callback;
    this.colors = colors;
    this.directory = directory;

    Directory localDirectory = new DefaultDirectory(directory);
    selectionService = new SelectionService();
    localDirectory.add(selectionService);

    this.repository = repository;

    this.masterFont = Gui.DEFAULT_TABLE_FONT_BOLD.deriveFont(14.0f);

    this.stringifier = localDirectory.get(DescriptionService.class).getStringifier(Category.TYPE);
    repository.addChangeListener(this);
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
    dialog = PicsouDialog.create(localDirectory.get(JFrame.class), Lang.get("choose.category.title"));
    loadDialogContent();
  }

  public void show() {
    setSelectedCategories();
    GuiUtils.showCentered(dialog);
  }

  private void close() {
    dialog.setVisible(false);
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(Category.TYPE)) {
      if (dialog.isVisible()) {
        dialog.getContentPane().removeAll();
        loadDialogContent();
        setSelectedCategories();
      }
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }

  private void loadDialogContent() {
    categoryIdToJLabel.clear();

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/categoryChooser.splits",
                                                      repository, directory);
    builder.addRepeat("masterRepeat",
                      Category.TYPE,
                      PicsouMatchers.masterCategories(),
                      new RepeatComponentFactory<Glob>() {
                        public void registerComponents(RepeatCellBuilder masterCellBuilder, Glob master) {
                          masterCellBuilder.add("masterLabel", createCategoryPanel(master));
                          GlobsPanelBuilder.addRepeat(
                            "subcatRepeat",
                            Category.TYPE,
                            PicsouMatchers.subCategories(master.get(Category.ID)),
                            new CategoryComparator(repository, directory),
                            repository,
                            masterCellBuilder,
                            new RepeatComponentFactory<Glob>() {
                              public void registerComponents(RepeatCellBuilder subCatCellBuilder, Glob subcat) {
                                subCatCellBuilder.add("subcatLabel", createCategoryPanel(subcat));
                              }
                            });
                        }
                      });

    builder.add("close", new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        close();
      }
    });
    builder.addLoader(new SplitsLoader() {
      public void load(Component component) {
        dialog.setContentPane((Container)component);
        dialog.pack();
      }
    });
    builder.load();

    JPanel panel = (JPanel)builder.getComponent("masterRepeat");
    panel.setLayout(new WrappedColumnLayout(4));
  }

  private void setSelectedCategories() {
    Set<Integer> preselectedCategoryIds = callback.getPreselectedCategoryIds();

    unselectAllCategories();

    for (Integer categoryId : preselectedCategoryIds) {
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

  private JPanel createCategoryPanel(Glob category) {
    final JPanel panel = Gui.createHorizontalBoxLayoutPanel();

    final JLabel label = createCategoryLabel(panel, category);
    panel.add(Box.createRigidArea(new Dimension(2, 0)));
    CategoryButtonsPanel buttonsPanel = new CategoryButtonsPanel(category,
                                                                 label,
                                                                 panel,
                                                                 addCategoryAction,
                                                                 renameCategoryAction,
                                                                 deleteCategoryAction,
                                                                 selectionService);
    panel.add(buttonsPanel.getPanel());
    return panel;
  }

  private JLabel createCategoryLabel(JPanel panel, final Glob category) {
    String categoryName = stringifier.toString(category, repository);
    final JLabel label = new JLabel(categoryName);
    label.setName("label." + categoryName);
    label.setFont(Category.isMaster(category) ? masterFont : Gui.DEFAULT_TABLE_FONT);
    label.setHorizontalAlignment(JLabel.LEFT);

    categoryIdToJLabel.put(category.get(Category.ID), label);
    label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    label.addMouseListener(new MouseAdapter() {
      public void mouseEntered(MouseEvent e) {
        selectionService.select(category);
        label.setForeground(colors.getRolloverCategoryColor());
      }

      public void mouseExited(MouseEvent e) {
        label.setForeground(selectedCategories.contains(label) ?
                            colors.getRolloverCategoryColor() :
                            colors.getCategoryColor());
      }

      public void mouseReleased(MouseEvent e) {
        callback.categorySelected(category);
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            close();
          }
        });
      }
    });

    panel.add(label);
    return label;
  }
}
