package com.budgetview.desktop.mobile;

import com.budgetview.desktop.components.dialogs.MessageDialog;
import com.budgetview.desktop.components.dialogs.MessageType;
import com.budgetview.desktop.mobile.utils.AbstractMobileAction;
import com.budgetview.mobile.BudgetValuesUpdater;
import com.budgetview.model.User;
import com.budgetview.model.UserPreferences;
import com.budgetview.utils.Lang;
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

import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

public class SendMobileDataAction extends AbstractMobileAction {

  private Directory directory;

  public SendMobileDataAction(GlobRepository repository, Directory directory) {
    super(Lang.get("mobile.menu.send.data"), repository, directory);
    this.directory = directory;
    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(UserPreferences.KEY)){
          updateEnableStat(repository);
        }
      }

      private void updateEnableStat(GlobRepository repository) {
        Glob userPreference = repository.find(UserPreferences.KEY);
        if (userPreference != null){
          String mail = userPreference.get(UserPreferences.MAIL_FOR_MOBILE);
          String password = userPreference.get(UserPreferences.PASSWORD_FOR_MOBILE);
          setEnabled(Strings.isNotEmpty(mail) && Strings.isNotEmpty(password));
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        updateEnableStat(repository);
      }
    });
  }

  protected void processMobileStatusChange(boolean enabled) {
    setEnabled(enabled);
  }

  public void actionPerformed(ActionEvent actionEvent) {
    Ref<String> msg = new Ref<String>();
    if (sendToMobile(repository, directory, msg, false)) {
      MessageDialog.show("mobile.data.send.title", MessageType.SUCCESS, directory, "mobile.data.send.content.ok");
    }
    else {
      MessageDialog.show("mobile.data.send.title", MessageType.ERROR, directory, "mobile.data.send.content.fail", msg.get());
    }
  }

  public static boolean sendToMobile(final GlobRepository repository, final Directory directory,
                                     Ref<String> msg, boolean pending) {
    Glob user = repository.findOrCreate(User.KEY);
    if (user == null || !user.get(User.CONNECTED)) {
      return false;
    }
    Glob userPreferences = repository.find(UserPreferences.KEY);
    if (userPreferences == null) {
      return false;
    }
    String mailForMobile = userPreferences.get(UserPreferences.MAIL_FOR_MOBILE);
    String passwordForMobile = userPreferences.get(UserPreferences.PASSWORD_FOR_MOBILE);
    if (Strings.isNullOrEmpty(mailForMobile) || Strings.isNullOrEmpty(passwordForMobile)) {
      return false;
    }
    GlobRepository tempRepository = new DefaultGlobRepository(new DefaultGlobIdGenerator());
    BudgetValuesUpdater.process(repository, tempRepository);

    Writer writer = null;
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      writer = new OutputStreamWriter(new GZIPOutputStream(out), "UTF-8");
      XmlGlobWriter.write(tempRepository.getAll(), tempRepository, writer);
      writer.close();
      writer = null;
      Glob userPreference = repository.get(UserPreferences.KEY);
      String mail = userPreference.get(UserPreferences.MAIL_FOR_MOBILE);
      String password = userPreference.get(UserPreferences.PASSWORD_FOR_MOBILE);
      return directory.get(MobileService.class).sendMobileData(mail, password, out.toByteArray(), msg, pending, repository);
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
        }
      }
    }
  }
}
