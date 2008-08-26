package org.designup.picsou.gui.transactions.categorization;

import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.description.CategoryComparator;
import org.designup.picsou.gui.transactions.columns.TransactionRendererColors;
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
import org.globsframework.model.*;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.DefaultChangeSetVisitor;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CategoryChooserDialog implements ChangeSetListener {
  private CategoryChooserCallback callback;
  private TransactionRendererColors colors;
  private Directory directory;
  private GlobRepository repository;
  private GlobStringifier categoryStringifier;
  private PicsouDialog dialog;
  private SelectionService selectionService;
  private Map<Key, JToggleButton> categoryToButton = new HashMap<Key, JToggleButton>();
  private ButtonGroup buttonGroup;

  public CategoryChooserDialog(CategoryChooserCallback callback, boolean monoSelection, TransactionRendererColors colors,
                               GlobRepository repository, Directory directory) {
    if (monoSelection) {
      buttonGroup = new ButtonGroup();
    }
    this.callback = callback;
    this.colors = colors;
    this.directory = directory;

    Directory localDirectory = new DefaultDirectory(directory);
    selectionService = new SelectionService();
    localDirectory.add(selectionService);

    this.repository = repository;


    this.categoryStringifier = localDirectory.get(DescriptionService.class).getStringifier(Category.TYPE);
    repository.addChangeListener(this);
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

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Category.TYPE, new DefaultChangeSetVisitor() {
      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        categoryToButton.get(key).setText(categoryStringifier.toString(repository.get(key), repository));
      }
    });
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }

  private void loadDialogContent() {

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/categoryChooser.splits",
                                                      repository, directory);
    builder.addRepeat("masterRepeat",
                      Category.TYPE,
                      PicsouMatchers.masterCategories(),
                      new RepeatComponentFactory<Glob>() {
                        public void registerComponents(RepeatCellBuilder masterCellBuilder, Glob master) {
                          JToggleButton masterToggle = createCategoryToggle(master);
                          masterCellBuilder.add("masterLabel", masterToggle);
                          GlobsPanelBuilder.addRepeat(
                            "subcatRepeat",
                            Category.TYPE,
                            PicsouMatchers.subCategories(master.get(Category.ID)),
                            new CategoryComparator(repository, directory),
                            repository,
                            masterCellBuilder,
                            new RepeatComponentFactory<Glob>() {
                              public void registerComponents(RepeatCellBuilder subCatCellBuilder, final Glob subcat) {
                                final JToggleButton toggle = createCategoryToggle(subcat);
                                subCatCellBuilder.add("subcatLabel", toggle);
                                subCatCellBuilder.addDisposeListener(new DisposeToggleListener(subcat, toggle));
                              }
                            });
                          masterCellBuilder.addDisposeListener(new DisposeToggleListener(master, masterToggle));
                        }
                      });

    builder.add("close", new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        close();
      }
    });
    builder.add("ok", new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        GlobList selectedCategories = new GlobList();
        for (Map.Entry<Key, JToggleButton> entry : categoryToButton.entrySet()) {
          if (entry.getValue().isSelected()) {
            selectedCategories.add(repository.get(entry.getKey()));
          }
        }
        callback.processSelection(selectedCategories);
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

  private JToggleButton createCategoryToggle(Glob category) {
    JToggleButton button = new JToggleButton(categoryStringifier.toString(category, repository));
    if (buttonGroup != null) {
      buttonGroup.add(button);
    }
    categoryToButton.put(category.getKey(), button);
    return button;
  }

  private void setSelectedCategories() {
    Set<Integer> preselectedCategoryIds = callback.getPreselectedCategoryIds();

    unselectAllCategories();
    for (Integer categoryId : preselectedCategoryIds) {
      JToggleButton toggle = categoryToButton.get(Key.create(Category.TYPE, categoryId));
      toggle.setSelected(true);
    }
  }

  private void unselectAllCategories() {
    for (JToggleButton toggle : categoryToButton.values()) {
      toggle.setSelected(false);
    }
  }

  private class DisposeToggleListener implements RepeatCellBuilder.DisposeListener {
    private final Glob subcat;
    private final JToggleButton toggle;

    public DisposeToggleListener(Glob subcat, JToggleButton toggle) {
      this.subcat = subcat;
      this.toggle = toggle;
    }

    public void dispose() {
      categoryToButton.remove(subcat.getKey());
      if (buttonGroup != null) {
        buttonGroup.remove(toggle);
      }
    }
  }
}
