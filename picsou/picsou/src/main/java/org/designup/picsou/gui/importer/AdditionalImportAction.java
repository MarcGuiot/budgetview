package org.designup.picsou.gui.importer;

import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;

import javax.swing.*;

public interface AdditionalImportAction {

  boolean isValid();

  String getMessage();

  String getButtonMessage();

  Action getAction();
}
