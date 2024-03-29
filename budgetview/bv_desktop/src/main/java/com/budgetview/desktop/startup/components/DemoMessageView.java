package com.budgetview.desktop.startup.components;

import com.budgetview.desktop.View;
import com.budgetview.desktop.components.FooterBanner;
import com.budgetview.model.User;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.TypeChangeSetListener;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class DemoMessageView extends View {

  private FooterBanner banner;

  public DemoMessageView(GlobRepository repository, Directory directory) {
    super(repository, directory);

    Action action = new ExitAction(); 
    banner = new FooterBanner(Lang.get("demoMessage.label"), action, false, repository, directory);
    
    repository.addChangeListener(new TypeChangeSetListener(User.TYPE) {
      public void update(GlobRepository repository) {
        updateView();
      }
    });
    updateView();
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("demoMessageView", banner.getPanel());
  }
  
  private void updateView() {
    Glob user = repository.get(User.KEY);
    banner.setVisible((user != null) && user.isTrue(User.IS_DEMO_USER));
  }
  
  private class ExitAction extends AbstractAction {
    private ExitAction() {
      super(Lang.get("demoMessage.exit"));
    }

    public void actionPerformed(ActionEvent actionEvent) {
      directory.get(LogoutService.class).gotoAutologin();
    }
  }
}
