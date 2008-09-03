package org.designup.picsou.gui.categories.actions;

import org.designup.picsou.client.AllocationLearningService;
import org.designup.picsou.gui.categories.CategoryChooserCallback;
import org.designup.picsou.gui.categories.CategoryChooserDialog;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.transactions.columns.TransactionRendererColors;
import org.designup.picsou.gui.utils.PicsouMatchers;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.utils.GuiUtils;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.GlobListStringifiers;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Set;

public abstract class DeleteCategoryAction extends AbstractCategoryAction {
  protected AllocationLearningService learningService;
  private Directory directory;

  public DeleteCategoryAction(GlobRepository repository, Directory directory) {
    super(Lang.get("delete"), repository, directory);
    this.directory = directory;
    learningService = directory.get(AllocationLearningService.class);
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
    Set<Integer> categories;
    if (Category.isMaster(category)) {
      categories = repository.getAll(Category.TYPE, PicsouMatchers.subCategories(category.get(Category.ID)))
        .getSortedSet(Category.ID);
      categories.add(category.get(Category.ID));
    }
    else {
      categories = Collections.singleton(category.get(Category.ID));
    }
    GlobList transactions = repository.getAll(Transaction.TYPE,
                                              GlobMatchers.fieldIn(Transaction.CATEGORY, categories));
    GlobList series = repository.getAll(Series.TYPE,
                                        GlobMatchers.fieldIn(Series.DEFAULT_CATEGORY, categories));
    GlobList transactionToCategory = repository.getAll(TransactionToCategory.TYPE,
                                                       GlobMatchers.fieldIn(TransactionToCategory.CATEGORY, categories));
    GlobList seriesToCategory = repository.getAll(SeriesToCategory.TYPE,
                                                  GlobMatchers.fieldIn(SeriesToCategory.CATEGORY, categories));

    repository.enterBulkDispatchingMode();
    Integer targetId;
    try {
      if (series.isEmpty() && transactions.isEmpty() && transactionToCategory.isEmpty()) {
        delete(categories, seriesToCategory);
        return null;
      }

      NewCategoryDialog categoryDialog = new NewCategoryDialog(directory, repository);
      if (!categoryDialog.selectTargetCategory(masterId, getParent())) {
        return null;
      }

      targetId = categoryDialog.getTargetId();
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
        GlobList seriesToNewMasterCategory =
          repository.getAll(SeriesToCategory.TYPE,
                            GlobMatchers.or(
                              GlobMatchers.fieldEquals(SeriesToCategory.SERIES, glob.get(SeriesToCategory.SERIES)),
                              GlobMatchers.fieldEquals(SeriesToCategory.CATEGORY, targetId)));
        if (seriesToNewMasterCategory.isEmpty()) {
          repository.create(SeriesToCategory.TYPE,
                            value(SeriesToCategory.CATEGORY, targetId),
                            value(SeriesToCategory.SERIES, glob.get(SeriesToCategory.SERIES)));
        }
      }
      for (Glob glob : transactionToCategory) {
        GlobList existingTransactionToCategory =
          repository.getAll(TransactionToCategory.TYPE,
                            GlobMatchers.and(
                              GlobMatchers.fieldEquals(TransactionToCategory.TRANSACTION,
                                                       glob.get(TransactionToCategory.TRANSACTION)),
                              GlobMatchers.fieldEquals(TransactionToCategory.CATEGORY, targetId)));
        if (existingTransactionToCategory.isEmpty()) {
          repository.create(TransactionToCategory.TYPE,
                            value(TransactionToCategory.CATEGORY, targetId),
                            value(TransactionToCategory.TRANSACTION,
                                  glob.get(TransactionToCategory.TRANSACTION)));
        }
      }
      delete(categories, seriesToCategory);
    }
    finally {
      repository.completeBulkDispatchingMode();
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

  static class NewCategoryDialog {
    private GlobRepository repository;
    private PicsouDialog categoryChooserDialog;
    private Integer targetId;
    private boolean returnStatus;
    private Directory localDirectory;
    private SelectionService selectionService;

    NewCategoryDialog(Directory directory, GlobRepository repository) {
      this.repository = repository;
      localDirectory = new DefaultDirectory(directory);
      selectionService = new SelectionService();
    }

    private boolean selectTargetCategory(Integer masterId, JDialog dialog) {
      localDirectory.add(selectionService);
      categoryChooserDialog = PicsouDialog.create(dialog, Lang.get("delete.category.title.label"));

      GlobsPanelBuilder builder = new GlobsPanelBuilder(DeleteCategoryAction.class,
                                                        "/layout/deleteCategory.splits", repository, localDirectory);

      builder.add("warningText", new JTextArea(Lang.get("delete.category.warning.text")));
      GlobListStringifier categoryStringifier = GlobListStringifiers
        .valueForEmpty(Lang.get("delete.category.empty"), localDirectory.get(DescriptionService.class).getListStringifier(Category.TYPE));
      builder.addLabel("categoryLabel", Category.TYPE, categoryStringifier);
      CategorieChooserAction chooserAction =
        new CategorieChooserAction(masterId, categoryChooserDialog, localDirectory, repository);
      builder.add("categoryChooser", chooserAction);

      final NewCategoryDialog.OkAction okAction = new OkAction();
      categoryChooserDialog.addInPanelWithButton(builder.<JPanel>load(), okAction, new CancelAction());
      selectionService.addListener(new GlobSelectionListener() {
        public void selectionUpdated(GlobSelection selection) {
          okAction.setEnabled(!selection.getAll(Category.TYPE).isEmpty());
        }
      }, Category.TYPE);
      if (masterId != null) {
        selectionService.select(repository.get(Key.create(Category.TYPE, masterId)));
      }
      categoryChooserDialog.pack();
      GuiUtils.showCentered(categoryChooserDialog);
      targetId = chooserAction.getTargetId();
      return returnStatus;
    }

    public Integer getTargetId() {
      return targetId;
    }

    private class OkAction extends AbstractAction {
      private OkAction() {
        super(Lang.get("ok"));
        setEnabled(false);
      }

      public void actionPerformed(ActionEvent e) {
        categoryChooserDialog.setVisible(false);
        returnStatus = true;
      }
    }

    private class CancelAction extends AbstractAction {
      private CancelAction() {
        super(Lang.get("cancel"));
      }

      public void actionPerformed(ActionEvent e) {
        categoryChooserDialog.setVisible(false);
        returnStatus = false;
      }
    }
  }

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
  }

  private static class CategorieChooserAction extends AbstractAction {
    private Integer masterId;
    private Dialog parent;
    private Directory directory;
    private GlobRepository repository;
    private Integer targetId;

    private CategorieChooserAction(Integer masterId, Dialog parent, Directory directory, GlobRepository repository) {
      super(Lang.get("delete.category.button.label"));
      this.masterId = masterId;
      this.parent = parent;
      this.directory = directory;
      this.repository = repository;
    }

    public void actionPerformed(ActionEvent e) {
      CategoryCallback callback = new CategoryCallback(masterId);
      CategoryChooserDialog dialog = new CategoryChooserDialog(callback, parent, true,
                                                               new TransactionRendererColors(directory),
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
