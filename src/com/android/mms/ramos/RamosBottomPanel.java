package com.android.mms.ramos;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.android.mms.R;
import android.util.Log;

public class RamosBottomPanel extends LinearLayout implements OnCheckedChangeListener{
	private static final String TAG = "RamosBottomPanel";
	private Activity mActivity;
	private FragmentManager mFragmentManager;
	//[ramos] modified by liting 20151019 for highlight the current page
	public static final int INDEX_FACE = 0;
	public static final int INDEX_ATTACHMENT = 1;
	public static final int INDEX_QUICK_REPLY = 2;	
	private static int CURRENT_INDEX = INDEX_FACE;
	private ArrayList<Fragment> mFragments;
	private RadioButton mFace;
	private RadioButton mAttach;
	private RadioButton mReply;
	//[ramos] end liting 

	public static abstract interface OnHandleMessage{
		public void handleMessage(Message msg);
	}
	public RamosBottomPanel(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mActivity=(Activity)context;
	}

	public RamosBottomPanel(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
		mActivity=(Activity)context;
	}

	public RamosBottomPanel(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
		mActivity=(Activity)context;
	}

	public RamosBottomPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mActivity=(Activity)context;
	}     
	     
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		Log.d(TAG,"onFinishInflate");
		mFragments = new ArrayList<Fragment>();
		mFragmentManager = mActivity.getFragmentManager();
		FragmentTransaction ft = mFragmentManager.beginTransaction();
		
		Fragment fg0 = new RamosFaceFragment();
		ft.add(R.id.content, fg0, "face");
		mFragments.add(fg0);
		
		Fragment fg1 = new RamosAttachmentFragment();
		ft.add(R.id.content, fg1, "attachment");
		mFragments.add(fg1);
		
		Fragment fg2 = new RamosQuickReplyFragment();
		ft.add(R.id.content, fg2, "quickreply");
		mFragments.add(fg2);
		
		ft.commit();		
		
		RadioGroup rg = (RadioGroup)findViewById(R.id.tabs);		
		rg.setOnCheckedChangeListener(this);
		//[ramos] modified by liting 20151019 for highlight the current page
		mFace = (RadioButton)findViewById(R.id.tab_face);
		mAttach = (RadioButton)findViewById(R.id.tab_attachment);
		mReply = (RadioButton)findViewById(R.id.tab_quick_reply);
		mFace.setChecked(true);
		//[ramos] end liting 
	}
	
	private void updateFragment(){
		FragmentTransaction ft = mFragmentManager.beginTransaction();
		for(int i=0; i < mFragments.size(); i++){
			if(i==CURRENT_INDEX){
				ft.attach(mFragments.get(i));
			}else{
				ft.detach(mFragments.get(i));
			}
		}
		ft.commit();
	}

	@Override
	public void onCheckedChanged(RadioGroup arg0, int checkedId) {
		switch (checkedId) {  
        case R.id.tab_face:  
        	CURRENT_INDEX = INDEX_FACE;
            break;  
        case R.id.tab_attachment:  
        	CURRENT_INDEX = INDEX_ATTACHMENT;
            break;  
        case R.id.tab_quick_reply:
        	CURRENT_INDEX = INDEX_QUICK_REPLY;
            break;              
		}
		updateFragment();
	}		
}

