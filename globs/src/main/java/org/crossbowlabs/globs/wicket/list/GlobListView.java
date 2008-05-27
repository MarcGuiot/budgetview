package org.crossbowlabs.globs.wicket.list;

import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.wicket.GlobPage;
import org.crossbowlabs.globs.wicket.GlobRepositoryLoader;
import org.crossbowlabs.globs.wicket.model.GlobListModel;
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
