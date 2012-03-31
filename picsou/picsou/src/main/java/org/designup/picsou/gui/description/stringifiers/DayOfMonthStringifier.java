package org.designup.picsou.gui.description.stringifiers;

import org.designup.picsou.model.Bank;
import org.designup.picsou.model.DayOfMonth;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.GlobFieldComparator;
import org.globsframework.utils.Utils;

import java.util.Comparator;

public class DayOfMonthStringifier implements GlobStringifier {
  public String toString(Glob glob, GlobRepository repository) {
    return glob.get(DayOfMonth.LABEL);
  }

  public Comparator<Glob> getComparator(GlobRepository repository) {
    return new GlobFieldComparator(DayOfMonth.ID);
  }
}
