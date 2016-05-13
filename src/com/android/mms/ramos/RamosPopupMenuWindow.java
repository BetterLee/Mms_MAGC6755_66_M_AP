package com.android.mms.ramos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.View.OnKeyListener;

import com.android.mms.R;
import android.util.Log;

public class RamosPopupMenuWindow {

    private final static String TAG = "RamosPopupMenuWindow";
	private Context mContext;
	private View mAnchorView;
	private PopupWindow popupWindow;
	private RelativeLayout contentView;
	private TextView mTitle;
	private ListView mList;
	private String title;
	private final ArrayList<Item> mItems = new ArrayList<Item>();
	private ArrayList<Item> mItemsTemp = new ArrayList<Item>();
    private OnPopupItemClickListener mOnPopupItemClickListener;
    ArrayList<String> TitleList = new ArrayList<String>();
    
    private final OnItemClickListener mOnItemClickListener =
            new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (popupWindow == null) return;
                    popupWindow.dismiss();
                    if (mOnPopupItemClickListener != null) {
                        mOnPopupItemClickListener.onPopupItemClick((int) id);
                    }
                }
            };
            
	public RamosPopupMenuWindow(Context context, View anchorView) {
		mContext = context;
		mAnchorView = anchorView;
	}
	
    public static interface OnPopupItemClickListener {
        public boolean onPopupItemClick(int itemId);
    }
    
    public void setTitle(String title) {
        this.title = title;
    }

	public void init() {
		contentView = (RelativeLayout) LayoutInflater.from(mContext).inflate(
				R.layout.ramos_popup_menu_layout, null);
		mTitle = (TextView)contentView.findViewById(R.id.title);
		mTitle.setText(title);
		
		mList = (ListView) contentView.findViewById(R.id.list);
		mList.setAdapter(new ItemDataAdapter());
		mList.setOnItemClickListener(mOnItemClickListener);
		
		contentView.findViewById(R.id.top_null).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				dismiss();
			}
		});
		makePopupWindow();

	}
	

	
	private void makePopupWindow() {
		if (popupWindow == null) {
			popupWindow = new PopupWindow(contentView, 
					LayoutParams.MATCH_PARENT, 
					LayoutParams.WRAP_CONTENT, true);
			popupWindow.setTouchable(true);
			popupWindow.setOutsideTouchable(true);
			popupWindow.setBackgroundDrawable(new ColorDrawable(0x80000000));
			popupWindow.update();
			popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
			//popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
			//popupWindow.setAnimationStyle(R.style.PopupAnimation);
		}
	}
	
	public void show() {
		if (popupWindow != null && !popupWindow.isShowing()) {
			popupWindow.showAtLocation(mAnchorView, Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
		}
	}
	
	public void dismiss() {
		if (popupWindow != null && popupWindow.isShowing()) {
			popupWindow.dismiss();
		}
	}
	

	
    public class Item {
        public int id;
        public String title;
		private boolean mEnabled;

        public Item(int id, String title, boolean mEnabled) {
            this.id = id;
            this.title = title;
			this.mEnabled = mEnabled;
        }

        public void setTitle(String title) {
            this.title = title;
        }
        
    }
    
    public int getCount() {
        return mItems.size();
    }

    //[ramos] begin liting 20160421 for the popupmenu of message linky
    public String getItem(int position) {
        return mItems.get(position).title;
    }
	
    public String getTitle() {
        return this.title;
    }
    //[ramos] end liting

    
    private class ItemDataAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mItems.get(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext)
                        .inflate(R.layout.ramos_popup_list_item, null);
            }
            TextView text = (TextView) convertView.findViewById(R.id.popup_list_title);
            text.setText(mItems.get(position).title);

			if (isEnabled(position)) {
				text.setAlpha(1f);
			} else {
				text.setAlpha(0.2f);
			}

            return convertView;
        }

		@Override
	    public boolean isEnabled(int position) {
	    
			return mItems.get(position).mEnabled;
	    }

		
    }


    public void addItem(int id, String title, boolean mEnabled) {
    	
        if ( !TitleList.contains(title) ){
        	TitleList.add(title);
            Item item = new Item(id,title, mEnabled);
            mItems.add(item);
        }

    }

	public void clear() {
		TitleList.clear();
		mItems.clear();
	}

    public void setOnPopupItemClickListener(OnPopupItemClickListener listener) {
        mOnPopupItemClickListener = listener;
    }
    
}

