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
import com.budgetview.android.utils.TransactionSet;
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
    this.transactionSet = new TransactionSet(args, repository);

    ListView list = (ListView)view.findViewById(R.id.transactionList);
    list.setAdapter(new TransactionListAdapter(inflater));

    return view;
  }

  private class TransactionListAdapter extends BaseAdapter {

    private GlobList transactionValuesList;
    private LayoutInflater inflater;

    private TransactionListAdapter(LayoutInflater inflater) {
      this.inflater = inflater;
      App app = (App)getActivity().getApplication();
      transactionValuesList =
        app.getRepository()
          .getAll(TransactionValues.TYPE, transactionSet.getMatcher())
          .sort(TransactionValues.SEQUENCE_NUMBER);
    }

    public int getCount() {
      return transactionValuesList.size();
    }

    public Object getItem(int i) {
      return transactionValuesList.get(i);
    }

    public long getItemId(int i) {
      return getItem(i).hashCode();
    }

    public View getView(int i, View previousView, ViewGroup parent) {

      View view = previousView;
      if (view == null) {
        view = inflater.inflate(R.layout.transaction_block, parent, false);
      }

      final Glob values = transactionValuesList.get(i);
      Double amount = values.get(TransactionValues.AMOUNT);
      Views.setText(view, R.id.transactionLabel, values.get(TransactionValues.LABEL));
      Views.setColoredText(view, R.id.transactionAmount, amount);
      Views.setText(view, R.id.transactionDate, getDate(values));

      if (values.isTrue(TransactionValues.PLANNED)) {
        Views.setTextColor(view, R.id.transactionLabel, R.color.item_label_disabled);
      }

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
      if (values.isTrue(TransactionValues.PLANNED)) {
        return Text.toPlannedOnDayMonthString(values.get(TransactionValues.BANK_DAY),
                                              values.get(TransactionValues.BANK_MONTH),
                                              getResources());
      }
      else {
        return Text.toOnDayMonthString(values.get(TransactionValues.BANK_DAY),
                                       values.get(TransactionValues.BANK_MONTH),
                                       getResources());
      }
    }
  }
}
