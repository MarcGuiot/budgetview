package org.globsframework.wicket.list;

import org.globsframework.model.Glob;
import org.globsframework.wicket.GlobPage;
import org.globsframework.wicket.GlobRepositoryLoader;
import org.globsframework.wicket.model.GlobListModel;
import wicket.Component;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;

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
