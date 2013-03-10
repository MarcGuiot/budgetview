package com.budgetview.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import com.budgetview.android.components.TabPage;
import com.budgetview.android.utils.SectionHeaderBlock;
import com.budgetview.android.utils.TransactionSet;
import com.budgetview.shared.model.SeriesValues;
import com.budgetview.shared.model.TransactionValues;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;

public class TransactionListFragment extends Fragment {

  private TransactionSet transactionSet;

  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.transaction_list, container, false);

    Bundle args = getArguments();
    GlobRepository repository = ((App)getActivity().getApplication()).getRepository();
    transactionSet = new TransactionSet(args, repository);

    AmountsBlockView seriesBlock = (AmountsBlockView)view.findViewById(R.id.transaction_amounts);
    seriesBlock.update(transactionSet.getSeriesValues(),
                       SeriesValues.AMOUNT, SeriesValues.PLANNED_AMOUNT,
                       SeriesValues.OVERRUN_AMOUNT, SeriesValues.REMAINING_AMOUNT);

    AccountSummaryBlockView positionBlock = (AccountSummaryBlockView)view.findViewById(R.id.transaction_account_position);
    positionBlock.update(transactionSet.getAccountEntity());

    ListView list = (ListView)view.findViewById(R.id.transactionList);
    list.setAdapter(new TransactionListAdapter(inflater));

    EmptyListView emptyView = (EmptyListView)view.findViewById(R.id.emptyListView);
    emptyView.update(R.string.transactionListEmptyMessage);
    list.setEmptyView(emptyView);

    return view;
  }

  private class TransactionListAdapter extends BaseAdapter {

    private GlobList transactionValuesList;
    private LayoutInflater inflater;
    private SectionHeaderBlock headerBlock;

    private TransactionListAdapter(LayoutInflater inflater) {
      this.inflater = inflater;
      App app = (App)getActivity().getApplication();
      transactionValuesList =
        app.getRepository()
          .getAll(TransactionValues.TYPE, transactionSet.getMatcher())
          .sort(TransactionValues.SEQUENCE_NUMBER);
    }

    public int getCount() {
      if (transactionValuesList.isEmpty()) {
        return 0;
      }
      return transactionValuesList.size() + 1;
    }

    public Object getItem(int index) {
      if (index == 0) {
        return "Header";
      }
      return transactionValuesList.get(index - 1);
    }

    public long getItemId(int i) {
      return getItem(i).hashCode();
    }

    public View getView(int index, View previousView, ViewGroup parent) {

      if (index == 0) {
        if (headerBlock == null) {
          headerBlock = new SectionHeaderBlock(R.string.transactionListSectionLabel, getResources());
        }
        return headerBlock.getView(inflater, previousView, parent);
      }

      View view = previousView;
      if (view == null || view.findViewById(R.id.transactionLabel) == null) {
        view = inflater.inflate(R.layout.transaction_block, parent, false);
      }

      final Glob values = transactionValuesList.get(index - 1);
      Double amount = values.get(TransactionValues.AMOUNT);
      Views.setText(view, R.id.transactionLabel, values.get(TransactionValues.LABEL));
      Views.setColoredText(view, R.id.transactionAmount, amount);
      Views.setText(view, R.id.transactionDate, getDate(values));

      view.setOnClickListener(new View.OnClickListener() {
        public void onClick(View view) {
          Intent intent = new Intent(getActivity(), TransactionPageActivity.class);
          intent.putExtra(TransactionPageActivity.TRANSACTION_ID_PARAMETER, values.get(TransactionValues.ID));
          transactionSet.save(intent);
          TabPage.copyDemoMode(getActivity(), intent);
          startActivity(intent);
        }
      });

      return view;
    }

    private String getDate(Glob values) {
      return Text.toOnDayMonthString(values.get(TransactionValues.BANK_DAY),
                                     values.get(TransactionValues.BANK_MONTH),
                                     getResources());
    }
  }
}
