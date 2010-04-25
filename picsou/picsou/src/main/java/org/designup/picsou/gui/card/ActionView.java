package org.designup.picsou.gui.card;

import org.designup.picsou.gui.card.widgets.TextNavigationWidget;
import org.designup.picsou.gui.card.widgets.ImportWidget;
import org.designup.picsou.gui.help.HelpAction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class ActionView extends WidgetView {

  public ActionView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/home/actionView.splits",
                                                      repository, directory);

    add("import", builder, new ImportWidget(repository, directory));

    add("help", builder, new TextNavigationWidget("openHelp",
                                                  Lang.get("helpWidget.title"),
                                                  Lang.get("helpWidget.text"),
                                                  "home/help.png",
                                                  "home/help_rollover.png",
                                                  new HelpAction(Lang.get("helpWidget.title"),
                                                                 "index", directory),
                                                  false, repository, directory));

    parentBuilder.add("actionView", builder);
  }
}