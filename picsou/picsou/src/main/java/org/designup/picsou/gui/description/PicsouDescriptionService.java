package org.designup.picsou.gui.description;

import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.Link;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.format.utils.AbstractGlobStringifier;
import org.globsframework.model.format.utils.DefaultDescriptionService;
import org.globsframework.utils.Strings;

import java.text.SimpleDateFormat;

public class PicsouDescriptionService extends DefaultDescriptionService {

  public PicsouDescriptionService() {
    super(new org.globsframework.model.format.Formats(Formatting.DATE_FORMAT,
                      new SimpleDateFormat("dd/MM/yyyy hh:mm:ss"),
                      Formatting.DECIMAL_FORMAT,
                      Lang.get("yes"), Lang.get("no")));
  }

  public String getLabel(GlobType type) {
    return translate(type.getName());
  }

  public String getLabel(Field field) {
    return translate(field.getName());
  }

  public String getLabel(Link link) {
    return translate(link.getName());
  }

  private String translate(String name) {
    String lang = Lang.find(name);
    if (lang == null) {
      lang = Strings.capitalize(name);
    }
    return lang;
  }

  public GlobStringifier getStringifier(GlobType globType) {
    if (globType.equals(Category.TYPE)) {
      return new CategoryStringifier();
    }
    if (globType.equals(TransactionType.TYPE)) {
      return new BundleBasedStringifier(TransactionType.NAME, TransactionType.TYPE.getName() + ".");
    }
    if (globType.equals(Series.TYPE)) {
      return new SeriesStringifier();
    }
    if (globType.equals(BudgetArea.TYPE)) {
      return new BundleBasedStringifier(BudgetArea.NAME, BudgetArea.TYPE.getName() + ".");
    }
    if (globType.equals(Account.TYPE)) {
      return new AccountStringifier();
    }
    if (globType.equals(Month.TYPE)) {
      return new AbstractGlobStringifier() {
        public String toString(Glob glob, GlobRepository repository) {
          return Month.getFullLabel(glob.get(Month.ID));
        }
      };
    }
    if (globType.equals(ProfileType.TYPE)) {
      return new BundleBasedStringifier(ProfileType.NAME, ProfileType.TYPE.getName() + ".");
    }
    return super.getStringifier(globType);
  }
}
