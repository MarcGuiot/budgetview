package org.designup.picsou.gui.categories.actions;

import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.components.ReadOnlyGlobTextFieldView;
import org.designup.picsou.model.Category;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.GlobListStringifiers;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CategoryDeletionDialog {
  private GlobRepository repository;
  private PicsouDialog categoryChooserDialog;
  private Integer targetId;
  private boolean returnStatus;
  private Directory localDirectory;
  private SelectionService selectionService;

  CategoryDeletionDialog(Directory directory, GlobRepository repository) {
    this.repository = repository;
    localDirectory = new DefaultDirectory(directory);
    selectionService = new SelectionService();
  }

  public boolean selectTargetCategory(Integer masterId, JDialog dialog) {
    localDirectory.add(selectionService);
    categoryChooserDialog = PicsouDialog.create(dialog, localDirectory);

    GlobsPanelBuilder builder = new GlobsPanelBuilder(DeleteCategoryAction.class,
                                                      "/layout/categoryDeletionDialog.splits", repository, localDirectory);

    builder.add("warningText", new JEditorPane("text/html", Lang.get("delete.category.warning.text")));

    GlobListStringifier categoryStringifier = GlobListStringifiers
      .valueForEmpty(Lang.get("delete.category.empty"), localDirectory.get(DescriptionService.class).getListStringifier(Category.TYPE));

    builder.add("categoryField", ReadOnlyGlobTextFieldView.init(Category.TYPE, repository, localDirectory, categoryStringifier));

    DeleteCategoryAction.CategoryChooserAction chooserAction =
      new DeleteCategoryAction.CategoryChooserAction(masterId, categoryChooserDialog, localDirectory, repository);
    builder.add("categoryChooser", chooserAction);

    final OkAction okAction = new OkAction();
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
