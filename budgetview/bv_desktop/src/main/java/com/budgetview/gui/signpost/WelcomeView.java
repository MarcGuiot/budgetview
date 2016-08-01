package com.budgetview.gui.signpost;

import com.budgetview.gui.View;
import com.budgetview.gui.signpost.actions.GotoDemoAccountAction;
import com.budgetview.model.SignpostStatus;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.actions.SetBooleanAction;
import org.globsframework.gui.splits.ImageLocator;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class WelcomeView extends View {
  public WelcomeView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/signpost/welcomeView.splits",
                                                      repository, directory);

    ImageIcon image = directory.get(ImageLocator.class).get(Lang.getFilePath("welcome", "welcome.png"));
    builder.add("image", new JLabel(image));

    builder.add("demo", new GotoDemoAccountAction(directory));
    builder.add("start", new SetBooleanAction(SignpostStatus.KEY,
                                              SignpostStatus.WELCOME_SHOWN,
                                              true, "Start",
                                              repository));

    parentBuilder.add("welcomeView", builder);
  }
}
