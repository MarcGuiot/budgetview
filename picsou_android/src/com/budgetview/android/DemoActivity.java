package com.budgetview.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import com.budgetview.android.components.Header;
import com.budgetview.android.datasync.DataSync;
import org.globsframework.utils.Strings;

public class DemoActivity extends Activity {
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
