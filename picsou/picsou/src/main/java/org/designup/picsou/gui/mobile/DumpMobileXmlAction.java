package org.designup.picsou.gui.mobile;

import org.designup.picsou.mobile.BudgetValuesUpdater;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.repository.DefaultGlobIdGenerator;
import org.globsframework.model.repository.DefaultGlobRepository;
import org.globsframework.xml.XmlGlobWriter;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class DumpMobileXmlAction extends AbstractAction {
  private GlobRepository repository;

  public DumpMobileXmlAction(GlobRepository repository) {
    super("[Dump mobile XML]");
    this.repository = repository;
  }

  public void actionPerformed(ActionEvent actionEvent) {
    GlobRepository tempRepository = new DefaultGlobRepository(new DefaultGlobIdGenerator());
    BudgetValuesUpdater.process(repository, tempRepository);
    XmlGlobWriter.write(tempRepository.getAll(), tempRepository, System.out);
  }
}
