package org.designup.picsou.gui.components.wizard;

import org.designup.picsou.gui.components.CloseAction;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.globsframework.utils.exceptions.InvalidState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class WizardDialog {

  private List<WizardPage> pages = new ArrayList<WizardPage>();
  private DefaultComboBoxModel comboModel;
  private GlobRepository repository;
  private Directory directory;
  private JLabel title;
  private PicsouDialog dialog;
  private JPanel contentPanel;
  private CardLayout cardLayout;
  private int currentIndex = -1;
  private NextPageAction next = new NextPageAction();
  private PreviousPageAction previous = new PreviousPageAction();
  private boolean shown;

  public WizardDialog(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    this.comboModel = new DefaultComboBoxModel();
    createDialog();
  }

  public void add(WizardPage page) {
    if (shown) {
      throw new InvalidState("Pages cannot be added when the wizard has been shown");
    }
    checkId(page);
    page.init();
    pages.add(page);
    comboModel.addElement(page);
    contentPanel.add(page.getId(), page.getPanel());
  }

  private void checkId(WizardPage page) {
    String id = page.getId();
    if (Strings.isNullOrEmpty(id)) {
      throw new InvalidParameter("Page " + page.getTitle() + " must have an id");
    }
    for (WizardPage wizardPage : pages) {
      if (id.equals(wizardPage.getId())) {
        throw new InvalidParameter("Id " + id + " used twice, for pages '" +
                                   page.getTitle() + "' and '" + wizardPage + "'");
      }
    }
  }

  public void show() {
    if (pages.isEmpty()) {
      throw new InvalidState("No pages in wizard");
    }

    for (WizardPage page : pages) {
      page.updateBeforeDisplay();
    }

    if (!shown) {
      showPage(0);
    }

    shown = true;

    GuiUtils.showCentered(dialog);
  }

  private void createDialog() {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/wizardDialog.splits", repository, directory);
    title = builder.add("title", new JLabel()).getComponent();

    cardLayout = new CardLayout();
    contentPanel = new JPanel(cardLayout);

    builder.add("content", contentPanel);

    builder.add("next", next);
    builder.add("previous", previous);

    final JComboBox combo = new JComboBox(comboModel);
    combo.setRenderer(new ComboRenderer());
    combo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int index = combo.getSelectedIndex();
        if (index != currentIndex) {
          showPage(index);
        }
      }
    });
    builder.add("combo", combo);

    JPanel dialogPanel = builder.load();

    dialog = PicsouDialog.create(directory.get(JFrame.class), false, directory);
    dialog.addPanelWithButtons(dialogPanel, new ValidateAction(), new CloseAction(dialog));
    dialog.pack();
  }

  private void showPage(int pageIndex) {
    currentIndex = pageIndex;

    WizardPage currentPage = pages.get(currentIndex);
    comboModel.setSelectedItem(currentPage);

    title.setText(currentPage.getTitle());
    cardLayout.show(contentPanel, currentPage.getId());

    next.setEnabled(currentIndex < pages.size() - 1);
    previous.setEnabled(currentIndex > 0);

    currentPage.updateAfterDisplay();
  }

  private class NextPageAction extends AbstractAction {
    private NextPageAction() {
      super(Lang.get("wizard.next"));
    }

    public void actionPerformed(ActionEvent e) {
      showPage(++currentIndex);
    }
  }

  private class PreviousPageAction extends AbstractAction {
    private PreviousPageAction() {
      super(Lang.get("wizard.previous"));
    }

    public void actionPerformed(ActionEvent e) {
      showPage(--currentIndex);
    }
  }

  private class ValidateAction extends AbstractAction {
    private ValidateAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      for (WizardPage page : pages) {
        page.applyChanges();
      }
      dialog.setVisible(false);
    }
  }

  private static class ComboRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      WizardPage page = (WizardPage)value;
      if (page == null) {
        setText("");
      }
      setText(page.getTitle());
      return this;
    }
  }

}
