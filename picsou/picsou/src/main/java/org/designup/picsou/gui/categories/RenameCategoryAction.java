package org.designup.picsou.gui.categories;

import org.crossbowlabs.globs.gui.GlobSelectionListener;
import org.crossbowlabs.globs.gui.SelectionService;
import org.crossbowlabs.globs.gui.actions.RenameGlobAction;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.model.format.GlobStringifier;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.globs.utils.exceptions.InvalidParameter;
import org.designup.picsou.gui.utils.PicsouDialog;
import org.designup.picsou.model.Category;
import org.designup.picsou.utils.Lang;

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
