package org.designup.picsou.gui.series.view;

import org.designup.picsou.gui.components.expansion.ExpandableTable;
import org.designup.picsou.gui.components.expansion.TableExpansionModel;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.directory.Directory;

import static org.globsframework.model.utils.GlobMatchers.*;

public class SeriesExpansionModel extends TableExpansionModel {

  public SeriesExpansionModel(final GlobRepository repository, ExpandableTable table, Directory directory) {
    super(SeriesWrapper.TYPE, repository, directory, table);

    directory.get(SelectionService.class).addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        GlobList wrappers = selection.getAll(SeriesWrapper.TYPE);
        for (Glob wrapper : wrappers) {
          if (SeriesWrapper.isSeries(wrapper)) {
            setExpanded(repository.findLinkTarget(wrapper, SeriesWrapper.PARENT));
          }
          else if (SeriesWrapper.isSubSeries(wrapper)) {
            setExpanded(repository.findLinkTarget(wrapper, SeriesWrapper.PARENT));
          }
        }
      }
    }, SeriesWrapper.TYPE);
  }

  protected boolean hasChildren(Key key, GlobMatcher baseMatcher, GlobRepository repository) {
    return repository.contains(SeriesWrapper.TYPE,
                               and(fieldEquals(SeriesWrapper.PARENT, key.get(SeriesWrapper.ID)),
                                   baseMatcher));
  }

  public boolean isRoot(Glob wrapper) {
    return (wrapper.get(SeriesWrapper.PARENT) == null);
  }

  public boolean isParent(Glob wrapper) {
    return ((wrapper.get(SeriesWrapper.PARENT) == null) ||
            SeriesWrapperType.get(wrapper).equals(SeriesWrapperType.SERIES));
  }

  protected Key getParentKey(Glob glob) {
    Integer parentId = glob.get(SeriesWrapper.PARENT);
    if (parentId == null) {
      return null;
    }
    return Key.create(SeriesWrapper.TYPE, parentId);
  }

  public boolean isExpansionAuthorized(Glob glob) {
    Integer id = glob.get(SeriesWrapper.ID);
    return !(SeriesWrapper.ALL_ID.equals(id)) && !(SeriesWrapper.UNCATEGORIZED_ID.equals(id));
  }

  public void setExpanded(Glob master) {
    if (isExpanded(master)) {
      return;
    }
    toggleExpansion(master, false);
  }
}
