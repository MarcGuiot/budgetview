package com.budgetview.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.budgetview.android.components.Header;
import com.budgetview.android.components.UpHandler;
import com.budgetview.android.datasync.DataSync;
import com.budgetview.android.datasync.DataSyncCallback;
import com.budgetview.android.datasync.DataSyncFactory;
import org.globsframework.utils.Strings;

public class DemoActivity extends Activity {

  public static final String USE_DEMO = "com.budgetview.DemoActivity.useDemo";

  public static void install(Button demoButton, final Activity activity) {
    boolean demoModeEnabled = isInDemoMode(activity);
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

  public static boolean isInDemoMode(Activity activity) {
    return activity.getIntent().getBooleanExtra(USE_DEMO, false);
  }

  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.demo_page);

    Header header = (Header)findViewById(R.id.header);
    header.init(this, new DemoUpHandler());

    TextView link = (TextView)findViewById(R.id.goto_website);
    link.setText(R.string.demoLink);
    Linkify.addLinks(link, Linkify.WEB_URLS);
  }

  public void onSendEmail(View view) {
    EditText emailEdit = (EditText)findViewById(R.id.login_email);
    String email = emailEdit.getText().toString();
    if (Strings.isNullOrEmpty(email)) {
      Views.showAlert(this, R.string.demoEmailEmpty);
      return;
    }
    DataSync dataSync = DataSyncFactory.create(this);
    dataSync.sendDownloadEmail(email, new DataSyncCallback() {
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

  private class DemoUpHandler implements UpHandler {
    public String getLabel() {
      return getResources().getString(R.string.app_name);
    }

    public void processUp() {
      finish();
    }
  }
}
