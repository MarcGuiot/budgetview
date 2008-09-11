package org.designup.picsou.gui.description;

import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.designup.picsou.model.Month;

import java.util.SortedSet;
import java.util.Set;

public class MonthListStringifier implements GlobListStringifier {
  public String toString(GlobList months, GlobRepository repository) {
    Set<Integer> monthIds = months.getValueSet(Month.ID);
    return "";
  }
}
