package org.designup.picsou.gui.projects.itemedition;

import org.designup.picsou.gui.components.images.GlobImageLabelView;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.projects.ProjectView;
import org.designup.picsou.model.ProjectItem;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.editors.GlobMultiLineTextEditor;
import org.globsframework.gui.editors.GlobTextEditor;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class ProjectItemExpensePanel extends ProjectItemEditionPanel {

  public ProjectItemExpensePanel(Glob item, GlobRepository parentRepository, Directory directory) {
    super(item, parentRepository, directory);
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

    GlobMultiLineTextEditor descriptionField = GlobMultiLineTextEditor.init(ProjectItem.DESCRIPTION, localRepository, directory)
      .forceSelection(itemKey);
    builder.add("descriptionField", descriptionField);

    builder.add("validate", validate);
    builder.add("cancel", new CancelAction());

    builder.add("handler", new HyperlinkHandler(directory));

    return builder.load();
  }

  protected GlobList getAssignedTransactions(Glob projectItem, GlobRepository repository) {
    GlobList transactions = GlobList.EMPTY;
    Glob subSeries = repository.findLinkTarget(projectItem, ProjectItem.SUB_SERIES);
    if (subSeries != null) {
      transactions = repository.findLinkedTo(subSeries, Transaction.SUB_SERIES);
    }
    else {
      Glob series = repository.findLinkTarget(projectItem, ProjectItem.SERIES);
      if (series != null) {
        transactions = repository.findLinkedTo(series, Transaction.SERIES);
      }
    }
    return transactions;
  }

  protected boolean isNewItem(Glob item) {
    return Strings.isNullOrEmpty(item.get(ProjectItem.LABEL));
  }
}
