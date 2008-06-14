package org.designup.picsou.gui.transactions;

import org.designup.picsou.gui.View;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorSource;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

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
