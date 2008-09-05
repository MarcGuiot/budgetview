package org.designup.picsou.gui.description;

import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.Link;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.Formats;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.format.utils.AbstractGlobStringifier;
import org.globsframework.model.format.utils.DefaultDescriptionService;
import org.globsframework.utils.Strings;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class PicsouDescriptionService extends DefaultDescriptionService {
  public static final DecimalFormat INTEGER_FORMAT = new DecimalFormat("0");
  public static final DecimalFormat DECIMAL_FORMAT = 
    new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));
  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
  private static SimpleDateFormat YEAR_MONTH_FORMAT = new SimpleDateFormat("MMMMMMMMMM yyyy", Locale.FRANCE);

  public PicsouDescriptionService() {
    super(new Formats(DATE_FORMAT,
                      new SimpleDateFormat("dd/MM/yyyy hh:mm:ss"),
                      DECIMAL_FORMAT,
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

  public static String toString(Double value) {
    if (value == null) {
      return "";
    }
    return DECIMAL_FORMAT.format(value);
  }

  public static String toString(Date date) {
    return DATE_FORMAT.format(date);
  }

  public static String toString(int year, int month) {
    GregorianCalendar calendar =
      new GregorianCalendar(year, month - 1, 1);
    return Strings.capitalize(YEAR_MONTH_FORMAT.format(calendar.getTime()));
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
          return Month.getLabel(glob.get(Month.ID));
        }
      };
    }
    return super.getStringifier(globType);
  }
}
