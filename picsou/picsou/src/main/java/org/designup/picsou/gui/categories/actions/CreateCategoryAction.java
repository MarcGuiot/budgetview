package org.designup.picsou.gui.categories.actions;

import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.model.Category;
import org.designup.picsou.utils.Lang;
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
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

public abstract class CreateCategoryAction extends CreateGlobAction {
  private GlobStringifier categoryStringifier;
  private Glob masterSelectedCategory;
  private Directory directory;
  private boolean isMaster;

  public CreateCategoryAction(GlobRepository repository, Directory directory, boolean isMaster) {
    super(Lang.get("create"), Category.TYPE, repository, directory);
    this.directory = directory;
    this.isMaster = isMaster;
    categoryStringifier = directory.get(DescriptionService.class).getStringifier(Category.TYPE);
    setEnabled(isMaster);
  }

  public void selectMaster(GlobList categories) {
    if (categories.isEmpty()) {
      masterSelectedCategory = null;
    }
    else {
      masterSelectedCategory = categories.get(0);
    }
    if (!isMaster) {
      setEnabled(masterSelectedCategory != null);
    }
  }

  public JDialog getDialog(ActionEvent e) {
    return PicsouDialog.create(getParent(), "category.create.title");
  }

  protected abstract Window getParent();

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

  protected void doCreate(GlobType type, StringField namingField, String name, GlobRepository repository) {
    repository.create(Category.TYPE,
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
    if (isMaster) {
      return null;
    }
    return masterSelectedCategory.get(Category.ID);
  }
}
