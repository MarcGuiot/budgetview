package org.designup.picsou.gui.categories.actions;

import org.designup.picsou.model.Category;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.actions.CreateGlobAction;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.StringField;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidParameter;

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
