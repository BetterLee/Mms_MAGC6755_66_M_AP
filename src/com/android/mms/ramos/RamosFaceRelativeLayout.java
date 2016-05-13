package com.android.mms.ramos;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.R;
import com.android.mms.ramos.RamosBottomPanel.OnHandleMessage;
import com.android.mms.ramos.RamosFaceUtil.ChatEmoji;

public class RamosFaceRelativeLayout extends RelativeLayout implements OnItemClickListener {

	private Context mContext;
	private OnCorpusSelectedListener mListener;
	private ViewPager mViewPager;
	private ArrayList<View> mPageFaces;
	private LinearLayout mLayout_point;
	private ArrayList<ImageView> mPointViews;
	private List<List<ChatEmoji>> mEmojis;
	private List<FaceAdapter> mFaceAdapters;
	private int mCurrentPage = 0;
	private OnHandleMessage mHandler;
	
	public RamosFaceRelativeLayout(Context context) {
		super(context);
		this.mContext = context;
		mHandler = (OnHandleMessage)context;
	}

	public RamosFaceRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		mHandler = (OnHandleMessage)context;
	}

	public RamosFaceRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
		mHandler = (OnHandleMessage)context;
	}

	public void setOnCorpusSelectedListener(OnCorpusSelectedListener listener) {
		mListener = listener;
	}

	public interface OnCorpusSelectedListener {

		void onCorpusSelected(ChatEmoji emoji);

		void onCorpusDeleted();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mEmojis = RamosFaceUtil.getInstace().mEmojiPages;
		onCreate();
	}

	private void onCreate() {
		Init_View();
		Init_viewPager();
		Init_Point();
		Init_Data();
	}

	private void Init_View() {
		mViewPager = (ViewPager) findViewById(R.id.vp_contains);
		mLayout_point = (LinearLayout) findViewById(R.id.iv_image);
	}

	private void Init_viewPager() {
		mPageFaces = new ArrayList<View>();
		View nullView1 = new View(mContext);
		nullView1.setBackgroundColor(Color.TRANSPARENT);
		mPageFaces.add(nullView1);
		mFaceAdapters = new ArrayList<FaceAdapter>();
		for (int i = 0; i < mEmojis.size(); i++) {
			GridView view = new GridView(mContext);
			FaceAdapter adapter = new FaceAdapter(mContext, mEmojis.get(i));
			view.setAdapter(adapter);
			mFaceAdapters.add(adapter);
			view.setOnItemClickListener(this);
			view.setNumColumns(RamosFaceUtil.PAGE_COLUMNS);
			view.setBackgroundColor(Color.TRANSPARENT);
			view.setHorizontalSpacing(1);
			view.setVerticalSpacing(1);
			view.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
			view.setCacheColorHint(0);
			view.setPadding(5, 0, 5, 0);
			view.setSelector(new ColorDrawable(Color.TRANSPARENT));
			view.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT));
			view.setGravity(Gravity.CENTER);
			mPageFaces.add(view);
		}

		View nullView2 = new View(mContext);
		nullView2.setBackgroundColor(Color.TRANSPARENT);
		mPageFaces.add(nullView2);
	}

	private void Init_Point() {

		mPointViews = new ArrayList<ImageView>();
		ImageView imageView;
		for (int i = 0; i < mPageFaces.size(); i++) {
			imageView = new ImageView(mContext);
			imageView.setBackgroundResource(R.drawable.d1);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT));
			layoutParams.leftMargin = 10;
			layoutParams.rightMargin = 10;
			layoutParams.width = 8;
			layoutParams.height = 8;
			mLayout_point.addView(imageView, layoutParams);
			if (i == 0 || i == mPageFaces.size() - 1) {
				imageView.setVisibility(View.GONE);
			}
			if (i == 1) {
				imageView.setBackgroundResource(R.drawable.d2);
			}
			mPointViews.add(imageView);

		}
	}

	/**
	 * 填充数据
	 */
	private void Init_Data() {
		mViewPager.setAdapter(new ViewPagerAdapter(mPageFaces));

		mViewPager.setCurrentItem(1);
		mCurrentPage = 0;
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				mCurrentPage = arg0 - 1;
				draw_Point(arg0);
				if (arg0 == mPointViews.size() - 1 || arg0 == 0) {
					if (arg0 == 0) {
						mViewPager.setCurrentItem(arg0 + 1);
						mPointViews.get(1).setBackgroundResource(R.drawable.d2);
					} else {
						mViewPager.setCurrentItem(arg0 - 1);
						mPointViews.get(arg0 - 1).setBackgroundResource(
								R.drawable.d2);
					}
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});

	}
	public void draw_Point(int index) {
		for (int i = 1; i < mPointViews.size(); i++) {
			if (index == i) {
				mPointViews.get(i).setBackgroundResource(R.drawable.d2);
			} else {
				mPointViews.get(i).setBackgroundResource(R.drawable.d1);
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		ChatEmoji emoji = (ChatEmoji) mFaceAdapters.get(mCurrentPage).getItem(arg2);
		EditText et = ((ComposeMessageActivity)mContext).getMessageEditor();
		if (emoji.getId() == R.drawable.ramos_face_del_icon) {
			int selection = et.getSelectionStart();
			String text = et.getText().toString();
			if (selection > 0) {
				String text2 = text.substring(selection - 1);
				if ("]".equals(text2)) {
					int start = text.lastIndexOf("[");
					int end = selection;
					et.getText().delete(start, end);
					return;
				}
				et.getText().delete(selection - 1, selection);
			}
		}
		if (!TextUtils.isEmpty(emoji.getCharacter())) {
			if (mListener != null)
				mListener.onCorpusSelected(emoji);
			SpannableString spannableString = RamosFaceUtil.getInstace()
					.addFace(getContext(), emoji.getId(), emoji.getCharacter());
			et.append(spannableString);	
		}

	}

	
	public class ViewPagerAdapter extends PagerAdapter {

	    private List<View> mViews;

	    public ViewPagerAdapter(List<View> views) {
	        super();
	        this.mViews=views;
	    }

	    @Override
	    public int getCount() {
	        return mViews.size();
	    }

	    @Override
	    public boolean isViewFromObject(View arg0, Object arg1) {
	        return arg0 == arg1;
	    }

	    @Override
	    public int getItemPosition(Object object) {
	        return super.getItemPosition(object);
	    }

	    @Override
	    public void destroyItem(View arg0, int arg1, Object arg2) {
	        ((ViewPager)arg0).removeView(mViews.get(arg1));
	    }

	    @Override
	    public Object instantiateItem(View arg0, int arg1) {
	        ((ViewPager)arg0).addView(mViews.get(arg1));
	        return mViews.get(arg1);
	    }
	}	
	
	public class FaceAdapter extends BaseAdapter {

	    private List<ChatEmoji> data;

	    private LayoutInflater inflater;

	    private int size=0;

	    public FaceAdapter(Context context, List<ChatEmoji> list) {
	        this.inflater=LayoutInflater.from(context);
	        this.data=list;
	        this.size=list.size();
	    }

	    @Override
	    public int getCount() {
	        return this.size;
	    }

	    @Override
	    public Object getItem(int position) {
	        return data.get(position);
	    }

	    @Override
	    public long getItemId(int position) {
	        return position;
	    }

	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	        ChatEmoji emoji=data.get(position);
	        ViewHolder viewHolder=null;
	        if(convertView == null) {
	            viewHolder=new ViewHolder();
	            convertView=inflater.inflate(R.layout.ramos_grid_view_item_face, null);
	            viewHolder.iv_face=(ImageView)convertView.findViewById(R.id.item_iv_face);
	            convertView.setTag(viewHolder);
	        } else {
	            viewHolder=(ViewHolder)convertView.getTag();
	        }
	        if(emoji.getId() == R.drawable.ramos_face_del_icon) {
	            convertView.setBackgroundDrawable(null);
	            viewHolder.iv_face.setImageResource(emoji.getId());
	        } else if(TextUtils.isEmpty(emoji.getCharacter())) {
	            convertView.setBackgroundDrawable(null);
	            viewHolder.iv_face.setImageDrawable(null);
	        } else {
	            viewHolder.iv_face.setTag(emoji);
	            viewHolder.iv_face.setImageResource(emoji.getId());
	        }

	        return convertView;
	    }

	    class ViewHolder {
	        public ImageView iv_face;
	    }
	}
	
}
