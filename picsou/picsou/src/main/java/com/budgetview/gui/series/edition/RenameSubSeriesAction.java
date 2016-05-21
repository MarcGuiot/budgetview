package com.budgetview.gui.series.edition;

import com.budgetview.gui.components.dialogs.PicsouDialog;
import com.budgetview.model.SubSeries;
import com.budgetview.utils.Lang;
import org.globsframework.gui.actions.RenameGlobAction;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidParameter;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class RenameSubSeriesAction extends RenameGlobAction {
  private JDialog owner;

  public RenameSubSeriesAction(GlobRepository repository, Directory directory, JDialog owner) {
    super(Lang.get("rename"), SubSeries.NAME, repository, directory);
    this.owner = owner;
    setEnabled(false);
  }

  public JDialog getDialog(ActionEvent e) {
    return PicsouDialog.create(this, owner, directory);
  }

  protected String getText() {
    return getCurrentObject().get(SubSeries.NAME);
  }

  protected void validateName(GlobType type, StringField namingField, String name, GlobRepository repository) throws InvalidParameter {

    Glob series = repository.findLinkTarget(getCurrentObject(), SubSeries.SERIES);

    boolean nameAlreadyUsed =
      repository.findLinkedTo(series, SubSeries.SERIES)
        .getValueSet(SubSeries.NAME)
        .contains(name);

    if (nameAlreadyUsed) {
      throw new InvalidParameter(Lang.get("subseries.name.already.used"));
    }
  }

  protected String getTitle() {
    return Lang.get("subseries.rename.title");
  }

  protected String getInputLabel() {
    return Lang.get("subseries.rename.inputlabel");
  }

  protected String getOkLabel() {
    return Lang.get("ok");
  }

  protected String getCancelLabel() {
    return Lang.get("close");
  }
}