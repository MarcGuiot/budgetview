package com.budgetview.android.datasync;

public interface DataSyncCallback {
    void onActionFinished();

    void onActionFailed();

    void onConnectionUnavailable();
}
