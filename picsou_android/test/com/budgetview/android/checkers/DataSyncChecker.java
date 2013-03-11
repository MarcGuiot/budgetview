package com.budgetview.android.checkers;

import com.budgetview.android.App;
import com.budgetview.android.checkers.utils.Expectation;
import com.budgetview.android.checkers.utils.ExpectationQueue;
import com.budgetview.android.datasync.DataSync;
import com.budgetview.android.datasync.DataSyncCallback;
import com.budgetview.android.datasync.DataSyncFactory;
import junit.framework.Assert;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.utils.Strings;
import org.robolectric.Robolectric;

import java.io.IOException;

public class DataSyncChecker {

  private final boolean verbose;
  private DataSync dataSync;
  private boolean connectionAvailable = true;
  private ExpectationQueue expectations = new ExpectationQueue();

  public DataSyncChecker(boolean verbose) {
    this.verbose = verbose;
    dataSync = new DummyDataSync();
    DataSyncFactory.setForcedSync(dataSync);
  }

  public void prepareLogin(String email, String password) {
    expectations.push(new ExpectLogin(email, password));
  }

  public LoadBuilder prepareLoad() {
    ExpectLoad expectLoad = new ExpectLoad();
    expectations.push(expectLoad);
    return expectLoad.loadBuilder;
  }

  public void checkAllCallsProcessed() {
    if (!expectations.isEmpty()) {
      Assert.fail("Remaining items: " + expectations.toString());
    }
  }

  public void setConnectionAvailable(boolean connectionAvailable) {
    this.connectionAvailable = connectionAvailable;
  }

  private class DummyDataSync implements DataSync {

    public void setUser(String email, String password) {
      expectations.pop(ExpectLogin.class).connect(email, password);
    }

    public void load(DataSyncCallback callback) {
      if (!connectionAvailable) {
        callback.onConnectionUnavailable();
        return;
      }
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
    private final String expectedPassword;

    private ExpectLogin(String email, String password) {
      this.expectedEmail = email;
      this.expectedPassword = password;
    }

    public void connect(String email, String password) {
      Assert.assertEquals(expectedEmail, email);
      Assert.assertEquals(expectedPassword, password);
    }
  }

  private class ExpectLoad implements Expectation {
    private final LoadBuilder loadBuilder = new LoadBuilder();

    public void load(DataSyncCallback callback) {
      GlobRepository repository = getApp().getRepository();
      loadBuilder.apply(repository);
      if (verbose) {
        GlobPrinter.print(repository);
      }
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
