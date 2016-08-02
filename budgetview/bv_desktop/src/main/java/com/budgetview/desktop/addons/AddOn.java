package com.budgetview.desktop.addons;

import com.budgetview.desktop.utils.Gui;
import com.budgetview.model.AddOns;
import com.budgetview.utils.Lang;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public abstract class AddOn {

  private BooleanField field;
  private String iconPath;
  private Icon icon;

  protected AddOn(BooleanField field, String iconPath) {
    this.field = field;
    this.iconPath = iconPath;
  }

  public String getName() {
    return Lang.get("addons." + field.getName().toLowerCase());
  }

  public Icon getIcon() {
    if (icon == null) {
      icon = Gui.IMAGE_LOCATOR.get(iconPath);
    }
    return icon;
  }

  public BooleanField getField() {
    return field;
  }

  public void activate(GlobRepository repository, Directory directory) {
    repository.update(AddOns.KEY, field, true);
    processPostActivation(repository, directory);
  }

  protected abstract void processPostActivation(GlobRepository repository, Directory directory);

  public String getDescription() {
    return Lang.get("addons." + field.getName().toLowerCase() + ".description");
  }

  public boolean isEnabled(GlobRepository repository) {
    Glob addOns = repository.find(AddOns.KEY);
    return addOns != null && addOns.isTrue(field);
  }
}
