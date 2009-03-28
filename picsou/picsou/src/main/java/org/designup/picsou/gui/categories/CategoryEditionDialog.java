package org.designup.picsou.gui.categories;

import org.designup.picsou.gui.categories.actions.CreateCategoryAction;
import org.designup.picsou.gui.categories.actions.DeleteCategoryAction;
import org.designup.picsou.gui.categories.actions.RenameCategoryAction;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.components.CancelAction;
import org.designup.picsou.gui.utils.PicsouMatchers;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.views.GlobListView;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetVisitor;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class CategoryEditionDialog {
  private PicsouDialog dialog;
  private GlobRepository repository;
  private Directory directory;
  private LocalGlobRepository localRepository;
  private Directory masterDirectory;
  private Directory subDirectory;
  private CreateCategoryAction createSubCategory;
  private GlobListView masterList;
  private Integer categoryToSelect;

  public CategoryEditionDialog(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    localRepository = LocalGlobRepositoryBuilder.init(repository)
      .copy(Category.TYPE, Transaction.TYPE, Series.TYPE, SeriesToCategory.TYPE).get();
    masterDirectory = new DefaultDirectory(directory);
    masterDirectory.add(new SelectionService());

    subDirectory = new DefaultDirectory(directory);
    subDirectory.add(new SelectionService());
    GlobsPanelBuilder builder = new GlobsPanelBuilder(CategoryEditionDialog.class, "/layout/categoryEditionDialog.splits",
                                                      localRepository, masterDirectory);

    masterList = builder.addList("masterCategoryList", Category.TYPE);
    masterList.setSingleSelectionMode();
    builder.add("createMasterCategory", new CreateCategoryAction(localRepository, masterDirectory, true) {
      protected Window getParent() {
        return dialog;
      }
    });
    builder.add("deleteMasterCategory", new DeleteCategoryAction(localRepository, masterDirectory) {
      protected JDialog getParent() {
        return dialog;
      }
    });
    builder.add("renameMasterCategory", new RenameCategoryAction(localRepository, masterDirectory) {
      protected Window getParent() {
        return dialog;
      }
    });

    GlobListView subList = GlobListView.init(Category.TYPE, localRepository, subDirectory).setName("subCategoryList");
    builder.add("subCategoryList", subList);
    subList.setSingleSelectionMode();
    createSubCategory = new CreateCategoryAction(localRepository, subDirectory, false) {
      protected Window getParent() {
        return dialog;
      }
    };
    builder.add("createSubCategory", createSubCategory);
    builder.add("deleteSubCategory", new DeleteCategoryAction(localRepository, subDirectory) {
      protected JDialog getParent() {
        return dialog;
      }
    });
    builder.add("renameSubCategory", new RenameCategoryAction(localRepository, subDirectory) {
      protected Window getParent() {
        return dialog;
      }
    });
    masterList.setFilter(PicsouMatchers.masterUserCategories());
    initListener(subList);

    dialog = PicsouDialog.create(getParent(), directory);
    dialog.addPanelWithButtons(builder.<JPanel>load(), new ValidateAction(), new CancelAction(dialog));
    //On ecoute les creation apres la mise a jour de la listView.
    initListenerForSelectionOnCreate();
  }

  public abstract Window getParent();

  private void initListenerForSelectionOnCreate() {
    localRepository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
        changeSet.safeVisit(Category.TYPE, new DefaultChangeSetVisitor() {
          public void visitCreation(Key key, FieldValues values) throws Exception {
            if (values.get(Category.MASTER) != null) {
              subDirectory.get(SelectionService.class).select(repository.get(key));
            }
            else {
              masterDirectory.get(SelectionService.class).select(repository.get(key));
            }
          }
        });
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
      }
    });
  }

  private void initListener(final GlobListView subList) {
    masterDirectory.get(SelectionService.class).addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        GlobList categories = selection.getAll(Category.TYPE);
        if (categories.isEmpty()) {
          subList.setFilter(GlobMatchers.NONE);
        }
        else {
          subList.setFilter(PicsouMatchers.subCategories(categories.get(0).get(Category.ID)));
          if (subList.getSize() != 0) {
            subDirectory.get(SelectionService.class).select(subList.getGlobAt(0));
          }
        }
        createSubCategory.selectMaster(categories);
      }
    }, Category.TYPE);
  }

  public void show(Collection categories) {
    for (Integer id : MasterCategory.RESERVED_CATEGORY_IDS) {
      categories.remove(id);
    }
    if (categories.size() == 1) {
      Glob category = localRepository.find(Key.create(Category.TYPE, categories.iterator().next()));
      if (category != null) {
        if (Category.isMaster(category)) {
          masterDirectory.get(SelectionService.class).select(category);
        }
        else {
          Glob masterCategory = localRepository.get(Key.create(Category.TYPE, category.get(Category.MASTER)));
          masterDirectory.get(SelectionService.class).select(masterCategory);
          subDirectory.get(SelectionService.class).select(category);
        }
      }
    }
    else {
      if (masterList.getSize() != 0) {
        masterDirectory.get(SelectionService.class).select(masterList.getGlobAt(0));
      }
    }
    dialog.pack();
    GuiUtils.showCentered(dialog);
    if (categoryToSelect != null) {
      directory.get(SelectionService.class).select(repository.get(Key.create(Category.TYPE, categoryToSelect)));
    }
  }

  public Window getDialog() {
    return dialog;
  }

  private class ValidateAction extends AbstractAction {
    public ValidateAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      final Set<Integer> categories = new HashSet<Integer>();
      localRepository.getCurrentChanges()
        .safeVisit(new DefaultChangeSetVisitor() {
          public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
            if (key.getGlobType().equals(Transaction.TYPE)) {
              if (values.contains(Transaction.CATEGORY)) {
                categories.add(values.get(Transaction.CATEGORY));
              }
            }
            if (key.getGlobType().equals(Series.TYPE)) {
              if (values.contains(Series.DEFAULT_CATEGORY)) {
                categories.add(values.get(Series.DEFAULT_CATEGORY));
              }
            }
          }
        });
      if (categories.isEmpty()) {
        final Set<Integer> masterIfDeletedCategories = new HashSet<Integer>();
        localRepository.getCurrentChanges()
          .safeVisit(Category.TYPE, new DefaultChangeSetVisitor() {
            public void visitDeletion(Key key, FieldValues values) throws Exception {
              if (values.get(Category.MASTER) != null) {
                masterIfDeletedCategories.add(values.get(Category.MASTER));
              }
            }
          });
        if (masterIfDeletedCategories.size() == 1) {
          Integer categoryId = masterIfDeletedCategories.iterator().next();
          if (localRepository.find(Key.create(Category.TYPE, categoryId)) != null) {
            categoryToSelect = categoryId;
          }
        }
      }
      else {
        if (categories.size() == 1) {
          categoryToSelect = categories.iterator().next();
        }
      }
      localRepository.commitChanges(true);
      dialog.setVisible(false);
    }
  }
}
