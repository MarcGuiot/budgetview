package org.designup.picsou.gui.categories;

import org.designup.picsou.model.Category;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.actions.RenameGlobAction;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidParameter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

public abstract class RenameCategoryAction extends RenameGlobAction implements GlobSelectionListener {
  private GlobStringifier categoryStringifier;

  public RenameCategoryAction(GlobRepository repository, Directory directory) {
    super("T", Category.NAME, repository, directory);
    directory.get(SelectionService.class).addListener(this, Category.TYPE);
    categoryStringifier = directory.get(DescriptionService.class).getStringifier(Category.TYPE);
    setEnabled(false);
  }

  protected boolean accept(Glob category) {
    return !Category.isMaster(category);
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
    return Lang.get("category.rename.title");
  }

  protected String getInputLabel() {
    return Lang.get("category.rename.inputlabel");
  }

  protected String getOkLabel() {
    return Lang.get("ok");
  }

  protected String getCancelLabel() {
    return Lang.get("close");
  }
}
