package org.designup.picsou.gui.signpost.sections;

import org.designup.picsou.gui.components.CloseAction;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Arrays;

public class SignpostSectionDialog {

  private GlobRepository repository;
  private Directory directory;

  public SignpostSectionDialog(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
  }

  public void show(SignpostSection completedSection) {
    PicsouDialog dialog = createDialog(completedSection);
    dialog.showCentered();
  }

  private PicsouDialog createDialog(SignpostSection completedSection) {

    PicsouDialog dialog = PicsouDialog.create(directory.get(JFrame.class), true, directory);

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/signpost/signpostSectionDialog.splits",
                                                      repository, directory);

    builder.add("title", new JLabel(completedSection.getCompletionTitle())).getComponent();

    builder.addRepeat("sections",
                      Arrays.asList(SignpostSection.values()),
                      new RepeatFactory(completedSection));

    builder.add("message", GuiUtils.createReadOnlyHtmlComponent(completedSection.getCompletionMessage()));

    dialog.addPanelWithButton(builder.<JPanel>load(), new CloseAction(dialog));
    dialog.pack();

    return dialog;
  }

  private static class RepeatFactory implements RepeatComponentFactory<SignpostSection> {
    private SignpostSection completedSection;

    public RepeatFactory(SignpostSection completedSection) {
      this.completedSection = completedSection;
    }

    public void registerComponents(RepeatCellBuilder cellBuilder,
                                   SignpostSection section) {
      SplitsNode<JPanel> sectionPanel = cellBuilder.add("sectionPanel", new JPanel());
      SplitsNode<JLabel> label = cellBuilder.add("sectionLabel", new JLabel(section.getLabel()));

      sectionPanel.applyStyle(getPanelStyle(section));
      label.applyStyle(getLabelStyle(section));
      GuiUtils.revalidate(sectionPanel.getComponent());
    }

    private String getPanelStyle(SignpostSection section) {
      if (section == completedSection) {
        return "inprogressPanel";
      }
      else if (section.getType().isCompleted(completedSection.getType())) {
        return "completedPanel";
      }
      else {
        return "unavailablePanel";
      }
    }

    private String getLabelStyle(SignpostSection section) {
      if (section == completedSection) {
        return "completedLabel";
      }
      else if (section.getType().isCompleted(completedSection.getType())) {
        return "completedLabel";
      }
      else {
        return "unavailableLabel";
      }
    }
  }
}
