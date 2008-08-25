package org.globsframework.wicket.list;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.globsframework.model.Glob;
import org.globsframework.wicket.GlobPage;
import org.globsframework.wicket.GlobRepositoryLoader;
import org.globsframework.wicket.model.GlobListModel;

public abstract class GlobListView extends ListView {

  protected GlobListView(String id, GlobListModel listModel) {
    super(id, listModel);
  }

  protected void populateItem(final ListItem item) {
    Glob declaration = (Glob)item.getModelObject();
    GlobRepositoryLoader loader = ((GlobPage)getPage()).getRepositoryLoader();
    Component component = getItemComponent(declaration, loader);
    item.add(component);
  }

  protected abstract Component getItemComponent(Glob glob, GlobRepositoryLoader loader);
}
