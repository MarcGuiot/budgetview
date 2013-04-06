package com.budgetview.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import com.budgetview.android.components.Header;
import com.budgetview.android.components.UpHandler;
import com.budgetview.android.datasync.*;

public class LoginActivity extends Activity {

  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.login_page);

    Header header = (Header)findViewById(R.id.header);
    header.init(this, new LoginUpHandler());

    showProgressBar(View.INVISIBLE);
  }

  public void onLogin(View view) {

    final LoginInfo loginInfo = getLoginInfo();
    if (!loginInfo.isSet()) {
      Views.showAlert(this, R.string.loginWithNoId);
      return;
    }

    final DataSync sync = DataSyncFactory.create(this);
    LoginInfo.save(loginInfo, LoginActivity.this);
    loadData(sync, loginInfo);
  }

  private void loadData(DataSync sync, LoginInfo loginInfo) {

    showProgressBar(View.VISIBLE);

    sync.load(loginInfo.email, loginInfo.password, new DownloadCallback() {
      public void onActionFinished() {
        showProgressBar(View.INVISIBLE);
        Intent intent = new Intent(LoginActivity.this, BudgetOverviewActivity.class);
        LoginActivity.this.startActivity(intent);
      }

      public void onConnectionUnavailable() {
        showProgressBar(View.INVISIBLE);
        Views.showAlert(LoginActivity.this, R.string.syncWithNoConnection);
      }

      public void onActionFailed() {
        showProgressBar(View.INVISIBLE);
        Views.showAlert(LoginActivity.this, R.string.syncWithInvalidId);
      }

      public void onDownloadFailed(String errorMessage) {
        showProgressBar(View.INVISIBLE);
        Views.showAlert(LoginActivity.this, errorMessage);
      }

      public void onDownloadFailed(Integer errorId) {
        showProgressBar(View.INVISIBLE);
        Views.showAlert(LoginActivity.this, errorId);
      }
    });
  }

  private void showProgressBar(int visible) {
    findViewById(R.id.login_progress_bar).setVisibility(visible);
  }

  private LoginInfo getLoginInfo() {
    EditText emailView = (EditText)findViewById(R.id.login_email);
    EditText passwordView = (EditText)findViewById(R.id.login_password);
    return new LoginInfo(emailView.getText().toString(), passwordView.getText().toString());
  }

  private class LoginUpHandler implements UpHandler {
    public String getLabel() {
      return getResources().getString(R.string.app_name);
    }

    public void processUp() {
      finish();
    }
  }
}
