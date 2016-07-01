package volpis.com.garadget.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import volpis.com.garadget.R;
import volpis.com.garadget.models.BottomPanelItem;


public class BottomPanelAdapter extends ArrayAdapter<BottomPanelItem> {

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<BottomPanelItem> tabs;

    private static final int TYPE_ITEM = 1;

    public BottomPanelAdapter(Context context, int resource, ArrayList<BottomPanelItem> tabs) {
        super(context, resource, tabs);
        this.tabs = tabs;
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public int getItemViewType(int position) {
        return tabs.get(position).getType();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        int type = getItemViewType(position);
        if (convertView == null) {
            holder = new ViewHolder();
            switch (type) {
                case TYPE_ITEM:
                    convertView = mInflater.inflate(R.layout.item_bottom_panel, null);
                    holder.textView = (TextView) convertView.findViewById(R.id.tv_title);
                    holder.imageView = (ImageView) convertView.findViewById(R.id.iv_tab_image);
                    convertView.setTag(holder);
                    break;
            }
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        switch (type) {
            case TYPE_ITEM:
                holder.imageView.setImageResource(tabs.get(position).getImageId());
                holder.textView.setText(tabs.get(position).getTitle());
                break;
        }
        convertView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        return convertView;
    }


    public static class ViewHolder {
        public TextView textView;
        public ImageView imageView;
    }

}
