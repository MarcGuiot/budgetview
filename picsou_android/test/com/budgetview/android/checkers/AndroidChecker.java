package com.budgetview.android.checkers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import junit.framework.Assert;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowAlertDialog;

public abstract class AndroidChecker<T extends Activity> {

  protected final Activity activity;

  public AndroidChecker(T activity) {
    this.activity = activity;
    callOnCreate(activity, null);
  }

  public AndroidChecker(Class<T> activityClass) {
    this.activity = getCurrentActivity(activityClass);
  }

  protected T getCurrentActivity(Class<T> tClass) {
    Intent intent = Robolectric.getShadowApplication().getNextStartedActivity();
    if (intent == null) {
      Assert.fail("Next activity not started");
    }
    ComponentName component = intent.getComponent();
    if (!tClass.getName().equals(component.getClassName())) {
      Assert.fail("Unexpected activity class: " + component.getClassName() + " - expected: " + tClass.getName());
    }

    try {
      T activity = tClass.newInstance();
      activity.setIntent(intent);
      callOnCreate(activity, intent.getExtras());
      return activity;
    }
    catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  private String toString(Bundle bundle) {
    StringBuilder builder = new StringBuilder();
    boolean first = true;
    for (String key : bundle.keySet()) {
      if (!first) {
        builder.append(", ");
      }
      builder.append(key + ":" + bundle.get(key) + " ");
      first = false;
    }
    return builder.toString();
  }

  protected abstract void callOnCreate(T activity, Bundle bundle);

  protected void checkNoDialogShown() {
    AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
    if (dialog != null) {
      Assert.fail("Unexpected dialog shown: " + Robolectric.shadowOf(dialog).getMessage());
    }
  }
}
