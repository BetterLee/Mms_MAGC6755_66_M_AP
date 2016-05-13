package com.android.mms.ramos;


import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.android.mms.R;
import com.android.mms.ramos.RamosBottomPanel.OnHandleMessage;
import com.android.mms.ui.LevelControlLayout;
import com.android.mms.ui.LevelControlLayout.OnScrollToScreenListener;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.content.res.Configuration;
import android.widget.ImageView;

public class RamosAttachmentFragment extends Fragment implements OnItemClickListener {
	public final static int INDEX_CONTACT = 0;
	public final static int INDEX_IMAGE = 1;
	public final static int INDEX_TAGE_PHOTO = 2;
	public final static int INDEX_VIDEO = 3;
	public final static int INDEX_RECORD_VIDEO = 4;
	public final static int INDEX_SOUND = 5;
	public final static int INDEX_RECORD_SOUND = 6;
	public final static int INDEX_SLIDESHOW = 7;
    //[ramos] modified by liting 20151118 for temporary
	public final static int INDEX_THEME = 8;
	public final static int INDEX_TIMER = 9;
    //[ramos] end liting
	private GridView mGridView;	
	private OnHandleMessage mHandler;	
    
    private LevelControlLayout mScrollLayout;
    private RadioButton mDotFirst;
    private RadioButton mDotSec;
    private int mOrientation;
    private int[] mColumnArray;
    private int mScreenIndex;
    private Context mContext;
    public static final String SHARE_ACTION = "shareAction";
    private String[] mSource;

