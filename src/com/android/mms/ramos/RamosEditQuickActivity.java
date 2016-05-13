package com.android.mms.ramos;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.LinearLayout;
import com.android.mms.ramos.RamosPopupMenuWindow;
import android.widget.Button;
import android.view.WindowManager.LayoutParams;
import android.view.Display;
import android.view.WindowManager;
import android.text.TextWatcher;
import android.text.Editable;

import com.android.mms.R;

public class RamosEditQuickActivity extends ListActivity implements OnClickListener, OnItemClickListener  ,RamosPopupMenuWindow.OnPopupItemClickListener{
	private ArrayList<String> mListData;
	private ArrayAdapter<String> mAdapter;
	private TextView mTVAdd;
	private int mEditPosition;
    private RamosPopupMenuWindow mPopupWindow;
    private static final int MENU_QUICK_REPLY_EDIT       = 1;
    private static final int MENU_QUICK_REPLY_DELETE        = 2;
    private AlertDialog mDeleteAlertDialog;
    private AlertDialog mEditDialog;


	
	@Override
	protected void onCreate(Bundle arg0) {
		Log.i("shuyong", "RamosEditQuickActivity::onCreate()>>>");
		
		setContentView(R.layout.ramos_edit_quick_reply);
		
		mTVAdd = (TextView)findViewById(R.id.edit_quick_reply);
//        Drawable drawableAdd = getDrawable(R.drawable.addattachment);
//        drawableAdd.setBounds(0, 0,drawableAdd.getMinimumWidth(),drawableAdd.getMinimumHeight());
//		mTVAdd.setCompoundDrawables(null, drawableAdd, null, null);
//		mTVAdd.setText(R.string.quick_reply);
		mTVAdd.setOnClickListener(this);
		
		mListData = RamosQuickReplyFragment.readFromFile();
		mAdapter = new ArrayAdapter<String>(this, R.layout.ramos_quick_reply_simple_list_item_1, mListData);
		
		setListAdapter(mAdapter);
		
		getListView().setOnItemClickListener(this);
		//[ramos] added by liting 20151019 for BUG0009029 
        View emptyView = findViewById(R.id.empty);
        getListView().setEmptyView(emptyView);
		//[ramos] end liting BUG0009029
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.ramos_actionbar);
		TextView actionbartitle=(TextView)findViewById(R.id.ramos_actionbar_title);
		actionbartitle.setText(R.string.quick_reply);
		TextView mMultChoiceEdit=(TextView)findViewById(R.id.ramos_edit);
		mMultChoiceEdit.setVisibility(View.GONE);

    	TextView returntextview=(TextView)findViewById(R.id.preference_return_textview);
		returntextview.setVisibility(View.VISIBLE);
		returntextview.setText(R.string.conversation_list_title);

