package org.designup.picsou.gui.projects;

import org.designup.picsou.gui.browsing.BrowsingService;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.MonthSlider;
import org.designup.picsou.gui.components.PopupGlobFunctor;
import org.designup.picsou.gui.components.SingleMonthAdapter;
import org.designup.picsou.gui.components.charts.SimpleGaugeView;
import org.designup.picsou.gui.components.images.GlobImageLabelView;
import org.designup.picsou.gui.components.images.IconFactory;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.model.PeriodSeriesStat;
import org.designup.picsou.gui.model.ProjectItemStat;
import org.designup.picsou.gui.model.ProjectStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.gui.projects.components.DefaultPictureIcon;
import org.designup.picsou.gui.projects.utils.ImageStatusUpdater;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.actions.ToggleBooleanAction;
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
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.model.utils.GlobListActionAdapter;
import org.globsframework.model.utils.GlobListFunctor;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public abstract class ProjectItemPanel implements Disposable {

  protected final Key itemKey;
  protected final GlobRepository parentRepository;
  protected final LocalGlobRepository localRepository;
  protected final Directory directory;
  protected DisposableGroup disposables = new DisposableGroup();
  private JPanel enclosingPanel;
  private JPanel viewPanel;
  private JPanel editionPanel;

  public ProjectItemPanel(Glob item, GlobRepository parentRepository, Directory directory) {
    this.itemKey = item.getKey();
    this.parentRepository = parentRepository;
    this.localRepository =
      LocalGlobRepositoryBuilder.init(parentRepository)
        .copy(ProjectItem.TYPE, ProjectTransfer.TYPE, Month.TYPE, CurrentMonth.TYPE, Account.TYPE)
        .get();
    this.directory = directory;

    this.enclosingPanel = new JPanel(new BorderLayout());
    createViewPanel();
    if (isNewItem(item)) {
      showEditPanel();
    }
    else {
      showPanel(viewPanel);
    }
  }

  protected abstract boolean isNewItem(Glob item);

  protected abstract JPanel createEditionPanel();

  protected abstract void initEditionFocus();

  protected abstract boolean check();

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

  protected IconFactory createDefaultIconFactory(final GlobsPanelBuilder builder) {
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
      editionPanel = createEditionPanel();
    }
    localRepository.rollback();
    showPanel(editionPanel);
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        initEditionFocus();
      }
    });
  }

  protected class ValidateAction extends AbstractAction {
    public ValidateAction() {
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

  protected class CancelAction extends AbstractAction {
    public CancelAction() {
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
      GlobList transactions = GlobList.EMPTY;
      Glob subSeries = parentRepository.findLinkTarget(projectItem, ProjectItem.SUB_SERIES);
      if (subSeries != null) {
        transactions = parentRepository.findLinkedTo(subSeries, Transaction.SUB_SERIES);
      }
      else {
        Glob series = parentRepository.findLinkTarget(projectItem, ProjectItem.SERIES);
        if (series != null) {
          transactions = parentRepository.findLinkedTo(series, Transaction.SERIES);
        }
      }
      directory.get(NavigationService.class).gotoData(transactions);
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
