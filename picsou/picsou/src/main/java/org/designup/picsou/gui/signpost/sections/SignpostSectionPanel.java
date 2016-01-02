package org.designup.picsou.gui.signpost.sections;

import org.designup.picsou.gui.help.actions.GotoHelpAction;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.SignpostSectionType;
import org.designup.picsou.model.SignpostStatus;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.ImageLocator;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.SplitsNode;
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
  protected GlobRepository repository;
  private Directory directory;

  private SplitsNode<JPanel> sectionPanel;
  private SplitsNode<JButton> title;
  private SplitsNode<JEditorPane> description;
  private SplitsNode<JButton> button;

  private boolean inProgress = false;
  private JButton helpButton;
  private JEditorPane helpText;

  protected SignpostSectionPanel(SignpostSection section,
                                 GlobRepository repository,
                                 Directory directory) {
    this.section = section;
    this.repository = repository;
    this.directory = directory;
  }

  public void registerComponents(PanelBuilder builder) {
    sectionPanel = builder.add("sectionPanel", new JPanel());

    AbstractAction action = getAction(directory);
    title = builder.add("sectionTitle", new JButton(action));

    description = builder.add("sectionDescription",
                                  GuiUtils.createReadOnlyHtmlComponent(section.getDescription()));

    JButton jButton = new JButton(action);
    ImageLocator imageLocator = directory.get(ImageLocator.class);
    jButton.setIcon(imageLocator.get(section.getIconPath()));
    this.button = builder.add("sectionButton", jButton);

    helpButton = new JButton(new GotoHelpAction(section.getHelpKey(), directory));
    builder.add("helpButton", helpButton);

    helpText = Gui.createHtmlDisplay(Lang.get("signpostView.help", section.getHelpRef()));
    builder.add("helpText", helpText);
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
      updateComponents("completed", false);
      inProgress = false;
      return;
    }
    if (section.getType() != currentType) {
      updateComponents("unavailable", false);
      inProgress = false;
      return;
    }

    inProgress = true;
    updateComponents("inprogress", true);
  }

  private void checkForCompletion(GlobRepository repository) {
    if (!inProgress || !isCompleted(repository) || isAllCompleted(repository)) {
      return;
    }

    SignpostSectionDialog dialog = new SignpostSectionDialog(repository, directory);
    dialog.show(section);

    SignpostStatus.setSection(section.getType().getNextSection(), repository);
  }

  private boolean isAllCompleted(GlobRepository repository) {
    return SignpostStatus.isOnboardingCompleted(repository);
  }

  protected abstract boolean isCompleted(GlobRepository repository);

  private void updateComponents(String style, boolean active) {
    sectionPanel.applyStyle(style + "Panel");
    title.applyStyle(style + "Label");
    description.applyStyle(style + "Description");
    button.applyStyle(style + "Button");
    helpButton.setVisible(active);
    helpText.setVisible(active);
    GuiUtils.revalidate(sectionPanel.getComponent());
    GuiUtils.revalidate(description.getComponent());
    GuiUtils.revalidate(title.getComponent());
    GuiUtils.revalidate(button.getComponent());
  }

  protected abstract AbstractAction getAction(Directory directory);
}
