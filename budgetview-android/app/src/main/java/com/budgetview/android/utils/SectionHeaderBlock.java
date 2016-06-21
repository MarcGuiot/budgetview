package com.budgetview.android.utils;

import android.content.res.Resources;
import android.view.View;

import com.budgetview.android.R;
import com.budgetview.android.Views;

public class SectionHeaderBlock extends AbstractBlock {
    private int titleId;
    private final Resources resources;

    public SectionHeaderBlock(int titleId, Resources resources) {
        super(R.layout.section_header_block);
        this.titleId = titleId;
        this.resources = resources;
    }

    protected boolean isProperViewType(View view) {
        return view.findViewById(R.id.accountSectionLabel) != null;
    }

    protected void populateView(View view) {
        Views.setText(view, R.id.accountSectionLabel, resources.getText(titleId));
    }
}
