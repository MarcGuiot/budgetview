package org.designup.picsou.gui.projects;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.AmountEditor;
import org.designup.picsou.gui.components.CancelAction;
import org.designup.picsou.gui.components.MonthRangeBound;
import org.designup.picsou.gui.components.charts.Gauge;
import org.designup.picsou.gui.components.dialogs.ConfirmationDialog;
import org.designup.picsou.gui.components.dialogs.MonthChooserDialog;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.components.tips.ErrorTip;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.description.MonthFieldListStringifier;
import org.designup.picsou.gui.model.ProjectStat;
import org.designup.picsou.gui.projects.utils.ProjectItemComparator;
import org.designup.picsou.gui.time.TimeService;
import org.designup.picsou.model.*;
import org.designup.picsou.triggers.ProjectTrigger;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.editors.GlobTextEditor;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.utils.GlobRepeat;
import org.globsframework.gui.views.GlobButtonView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.model.utils.*;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.*;

public class ProjectEditionDialog {

  private LocalGlobRepository localRepository;
  private GlobRepository parentRepository;
  private Directory directory;

  private Key currentProjectKey;
  private Double actualAmount = 0.0;

  private PicsouDialog dialog;
  private JLabel titleLabel = new JLabel();
  private GlobRepeat repeat;
  private GlobTextEditor projectNameEditor;
  private Map<Key, JTextField> itemNameFields = new HashMap<Key, JTextField>();

  private JLabel totalActualLabel = new JLabel();
  private JLabel totalPlannedLabel = new JLabel();
  private Gauge gauge = new Gauge();

  public ProjectEditionDialog(GlobRepository parentRepository, Directory directory) {
    this(parentRepository, directory, directory.get(JFrame.class));
  }

  public ProjectEditionDialog(GlobRepository parentRepository, Directory directory, Window owner) {
    this.directory = directory;
    this.parentRepository = parentRepository;
    this.localRepository = LocalGlobRepositoryBuilder.init(parentRepository)
      .copy(Project.TYPE, ProjectItem.TYPE, Month.TYPE, Series.TYPE, SeriesBudget.TYPE)
      .get();
    this.localRepository.addTrigger(new ProjectTrigger());

    createDialog(owner);
    registerTotalUpdater();
  }

  private void registerTotalUpdater() {
    localRepository.addChangeListener(new TypeChangeSetListener(ProjectItem.TYPE) {
      protected void update(GlobRepository repository) {
        updateTotal();
      }
    });
  }

  private void updateTotal() {
    if ((currentProjectKey == null) || (!localRepository.contains(currentProjectKey))) {
      return;
    }
    double plannedAmount = 0.0;
    for (Glob item : localRepository.findLinkedTo(localRepository.get(currentProjectKey), ProjectItem.PROJECT)) {
      Double amount = item.get(ProjectItem.AMOUNT);
      if (amount != null) {
        plannedAmount += amount;
      }
    }

    totalActualLabel.setText(Formatting.toString(actualAmount, BudgetArea.EXTRAS));
    totalPlannedLabel.setText(Formatting.toString(plannedAmount, BudgetArea.EXTRAS));
    gauge.setValues(actualAmount, plannedAmount);
  }

  private void createDialog(Window owner) {
    dialog = PicsouDialog.create(owner, true, directory);

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/projects/projectEditionDialog.splits",
                                                      localRepository, directory);

    builder.add("title", titleLabel);

    projectNameEditor = GlobTextEditor.init(Project.NAME, localRepository, directory);
    builder.add("projectName", projectNameEditor.getComponent());

    repeat = builder.addRepeat("items",
                               ProjectItem.TYPE,
                               GlobMatchers.NONE,
                               new ProjectItemComparator(),
                               new ProjectItemRepeatFactory());

    builder.add("addItem", new AddItemAction());

    builder.add("totalActual", totalActualLabel);
    builder.add("totalPlanned", totalPlannedLabel);
    builder.add("gauge", gauge);

