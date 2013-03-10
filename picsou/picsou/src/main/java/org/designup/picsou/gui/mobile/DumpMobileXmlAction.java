package org.designup.picsou.gui.mobile;

import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.mobile.BudgetValuesUpdater;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.repository.DefaultGlobIdGenerator;
import org.globsframework.model.repository.DefaultGlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.xml.XmlGlobWriter;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class DumpMobileXmlAction extends AbstractAction {
  private GlobRepository repository;
  private Directory directory;
  private ConfigService configService;

  public DumpMobileXmlAction(GlobRepository repository, Directory directory) {
    super("[Dump mobile XML]");
    this.repository = repository;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent actionEvent) {
    GlobRepository tempRepository = new DefaultGlobRepository(new DefaultGlobIdGenerator());
    BudgetValuesUpdater.process(repository, tempRepository);
    XmlGlobWriter.write(tempRepository.getAll(), tempRepository, System.out);
  }
}
