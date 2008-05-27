package org.designup.picsou.gui.utils;

import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.format.utils.AbstractGlobStringifier;
import org.designup.picsou.utils.Lang;

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
