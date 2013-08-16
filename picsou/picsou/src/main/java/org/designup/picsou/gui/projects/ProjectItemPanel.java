package org.designup.picsou.gui.projects;

import org.designup.picsou.gui.browsing.BrowsingService;
import org.designup.picsou.gui.components.AmountEditor;
import org.designup.picsou.gui.components.JPopupButton;
import org.designup.picsou.gui.components.MonthSlider;
import org.designup.picsou.gui.components.PopupGlobFunctor;
import org.designup.picsou.gui.components.charts.SimpleGaugeView;
import org.designup.picsou.gui.components.images.GlobImageLabelView;
import org.designup.picsou.gui.components.tips.ErrorTip;
import org.designup.picsou.gui.description.stringifiers.MonthFieldListStringifier;
import org.designup.picsou.gui.description.stringifiers.MonthRangeFormatter;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.model.ProjectItemStat;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.ProjectItem;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.editors.GlobMultiLineTextEditor;
import org.globsframework.gui.editors.GlobTextEditor;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.DisposableGroup;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.views.GlobButtonView;
import org.globsframework.gui.views.GlobHtmlView;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.model.utils.GlobListFunctor;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ProjectItemPanel implements Disposable {

  private final Key itemKey;
  private final GlobRepository parentRepository;
  private final LocalGlobRepository localRepository;
  private final Directory directory;

  private JPanel enclosingPanel;
  private JPanel viewPanel;
  private JPanel editionPanel;
  private DisposableGroup disposables = new DisposableGroup();
  private GlobTextEditor nameField;
  private JTextField amountEditorField;

  public ProjectItemPanel(Glob item, GlobRepository parentRepository, Directory directory) {
    this.itemKey = item.getKey();
    this.parentRepository = parentRepository;
    this.localRepository =
      LocalGlobRepositoryBuilder.init(parentRepository)
        .copy(ProjectItem.TYPE, Month.TYPE, CurrentMonth.TYPE)
        .get();
    this.directory = directory;

    this.enclosingPanel = new JPanel(new BorderLayout());
    createViewPanel();
    if (Strings.isNotEmpty(item.get(ProjectItem.LABEL))) {
      showPanel(viewPanel);
    }
    else {
      showEditPanel();
    }
  }

  private void showPanel(JPanel panel) {
    this.enclosingPanel.removeAll();
    this.enclosingPanel.add(panel, BorderLayout.CENTER);
    GuiUtils.revalidate(enclosingPanel);
  }

  private void createViewPanel() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/projects/projectItemViewPanel.splits",
                                                      parentRepository, directory);

    ModifyAction modifyAction = new ModifyAction();

    JPopupMenu itemPopup = new JPopupMenu();
    itemPopup.add(modifyAction);
    itemPopup.add(new DeleteItemAction(itemKey));
    PopupGlobFunctor functor = new PopupGlobFunctor(itemPopup);
    GlobButtonView itemButton = GlobButtonView.init(ProjectItem.LABEL, parentRepository, directory, functor)
      .forceSelection(itemKey);
    functor.setComponent(itemButton.getComponent());
    builder.add("itemButton", itemButton);
    disposables.add(itemButton);

    GlobImageLabelView imageLabel = GlobImageLabelView.init(ProjectItem.IMAGE_PATH, parentRepository, directory)
      .setAutoHide(true)
      .forceKeySelection(itemKey);
    builder.add("imageLabel", imageLabel.getLabel());

    GlobLabelView monthLabel = GlobLabelView.init(ProjectItem.TYPE, parentRepository, directory,
                                                  new MonthFieldListStringifier(ProjectItem.MONTH, MonthRangeFormatter.COMPACT))
      .forceSelection(itemKey);
    builder.add("monthLabel", monthLabel);
    disposables.add(monthLabel);

    Key itemStatKey = Key.create(ProjectItemStat.TYPE, itemKey.get(ProjectItem.ID));

    GlobLabelView actualLabel = GlobLabelView.init(ProjectItemStat.ACTUAL_AMOUNT, parentRepository, directory)
      .forceSelection(itemStatKey);
    builder.add("actualLabel", actualLabel);
    disposables.add(actualLabel);

    SimpleGaugeView itemGauge = SimpleGaugeView.init(ProjectItemStat.ACTUAL_AMOUNT, ProjectItemStat.PLANNED_AMOUNT,
                                                     parentRepository, directory);
    itemGauge.setKey(itemStatKey);
    builder.add("itemGauge", itemGauge.getComponent());
    disposables.add(itemGauge);

    GlobLabelView plannedLabel = GlobLabelView.init(ProjectItem.PLANNED_AMOUNT, parentRepository, directory)
      .forceSelection(itemKey);
    builder.add("plannedLabel", plannedLabel);
    disposables.add(plannedLabel);

    builder.add("handler", new HyperlinkHandler(directory));

    GlobButtonView link = GlobButtonView.init(ProjectItem.URL, parentRepository, directory, new GotoUrlFunctor())
      .setAutoHideIfEmpty(true)
      .forceSelection(itemKey);
    builder.add("link", link);
    disposables.add(link);

    GlobHtmlView description = GlobHtmlView.init(ProjectItem.DESCRIPTION, localRepository, directory)
      .setAutoHideIfEmpty(true)
      .forceSelection(itemKey);
    builder.add("description", description);
    disposables.add(description);

    builder.add("modify", modifyAction);

    viewPanel = builder.load();
  }

  private void createEditionPanel() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/projects/projectItemEditionPanel.splits",
                                                      localRepository, directory);

    nameField = GlobTextEditor.init(ProjectItem.LABEL, localRepository, directory)
      .forceSelection(itemKey);
    builder.add("nameField", nameField);
    disposables.add(nameField);

    GlobImageLabelView imageLabel =
      GlobImageLabelView.init(ProjectItem.IMAGE_PATH, localRepository, directory)
      .forceKeySelection(itemKey);
    builder.add("imageLabel", imageLabel.getLabel());
    builder.add("imageActions", imageLabel.getPopupButton(Lang.get("projectView.item.edition.imageActions")));

    MonthSlider monthButton = new MonthSlider(itemKey, ProjectItem.MONTH, localRepository, directory);
    builder.add("monthEditor", monthButton);
    disposables.add(monthButton);

    AmountEditor amountEditor =
      new AmountEditor(ProjectItem.PLANNED_AMOUNT, localRepository, directory, false, null)
        .forceSelection(itemKey)
        .update(false, false);
    builder.add("amountEditor", amountEditor.getPanel());
    disposables.add(amountEditor);
    amountEditorField = amountEditor.getNumericEditor().getComponent();

    GlobTextEditor urlField = GlobTextEditor.init(ProjectItem.URL, localRepository, directory)
      .forceSelection(itemKey);
    builder.add("urlField", urlField);
    disposables.add(urlField);

    GlobMultiLineTextEditor descriptionField = GlobMultiLineTextEditor.init(ProjectItem.DESCRIPTION, localRepository, directory)
      .forceSelection(itemKey);
    builder.add("descriptionField", descriptionField);
    disposables.add(descriptionField);

    JPopupMenu actionsMenu = new JPopupMenu();
    actionsMenu.add(new DeleteItemAction(itemKey));
    JPopupButton actionsPopup = new JPopupButton(Lang.get("projectView.item.edition.actions"), actionsMenu);
    builder.add("actions", actionsPopup);


    builder.add("validate", new ValidateAction());
    builder.add("cancel", new CancelAction());

    builder.add("handler", new HyperlinkHandler(directory));

    editionPanel = builder.load();
  }

  private class ModifyAction extends AbstractAction {
    private ModifyAction() {
      super(Lang.get("projectView.item.view.modify"));
    }

    public void actionPerformed(ActionEvent e) {
      showEditPanel();
    }
  }

  private void showEditPanel() {
    if (editionPanel == null) {
      createEditionPanel();
    }
    localRepository.rollback();
    showPanel(editionPanel);
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if (Strings.isNullOrEmpty(nameField.getComponent().getText())) {
          GuiUtils.selectAndRequestFocus(nameField.getComponent());
        }
        else {
          GuiUtils.selectAndRequestFocus(amountEditorField);
        }
      }
    });
  }

  private boolean check() {
    JTextField nameField = this.nameField.getComponent();
    if (Strings.isNullOrEmpty(nameField.getText())) {
      ErrorTip.showLeft(nameField,
                        Lang.get("projectEdition.error.noItemName"),
                        directory);
      GuiUtils.selectAndRequestFocus(nameField);
      return false;
    }

    return true;
  }

  private class ValidateAction extends AbstractAction {
    private ValidateAction() {
      super(Lang.get("projectView.item.edition.validate"));
    }

    public void actionPerformed(ActionEvent e) {
      if (!check()) {
        return;
      }
      localRepository.commitChanges(false);
      showPanel(viewPanel);
    }
  }

  private class CancelAction extends AbstractAction {
    private CancelAction() {
      super(Lang.get("projectView.item.edition.cancel"));
    }

    public void actionPerformed(ActionEvent e) {
      showPanel(viewPanel);
      localRepository.rollback();
      Glob item = parentRepository.get(itemKey);
      if (Strings.isNullOrEmpty(item.get(ProjectItem.LABEL))) {
        parentRepository.delete(itemKey);
      }
    }
  }

  public void dispose() {
    disposables.dispose();
  }

  public JPanel getPanel() {
    return enclosingPanel;
  }

  private class DeleteItemAction extends AbstractAction {
    private Key projectItemKey;

    private DeleteItemAction(Key projectItemKey) {
      super(Lang.get("delete"));
      this.projectItemKey = projectItemKey;
    }

    public void actionPerformed(ActionEvent actionEvent) {
      parentRepository.delete(projectItemKey);
    }
  }

  private class GotoUrlFunctor implements GlobListFunctor {
    public void run(GlobList list, GlobRepository repository) {
      if (list.size() != 1) {
        return;
      }
      String url = list.getFirst().get(ProjectItem.URL);
      if (Strings.isNotEmpty(url)) {
        directory.get(BrowsingService.class).launchBrowser(url);
      }
    }
  }
}
