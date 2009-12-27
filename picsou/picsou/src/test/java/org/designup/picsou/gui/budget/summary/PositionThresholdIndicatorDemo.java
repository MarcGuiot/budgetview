package org.designup.picsou.gui.budget.summary;

import org.designup.picsou.gui.utils.ApplicationColors;
import org.designup.picsou.model.AccountPositionThreshold;
import org.globsframework.gui.splits.layout.SingleComponentLayout;
import org.globsframework.gui.splits.SplitsEditor;
import org.globsframework.gui.SelectionService;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;

public class PositionThresholdIndicatorDemo {
  public static void main(String[] args) throws Exception {

    GlobRepository repository = GlobRepositoryBuilder.init().get();
    repository.create(AccountPositionThreshold.KEY,
                      value(AccountPositionThreshold.THRESHOLD, 1000.00));

    Directory directory = new DefaultDirectory();
    directory.add(new SelectionService());
    ApplicationColors.registerColorService(directory);

    PositionThresholdIndicator indicator =
      new PositionThresholdIndicator(repository, directory,
                                     "budgetSummaryDialog.threshold.top",
                                     "budgetSummaryDialog.threshold.bottom",
                                     "budgetSummaryDialog.threshold.border");
    indicator.setValue(1500.0);


    JFrame frame = new JFrame();
    frame.setBackground(Color.WHITE);
    JPanel mainPanel = new JPanel(new SingleComponentLayout(new Insets(50, 50, 50, 50)));
    mainPanel.add(indicator);
    frame.add(mainPanel);
    frame.pack();
    frame.setSize(180, 180);
    frame.setVisible(true);

    SplitsEditor.show(frame, directory);
  }
}
