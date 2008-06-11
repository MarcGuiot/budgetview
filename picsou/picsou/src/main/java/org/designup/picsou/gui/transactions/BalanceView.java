package org.designup.picsou.gui.transactions;

import org.crossbowlabs.globs.gui.GlobsPanelBuilder;
import org.crossbowlabs.globs.gui.views.GlobLabelView;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.splits.color.ColorChangeListener;
import org.crossbowlabs.splits.color.ColorSource;
import org.designup.picsou.gui.View;
import org.designup.picsou.model.Transaction;

import javax.swing.*;
import java.awt.*;

public class BalanceView extends View {
  private GlobLabelView balanceView;
  private JLabel balanceLabel;

  public BalanceView(GlobRepository globRepository, Directory directory) {
    super(globRepository, directory);
    balanceView = GlobLabelView.init(Transaction.TYPE, globRepository, directory,
                                     new BalanceStringifier(globRepository, directory));
    colorService.addListener(new ColorChangeListener() {
      public void colorsChanged(ColorSource colorSource) {
        balanceView.update();
      }
    });
    balanceLabel = balanceView.getComponent();
    balanceLabel.setOpaque(false);
    balanceLabel.setHorizontalAlignment(JLabel.LEFT);
    balanceLabel.setFont(new JLabel().getFont().deriveFont(Font.PLAIN));
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("balanceInfo", balanceLabel);
  }
}
