package org.designup.picsou.gui.utils.dev;

import org.designup.picsou.mobile.BudgetValuesUpdater;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.repository.DefaultGlobIdGenerator;
import org.globsframework.model.repository.DefaultGlobRepository;
import org.globsframework.xml.XmlGlobWriter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class DumpMobileXmlAction extends AbstractAction {
  private GlobRepository repository;

  public DumpMobileXmlAction(GlobRepository repository) {
    super("[Dump XML for android app]");
    this.repository = repository;
  }

  public void actionPerformed(ActionEvent actionEvent) {

    GlobRepository tempRepository = new DefaultGlobRepository(new DefaultGlobIdGenerator());
    BudgetValuesUpdater.process(repository, tempRepository);

    File targetPath = new File("./picsou_android/res/raw/globsdata.xml");
    targetPath.getParentFile().mkdirs();

    Writer writer = null;
    try {
      writer = new FileWriter(targetPath);
      XmlGlobWriter.write(tempRepository.getAll(), tempRepository, writer);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
    finally {
      if (writer != null) {
        try {
          writer.close();
        }
        catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
    System.out.println("DumpMobileXmlAction: content dumped to " + targetPath.getAbsolutePath());
  }
}
