package org.designup.picsou.gui.help;

import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
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

  public HelpDialog(HelpSource source, GlobRepository repository, Directory directory) {
    this.source = source;
    createDialog(repository, directory);
  }

  private void createDialog(GlobRepository repository, Directory directory) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/helpDialog.splits",
                                                      repository, directory);
    title = builder.add("title", new JLabel());
    editor = builder.add("editor", new JEditorPane());

    editor.addHyperlinkListener(new HyperlinkHandler(directory));

    builder.add("home", homePageAction);
    builder.add("forward", forwardPageAction);
    builder.add("back", backPageAction);
    updateNavigationActions();

    JPanel panel = builder.load();

    dialog = PicsouDialog.createWithButton(directory.get(JFrame.class), false, panel, new CloseAction(),
                                           directory);
    dialog.pack();
  }

  public void show(String ref) {
    forwardPages.clear();
    openPage(ref, true);
    if (!dialog.isVisible()) {
      GuiUtils.showCentered(dialog);
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

  private class CloseAction extends AbstractAction {
    public CloseAction() {
      super(Lang.get("close"));
    }

    public void actionPerformed(ActionEvent e) {
      dialog.setVisible(false);
    }
  }

}
