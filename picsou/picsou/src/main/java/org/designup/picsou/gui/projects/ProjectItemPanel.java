package org.designup.picsou.gui.projects;

import org.designup.picsou.gui.browsing.BrowsingService;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.AmountEditor;
import org.designup.picsou.gui.components.MonthSlider;
import org.designup.picsou.gui.components.PopupGlobFunctor;
import org.designup.picsou.gui.components.SingleMonthAdapter;
import org.designup.picsou.gui.components.charts.SimpleGaugeView;
import org.designup.picsou.gui.components.images.GlobImageLabelView;
import org.designup.picsou.gui.components.images.IconFactory;
import org.designup.picsou.gui.components.tips.ErrorTip;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.model.ProjectItemStat;
import org.designup.picsou.gui.projects.components.DefaultPictureIcon;
import org.designup.picsou.gui.projects.utils.ImageStatusUpdater;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.ProjectItem;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.actions.ToggleBooleanAction;
import org.globsframework.gui.editors.GlobMultiLineTextEditor;
import org.globsframework.gui.editors.GlobNumericEditor;
import org.globsframework.gui.editors.GlobTextEditor;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.DisposableGroup;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.utils.AbstractGlobBooleanUpdater;
import org.globsframework.gui.utils.GlobBooleanNodeStyleUpdater;
import org.globsframework.gui.views.GlobButtonView;
import org.globsframework.gui.views.GlobHtmlView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.model.utils.GlobListActionAdapter;
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
  private GlobNumericEditor monthCountEditor;

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
    ShowTransactionsAction showTransactionsAction = new ShowTransactionsAction("projectView.item.edition.actions.showTransactions");
    final ToggleBooleanAction activateAction = new ToggleBooleanAction(itemKey, ProjectItem.ACTIVE,
                                                                       Lang.get("projectEdition.setActive.textForTrue"),
                                                                       Lang.get("projectEdition.setActive.textForFalse"),
                                                                       parentRepository);
    disposables.add(activateAction);

    JPopupMenu itemPopup = new JPopupMenu();
    itemPopup.add(modifyAction);
    itemPopup.add(activateAction);
    itemPopup.addSeparator();
    itemPopup.add(showTransactionsAction);
    itemPopup.addSeparator();
    itemPopup.add(new DeleteItemAction(itemKey));
    PopupGlobFunctor functor = new PopupGlobFunctor(itemPopup);
    GlobButtonView itemButton = GlobButtonView.init(ProjectItem.LABEL, parentRepository, directory, functor)
      .forceSelection(itemKey);
    functor.setComponent(itemButton.getComponent());
    SplitsNode<JButton> itemButtonNode = builder.add("itemButton", itemButton.getComponent());
    disposables.add(itemButton);
    GlobBooleanNodeStyleUpdater styleUpdater =
      new GlobBooleanNodeStyleUpdater(ProjectItem.ACTIVE, itemButtonNode,
                                      "activeProjectItem", "inactiveProjectItem",
                                      parentRepository);
    disposables.add(styleUpdater);

    GlobImageLabelView imageLabel =
      GlobImageLabelView.init(ProjectItem.PICTURE, ProjectView.MAX_PICTURE_SIZE, parentRepository, directory)
        .setAutoHide(true)
        .setDefaultIconFactory(createDefaultIconFactory(builder))
        .forceKeySelection(itemKey);
    builder.add("imageLabel", imageLabel.getLabel());
    disposables.add(new ImageStatusUpdater(itemKey, ProjectItem.ACTIVE, imageLabel, parentRepository));

    MonthSlider monthSlider = new MonthSlider(new SingleMonthAdapter(ProjectItem.MONTH), parentRepository, directory);
    monthSlider.setKey(itemKey);
    builder.add("monthSlider", monthSlider);
    disposables.add(monthSlider);

    Key itemStatKey = Key.create(ProjectItemStat.TYPE, itemKey.get(ProjectItem.ID));

    GlobButtonView actualAmount = GlobButtonView.init(ProjectItemStat.ACTUAL_AMOUNT, parentRepository, directory,
                                                      new GlobListActionAdapter(showTransactionsAction))
      .forceSelection(itemStatKey);
    builder.add("actualAmount", actualAmount);
    disposables.add(actualAmount);

    GlobButtonView plannedAmount = GlobButtonView.init(ProjectItemStat.PLANNED_AMOUNT, parentRepository, directory,
                                                       new GlobListActionAdapter(modifyAction))
      .forceSelection(itemStatKey);
    SplitsNode<JButton> plannedAmountNode = builder.add("plannedAmount", plannedAmount.getComponent());
    disposables.add(plannedAmount);

    GlobBooleanNodeStyleUpdater plannedAmountUpdater =
      new GlobBooleanNodeStyleUpdater(ProjectItem.ACTIVE, plannedAmountNode,
                                      "activeItemAmount", "inactiveItemAmount",
                                      parentRepository);
    disposables.add(plannedAmountUpdater);

    SimpleGaugeView itemGauge = SimpleGaugeView.init(ProjectItemStat.ACTUAL_AMOUNT, ProjectItemStat.PLANNED_AMOUNT,
                                                     parentRepository, directory);
    itemGauge.setKey(itemStatKey);
    builder.add("itemGauge", itemGauge.getComponent());
    disposables.add(itemGauge);

    builder.addToggleEditor("activeToggle", ProjectItem.ACTIVE)
      .forceSelection(itemKey);

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

    JLabel categorizationWarning = new JLabel(Lang.get("projectView.item.categorizationWarning.message"));
    builder.add("categorizationWarning", categorizationWarning);
    Action categorizationWarningAction = new ShowTransactionsAction("projectView.item.categorizationWarning.show");
    builder.add("categorizationWarningAction", categorizationWarningAction);
    CategorizationWarningUpdater updater = new CategorizationWarningUpdater(itemStatKey,
                                                                            categorizationWarning,
                                                                            categorizationWarningAction);
    disposables.add(updater);

    viewPanel = builder.load();

    styleUpdater.setKey(itemKey);
    plannedAmountUpdater.setKey(itemKey);
  }

  private void createEditionPanel() {
    final GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/projects/projectItemEditionPanel.splits",
                                                            localRepository, directory);

    ValidateAction validate = new ValidateAction();

    nameField = GlobTextEditor.init(ProjectItem.LABEL, localRepository, directory)
      .forceSelection(itemKey)
      .setValidationAction(validate);
    builder.add("nameField", nameField);
    disposables.add(nameField);

    GlobImageLabelView imageLabel =
      GlobImageLabelView.init(ProjectItem.PICTURE, ProjectView.MAX_PICTURE_SIZE, localRepository, directory)
        .setDefaultIconFactory(createDefaultIconFactory(builder))
        .forceKeySelection(itemKey);
    builder.add("imageLabel", imageLabel.getLabel());
    builder.add("imageActions", imageLabel.getPopupButton(Lang.get("projectView.item.edition.imageActions")));

    MonthSlider monthSlider = new MonthSlider(new SingleMonthAdapter(ProjectItem.MONTH) {
      public String convertToString(Integer monthId) {
        return Month.getFullMonthLabelWith4DigitYear(monthId);
      }
    }, localRepository, directory);
    monthSlider.setKey(itemKey);
    builder.add("monthEditor", monthSlider);
    disposables.add(monthSlider);

    AmountEditor amountEditor =
      new AmountEditor(ProjectItem.PLANNED_AMOUNT, localRepository, directory, false, null)
        .forceSelection(itemKey)
        .addAction(validate)
        .update(false, false);
    builder.add("amountEditor", amountEditor.getPanel());
    disposables.add(amountEditor);
    amountEditorField = amountEditor.getNumericEditor().getComponent();

    monthCountEditor = GlobNumericEditor.init(ProjectItem.MONTH_COUNT, localRepository, directory)
      .setPositiveNumbersOnly(true)
      .forceSelection(itemKey)
      .setValidationAction(validate);
    builder.add("monthCountEditor", monthCountEditor);
    disposables.add(monthCountEditor);

    GlobTextEditor urlField = GlobTextEditor.init(ProjectItem.URL, localRepository, directory)
      .forceSelection(itemKey);
    builder.add("urlField", urlField);
    disposables.add(urlField);

    GlobMultiLineTextEditor descriptionField = GlobMultiLineTextEditor.init(ProjectItem.DESCRIPTION, localRepository, directory)
      .forceSelection(itemKey);
    builder.add("descriptionField", descriptionField);
    disposables.add(descriptionField);

    builder.add("validate", validate);
    builder.add("cancel", new CancelAction());

    builder.add("handler", new HyperlinkHandler(directory));

    editionPanel = builder.load();
  }

  private IconFactory createDefaultIconFactory(final GlobsPanelBuilder builder) {
    return new IconFactory() {
      public Icon createIcon(Dimension size) {
        DefaultPictureIcon defaultIcon = new DefaultPictureIcon(size, directory);
        builder.addDisposable(defaultIcon);
        return defaultIcon;
      }
    };
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

    Glob projectItem = localRepository.get(itemKey);
    Integer count = projectItem.get(ProjectItem.MONTH_COUNT);
    if (count == null || count < 1) {
      JTextField monthCountField = monthCountEditor.getComponent();
      ErrorTip.showLeft(monthCountField,
                        Lang.get("projectEdition.error.invalidMonthCount"),
                        directory);
      GuiUtils.selectAndRequestFocus(monthCountField);
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

  private class ShowTransactionsAction extends AbstractAction {
    private ShowTransactionsAction(String messageKey) {
      super(Lang.get(messageKey));
    }

    public void actionPerformed(ActionEvent e) {
      Glob projectItem = parentRepository.get(itemKey);
      Glob subSeries = parentRepository.findLinkTarget(projectItem, ProjectItem.SUB_SERIES);
      if (subSeries != null) {
        GlobList transactions = parentRepository.findLinkedTo(subSeries, Transaction.SUB_SERIES);
        directory.get(NavigationService.class).gotoData(transactions);
      }
    }
  }

  private class CategorizationWarningUpdater extends AbstractGlobBooleanUpdater {

    private JLabel label;
    private Action action;

    public CategorizationWarningUpdater(Key itemKey, JLabel label, Action categorizationWarningAction) {
      super(ProjectItemStat.CATEGORIZATION_WARNING, parentRepository);
      this.label = label;
      this.action = categorizationWarningAction;
      setKey(itemKey);
    }

    protected void doUpdate(boolean showWarning) {
      label.setVisible(showWarning);
      action.setEnabled(showWarning);
    }
  }
}
