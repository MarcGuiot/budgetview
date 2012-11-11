package com.budgetview.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.budgetview.android.components.Header;
import com.budgetview.android.datasync.DataSync;
import org.globsframework.utils.Strings;

public class DemoActivity extends Activity {

  public static final String USE_DEMO = "com.budgetview.DemoActivity.useDemo";

  public static void install(Button demoButton, final Activity activity) {
    boolean demoModeEnabled = activity.getIntent().getBooleanExtra(USE_DEMO, false);
    if (demoModeEnabled) {
      demoButton.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
          Intent intent = new Intent(activity, DemoActivity.class);
          activity.startActivity(intent);
        }
      });
    }
    else {
      demoButton.setVisibility(View.GONE);
    }
  }

  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.demo_page);

    Header header = (Header)findViewById(R.id.header);
    header.setActivity(this);
  }

  public void onSendEmail(View view) {
    EditText emailEdit = (EditText)findViewById(R.id.login_email);
    String email = emailEdit.getText().toString();
    if (Strings.isNullOrEmpty(email)) {
      Views.showAlert(this, R.string.demoEmailEmpty);
      return;
    }
    DataSync dataSync = new DataSync(this);
    dataSync.sendDownloadEmail(email, new DataSync.Callback() {
      public void onActionFinished() {
        Views.showAlert(DemoActivity.this, R.string.demoEmailSent);
      }

      public void onActionFailed() {
        Views.showAlert(DemoActivity.this, R.string.demoEmailSendingFailure);
      }

      public void onConnectionUnavailable() {
        Views.showAlert(DemoActivity.this, R.string.demoEmailConnectionUnavailable);
      }
    });
  }
}
