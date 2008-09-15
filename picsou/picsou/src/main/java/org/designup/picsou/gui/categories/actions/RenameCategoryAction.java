package org.designup.picsou.gui.categories.actions;

import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.utils.PicsouMatchers;
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
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidParameter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public abstract class RenameCategoryAction extends RenameGlobAction implements GlobSelectionListener {
  private GlobStringifier categoryStringifier;

  public RenameCategoryAction(GlobRepository repository, Directory directory) {
    super(Lang.get("rename"), Category.NAME, repository, directory);
    directory.get(SelectionService.class).addListener(this, Category.TYPE);
    categoryStringifier = directory.get(DescriptionService.class).getStringifier(Category.TYPE);
    setEnabled(false);
  }

  protected boolean accept(Glob category) {
    return !Category.isReserved(category);
  }

  public JDialog getDialog(ActionEvent e) {
    return PicsouDialog.create(getParent(), directory);
  }

  protected abstract Window getParent();

  protected String getText() {
    return categoryStringifier.toString(getCurrentObject(), repository);
  }

  protected void validateName(GlobType type, StringField namingField, String name, GlobRepository repository) throws InvalidParameter {
    GlobMatcher matcher;
    if (Category.isMaster(getCurrentObject())) {
      matcher = PicsouMatchers.masterCategories();
    }
    else {
      matcher = PicsouMatchers.subCategories(getCurrentObject().get(Category.MASTER));
    }
    for (Glob category : repository.getAll(Category.TYPE, matcher)) {
      if (name.equals(categoryStringifier.toString(category, repository)) &&
          !category.getKey().equals(getCurrentObject().getKey())) {
        throw new InvalidParameter(Lang.get("category.name.already.used"));
      }
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
