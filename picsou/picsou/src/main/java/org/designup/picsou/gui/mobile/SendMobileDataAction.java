package org.designup.picsou.gui.mobile;

import org.designup.picsou.gui.components.dialogs.MessageDialog;
import org.designup.picsou.gui.components.dialogs.MessageType;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.mobile.BudgetValuesUpdater;
import org.designup.picsou.model.User;
import org.designup.picsou.model.UserPreferences;
import org.designup.picsou.utils.Lang;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.repository.DefaultGlobIdGenerator;
import org.globsframework.model.repository.DefaultGlobRepository;
import org.globsframework.utils.Ref;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;
import org.globsframework.xml.XmlGlobWriter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

public class SendMobileDataAction extends AbstractAction {
  private GlobRepository repository;
  private Directory directory;
  private ConfigService configService;

  public SendMobileDataAction(GlobRepository repository, Directory directory) {
    super(Lang.get("mobile.menu.send.data"));
    this.repository = repository;
    this.directory = directory;
    this.configService = directory.get(ConfigService.class);
    updateStatus(repository);
    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(UserPreferences.KEY)) {
          updateStatus(repository);
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (changedTypes.contains(UserPreferences.TYPE)) {
          updateStatus(repository);
        }
      }
    });
  }

  private Glob updateStatus(GlobRepository repository) {
    Glob preference = repository.find(UserPreferences.KEY);
    if (preference != null) {
      setEnabled(Strings.isNotEmpty(preference.get(UserPreferences.MAIL_FOR_MOBILE)));
    }
    return preference;
  }

  public void actionPerformed(ActionEvent actionEvent) {
    Ref<String> msg = new Ref<String>();
    if (sendToMobile(repository, configService, msg)) {
      MessageDialog.show("mobile.data.send.title", MessageType.SUCCESS, directory, "mobile.data.send.content.ok");
    }
    else {
      MessageDialog.show("mobile.data.send.title", MessageType.ERROR, directory, "mobile.data.send.content.fail", msg.get());
    }
  }

  public static boolean sendToMobile(final GlobRepository sourceRepository, final ConfigService configService, Ref<String> msg) {
    Glob user = sourceRepository.find(User.KEY);
    if (user == null || !user.get(User.CONNECTED)) {
      return false;
    }
    Glob userPreferences = sourceRepository.find(UserPreferences.KEY);
    if (userPreferences == null) {
      return false;
    }
    String mailForMobile = userPreferences.get(UserPreferences.MAIL_FOR_MOBILE);
    String passwordForMobile = userPreferences.get(UserPreferences.PASSWORD_FOR_MOBILE);
    if (Strings.isNullOrEmpty(mailForMobile) || Strings.isNullOrEmpty(passwordForMobile)) {
      return false;
    }
    GlobRepository tempRepository = new DefaultGlobRepository(new DefaultGlobIdGenerator());
    BudgetValuesUpdater.process(sourceRepository, tempRepository);

    Writer writer = null;
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      writer = new OutputStreamWriter(new GZIPOutputStream(out), "UTF-8");
      XmlGlobWriter.write(tempRepository.getAll(), tempRepository, writer);
      writer.close();
      Glob userPreference = sourceRepository.get(UserPreferences.KEY);
      String mail = userPreference.get(UserPreferences.MAIL_FOR_MOBILE);
      String password = userPreference.get(UserPreferences.PASSWORD_FOR_MOBILE);
      return configService.sendMobileData(mail, password, out.toByteArray(), msg);
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
