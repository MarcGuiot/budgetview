package org.designup.picsou.gui.utils.dev;

import org.designup.picsou.gui.components.dialogs.MessageDialog;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.mobile.BudgetValuesUpdater;
import org.designup.picsou.model.UserPreferences;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.repository.DefaultGlobIdGenerator;
import org.globsframework.model.repository.DefaultGlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.xml.XmlGlobWriter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.GZIPOutputStream;

public class DumpMobileXmlAction extends AbstractAction {
  private GlobRepository repository;
  private Directory directory;
  private ConfigService configService;

  public DumpMobileXmlAction(GlobRepository repository, Directory directory) {
    super("[Dump XML for android app]");
    this.repository = repository;
    this.directory = directory;
    configService = directory.get(ConfigService.class);
  }

  public void actionPerformed(ActionEvent actionEvent) {
    GlobRepository tempRepository = new DefaultGlobRepository(new DefaultGlobIdGenerator());
    BudgetValuesUpdater.process(repository, tempRepository);

    Writer writer = null;
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      writer = new OutputStreamWriter(new GZIPOutputStream(out), "UTF-8");
      XmlGlobWriter.write(tempRepository.getAll(), tempRepository, writer);
      writer.close();
      Glob userPreference = repository.get(UserPreferences.KEY);
      String mail = userPreference.get(UserPreferences.MAIL_FOR_MOBILE);
      String password = userPreference.get(UserPreferences.PASSWORD_FOR_MOBILE);
      if (configService.sendMobileData(mail, password, out.toByteArray())){
        MessageDialog.show("mobile.data.send.title", directory, "mobile.data.send.content.ok");
      }
      else {
        MessageDialog.show("mobile.data.send.title", directory, "mobile.data.send.content.fail");
      }
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
  }
}
