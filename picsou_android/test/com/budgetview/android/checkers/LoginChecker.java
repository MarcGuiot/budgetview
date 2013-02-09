package com.budgetview.android.checkers;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.budgetview.android.LoginActivity;
import com.budgetview.android.R;
import org.robolectric.Robolectric;

public class LoginChecker extends AndroidChecker<LoginActivity> {

  public LoginChecker() throws Exception {
    super(LoginActivity.class);
  }

  protected void callOnCreate(LoginActivity activity, Bundle bundle) {
    activity.onCreate(bundle);
  }

  public LoginChecker setEmail(String email) {
    Robolectric.shadowOf((EditText)activity.findViewById(R.id.login_email)).setText(email);
    return this;
  }

  public LoginChecker setPassword(String password) {
    Robolectric.shadowOf((TextView)activity.findViewById(R.id.login_password)).setText(password);
    return this;
  }

  public BudgetOverviewChecker enter() {
    Robolectric.shadowOf((Button)activity.findViewById(R.id.login)).performClick();
    checkNoDialogShown();
    return new BudgetOverviewChecker();
  }
}
