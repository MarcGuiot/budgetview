package com.budgetview.desktop.description.stringifiers;

import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;

import java.util.SortedSet;

public class MonthFieldListStringifier implements GlobListStringifier {

  private IntegerField monthField;
  private MonthRangeFormatter formatter;

  public MonthFieldListStringifier(IntegerField monthField, MonthRangeFormatter formatter) {
    this.monthField = monthField;
    this.formatter = formatter;
  }

  public String toString(GlobList list, GlobRepository repository) {
    SortedSet<Integer> months = list.getSortedSet(monthField);
    return MonthListStringifier.toString(months, formatter);
  }
}
