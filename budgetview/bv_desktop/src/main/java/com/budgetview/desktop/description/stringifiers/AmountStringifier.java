package com.budgetview.desktop.description.stringifiers;

import com.budgetview.desktop.description.Formatting;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.GlobFieldComparator;

import java.util.Comparator;

public class AmountStringifier implements GlobStringifier {

  private DoubleField field;

  public AmountStringifier(DoubleField field) {
    this.field = field;
  }

  public String toString(Glob glob, GlobRepository repository) {
    if (glob == null) {
      return "";
    }
    Double value = glob.get(field);
    if (value == null) {
      return "";
    }
    return Formatting.DECIMAL_FORMAT.format(value);
  }

  public Comparator<Glob> getComparator(GlobRepository repository) {
    return new GlobFieldComparator(field);
  }
}
