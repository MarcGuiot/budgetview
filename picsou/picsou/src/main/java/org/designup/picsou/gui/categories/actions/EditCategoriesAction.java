package org.designup.picsou.gui.categories.actions;

import org.designup.picsou.gui.categories.CategoryEditionDialog;
import org.designup.picsou.model.Category;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Set;

public class EditCategoriesAction extends AbstractAction implements GlobSelectionListener {
  private GlobRepository repository;
  private Directory directory;
  private Set<Integer> categories = Collections.emptySet();

  public EditCategoriesAction(GlobRepository repository, Directory directory) {
    super(Lang.get("category.edition"));
    this.repository = repository;
    this.directory = directory;
    directory.get(SelectionService.class).addListener(this, Category.TYPE);
  }

  public void actionPerformed(ActionEvent e) {
    CategoryEditionDialog dialog = new CategoryEditionDialog(repository, directory);
    dialog.show(categories);
  }

  public void selectionUpdated(GlobSelection selection) {
    categories = selection.getAll(Category.TYPE).getValueSet(Category.ID);
  }
}
