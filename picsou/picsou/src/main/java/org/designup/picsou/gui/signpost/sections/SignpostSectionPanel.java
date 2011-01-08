package org.designup.picsou.gui.signpost.sections;

import org.designup.picsou.model.SignpostSectionType;
import org.designup.picsou.model.SignpostStatus;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Set;

public abstract class SignpostSectionPanel {

  private SignpostSection section;
  private GlobRepository repository;
  private Directory directory;

  private SplitsNode<JPanel> sectionPanel;
  private SplitsNode<JLabel> label;
  private SplitsNode<JEditorPane> descriptionEditor;

  private boolean inProgress = false;

  protected SignpostSectionPanel(SignpostSection section,
                                 GlobRepository repository,
                                 Directory directory) {
    this.section = section;
    this.repository = repository;
    this.directory = directory;
  }

  public void registerComponents(RepeatCellBuilder cellBuilder) {
    sectionPanel = cellBuilder.add("sectionPanel", new JPanel());
    label = cellBuilder.add("sectionLabel", new JLabel(section.getLabel()));
    descriptionEditor = cellBuilder.add("sectionDescription",
                                        GuiUtils.createReadOnlyHtmlComponent(section.getDescription()));
  }

  public void init() {
    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(SignpostStatus.KEY)) {
          checkForInProgress();
        }
        checkForCompletion(repository);
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (changedTypes.contains(SignpostStatus.TYPE)) {
          checkForInProgress();
        }
        checkForCompletion(repository);
      }
    });
    checkForInProgress();
    checkForCompletion(repository);
  }

  public void checkForInProgress() {

    Glob signpostStatus = repository.find(SignpostStatus.KEY);
    if (signpostStatus == null) {
      return;
    }
    SignpostSectionType currentType = SignpostSectionType.getType(signpostStatus.get(SignpostStatus.CURRENT_SECTION));
    if (section.getType().isCompleted(currentType)) {
      updateComponents("completed");
      inProgress = false;
      return;
    }
    if (section.getType() != currentType) {
      updateComponents("unavailable");
      inProgress = false;
      return;
    }

    inProgress = true;
    updateComponents("inprogress");
  }

  private void checkForCompletion(GlobRepository repository) {
    if (!inProgress || !isCompleted(repository)) {
      return;
    }

    SignpostSectionDialog dialog = new SignpostSectionDialog(repository, directory);
    dialog.show(section);

    SignpostStatus.setSection(section.getType().getNextSection(), repository);
  }

  protected abstract boolean isCompleted(GlobRepository repository);

  private void updateComponents(String style) {
    sectionPanel.applyStyle(style + "Panel");
    label.applyStyle(style + "Label");
    descriptionEditor.applyStyle(style + "Description");
    GuiUtils.revalidate(sectionPanel.getComponent());
    GuiUtils.revalidate(descriptionEditor.getComponent());
    GuiUtils.revalidate(label.getComponent());
  }
}
