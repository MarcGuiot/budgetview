package com.budgetview.desktop.description;

import com.budgetview.utils.Lang;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.utils.AbstractGlobStringifier;

public class BundleBasedStringifier extends AbstractGlobStringifier {
  private String prefix;
  private StringField namingField;

  public BundleBasedStringifier(StringField namingField, String prefix) {
    this.namingField = namingField;
    this.prefix = prefix;
  }

  public String toString(Glob glob, GlobRepository globRepository) {
    if (glob == null) {
      return "";
    }
    return Lang.get(prefix + glob.get(namingField));
  }
}
