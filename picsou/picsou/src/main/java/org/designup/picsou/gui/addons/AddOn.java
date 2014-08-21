package org.designup.picsou.gui.addons;

import org.designup.picsou.model.AddOns;
import org.designup.picsou.utils.Lang;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.model.GlobRepository;

public abstract class AddOn {

  private BooleanField field;

  protected AddOn(BooleanField field) {
    this.field = field;
  }

  public String getName() {
    return Lang.get("addons." + field.getName().toLowerCase());
  }

  public BooleanField getField() {
    return field;
  }

  public void activate(GlobRepository repository) {
    repository.update(AddOns.KEY, field, true);
  }
}
