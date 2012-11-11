package com.budgetview.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.budgetview.android.components.Header;
import com.budgetview.android.datasync.DataSync;
import com.budgetview.android.datasync.LoginInfo;

public class LoginActivity extends Activity {

  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.login_page);

    Header header = (Header)findViewById(R.id.header);
    header.setActivity(this);

    showProgressBar(View.INVISIBLE);
  }

  public void onLogin(View view) {

    final LoginInfo loginInfo = getLoginInfo();
    if (!loginInfo.isSet()) {
      Views.showAlert(this, R.string.loginWithNoId);
      return;
    }

    DataSync sync = new DataSync(this);
    sync.connect(loginInfo.email, loginInfo.password, new DataSync.Callback() {
      public void onActionFinished() {
        LoginInfo.save(loginInfo, LoginActivity.this);
        loadData();
      }

      public void onConnectionUnavailable() {
        Views.showAlert(LoginActivity.this, R.string.syncWithNoConnection);
      }

      public void onActionFailed() {
        Views.showAlert(LoginActivity.this, R.string.syncWithInvalidId);
      }
    });
  }

  private void loadData() {

    showProgressBar(View.VISIBLE);

    DataSync sync = new DataSync(this);
    sync.load(new DataSync.Callback() {
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
}
