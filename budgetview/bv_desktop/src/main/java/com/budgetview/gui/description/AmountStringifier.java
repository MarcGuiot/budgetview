package com.budgetview.gui.description;

import com.budgetview.gui.description.stringifiers.ForcedPlusGlobListStringifier;
import com.budgetview.model.BudgetArea;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.GlobListStringifiers;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.GlobFieldComparator;

import java.util.Comparator;

public class AmountStringifier {

  public static GlobStringifier getForSingle(final DoubleField field, final BudgetArea budgetArea) {
    return new GlobStringifier() {
      public String toString(Glob glob, GlobRepository repository) {
        return Formatting.toString(glob.get(field), budgetArea);
      }

      public Comparator<Glob> getComparator(GlobRepository repository) {
        return new GlobFieldComparator(field);
      }
    };
  }

  public static GlobListStringifier getForList(DoubleField field, BudgetArea budgetArea) {
    ForcedPlusGlobListStringifier plusGlobListStringifier =
      new ForcedPlusGlobListStringifier(budgetArea,
                                        GlobListStringifiers.sum(field, Formatting.DECIMAL_FORMAT, !budgetArea.isIncome()));
    return plusGlobListStringifier;
  }
}
