package org.designup.picsou.gui.categories.actions;

import org.designup.picsou.model.Category;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public abstract class AbstractCategoryAction extends AbstractAction implements GlobSelectionListener {
  protected GlobRepository repository;
  protected GlobList selectedCategories;
  protected SelectionService selectionService;

  public AbstractCategoryAction(String name, GlobRepository repository, Directory directory) {
    super(name);
    this.repository = repository;
    this.selectionService = directory.get(SelectionService.class);
    this.selectionService.addListener(this, Category.TYPE);
  }

  public final void selectionUpdated(GlobSelection selection) {
    selectCategory(selection.getAll(Category.TYPE));
  }

  private void selectCategory(GlobList categories) {
    if (appliesFor(categories)) {
      this.selectedCategories = categories;
    }
    else {
      this.selectedCategories = null;
    }
    setEnabled(selectedCategories != null);
  }

  public final void actionPerformed(ActionEvent e) {
    process(selectedCategories);
  }

  public abstract boolean appliesFor(GlobList categories);

  protected abstract void process(GlobList selectedCategories);
}