		LinearLayout linear=(LinearLayout)findViewById(R.id.preference_actionbar_return);
		linear.setVisibility(View.VISIBLE);
		linear.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    finish();
                }
            });
		mPopupWindow = new RamosPopupMenuWindow(this, getListView());
		mPopupWindow.setOnPopupItemClickListener(this);
		mPopupWindow.setTitle(getResources().getString(R.string.menu_option));
		mPopupWindow.init();


		super.onCreate(arg0);
	}

	@Override
	public void onClick(View paramView) {
		// TODO Auto-generated method stub		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		mEditDialog = builder.create();
		mEditDialog.show();
		LayoutParams layoutParams;
		mEditDialog.setCanceledOnTouchOutside(true);
		mEditDialog.setContentView(R.layout.ramos_dialog_edit);
		layoutParams = mEditDialog.getWindow().getAttributes();
		WindowManager m = this.getWindowManager();
		Display d = m.getDefaultDisplay();	
		layoutParams.width = (int) (d.getWidth());
		layoutParams.height = getResources().getDimensionPixelSize(R.dimen.ramos_dialog_edit_height);
		mEditDialog.getWindow().setAttributes(layoutParams); 
		final EditText msg = (EditText)mEditDialog.findViewById(R.id.edit_message);
		msg.setFocusable(true); 
		//[ramos] added by liting 20151026 for BUG0009428
		TextView title = (TextView) mEditDialog.findViewById(R.id.title);
		title.setText(R.string.add);
		//[ramos] end liting BUG0009428
		Button btnLeft = (Button) mEditDialog.findViewById(R.id.btn_left);		
		final Button btnRight = (Button) mEditDialog.findViewById(R.id.btn_right); 
		btnRight.setTextColor(R.color.grey);
		btnRight.setAlpha(0.2f);
		btnLeft.setOnClickListener(new Button.OnClickListener() {
		
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					mEditDialog.dismiss();
				}
		
		});
		btnRight.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String str = msg.getEditableText().toString();
				if(!str.isEmpty()) {
					mListData.add(str);
					updateData();
					mEditDialog.dismiss();
				}
			}
		});
		//[ramos] added by liting 20151030 for BUG0009429
		msg.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				String str = msg.getEditableText().toString();
				if(str.isEmpty()) {
					btnRight.setTextColor(R.color.grey);
					btnRight.setAlpha(0.2f);
				} else {
					btnRight.setTextColor(0xff03a3ff);
					btnRight.setAlpha(1f);
				}
			}
			
		});
		//[ramos] end liting
		mEditDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM); 
		mEditDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
	}

	protected void updateData() {
		// TODO Auto-generated method stub
		mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mListData);
		setListAdapter(mAdapter);
		RamosQuickReplyFragment.WriteToFile(mListData);
	}

	@Override
	public void onItemClick(AdapterView<?> paramAdapterView, View paramView,
			int position, long id) {
		mEditPosition = position;

		mPopupWindow.clear();
		mPopupWindow.addItem(MENU_QUICK_REPLY_EDIT, getResources().getString(R.string.edit), true);
		mPopupWindow.addItem(MENU_QUICK_REPLY_DELETE, getResources().getString(R.string.delete), true);
		mPopupWindow.show();
	}

    @Override
    public boolean onPopupItemClick(int itemId) {
        switch (itemId) {
			case MENU_QUICK_REPLY_EDIT:
				edit();
                break;
			case MENU_QUICK_REPLY_DELETE:
				confirmDeleteThreadDialog(mEditPosition);
                break;
        }

        return true;
    }
	private void confirmDeleteThreadDialog(final int Position) {
		View contents = View.inflate(this, R.layout.ramos_dialog, null);

		TextView msg = (TextView)contents.findViewById(R.id.message);
		msg.setText(R.string.confirm_delete_quick_reply);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		mDeleteAlertDialog = builder.create();
		mDeleteAlertDialog.show();
		LayoutParams layoutParams;
		mDeleteAlertDialog.setCanceledOnTouchOutside(true);
		mDeleteAlertDialog.setContentView(contents);
		layoutParams = mDeleteAlertDialog.getWindow().getAttributes();
		layoutParams.width = getResources().getDimensionPixelSize(R.dimen.ramos_dialog_width);//(int) (d.getWidth());
		//layoutParams.height = getResources().getDimensionPixelSize(R.dimen.ramos_dialog_height);
		mDeleteAlertDialog.getWindow().setAttributes(layoutParams); 
		
		Button btnLeft = (Button) mDeleteAlertDialog.findViewById(R.id.btn_left);		
		Button btnRight = (Button) mDeleteAlertDialog.findViewById(R.id.btn_right); 
		btnLeft.setOnClickListener(new Button.OnClickListener() {
		
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					mDeleteAlertDialog.dismiss();
				}
		
		});
		btnRight.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mListData.remove(Position);
				updateData();
				mDeleteAlertDialog.dismiss();
			}
		});

	}

	protected void edit() {
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		mEditDialog = builder.create();
		mEditDialog.show();
		WindowManager m = this.getWindowManager();
		Display d = m.getDefaultDisplay();	
		LayoutParams layoutParams;
		mEditDialog.setCanceledOnTouchOutside(true);
		mEditDialog.setContentView(R.layout.ramos_dialog_edit);
		final EditText msg = (EditText)mEditDialog.findViewById(R.id.edit_message);
		msg.setText(mListData.get(mEditPosition));
		msg.setFocusable(true); 
		msg.setSelection(msg.getEditableText().toString().length());
		layoutParams = mEditDialog.getWindow().getAttributes();
		layoutParams.width = (int) (d.getWidth());
		layoutParams.height = getResources().getDimensionPixelSize(R.dimen.ramos_dialog_edit_height);
		mEditDialog.getWindow().setAttributes(layoutParams); 
		Button btnLeft = (Button) mEditDialog.findViewById(R.id.btn_left);		
		final Button btnRight = (Button) mEditDialog.findViewById(R.id.btn_right); 
		btnLeft.setOnClickListener(new Button.OnClickListener() {
		
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					mEditDialog.dismiss();
				}
		
		});
		btnRight.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String str = msg.getEditableText().toString();
				if(!str.isEmpty()) {
					mListData.set(mEditPosition, str);
					updateData();
					mEditDialog.dismiss();
				}
			}
		});
		//[ramos] added by liting 20151030 for BUG0009429
		msg.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				String str = msg.getEditableText().toString();
				if(str.isEmpty()) {
					btnRight.setTextColor(R.color.grey);
					btnRight.setAlpha(0.2f);
				} else {
					btnRight.setTextColor(0xff03a3ff);
					btnRight.setAlpha(1f);
				}
			}
			
		});
		//[ramos] end liting
		mEditDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM); 
		mEditDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

	}

}
