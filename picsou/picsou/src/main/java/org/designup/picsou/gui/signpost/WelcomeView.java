package org.designup.picsou.gui.signpost;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.signpost.actions.GotoDemoAccountAction;
import org.designup.picsou.model.SignpostStatus;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.actions.SetBooleanAction;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class WelcomeView extends View {
  public WelcomeView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/signpost/welcomeView.splits",
                                                      repository, directory);

    builder.add("demo", new GotoDemoAccountAction(directory));
    builder.add("start", new SetBooleanAction(SignpostStatus.KEY,
                                                         SignpostStatus.WELCOME_SHOWN,
                                                         true, "Start",
                                                         repository));

    parentBuilder.add("welcomeView", builder);
  }
}