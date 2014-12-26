package org.designup.picsou.gui.projects.itemedition;

import org.designup.picsou.gui.components.images.GlobImageLabelView;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.projects.ProjectView;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.ProjectItem;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.editors.GlobLinkComboEditor;
import org.globsframework.gui.editors.GlobMultiLineTextEditor;
import org.globsframework.gui.editors.GlobTextEditor;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.TypeChangeSetListener;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

import static org.globsframework.model.utils.GlobMatchers.*;

public class ProjectItemExpensePanel extends ProjectItemEditionPanel {

  private GlobLinkComboEditor accountSelection;
  private GlobLabelView accountLabel;
  private TypeChangeSetListener listener;

  public ProjectItemExpensePanel(Glob item, JScrollPane scrollPane, GlobRepository parentRepository, Directory directory) {
    super(item, scrollPane, parentRepository, directory);
  }

  protected JPanel createEditionPanel() {
    final GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/projects/projectItemExpenseEditionPanel.splits",
                                                            localRepository, directory);

    addCommonComponents(builder, false);

    GlobImageLabelView imageLabel =
      GlobImageLabelView.init(ProjectItem.PICTURE, ProjectView.MAX_PICTURE_SIZE, localRepository, directory)
        .setDefaultIconFactory(createDefaultIconFactory(builder))
        .forceKeySelection(itemKey);
    builder.add("imageLabel", imageLabel.getLabel());
    builder.add("imageActions", imageLabel.getPopupButton(Lang.get("projectView.item.edition.imageActions")));

    GlobTextEditor urlField = GlobTextEditor.init(ProjectItem.URL, localRepository, directory)
      .forceSelection(itemKey);
    builder.add("urlField", urlField);

    accountSelection = builder.addComboEditor("accountSelection", ProjectItem.ACCOUNT)
      .setFilter(new Account.MainAccountMatcher())
      .forceSelection(itemKey);

    accountLabel = builder.addLabel("accountLabel", ProjectItem.ACCOUNT)
      .forceSelection(itemKey);
    builder.addDisposable(new Disposable() {
      public void dispose() {
        accountLabel = null;
        accountSelection = null;
      }
    });

    registerAccountSelectionUpdater();

    GlobMultiLineTextEditor descriptionField = GlobMultiLineTextEditor.init(ProjectItem.DESCRIPTION, localRepository, directory)
      .forceSelection(itemKey);
    builder.add("descriptionField", descriptionField);

    builder.add("validate", validate);
    builder.add("cancel", new CancelAction());

    builder.add("handler", new HyperlinkHandler(directory));
    disposables.add(builder);
    return builder.load();
  }

  private void registerAccountSelectionUpdater() {
    listener = new TypeChangeSetListener(Transaction.TYPE) {
      public void update(GlobRepository repository) {
        if ((accountSelection == null) || (accountLabel == null)) {
          return;
        }
        if (itemKey == null || !Series.hasRealTransactions(parentRepository, localRepository.get(itemKey).get(ProjectItem.SERIES))) {
          accountSelection.setVisible(true);
          accountLabel.getComponent().setVisible(false);
        }
        else {
          accountSelection.setVisible(false);
          accountLabel.getComponent().setVisible(true);
        }
      }
    };
    parentRepository.addChangeListener(listener);
    listener.update(parentRepository);
    disposables.add(new Disposable() {
      public void dispose() {
        parentRepository.removeChangeListener(listener);
      }
    });
  }

  protected GlobList getAssignedTransactions(Glob projectItem, GlobRepository repository) {
    Glob series = repository.findLinkTarget(projectItem, ProjectItem.SERIES);
    if (series != null) {
      return repository.getAll(Transaction.TYPE,
                               and(linkedTo(series, Transaction.SERIES),
                                   isFalse(Transaction.PLANNED))
      );
    }
    return GlobList.EMPTY;
  }

  protected boolean isNewItem(Glob item) {
    return Strings.isNullOrEmpty(item.get(ProjectItem.LABEL));
  }

  protected boolean usesImages() {
    return true;
  }

  public void dispose() {
    super.dispose();
  }
}
