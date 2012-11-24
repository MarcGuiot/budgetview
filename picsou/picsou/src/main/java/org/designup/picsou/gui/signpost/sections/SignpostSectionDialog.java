package org.designup.picsou.gui.signpost.sections;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.dialogs.CloseDialogAction;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.model.SignpostSectionType;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;

public class SignpostSectionDialog {

  private GlobRepository repository;
  private Directory directory;
  private PicsouDialog dialog;

  public SignpostSectionDialog(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
  }

  public void show(SignpostSection completedSection) {
    PicsouDialog dialog = createDialog(completedSection);
    dialog.showCentered();
  }

  private PicsouDialog createDialog(SignpostSection completedSection) {

    dialog = PicsouDialog.create(directory.get(JFrame.class), true, directory);

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/signpost/signpostSectionDialog.splits",
                                                      repository, directory);

    builder.add("title", new JLabel(completedSection.getCompletionTitle())).getComponent();

    builder.addRepeat("sections",
                      Arrays.asList(SignpostSection.values()),
                      new RepeatFactory(completedSection));

    builder.add("message", GuiUtils.createReadOnlyHtmlComponent(completedSection.getCompletionMessage()));

    dialog.addPanelWithButton(builder.<JPanel>load(), new CloseSectionAction(dialog, completedSection));
    dialog.pack();

    return dialog;
  }

  private class RepeatFactory implements RepeatComponentFactory<SignpostSection> {
    private SignpostSection completedSection;

    public RepeatFactory(SignpostSection completedSection) {
      this.completedSection = completedSection;
    }

    public void registerComponents(RepeatCellBuilder cellBuilder,
                                   final SignpostSection section) {
      SplitsNode<JPanel> sectionPanel = cellBuilder.add("sectionPanel", new JPanel());
      SplitsNode<JLabel> label = cellBuilder.add("sectionTitle", new JLabel(section.getLabel()));

      sectionPanel.applyStyle(getPanelStyle(section));
      label.applyStyle(getLabelStyle(section));
      label.getComponent().setEnabled(true);
      GuiUtils.revalidate(sectionPanel.getComponent());
    }

    private String getPanelStyle(SignpostSection section) {
      if (isNextStep(section)) {
        return "inprogressPanel";
      }
      else if (isCompleted(section)) {
        return "completedPanel";
      }
      else {
        return "unavailablePanel";
      }
    }

    private String getLabelStyle(SignpostSection section) {
      if (isNextStep(section)) {
        return "completedLabel";
      }
      else if (isCompleted(section)) {
        return "completedLabel";
      }
      else {
        return "unavailableLabel";
      }
    }

    private boolean isNextStep(SignpostSection section) {
      return section.getType() == getNextStep();
    }

    private SignpostSectionType getNextStep() {
      return (completedSection == SignpostSection.BUDGET) ? completedSection.getType() : completedSection.getType().getNextSection();
    }

    private boolean isCompleted(SignpostSection section) {
      return section == completedSection || section.getType().isCompleted(completedSection.getType());
    }
  }

  private class CloseSectionAction extends CloseDialogAction {
    private boolean isLastSection;

    public CloseSectionAction(JDialog dialog, SignpostSection completedSection) {
      super(dialog);
      isLastSection = completedSection.getType().isLast();
    }

    public void actionPerformed(ActionEvent e) {
      if (isLastSection) {
        directory.get(NavigationService.class).gotoHome();
      }
      super.actionPerformed(e);
    }
  }
}
