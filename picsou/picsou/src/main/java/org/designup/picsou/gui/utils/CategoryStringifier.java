package org.designup.picsou.gui.utils;

import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.designup.picsou.model.Category;

public class CategoryStringifier extends BundleBasedStringifier {

  public CategoryStringifier() {
    super(Category.NAME, Category.TYPE.getName() + ".");
  }

  public String toString(Glob glob, GlobRepository repository) {
    if (glob == null) {
      return "";
    }
    if (glob.get(Category.MASTER) == null) {
      return super.toString(glob, repository);
    }
    return glob.get(Category.NAME);
  }
}
