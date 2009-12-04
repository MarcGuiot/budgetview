package org.designup.picsou.gui.help;

import org.designup.picsou.gui.components.CloseAction;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Stack;

public class HelpDialog {
  private PicsouDialog dialog;
  private JLabel title;
  private JEditorPane editor;
  private Action homePageAction = new HomePageAction();
  private Action forwardPageAction = new ForwardPageAction();
  private Action backPageAction = new BackPageAction();

  private HelpSource source;

  private String currentPage;
  private Stack<String> backPages = new Stack<String>();
  private Stack<String> forwardPages = new Stack<String>();

  public HelpDialog(HelpSource source, GlobRepository repository, Directory directory, Window owner) {
    this.source = source;
    createDialog(owner, repository, directory);
  }

  private void createDialog(Window owner, GlobRepository repository, Directory directory) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/helpDialog.splits",
                                                      repository, directory);
    title = builder.add("title", new JLabel()).getComponent();
    editor = builder.add("editor", new JEditorPane()).getComponent();

    editor.addHyperlinkListener(new HyperlinkHandler(directory, owner));

    builder.add("home", homePageAction);
    builder.add("forward", forwardPageAction);
    builder.add("back", backPageAction);
    updateNavigationActions();

    JPanel panel = builder.load();

    dialog = PicsouDialog.create(owner, false, directory);
    dialog.setPanelAndButton(panel, new CloseAction(dialog));
    dialog.pack();
  }

  public void show(String ref) {
    forwardPages.clear();
    openPage(ref, true);
    if (!dialog.isVisible()) {
      dialog.showCentered();
    }
  }

  private void openPage(String ref, boolean updateHistory) {
    title.setText(source.getTitle(ref));
    editor.setText(source.getContent(ref));
    editor.setCaretPosition(0);

    if (updateHistory && (currentPage != null)) {
      backPages.push(currentPage);
    }
    currentPage = ref;
    updateNavigationActions();
  }

  public boolean isVisible() {
    return dialog.isVisible();
  }

  public void close() {
    dialog.setVisible(false);
  }

  private class HomePageAction extends AbstractAction {
    private static final String INDEX_PAGE = "index";

    private HomePageAction() {
      super(Lang.get("help.index"));
    }

    public void actionPerformed(ActionEvent e) {
      openPage(INDEX_PAGE, true);
    }
  }

  private class BackPageAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      if (currentPage != null) {
        forwardPages.push(currentPage);
      }
      openPage(backPages.pop(), false);
    }
  }

  private class ForwardPageAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      if (currentPage != null) {
        backPages.push(currentPage);
      }
      openPage(forwardPages.pop(), false);
    }
  }

  private void updateNavigationActions() {
    homePageAction.setEnabled(!HomePageAction.INDEX_PAGE.equals(currentPage));
    backPageAction.setEnabled(!backPages.isEmpty());
    forwardPageAction.setEnabled(!forwardPages.isEmpty());
  }
}
