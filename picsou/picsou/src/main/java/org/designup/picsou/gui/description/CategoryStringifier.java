package org.designup.picsou.gui.description;

import org.designup.picsou.model.Category;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.utils.AbstractGlobStringifier;

public class CategoryStringifier extends AbstractGlobStringifier {
  private static final String PREFIX = Category.TYPE.getName() + ".";

  public CategoryStringifier() {
  }

  public String toString(Glob category, GlobRepository repository) {
    if (category == null) {
      return "";
    }
    String name = category.get(Category.NAME);
    if (name != null) {
      return name;
    }
    String innerName = category.get(Category.INNER_NAME);
    return Lang.get(PREFIX + innerName);
  }
}