    dialog.addPanelWithButtons(builder.<JPanel>load(),
                               new OkAction(), new CancelAction(dialog),
                               new DeleteProjectAction());
    dialog.pack();
  }

  public void showNewProject() {
    titleLabel.setText(Lang.get("projectEdition.title.create"));
    actualAmount = 0.00;
    localRepository.rollback();
    currentProjectKey = localRepository.create(Project.TYPE).getKey();
    createItem();
    doShow();
  }

  public void show(Key projectKey) {
    titleLabel.setText(Lang.get("projectEdition.title.edit"));
    currentProjectKey = projectKey;
    updateActual();
    localRepository.rollback();
    doShow();
  }

  private void doShow() {
    projectNameEditor.forceSelection(currentProjectKey);
    repeat.setFilter(linkedTo(currentProjectKey, ProjectItem.PROJECT));
    dialog.showCentered();
  }

  private void updateActual() {
    if (currentProjectKey == null) {
      actualAmount = 0.00;
      return;
    }
    Glob stat = parentRepository.find(Key.create(ProjectStat.TYPE, currentProjectKey.get(Project.ID)));
    if (stat == null) {
      actualAmount = 0.00;
      return;
    }
    actualAmount = stat.get(ProjectStat.ACTUAL_AMOUNT);
  }

  private boolean check() {
    JTextField projectNameField = projectNameEditor.getComponent();
    if (Strings.isNullOrEmpty(projectNameField.getText())) {
      ErrorTip.showLeft(projectNameField,
                        Lang.get("projectEdition.error.noProjectName"),
                        directory);
      projectNameField.requestFocus();
      return false;
    }

    for (Glob item : repeat.getCurrentGlobs()) {
      JTextField itemNameField = itemNameFields.get(item.getKey());
      if (Strings.isNullOrEmpty(itemNameField.getText())) {
        ErrorTip.showLeft(itemNameField,
                          Lang.get("projectEdition.error.noItemName"),
                          directory);
        itemNameField.requestFocus();
        return false;
      }
    }

    return true;
  }

  private class ProjectItemRepeatFactory implements RepeatComponentFactory<Glob> {
    public void registerComponents(RepeatCellBuilder cellBuilder, final Glob item) {
      final Key itemKey = item.getKey();
      JTextField nameField =
        GlobTextEditor.init(ProjectItem.LABEL, localRepository, directory)
          .forceSelection(itemKey)
          .getComponent();
      nameField.setName("itemLabel");
      cellBuilder.add("itemLabel", nameField);
      itemNameFields.put(itemKey, nameField);
      cellBuilder.addDisposeListener(new Disposable() {
        public void dispose() {
          itemNameFields.remove(itemKey);
        }
      });

      cellBuilder.add("month",
                      GlobButtonView.init(ProjectItem.TYPE,
                                          localRepository, directory,
                                          new MonthFieldListStringifier(ProjectItem.MONTH),
                                          new EditMonthCallback())
                        .forceSelection(itemKey)
                        .getComponent());

      AmountEditor amountEditor =
        new AmountEditor(ProjectItem.AMOUNT, localRepository, directory, true, null)
          .forceSelection(itemKey)
          .update(false, false);
      cellBuilder.add("amountEditor", amountEditor.getPanel());

      cellBuilder.add("deleteItem", new DeleteItemAction(itemKey));
    }
  }

  private class OkAction extends AbstractAction {
    public OkAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      if (!check()) {
        return;
      }
      localRepository.commitChanges(false);
      dialog.setVisible(false);
    }
  }

  private class DeleteProjectAction extends AbstractAction {
    private DeleteProjectAction() {
      super(Lang.get("projectEdition.delete"));
    }

    public void actionPerformed(ActionEvent actionEvent) {
      Key seriesKey = localRepository.get(currentProjectKey).getTargetKey(Project.SERIES);
      final List<Key> transactionKeys =
        parentRepository.getAll(Transaction.TYPE,
                                and(linkedTo(seriesKey, Transaction.SERIES),
                                    isFalse(Transaction.PLANNED)))
          .getKeyList();

      if (!transactionKeys.isEmpty()) {
        ConfirmationDialog confirm = new ConfirmationDialog("projectEdition.deleteConfirmation.title",
                                                            Lang.get("projectEdition.deleteConfirmation.message"),
                                                            dialog, directory) {

          protected String getOkButtonText() {
            return Lang.get("projectEdition.deleteConfirmation.ok");
          }

          protected void processCustomLink(String href) {
            if (href.equals("seeOperations")) {
              dispose();
              dialog.setVisible(false);
              showTransactions(transactionKeys);
            }
          }

          protected void postValidate() {
            deleteProject(transactionKeys);
          }
        };
        confirm.show();
      }
      else {
        deleteProject(Collections.<Key>emptyList());
      }
    }
  }

  private void deleteProject(List<Key> transactions) {
    localRepository.delete(currentProjectKey);
    localRepository.commitChanges(false);
    dialog.setVisible(false);
    if (!transactions.isEmpty()) {
      showTransactions(transactions);
    }
  }

  private void showTransactions(List<Key> transactionKeys) {
    selectMonthRange(transactionKeys);
    GlobList transactions = new GlobList();
    for (Key transactionKey : transactionKeys) {
      transactions.add(parentRepository.get(transactionKey));
    }
    directory.get(NavigationService.class).gotoCategorization(transactions, false);
  }

  private void selectMonthRange(List<Key> transactionKeys) {
    SelectionService selectionService = directory.get(SelectionService.class);
    SortedSet<Integer> monthIds = new TreeSet<Integer>();
    for (Key transactionKey : transactionKeys) {
      monthIds.add(parentRepository.get(transactionKey).get(Transaction.MONTH));
    }
    monthIds.addAll(selectionService.getSelection(Month.TYPE).getValueSet(Month.ID));

    GlobList months = new GlobList();
    for (Integer monthId : Month.range(monthIds.first(), monthIds.last())) {
      months.add(parentRepository.get(Key.create(Month.TYPE, monthId)));
    }
    selectionService.select(months, Month.TYPE);
  }

  private class DeleteItemAction extends AbstractAction {
    private Key projectItemKey;

    private DeleteItemAction(Key projectItemKey) {
      this.projectItemKey = projectItemKey;
    }

    public void actionPerformed(ActionEvent actionEvent) {
      localRepository.delete(projectItemKey);
      if (!localRepository.contains(ProjectItem.TYPE, linkedTo(currentProjectKey, ProjectItem.PROJECT))) {
        createItem();
      }
    }
  }

  private class EditMonthCallback implements GlobListFunctor {
    public void run(GlobList list, GlobRepository repository) {
      if (list.isEmpty()) {
        return;
      }
      Glob item = list.getFirst();
      Integer month = item.get(ProjectItem.MONTH);
      MonthChooserDialog monthChooser = new MonthChooserDialog(dialog, directory);
      int result = monthChooser.show(month, MonthRangeBound.NONE, month);
      if (result > 0) {
        localRepository.update(item.getKey(), ProjectItem.MONTH, result);
      }
    }
  }

  private class AddItemAction extends AbstractAction {

    private AddItemAction() {
      super(Lang.get("projectEdition.addItem"));
    }

    public void actionPerformed(ActionEvent actionEvent) {
      Key itemKey = createItem();
      itemNameFields.get(itemKey).requestFocus();
    }
  }

  private Key createItem() {
    Glob item = localRepository.create(ProjectItem.TYPE,
                                       value(ProjectItem.LABEL, ""),
                                       value(ProjectItem.MONTH, getLastMonth()),
                                       value(ProjectItem.PROJECT, currentProjectKey.get(Project.ID)));
    return item.getKey();
  }

  private Integer getLastMonth() {
    GlobList existingItems =
      localRepository.getAll(ProjectItem.TYPE,
                             linkedTo(currentProjectKey, ProjectItem.PROJECT));
    if (existingItems.isEmpty()) {
      return directory.get(TimeService.class).getCurrentMonthId();
    }
    return existingItems.getSortedSet(ProjectItem.MONTH).last();
  }
}
