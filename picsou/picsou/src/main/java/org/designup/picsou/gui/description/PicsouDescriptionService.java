package org.designup.picsou.gui.description;

import org.designup.picsou.gui.accounts.utils.MonthDay;
import org.designup.picsou.gui.description.stringifiers.*;
import org.designup.picsou.gui.projects.utils.ProjectStringifier;
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
import org.globsframework.model.utils.GlobFieldComparator;
import org.globsframework.utils.Strings;

import java.text.SimpleDateFormat;
import java.util.Comparator;

public class PicsouDescriptionService extends DefaultDescriptionService {

  public static final SimpleDateFormat LOCAL_TIME_STAMP = new SimpleDateFormat("EEEE d MMM yyyy HH:mm");

  public PicsouDescriptionService() {
    super(createFormats());
  }


  public void updateFormats() {
    setFormats(createFormats());
  }

  private static Formats createFormats() {
    return new Formats(Formatting.getDateFormat(),
                       Formatting.getDateAndTimeFormat(),
                       Formatting.DECIMAL_FORMAT,
                       Lang.get("yes"), Lang.get("no"));
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
    if (globType.equals(AccountCardType.TYPE)) {
      return new AccountCardTypeStringifier();
    }
    if (globType.equals(Bank.TYPE)) {
      return new BankStringifier();
    }
    if (globType.equals(MonthDay.TYPE)) {
      return new DayGlobStringifier();
    }
    if (globType.equals(MonthDay.TYPE)) {
      return new DayGlobStringifier();
    }
    if (globType.equals(AccountType.TYPE)) {
      return new BundleBasedStringifier(AccountType.NAME, "account.type.");
    }
    if (globType.equals(Month.TYPE)) {
      return new AbstractGlobStringifier() {
        public String toString(Glob glob, GlobRepository repository) {
          if (glob == null) {
            return null;
          }
          return Month.getFullLabel(glob.get(Month.ID));
        }
      };
    }
    if (globType.equals(ProfileType.TYPE)) {
      return new BundleBasedStringifier(ProfileType.NAME, ProfileType.TYPE.getName() + ".");
    }
    if (globType.equals(Project.TYPE)) {
      return new ProjectStringifier();
    }
    if (globType.equals(DayOfMonth.TYPE)) {
      return new DayOfMonthStringifier();
    }
    return super.getStringifier(globType);
  }

  public GlobStringifier getStringifier(Field targetField) {
    if (targetField == Transaction.LABEL) {
      return new TransactionStringifier(super.getStringifier(targetField));
    }
    return super.getStringifier(targetField);
  }

  private static class TransactionStringifier implements GlobStringifier {
    private GlobStringifier stringifier;
    private String planned;

    public TransactionStringifier(GlobStringifier stringifier) {
      this.stringifier = stringifier;
      planned = Lang.get("transaction.planned");
    }

    public String toString(Glob transaction, GlobRepository repository) {
      if (transaction == null) {
        return null;
      }
      if (transaction.isTrue(Transaction.PLANNED)) {
        String label = stringifier.toString(transaction, repository);
        return planned + (label == null ? "" : label);
      }
      return stringifier.toString(transaction, repository);
    }

    public Comparator<Glob> getComparator(GlobRepository repository) {
      return stringifier.getComparator(repository);
    }
  }

  private static class DayGlobStringifier implements GlobStringifier {
    public String toString(Glob glob, GlobRepository repository) {
      if (glob == null) {
        return null;
      }
      return Integer.toString(glob.get(MonthDay.ID));
    }

    public Comparator<Glob> getComparator(GlobRepository repository) {
      return new GlobFieldComparator(MonthDay.ID);
    }
  }
}
