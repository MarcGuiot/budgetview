package com.budgetview.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.budgetview.android.datasync.DataSync;
import com.budgetview.android.datasync.DataSyncCallback;
import com.budgetview.android.datasync.DataSyncFactory;
import com.budgetview.android.datasync.LoginInfo;

import java.io.IOException;

public class HomeActivity extends Activity {

  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    App app = (App)getApplication();
    if (app.isLoaded()) {
      gotoBudgetOverview(true, false);
      return;
    }

    final DataSync dataSync = DataSyncFactory.create(this);
    if (!dataSync.canConnect() && dataSync.loadTempFile()) {
      gotoBudgetOverview(true, false);
      return;
    }

    LoginInfo loginInfo = LoginInfo.load(this);
    if (!loginInfo.isSet()) {
      showHomePage();
      return;
    }

    loadData(dataSync, loginInfo);
  }

  private void loadData(DataSync dataSync, LoginInfo loginInfo) {
    showSplashPage();
    dataSync.load(loginInfo.email, loginInfo.password, new DataSyncCallback() {
      public void onActionFinished() {
        gotoBudgetOverview(true, false);
      }

      public void onConnectionUnavailable() {
        showHomePage();
      }

      public void onActionFailed() {
        showHomePage();
      }
    });
  }

  private void showHomePage() {
    setContentView(R.layout.home_page);
  }

  private void showSplashPage() {
    setContentView(R.layout.splash_page);
  }

  public void onLogin(View view) {
    Intent intent = new Intent(this, LoginActivity.class);
    this.startActivity(intent);
  }

  public void onDemo(View view) throws IOException {
    DataSync dataSync = DataSyncFactory.create(this);
    dataSync.loadDemoFile();
    gotoBudgetOverview(false, true);
  }

  private void gotoBudgetOverview(boolean clearBackHistory, boolean useDemoMode) {
    Intent intent = new Intent(this, BudgetOverviewActivity.class);
    if (useDemoMode) {
      intent.putExtra(DemoActivity.USE_DEMO, true);
    }
    if (clearBackHistory) {
      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      finish();
    }
    startActivity(intent);
  }
}
