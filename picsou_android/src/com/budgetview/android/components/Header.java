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
import com.budgetview.android.*;
import com.budgetview.android.datasync.DataSync;
import com.budgetview.android.datasync.DataSyncCallback;
import com.budgetview.android.datasync.DataSyncFactory;
import com.budgetview.android.datasync.LoginInfo;

public class Header extends LinearLayout {

  private Activity activity;
  private UpHandler upHandler;

  public Header(Context context) {
    super(context);
    initComponents(context);
  }

  public Header(Context context, AttributeSet attrs) {
    super(context, attrs);
    initComponents(context);
  }

  public Header(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    initComponents(context);
  }

  private void initComponents(Context context) {
    LayoutInflater inflater = (LayoutInflater)
      context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.header, this, true);
    setBackVisible(View.INVISIBLE);

    installRefreshListener();
  }

  private void installRefreshListener() {
    findViewById(R.id.header_refresh).setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        refresh();
      }
    });
  }

  public void init(Activity activity, UpHandler handler) {
    this.upHandler = handler;

    TextView button = (TextView)findViewById(R.id.header_title);
    button.setText(handler.getLabel());

    installNavigateUpListener(R.id.header_back_arrow);
    installNavigateUpListener(R.id.header_logo);
    installNavigateUpListener(R.id.header_title);

    this.activity = activity;
    setBackVisible(View.VISIBLE);
    updateRefreshVisibility(activity);
  }

  private void installNavigateUpListener(int viewId) {
    findViewById(viewId).setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        upHandler.processUp();
      }
    });
  }

  private void updateRefreshVisibility(Activity activity) {
    boolean visible = (activity != null) && !DemoActivity.isInDemoMode(activity);
    findViewById(R.id.header_refresh).setVisibility(visible ? VISIBLE : GONE);
  }

  private void setBackVisible(int visible) {
    ImageView button = (ImageView)findViewById(R.id.header_back_arrow);
    button.setVisibility(visible);
  }

  public void refresh() {
    if (activity == null) {
      return;
    }
    showProgressBar();
    DataSync dataSync = DataSyncFactory.create(activity);
    LoginInfo loginInfo = LoginInfo.load(activity);
    if (!loginInfo.isSet()) {
      Intent intent = new Intent(activity, HomeActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      activity.finish();
      activity.startActivity(intent);
      return;
    }

    dataSync.load(loginInfo.email, loginInfo.password, new DataSyncCallback() {
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
