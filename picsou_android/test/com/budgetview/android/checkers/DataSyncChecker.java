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
import org.robolectric.Robolectric;

import java.io.IOException;

public class DataSyncChecker {

  private final boolean verbose;
  private boolean connectionAvailable = true;
  private ExpectationQueue expectations = new ExpectationQueue();

  public DataSyncChecker(boolean verbose) {
    this.verbose = verbose;
    DataSyncFactory.setForcedSync(new DummyDataSync());
  }

  public LoadBuilder prepareLoad(String email, String password) {
    ExpectLoad expectLoad = new ExpectLoad(email, password);
    expectations.push(expectLoad);
    return expectLoad.loadBuilder;
  }

  public LoadBuilder prepareOpenDemo() {
    ExpectLoadDemoFile expectLoad = new ExpectLoadDemoFile();
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

    public void load(String email, String password, DataSyncCallback callback) {
      if (!connectionAvailable) {
        callback.onConnectionUnavailable();
        return;
      }
      expectations.pop(ExpectLoad.class).load(email, password, callback);
    }

    public void loadDemoFile() throws IOException {
      expectations.pop(ExpectLoadDemoFile.class).loadDemoFile();
    }

    public boolean loadTempFile() {
      return expectations.pop(ExpectLoadTempFile.class).loadTempFile();
    }

    public void deleteTempFile() {
      Assert.fail("tbd");
    }

    public void sendDownloadEmail(String email, DataSyncCallback callback) {
      expectations.pop(ExpectSendDownloadEmail.class).sendDownloadEmail(email, callback);
    }

    public boolean canConnect() {
      return connectionAvailable;
    }
  }

  private App getApp() {
    return (App)Robolectric.application;
  }

  private class ExpectLoad implements Expectation {
    private final LoadBuilder loadBuilder = new LoadBuilder();
    private String expectedEmail;
    private final String expectedPassword;

    private ExpectLoad(String expectedEmail, String expectedPassword) {
      this.expectedEmail = expectedEmail;
      this.expectedPassword = expectedPassword;
    }

    public void load(String email, String password, DataSyncCallback callback) {
      Assert.assertEquals(expectedEmail, email);
      Assert.assertEquals(expectedPassword, password);
      GlobRepository repository = getApp().getRepository();
      loadBuilder.apply(repository);
      if (verbose) {
        GlobPrinter.print(repository);
      }
      callback.onActionFinished();
    }
  }

  private class ExpectLoadDemoFile implements Expectation {
    private final LoadBuilder loadBuilder = new LoadBuilder();

    public void loadDemoFile() {
      GlobRepository repository = getApp().getRepository();
      loadBuilder.apply(repository);
    }
  }

  private class ExpectLoadTempFile implements Expectation {
    public boolean loadTempFile() {
      Assert.fail("tbd");
      return false;
    }
  }

  private class ExpectSendDownloadEmail implements Expectation {
    public void sendDownloadEmail(String email, DataSyncCallback callback) {
      Assert.fail("tbd");
    }
  }
}
