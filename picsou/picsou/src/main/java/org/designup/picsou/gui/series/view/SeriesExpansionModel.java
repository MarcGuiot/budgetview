package org.designup.picsou.gui.series.view;

import org.designup.picsou.gui.components.expansion.ExpandableTable;
import org.designup.picsou.gui.components.expansion.TableExpansionModel;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;

public class SeriesExpansionModel extends TableExpansionModel {
  public SeriesExpansionModel(GlobRepository repository, ExpandableTable table) {
    super(SeriesWrapper.TYPE, SeriesWrapper.ID, repository, table);
  }

  protected GlobMatcher getMasterMatcher() {
    return GlobMatchers.isNull(SeriesWrapper.MASTER);
  }

  protected boolean hasChildren(Integer id, GlobRepository repository) {
    return !repository.getAll(SeriesWrapper.TYPE, GlobMatchers.fieldEquals(SeriesWrapper.MASTER, id)).isEmpty();
  }

  public boolean isMaster(Glob glob) {
    return glob.get(SeriesWrapper.MASTER) == null;
  }

  protected Integer getMasterId(Glob glob) {
    return glob.get(SeriesWrapper.MASTER);
  }

  public boolean isExpansionDisabled(Glob glob) {
    Integer id = glob.get(SeriesWrapper.ID);
    return (SeriesWrapper.ALL_ID.equals(id)) || (SeriesWrapper.UNCATEGORIZED_ID.equals(id));
  }

  public void setExpanded(Glob master) {
    if (isExpanded(master)) {
      return;
    }
    toggleExpansion(master);
  }
}
