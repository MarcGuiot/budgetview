package com.budgetview.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;

import com.budgetview.android.datasync.DataSyncFactory;
import com.budgetview.android.datasync.LoginInfo;
import com.budgetview.android.utils.AbstractBlock;

public class LogoutBlockView extends AbstractBlock {
    private final Activity activity;

    public LogoutBlockView(Activity activity) {
        super(R.layout.logout_block);
        this.activity = activity;
    }

    protected boolean isProperViewType(View view) {
        return view.findViewById(R.id.logout_button) != null;
    }

    protected void populateView(View view) {
        view.findViewById(R.id.logout_button).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                logout(activity);
            }
        });
    }

    public static void logout(final Activity activity) {
        AlertDialog dialog = new AlertDialog.Builder(activity).create();
        dialog.setTitle(activity.getString(R.string.logoutTitle));
        dialog.setMessage(activity.getString(R.string.logoutMessage));
        dialog.setCancelable(true);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, activity.getString(R.string.logoutYes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int buttonId) {
                doLogout(activity);
                dialog.cancel();
            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, activity.getString(R.string.logoutNo), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int buttonId) {
                dialog.cancel();
            }
        });
        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        dialog.show();
    }

    private static void doLogout(Activity activity) {
        App app = (App) activity.getApplication();
        app.reset();

        DataSyncFactory.create(activity).deleteTempFile();

        LoginInfo.clear(activity);

        Intent intent = new Intent(activity, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }
}
