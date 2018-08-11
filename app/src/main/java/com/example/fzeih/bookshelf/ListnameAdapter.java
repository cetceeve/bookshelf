package com.example.fzeih.bookshelf;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.LinkedHashMap;

public class ListnameAdapter extends BaseAdapter {
    private LinkedHashMap<String, String> mListMap = new LinkedHashMap<String, String>();
    private String[] mBookListKeys;

    public ListnameAdapter(LinkedHashMap<String, String> listMap) {
        mListMap = listMap;
        mBookListKeys = mListMap.keySet().toArray(new String[listMap.size()]);
    }

    @Override
    public int getCount() {
        return mListMap.size();
    }

    @Override
    public Object getItem(int position) {
        return mListMap.get(mBookListKeys[position]);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) parent.getContext()).getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        TextView textView = convertView.findViewById(android.R.id.text1);

        String listName = getItem(pos).toString();

        textView.setText(listName);

        return convertView;
    }
}
