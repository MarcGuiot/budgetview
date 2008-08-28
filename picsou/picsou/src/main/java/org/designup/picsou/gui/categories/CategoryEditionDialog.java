package org.designup.picsou.gui.categories;

import org.designup.picsou.gui.categories.actions.CreateCategoryAction;
import org.designup.picsou.gui.categories.actions.DeleteCategoryAction;
import org.designup.picsou.gui.categories.actions.RenameCategoryAction;
import org.designup.picsou.gui.components.PicsouDialog;
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
import java.util.Set;

public class CategoryEditionDialog {
  private JDialog dialog;
  private LocalGlobRepository localRepository;
  private Directory masterDirectory;
  private Directory subDirectory;
  private CreateCategoryAction createSubCategory;
  private GlobListView masterList;

  public CategoryEditionDialog(GlobRepository repository, Directory directory) {
    localRepository = LocalGlobRepositoryBuilder.init(repository)
      .copy(Category.TYPE, Transaction.TYPE, Series.TYPE, SeriesToCategory.TYPE,
            TransactionToCategory.TYPE).get();
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

    dialog = PicsouDialog.createWithButtons(Lang.get("categorization.title"),
                                            directory.get(JFrame.class),
                                            builder.<JPanel>load(),
                                            new ValidateAction(), new CancelAction());
    //On ecoute les creation apres la mise a jour de la listView.
    initListenerForSelectionOnCreate();
  }

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

  public void show(GlobList categories) {
    categories.filterSelf(GlobMatchers.not(GlobMatchers.fieldIn(Category.ID,
                                                                MasterCategory.RESERVED_CATEGORY_IDS)), localRepository);
    if (categories.size() == 1) {
      Glob category = localRepository.get(categories.get(0).getKey());
      if (Category.isMaster(category)) {
        masterDirectory.get(SelectionService.class).select(category);
      }
      else {
        Glob masterCategory = localRepository.get(Key.create(Category.TYPE, category.get(Category.ID)));
        masterDirectory.get(SelectionService.class).select(masterCategory);
        subDirectory.get(SelectionService.class).select(category);
      }
    }
    else {
      if (masterList.getSize() != 0) {
        masterDirectory.get(SelectionService.class).select(masterList.getGlobAt(0));
      }
    }
    dialog.pack();
    GuiUtils.showCentered(dialog);


  }

  private class ValidateAction extends AbstractAction {
    public ValidateAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      localRepository.getCurrentChanges()
        .safeVisit(new DefaultChangeSetVisitor());
      localRepository.commitChanges(true);
      dialog.setVisible(false);
    }
  }

  private class CancelAction extends AbstractAction {
    public CancelAction() {
      super(Lang.get("cancel"));
    }

    public void actionPerformed(ActionEvent e) {
      dialog.setVisible(false);
    }
  }
}
