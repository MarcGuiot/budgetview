package com.budgetview.android.components;

import android.support.v4.app.Fragment;

public interface TabPageHandler {
    Fragment createFragmentWithArgs(int monthId);
}
