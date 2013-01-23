package com.budgetview.android.checkers;

import com.budgetview.android.App;
import com.budgetview.android.checkers.utils.Expectation;
import com.budgetview.android.checkers.utils.ExpectationQueue;
import com.budgetview.android.datasync.DataSync;
import com.budgetview.android.datasync.DataSyncCallback;
import com.budgetview.android.datasync.DataSyncFactory;
import com.xtremelabs.robolectric.Robolectric;
import junit.framework.Assert;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.utils.Strings;

import java.io.IOException;

public class DataSyncChecker {

  private DataSync dataSync;
  private ExpectationQueue expectations = new ExpectationQueue();

  public DataSyncChecker() {
    dataSync = new DummyDataSync();
    DataSyncFactory.setForcedSync(dataSync);
  }

  public void acceptLogin(String email) {
    expectations.push(new ExpectLogin(email, true));
  }

  public void rejectLogin(String email) {
    expectations.push(new ExpectLogin(email, false));
  }

  public LoadBuilder prepareLoad() {
    ExpectLoad expectLoad = new ExpectLoad();
    expectations.push(expectLoad);
    return expectLoad.loadBuilder;
  }

  private class DummyDataSync implements DataSync {

    public void connect(String email, String password, DataSyncCallback callback) {
      expectations.pop(ExpectLogin.class).connect(email, password, callback);
    }

    public void load(DataSyncCallback callback) {
      expectations.pop(ExpectLoad.class).load(callback);
    }

    public void loadDemoFile() throws IOException {
      expectations.pop(ExpectLoadDemoFile.class).loadDemoFile();
    }

    public boolean loadTempFile() {
      return expectations.pop(ExpectLoadTempFile.class).loadTempFile();
    }

    public boolean sendDownloadEmail(String email, DataSyncCallback callback) {
      return expectations.pop(ExpectSendDownloadEmail.class).sendDownloadEmail(email, callback);
    }
  }

  private App getApp() {
    return (App)Robolectric.application;
  }

  private class ExpectLogin implements Expectation {
    private String expectedEmail;
    private boolean acceptLogin;

    private ExpectLogin(String email, boolean acceptLogin) {
      this.expectedEmail = email;
      this.acceptLogin = acceptLogin;
    }

    public void connect(String email, String password, DataSyncCallback callback) {
      Assert.assertEquals(expectedEmail, email);
      if (Strings.isNullOrEmpty(password)) {
        Assert.fail("Provided invalid empty password");
      }
      if (acceptLogin) {
        callback.onActionFinished();
      }
      else {
        callback.onActionFailed();
      }
    }
  }

  private class ExpectLoad implements Expectation {
    private final LoadBuilder loadBuilder = new LoadBuilder();

    public void load(DataSyncCallback callback) {
      GlobRepository repository = getApp().getRepository();
      loadBuilder.apply(repository);
      GlobPrinter.print(repository);
      callback.onActionFinished();
    }
  }

  private class ExpectLoadDemoFile implements Expectation {
    public void loadDemoFile() {
      Assert.fail("tbd");
    }
  }

  private class ExpectLoadTempFile implements Expectation {
    public boolean loadTempFile() {
      Assert.fail("tbd");
      return false;
    }
  }

  private class ExpectSendDownloadEmail implements Expectation {
    public boolean sendDownloadEmail(String email, DataSyncCallback callback) {
      Assert.fail("tbd");
      return false;
    }
  }
}
