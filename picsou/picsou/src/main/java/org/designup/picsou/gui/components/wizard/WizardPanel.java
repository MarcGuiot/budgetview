package org.designup.picsou.gui.components.wizard;

import org.designup.picsou.gui.help.HelpService;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Functor;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.globsframework.utils.exceptions.InvalidState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class WizardPanel {

  private GlobRepository repository;
  private Directory directory;
  private Functor completionCallback;

  private JPanel panel;
  private JPanel contentPanel;
  private CardLayout contentCardLayout;

  private List<WizardPage> pages = new ArrayList<WizardPage>();
  private int currentIndex = -1;

  private NextPageAction next = new NextPageAction();
  private PreviousPageAction previous = new PreviousPageAction();
  private ShowHelpAction help = new ShowHelpAction();

  public WizardPanel(GlobRepository repository, Directory directory, Functor completionCallback) {
    this.repository = repository;
    this.directory = directory;
    this.completionCallback = completionCallback;
    createPanel();
  }

  public void add(WizardPage page) {
    checkId(page);
    page.init();
    pages.add(page);
    contentPanel.add(page.getId(), page.getPanel());
  }

  public void showFirstPage() {
    if (pages.isEmpty()) {
      throw new InvalidState("There is no page to show");
    }
    showPage(0);
  }

  public JPanel getPanel() {
    if (panel == null) {
      createPanel();
    }
    return panel;
  }

  private void checkId(WizardPage page) {
    String id = page.getId();
    if (Strings.isNullOrEmpty(id)) {
      throw new InvalidParameter("Every wizard page must have an id");
    }
    for (WizardPage wizardPage : pages) {
      if (id.equals(wizardPage.getId())) {
        throw new InvalidParameter("Id " + id + " used twice");
      }
    }
  }

  private void createPanel() {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/utils/wizardPanel.splits", repository, directory);

    contentCardLayout = new CardLayout();
    contentPanel = new JPanel(contentCardLayout);

    builder.add("content", contentPanel);

    builder.add("next", next);
    builder.add("previous", previous);
    builder.add("help", help);

    panel = builder.load();
  }

  private void showPage(int pageIndex) {
    currentIndex = pageIndex;

    WizardPage currentPage = pages.get(currentIndex);
    currentPage.updateBeforeDisplay();

    contentCardLayout.show(contentPanel, currentPage.getId());
    help.setHelpRef(currentPage.getHelpRef());

    next.update();
    previous.update();

    currentPage.updateAfterDisplay();
  }

  private class NextPageAction extends AbstractAction {

    boolean lastPage;

    private NextPageAction() {
      super();
    }

    public void update() {
      lastPage = currentIndex >= pages.size() - 1;
      putValue(Action.NAME, Lang.get(lastPage ? "wizard.end" : "wizard.next"));
    }

    public void actionPerformed(ActionEvent event) {
      if (lastPage) {
        try {
          completionCallback.run();
        }
        catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
      else {
        showPage(++currentIndex);
      }
    }
  }

  private class PreviousPageAction extends AbstractAction {
    private PreviousPageAction() {
      super(Lang.get("wizard.previous"));
    }

    public void update() {
      setEnabled(currentIndex > 0);
    }

    public void actionPerformed(ActionEvent e) {
      showPage(--currentIndex);
    }
  }

  private class ShowHelpAction extends AbstractAction {

    private String helpRef;

    private ShowHelpAction() {
      super(Lang.get("help"));
    }

    public void setHelpRef(String helpRef) {
      this.helpRef = helpRef;
      setEnabled(Strings.isNotEmpty(helpRef));
    }

    public void actionPerformed(ActionEvent e) {
      directory.get(HelpService.class).show(helpRef, directory.get(JFrame.class));
    }
  }
}
