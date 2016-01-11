package org.designup.picsou.gui.series.edition.carryover;

import org.designup.picsou.gui.components.dialogs.CancelAction;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CarryOverDialog implements Disposable {
  private double plannedForNextMonth;
  private double remainderForSingle;
  private double remainderForSeveral;
  private GlobRepository repository;
  private Directory directory;
  private GlobsPanelBuilder builder;

  private CarryOverOption selectedOption = CarryOverOption.NONE;
  private PicsouDialog dialog;

  public CarryOverDialog(double initiallyPlannedForNextMonth,
                         double remainderForSeveral,
                         double remainderForSingle,
                         GlobRepository repository, Directory directory) {
    this.plannedForNextMonth = initiallyPlannedForNextMonth;
    this.remainderForSingle = remainderForSingle;
    this.remainderForSeveral = remainderForSeveral;
    this.repository = repository;
    this.directory = directory;
    createDialog();
  }

  private void createDialog() {
    builder = new GlobsPanelBuilder(getClass(), "/layout/series/carryOverDialog.splits",
                                    repository, directory);
    
    builder.add("message", Gui.createHtmlDisplay(Lang.get("series.carryOver.dialog.message", 
                                                          Formatting.toAbsString(plannedForNextMonth))));

    JRadioButton singleRadio =
      createRadio(Lang.get("series.carryOver.dialog.option.single",
                           Formatting.toAbsString(plannedForNextMonth),
                           Formatting.toAbsString(remainderForSingle)),
                  CarryOverOption.FORCE_SINGLE_MONTH);
    builder.add("single", singleRadio);

    JRadioButton severalRadio =
      createRadio(Lang.get("series.carryOver.dialog.option.several",
                           Formatting.toAbsString(remainderForSeveral)),
                  CarryOverOption.SEVERAL_MONTHS);
    builder.add("several", severalRadio);
    
    ButtonGroup group = new ButtonGroup();
    group.add(singleRadio);
    group.add(severalRadio);
    singleRadio.doClick();

    JPanel panel = builder.load();

    dialog = PicsouDialog.create(this, directory.get(JFrame.class), directory);
    dialog.addPanelWithButtons(panel, new OkAction(), new CancelAndDisposeAction(dialog));
  }

  private JRadioButton createRadio(final String text, final CarryOverOption option) {
    return new JRadioButton(new AbstractAction(text) {
    public void actionPerformed(ActionEvent actionEvent) {
      selectedOption = option;
    }
  });
  }

  public CarryOverOption show() {
    dialog.pack();
    GuiUtils.showCentered(dialog);
    dispose();
    return selectedOption;
  }
  
  private class OkAction extends AbstractAction {
    private OkAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent actionEvent) {
      dialog.setVisible(false);
    }
  }

  private class CancelAndDisposeAction extends CancelAction {
    public CancelAndDisposeAction(PicsouDialog dialog) {
      super(dialog);
    }

    public void actionPerformed(ActionEvent e) {
      selectedOption = CarryOverOption.NONE;
      super.actionPerformed(e);
    }
  }

  public void dispose() {
    builder.dispose();
    dialog.dispose();
    dialog = null;
  }
}
