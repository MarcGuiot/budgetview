package org.designup.picsou.gui.categories;

import org.crossbowlabs.globs.gui.GlobSelection;
import org.crossbowlabs.globs.gui.GlobSelectionListener;
import org.crossbowlabs.globs.gui.SelectionService;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.designup.picsou.model.Category;

import javax.swing.*;
import java.awt.event.ActionEvent;

public abstract class AbstractCategoryAction extends AbstractAction implements GlobSelectionListener {
  protected GlobRepository repository;
  protected GlobList selectedCategories;
  protected SelectionService selectionService;
  protected JFrame parent;

  public AbstractCategoryAction(String name, GlobRepository repository, Directory directory) {
    super(name);
    this.repository = repository;
    this.selectionService = directory.get(SelectionService.class);
    this.selectionService.addListener(this, Category.TYPE);
    this.parent = directory.get(JFrame.class);
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
