package com.budgetview.android.components;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.budgetview.android.BudgetOverviewActivity;
import com.budgetview.android.DemoActivity;
import com.budgetview.android.R;
import com.budgetview.android.Views;
import com.budgetview.android.datasync.DataSync;
import com.budgetview.android.datasync.DataSyncCallback;
import com.budgetview.android.datasync.DataSyncFactory;

public class Header extends LinearLayout {

  private Activity activity;

  public Header(Context context) {
    super(context);
    init(context);
  }

  public Header(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public Header(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(context);
  }

  private void init(Context context) {
    LayoutInflater inflater = (LayoutInflater)
      context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.header, this, true);
    setBackVisible(View.INVISIBLE);

    installNavigateUpListener(R.id.header_back_arrow);
    installNavigateUpListener(R.id.header_logo);
    installNavigateUpListener(R.id.header_title);
    installRefreshListener();
  }

  private void installNavigateUpListener(int viewId) {
    findViewById(viewId).setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        navigateUp();
      }
    });
  }

  private void installRefreshListener() {
    findViewById(R.id.header_refresh).setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        refresh();
      }
    });
  }

  public void setTitle(String title) {
    TextView button = (TextView)findViewById(R.id.header_title);
    button.setText(title);
  }

  public void setActivity(final Activity activity) {
    this.activity = activity;
    setBackVisible(View.VISIBLE);
  }

  private void setBackVisible(int visible) {
    ImageView button = (ImageView)findViewById(R.id.header_back_arrow);
    button.setVisibility(visible);
  }

  public void navigateUp() {
    activity.finish();
  }

  public void refresh() {
    if (activity == null) {
      return;
    }
    showProgressBar();
    DataSync dataSync = DataSyncFactory.create(activity);
    dataSync.load(new DataSyncCallback() {
      public void onActionFinished() {
        Intent intent = new Intent(activity, BudgetOverviewActivity.class);
        intent.putExtra(DemoActivity.USE_DEMO, false);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.finish();
        activity.startActivity(intent);
      }

      public void onConnectionUnavailable() {
        hideProgressBar();
        Views.showAlert(activity, R.string.syncWithNoConnection);
      }

      public void onActionFailed() {
        hideProgressBar();
        Views.showAlert(activity, R.string.syncWithInvalidId);
      }
    });
  }

  private void showProgressBar() {
    findViewById(R.id.header_refresh_progress).setVisibility(VISIBLE);
    findViewById(R.id.header_refresh).setVisibility(GONE);
  }

  private void hideProgressBar() {
    findViewById(R.id.header_refresh_progress).setVisibility(GONE);
    findViewById(R.id.header_refresh).setVisibility(VISIBLE);
  }
}
