package org.designup.picsou.gui.importer;

import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;

import javax.swing.*;

public interface AdditionalImportAction {

  boolean shouldApplyAction();

  String getMessage();

  String getButtonMessage();

  Action getAction();
}