	@Override
	public void onAttach(Activity activity) {
		mHandler = (OnHandleMessage)activity;
        mContext = activity;
		super.onAttach(activity);
	}
	@Override
	public void onDetach() {
		mHandler = null;
		super.onDetach();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.ramos_fragment_attachment, null);
		return v;
	}
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {		
//		GridViewAdapter adapter = new GridViewAdapter(getActivity());
//		mGridView = (GridView)view.findViewById(R.id.gridview);
//		mGridView.setAdapter(adapter);
//		mGridView.setOnItemClickListener(this);
        mScrollLayout = (LevelControlLayout) view.findViewById(R.id.ramos_share_panel_zone);
        mDotFirst = (RadioButton) view.findViewById(R.id.ramos_rb_dot_first);
        mDotSec = (RadioButton) view.findViewById(R.id.ramos_rb_dot_sec);
        resetShareItem();
		super.onViewCreated(view, savedInstanceState);
	}
	
	
    public void resetShareItem() {
        mOrientation = getResources().getConfiguration().orientation;
        if (mScrollLayout.getChildCount() != 0) {
            mScrollLayout.removeAllViews();
        }
        addSharePage(0);
        addSharePage(1);
        mDotSec.setVisibility(View.VISIBLE);
        mDotFirst.setVisibility(View.VISIBLE);
        mDotFirst.setChecked(true);
        mScrollLayout.setOnScrollToScreen(new OnScrollToScreenListener() {
            @Override
            public void doAction(int whichScreen) {
                mScreenIndex = whichScreen;
                if (whichScreen == 0) {
                    mDotFirst.setChecked(true);
                } else {
                    mDotSec.setChecked(true);
                }
            }
        });
        mScrollLayout.setDefaultScreen(mScreenIndex);
        mScrollLayout.autoRecovery();
    }

    private void addSharePage(int index) {
        mColumnArray = getResources().getIntArray(R.array.share_column);
        View v = LayoutInflater.from(mContext)
                .inflate(R.layout.share_flipper, mScrollLayout, false);
        GridView gridView = (GridView) v.findViewById(R.id.gv_share_gridview);
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            gridView.setNumColumns(mColumnArray[0]);
        } else {
            gridView.setNumColumns(mColumnArray[1]);
        }
        ShareAdapter adapter = new ShareAdapter(getLableArray(index), getIconArray(index));
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int actionPosition = getActionId(position);
                Message msg = Message.obtain();
                msg.what = 2;
                msg.obj = actionPosition;
                mHandler.handleMessage(msg);
            }
        });
        mScrollLayout.addView(v);
    }
    
    private String[] getLableArray(int index) {
            mSource = getResources().getStringArray(R.array.ramos_share_string_array);
        int onePage;
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            onePage = mColumnArray[0] * 2;
        } else {
            onePage = mColumnArray[1];
        }
        if (index == 0) {
            String[] index0 = new String[onePage];
            for (int i = 0; i < onePage; i++) {
                index0[i] = mSource[i];
            }
            return index0;
        } else {
            int count = mSource.length - onePage;
            String[] index1 = new String[count];
            for (int i = 0; i < count; i++) {
                index1[i] = mSource[onePage + i];
            }
            return index1;
        }
    }

    private int[] getIconArray(int index) {
        int[] source = {
                R.drawable.ramos_radio_btn_attach_contact_selector,
                R.drawable.ramos_radio_btn_attach_image_selector,
                R.drawable.ramos_radio_btn_attach_take_pic_selector,
                R.drawable.ramos_radio_btn_attach_video_selector,
                R.drawable.ramos_radio_btn_attach_record_video_selector,
                R.drawable.ramos_radio_btn_attach_audio_selector,
                R.drawable.ramos_radio_btn_attach_record_audio_selector,
                R.drawable.ramos_radio_btn_attach_slider_selector,
                R.drawable.ramos_radio_btn_attach_theme_selector//,
//                R.drawable.ramos_radio_btn_attach_timer_selector //[ramos] deleted by liting 20151118 for temporary
        };

        int onePage;
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            onePage = mColumnArray[0] * 2;
        } else {
            onePage = mColumnArray[1];
        }
        if (index == 0) {
            int[] index0 = new int[onePage];
            for (int i = 0; i < onePage; i++) {
                index0[i] = source[i];
            }
            return index0;
        } else {
            int count = source.length - onePage;
            int[] index1 = new int[count];
            for (int i = 0; i < count; i++) {
                index1[i] = source[onePage + i];
            }
            return index1;
        }
    }

    private int getActionId(int position) {
        int onePage;
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            onePage = mColumnArray[0] * 2;
        } else {
            onePage = mColumnArray[1];
        }
        if (mScreenIndex == 0) {
            return position;
        } else {
            return onePage + position;
        }
    }


	public class GridViewAdapter extends BaseAdapter {
		private Context mContext;
		private int[] mItems = {
				R.string.attach_contact,
				R.string.attach_image,
				R.string.attach_take_photo,
				R.string.attach_video,
				R.string.attach_record_video,
				R.string.attach_sound,
				R.string.attach_record_sound,
				R.string.attach_slideshow,
				//R.string.attach_timer,  //[ramos] deleted by liting 20151118 for temporary
				R.string.attach_theme
		};
		
		private int[] mDrawables = {
				R.drawable.ramos_radio_btn_attach_contact_selector,
				R.drawable.ramos_radio_btn_attach_image_selector,
				R.drawable.ramos_radio_btn_attach_take_pic_selector,
				R.drawable.ramos_radio_btn_attach_video_selector,
				R.drawable.ramos_radio_btn_attach_record_video_selector,
				R.drawable.ramos_radio_btn_attach_audio_selector,
				R.drawable.ramos_radio_btn_attach_record_audio_selector,
				R.drawable.ramos_radio_btn_attach_slider_selector,
				//R.drawable.ramos_radio_btn_attach_timer_selector, //[ramos] deleted by liting 20151118 for temporary
				R.drawable.ramos_radio_btn_attach_theme_selector
		};
		
	    public GridViewAdapter(Context context) {
	    	mContext = context;
	    }

	    @Override
	    public int getCount() {
	        return mItems.length;
	    }

	    @Override
	    public Object getItem(int position) {
	        return position;
	    }

	    @Override
	    public long getItemId(int position) {
	        return position;
	    }

	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	    	if(convertView == null) {
	        	convertView = new TextView(mContext);
	        }
	        TextView v = (TextView)convertView;
//	        v.setPadding(0, 5, 0, 0);
//	        v.setBackgroundResource(R.drawable.ramos_grid_item_selector);
	        
	    	Drawable d = getResources().getDrawable(mDrawables[position]);
	    	d.setBounds(0,0,d.getMinimumWidth()-12, d.getMinimumHeight()-12);
	        v.setCompoundDrawables(null, d, null, null);
	        v.setGravity(Gravity.CENTER_HORIZONTAL);
	        v.setText(mItems[position]);
	        //[ramos] added by liting 20151021 for BUG0008868
	        v.setTextSize(14);
	        v.setTextColor(0xff4e5151);
	        //[ramos] end liting
	        //[ramos] added by liting 20160408 for BUG0012026
	        v.setBackgroundColor(0xffeceff1);
	        //[ramos] end liting
	        return convertView;
	    }
	}
    
    private class ShareAdapter extends BaseAdapter {

        private String[] mStringArray;
        private int[] mIconArray;

        public ShareAdapter(Context context) {

        }

        public ShareAdapter(String[] stringArray, int[] iconArray) {
            mStringArray = stringArray;
            mIconArray = iconArray;
        }

        @Override
        public int getCount() {
            int count = 0;
            if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                count = mColumnArray[0] * 2;
            } else {
                count = mColumnArray[1];
            }
            return count;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.ramos_share_grid_common_item, null);
                convertView.setTag(convertView);
            } else {
                convertView = (View) convertView.getTag();
            }

            TextView text = (TextView) convertView.findViewById(R.id.ramos_tv_share_name);
            ImageView img = (ImageView) convertView.findViewById(R.id.ramos_iv_share_icon);
            if (position < mStringArray.length) {
                text.setText(mStringArray[position]);
                img.setImageResource(mIconArray[position]);
            }
            return convertView;
        }
    }


	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Message msg = Message.obtain();
		msg.what = 2;
		msg.obj = arg2;
		mHandler.handleMessage(msg);
	}
}
