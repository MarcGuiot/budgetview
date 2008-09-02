package org.designup.picsou.gui.description;

import org.designup.picsou.model.Category;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.utils.AbstractGlobStringifier;

public class CategoryStringifier extends AbstractGlobStringifier {
  private static String prefix = Category.TYPE.getName() + ".";

  public CategoryStringifier() {
  }

  public String toString(Glob glob, GlobRepository repository) {
    if (glob == null) {
      return "";
    }
    String name = glob.get(Category.NAME);
    if (name != null) {
      return name;
    }
    String innerName = glob.get(Category.INNER_NAME);
    return Lang.get(prefix + innerName);
  }
}
