package com.budgetview.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.budgetview.android.datasync.DataSync;
import com.budgetview.android.datasync.LoginInfo;

import java.io.IOException;

public class HomeActivity extends Activity {

  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    App app = (App)getApplication();
    if (app.isLoaded()) {
      gotoBudgetOverview(true);
      return;
    }

    LoginInfo loginInfo = LoginInfo.load(this);
    if (!loginInfo.isSet()) {
      showHomePage();
      return;
    }

    final DataSync dataSync = new DataSync(this);
    dataSync.connect(loginInfo.email, loginInfo.password, new DataSync.Callback() {
      public void onActionFinished() {
        loadData(dataSync);
      }

      public void onConnectionUnavailable() {
        if (dataSync.loadTempFile()) {
          gotoBudgetOverview(true);
        }
        else {
          showHomePage();
        }
      }

      public void onActionFailed() {
        showHomePage();
      }
    });
  }

  private void loadData(DataSync dataSync) {
    showSplashPage();
    dataSync.load(new DataSync.Callback() {
      public void onActionFinished() {
        gotoBudgetOverview(true);
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
    DataSync dataSync = new DataSync(this);
    dataSync.loadDemoFile();
    gotoBudgetOverview(false);
  }

  private void gotoBudgetOverview(boolean clearBackHistory) {
    Intent intent = new Intent(this, BudgetOverviewActivity.class);
    if (clearBackHistory) {
      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      finish();
    }
    startActivity(intent);
  }
}
