package org.designup.picsou.gui.description;

import org.designup.picsou.model.Category;
import org.designup.picsou.utils.Lang;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.utils.AbstractGlobStringifier;

public class CategoryStringifier extends AbstractGlobStringifier {
  private static StringField namingField = Category.NAME;
  private static String prefix = Category.TYPE.getName() + ".";

  public CategoryStringifier() {
  }

  public String toString(Glob glob, GlobRepository repository) {
    if (glob == null) {
      return "";
    }
    if (glob.get(Category.MASTER) == null) {
      String translatedName = Lang.find(prefix + glob.get(namingField));
      if (translatedName == null) {
        return glob.get(Category.NAME);
      }
      else {
        return translatedName;
      }
    }
    return glob.get(Category.NAME);
  }
}
