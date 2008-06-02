package org.designup.picsou.gui.categories;

import org.crossbowlabs.globs.gui.GlobSelection;
import org.crossbowlabs.globs.gui.GlobSelectionListener;
import org.crossbowlabs.globs.gui.SelectionService;
import org.crossbowlabs.globs.gui.actions.CreateGlobAction;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import static org.crossbowlabs.globs.model.FieldValue.value;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.model.format.GlobStringifier;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.globs.utils.exceptions.InvalidParameter;
import org.designup.picsou.model.Category;
import org.designup.picsou.utils.Lang;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

public abstract class CreateCategoryAction extends CreateGlobAction implements GlobSelectionListener {
  private GlobStringifier categoryStringifier;
  private Glob selectedCategory;

  public CreateCategoryAction(GlobRepository repository, Directory directory) {
    super("+", Category.TYPE, repository, directory);
    directory.get(SelectionService.class).addListener(this, Category.TYPE);
    categoryStringifier = directory.get(DescriptionService.class).getStringifier(Category.TYPE);
    setEnabled(false);
  }

  public void selectionUpdated(GlobSelection selection) {
    GlobList categories = selection.getAll(Category.TYPE);
    if (categories.size() != 1) {
      selectedCategory = null;
      return;
    }
    selectedCategory = categories.get(0);
    if (Category.isReserved(selectedCategory) || Category.isSystem(selectedCategory)) {
      selectedCategory = null;
    }
    setEnabled(selectedCategory != null);
  }

  public abstract JDialog getDialog(ActionEvent e);

  protected void validateName(GlobType type, StringField namingField, String name, GlobRepository repository) throws InvalidParameter {
    Set<String> existingNames = new HashSet<String>();
    for (Glob category : repository.getAll(Category.TYPE)) {
      existingNames.add(categoryStringifier.toString(category, repository));
    }
    if (existingNames.contains(name)) {
      throw new InvalidParameter(Lang.get("category.name.already.used"));
    }
  }

  protected String getTitle() {
    return Lang.get("category.create.title");
  }

  protected String getInputLabel() {
    return Lang.get("category.create.inputlabel");
  }

  protected Glob doCreate(GlobType type, StringField namingField, String name, GlobRepository repository) {
    return repository.create(Category.TYPE,
                             value(Category.NAME, name),
                             value(Category.MASTER, getMasterId()));
  }

  protected String getOkLabel() {
    return Lang.get("ok");
  }

  protected String getCancelLabel() {
    return Lang.get("close");
  }

  private Integer getMasterId() {
    Integer masterId = selectedCategory.get(Category.MASTER);
    if (masterId != null) {
      return masterId;
    }
    return selectedCategory.get(Category.ID);
  }
}
