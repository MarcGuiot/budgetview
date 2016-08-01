package com.budgetview.gui.description;

import com.budgetview.model.Series;
import com.budgetview.model.SeriesGroup;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobTreeComparator;
import org.globsframework.utils.Utils;

public class SeriesAndGroupsComparator extends GlobTreeComparator {
  private GlobRepository repository;

  public SeriesAndGroupsComparator(GlobRepository repository) {
    this.repository = repository;
  }

  protected Glob findParent(Glob glob) {
    if (SeriesGroup.TYPE.equals(glob.getType())) {
      return null;
    }
    return repository.findLinkTarget(glob, Series.GROUP);
  }

  protected int doCompareFields(Glob glob1, Glob glob2) {
    return Utils.compare(getName(glob1), getName(glob2));
  }

  private String getName(Glob glob) {
    if (SeriesGroup.TYPE.equals(glob.getType())) {
      return glob.get(SeriesGroup.NAME);
    }
    return glob.get(Series.NAME);
  }

}
