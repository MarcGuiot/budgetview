package org.designup.picsou.gui.categories.actions;

import org.designup.picsou.gui.categories.CategoryChooserCallback;
import org.designup.picsou.gui.categories.CategoryChooserDialog;
import org.designup.picsou.gui.utils.PicsouMatchers;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesToCategory;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.SelectionService;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Set;

public abstract class DeleteCategoryAction extends AbstractCategoryAction {
  private Directory directory;

  public DeleteCategoryAction(GlobRepository repository, Directory directory) {
    super(Lang.get("delete"), repository, directory);
    this.directory = directory;
    setEnabled(false);
  }

  public boolean appliesFor(GlobList categories) {
    if (categories.size() < 1) {
      return false;
    }
    for (Glob category : categories) {
      if (Category.isReserved(category)) {
        return false;
      }
    }
    return true;
  }

  protected void process(GlobList selectedCategories) {
    if (selectedCategories.size() != 1) {
      return;
    }
    Glob category = selectedCategories.get(0);
    Integer masterId = null;
    if (!Category.isMaster(category)) {
      masterId = category.get(Category.MASTER);
    }
    Integer targetId = migrateTransactionAndSeriesInCategory(category, masterId);
    if (targetId != null) {
      selectionService.select(repository.get(Key.create(Category.TYPE, targetId)));
    }
  }

  private Integer migrateTransactionAndSeriesInCategory(Glob category, final Integer masterId) {
    Integer categoryId = category.get(Category.ID);

    Set<Integer> categories;
    if (Category.isMaster(category)) {
      categories = repository.getAll(Category.TYPE, PicsouMatchers.subCategories(categoryId))
        .getSortedSet(Category.ID);
      categories.add(categoryId);
    }
    else {
      categories = Collections.singleton(categoryId);
    }
    GlobList transactions = repository.getAll(Transaction.TYPE,
                                              GlobMatchers.fieldIn(Transaction.CATEGORY, categories));
    GlobList series = repository.getAll(Series.TYPE,
                                        GlobMatchers.fieldIn(Series.DEFAULT_CATEGORY, categories));
    GlobList seriesToCategory = repository.getAll(SeriesToCategory.TYPE,
                                                  GlobMatchers.fieldIn(SeriesToCategory.CATEGORY, categories));

    Integer targetId;
    if (series.isEmpty() && transactions.isEmpty()) {
      try {
        repository.startChangeSet();
        delete(categories, seriesToCategory);
      }
      finally {
        repository.completeChangeSet();
      }
      return null;
    }

    CategoryDeletionDialog categoryDeletionDialog = new CategoryDeletionDialog(directory, repository);
    if (!categoryDeletionDialog.selectTargetCategory(categoryId, masterId, getParent())) {
      return null;
    }
    repository.startChangeSet();
    try {

      targetId = categoryDeletionDialog.getTargetId();
      if (targetId == null) {
        return null;
      }
      for (Glob glob : transactions) {
        repository.update(glob.getKey(), Transaction.CATEGORY, targetId);
      }

      for (Glob glob : series) {
        repository.update(glob.getKey(), Series.DEFAULT_CATEGORY, targetId);
      }
      for (Glob glob : seriesToCategory) {
        GlobList seriesToNewTargetCategory =
          repository.getAll(SeriesToCategory.TYPE,
                            GlobMatchers.and(
                              GlobMatchers.fieldEquals(SeriesToCategory.SERIES, glob.get(SeriesToCategory.SERIES)),
                              GlobMatchers.fieldEquals(SeriesToCategory.CATEGORY, targetId)));
        if (seriesToNewTargetCategory.isEmpty()) {
          repository.create(SeriesToCategory.TYPE,
                            value(SeriesToCategory.CATEGORY, targetId),
                            value(SeriesToCategory.SERIES, glob.get(SeriesToCategory.SERIES)));
        }
      }
      delete(categories, seriesToCategory);
    }
    finally {
      repository.completeChangeSet();
    }
    return targetId;
  }

  private void delete(Set<Integer> categories, GlobList seriesToCategory) {
    repository.delete(seriesToCategory);
    for (Integer id : categories) {
      repository.delete(Key.create(Category.TYPE, id));
    }
  }

  protected abstract JDialog getParent();

  private static class CategoryCallback implements CategoryChooserCallback {
    private Integer targetId;
    private final Integer masterId;

    public CategoryCallback(Integer masterId) {
      this.masterId = masterId;
    }

    public Integer getTargetId() {
      return targetId;
    }

    public void processSelection(GlobList categories) {
      if (categories.isEmpty()) {
        targetId = null;
      }
      else {
        targetId = categories.get(0).get(Category.ID);
      }
    }

    public Set<Integer> getPreselectedCategoryIds() {
      if (masterId != null) {
        return Collections.singleton(masterId);
      }
      return Collections.emptySet();
    }

    public Set<Integer> getUnUncheckable() {
      return Collections.emptySet();
    }
  }

  static class CategoryChooserAction extends AbstractAction {
    private Integer categoryIdToDelete;
    private Integer masterId;
    private Dialog parent;
    private Directory directory;
    private GlobRepository repository;
    private Integer targetId;

    public CategoryChooserAction(Integer categoryIdToDelete, Integer masterId,
                                 Dialog parent, Directory directory, GlobRepository repository) {
      super(Lang.get("delete.category.button.label"));
      this.categoryIdToDelete = categoryIdToDelete;
      this.masterId = masterId;
      this.parent = parent;
      this.directory = directory;
      this.repository = repository;
    }

    public void actionPerformed(ActionEvent e) {
      CategoryCallback callback = new CategoryCallback(masterId);
      CategoryChooserDialog dialog = new CategoryChooserDialog(callback, parent, true, categoryIdToDelete,
                                                               repository, directory);
      dialog.show();
      targetId = callback.getTargetId();
      if (targetId != null) {
        directory.get(SelectionService.class).select(repository.get(Key.create(Category.TYPE, targetId)));
      }
    }

    public Integer getTargetId() {
      return targetId;
    }
  }

}
