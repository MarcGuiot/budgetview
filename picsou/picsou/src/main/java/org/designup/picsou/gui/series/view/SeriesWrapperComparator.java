package org.designup.picsou.gui.series.view;

import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.utils.Utils;

import java.util.Comparator;

public class SeriesWrapperComparator implements Comparator<Glob> {
  private static final Integer ALL_CATEGORY_ID = SeriesWrapper.ALL_ID;
  private static final Integer UNCATEGORIZED_ID = SeriesWrapper.UNCATEGORIZED_ID;

  private GlobRepository parentRepository;
  private GlobRepository localRepository;
  private GlobStringifier nameStringifier;

  public SeriesWrapperComparator(GlobRepository parentRepository, GlobRepository localRepository, GlobStringifier stringifier) {
    this.parentRepository = parentRepository;
    this.localRepository = localRepository;
    this.nameStringifier = stringifier;
  }

  public int compare(Glob wrapper1, Glob wrapper2) {
    if (Utils.equal(wrapper1.get(SeriesWrapper.ID), ALL_CATEGORY_ID)) {
      return -1;
    }
    if (Utils.equal(wrapper2.get(SeriesWrapper.ID), ALL_CATEGORY_ID)) {
      return 1;
    }
    if (Utils.equal(wrapper1.get(SeriesWrapper.ID), UNCATEGORIZED_ID)) {
      return -1;
    }
    if (Utils.equal(wrapper2.get(SeriesWrapper.ID), UNCATEGORIZED_ID)) {
      return 1;
    }

    Glob master1 = getMaster(wrapper1);
    Glob master2 = getMaster(wrapper2);

    boolean wrapper1IsMaster = master1 == null;
    boolean wrapper2IsMaster = master2 == null;

    if (wrapper1IsMaster && wrapper2IsMaster) {
      return compareMasters(wrapper1, wrapper2);
    }

    if (!wrapper1IsMaster && !wrapper2IsMaster && !master1.equals(master2)) {
      return compareMasters(master1, master2);
    }

    if (wrapper1IsMaster && !wrapper2IsMaster) {
      if (wrapper1.equals(master2)) {
        return -1;
      }
      return compareMasters(wrapper1, master2);
    }

    if (!wrapper1IsMaster && wrapper2IsMaster) {
      if (master1.equals(wrapper2)) {
        return 1;
      }
      return compareMasters(master1, wrapper2);
    }

    String name1 = getName(wrapper1);
    String name2 = getName(wrapper2);

    return name1.compareTo(name2);
  }

  private int compareMasters(Glob master1, Glob master2) {
    return Integer.signum(master1.get(SeriesWrapper.ITEM_ID) - master2.get(SeriesWrapper.ITEM_ID));
  }

  private Glob getMaster(Glob wrapper) {
    Integer masterId = wrapper.get(SeriesWrapper.MASTER);
    if (masterId != null) {
      return localRepository.get(Key.create(SeriesWrapper.TYPE, masterId));
    }
    return null;
  }

  protected String getName(Glob category) {
    return nameStringifier.toString(category, parentRepository);
  }
}
