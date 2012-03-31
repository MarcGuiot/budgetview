package org.designup.picsou.gui.description.stringifiers;

import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;

import java.util.SortedSet;

public class MonthFieldListStringifier implements GlobListStringifier {

  private IntegerField monthField;

  public MonthFieldListStringifier(IntegerField monthField) {
    this.monthField = monthField;
  }

  public String toString(GlobList list, GlobRepository repository) {
    SortedSet<Integer> months = list.getSortedSet(monthField);
    return MonthListStringifier.toString(months);
  }
}
