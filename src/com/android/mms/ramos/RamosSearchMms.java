package com.android.mms.ramos;

import java.util.ArrayList;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.android.mms.R;
import com.android.mms.data.Conversation;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.MessageUtils;
import com.android.mms.data.Contact;
import android.provider.Telephony;
//[ramos] added by liting 20151103 for BUG0009745 & BUG0009743
import java.io.UnsupportedEncodingException;
//[ramos] end liting

public class RamosSearchMms extends Activity {

	private static final String TAG = "RamosSearchMms";
	ListView listview;
	EditText key_edit;
	Context mContext;
	boolean isCollect = false;
	private Cursor mCursor;
	public void hideKeyboard(EditText edit) {
		if (edit != null) {
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
		}
		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		//[ramos] deleted by liting 20151017 for BUG0008463
		//hideKeyboard(key_edit);
		//[ramos] end liting for BUG0008463
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
    
	@Override
	protected void onDestroy() {
		super.onDestroy();
        if (null != mCursor && !mCursor.isClosed()) {
            mCursor.close();
        }
        Log.d(TAG,"onDestroy");
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.ramos_search);
		isCollect = getIntent().getBooleanExtra("collect", false);
		mContext = this;
		key_edit = (EditText) findViewById(R.id.key_edit);
		findViewById(R.id.btn_cancel).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
			
		});
  
		listview = (ListView) findViewById(R.id.listview);
		listview.setOnTouchListener(listener);
		
		key_edit.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				//Filter f = mAdapter.getFilter();
				//f.filter(arg0.toString());
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
				updateList();
			}
			
		});
		
		
	}
	
	OnTouchListener listener = new OnTouchListener() {

		@Override
		public boolean onTouch(View arg0, MotionEvent arg1) {
			// TODO Auto-generated method stub
			finish();
			
			return false;
		}
		
	};
	
	private void updateList(){
		
		String key = key_edit.getText().toString();
		/*		  final Cursor mCursor = getContentResolver().query(  
						Uri.parse("content://sms"), new String[] { "_id", "address", "body", "thread_id", "time" },  
						"body like ?", new String[] { "%" + key + "%" }, null); 
		
				SimpleCursorAdapter sim = new SimpleCursorAdapter(RamosSearchMms.this,	
						R.layout.ramos_simple_list_item_2, mCursor, new String[] {	
								"address", "body", "time" }, new int[] {  
								R.id.text1, R.id.text_sub, R.id.date}); 
		*/
		if(key != null && !key.equals("")) {
			//[ramos] modified by liting 20151029 for BUG0008359
			//mCursor = getContentResolver().query(  
			//		Uri.parse("content://sms"), new String[] { "_id", "address", "body", "thread_id", "date"},	
			//		"(body like ?) OR (address like ?)", new String[] { "%" + key + "%" , "%" + key + "%"}, null);  
			mCursor = getContentResolver().query(  
					Telephony.MmsSms.SEARCH_URI.buildUpon().appendQueryParameter("pattern", key).build(), 
					null, null, null, null);  
			//[ramos] end liting
		} else {
			mCursor = null;
		}

        SimpleCursorAdapter sim = new SimpleCursorAdapter(RamosSearchMms.this,  
        		R.layout.ramos_simple_list_item_2, mCursor, new String[] {  
                        "address", "body", "date"}, new int[] {  
                        R.id.text1, R.id.text_sub, R.id.date});  
        sim.setViewBinder(viewBinder);
        
        listview.setAdapter(sim);
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				int count = listview.getCount();

		        long tid = mCursor.getLong(mCursor.getColumnIndex("thread_id"));

				startActivity(ComposeMessageActivity.createIntent(mContext, tid));
		        
			}
			
		});
		View empty_tip = findViewById(R.id.empty_tip);
		
		if(key == null || key.equals("")) {
			listview.setBackgroundColor(0x99ffffff);
			listview.setOnTouchListener(listener);
			empty_tip.setVisibility(View.GONE);
		} else {
			if (sim.getCount() > 0) {
				listview.setBackgroundColor(0xfff3f3f3);
				listview.setOnTouchListener(null);
				empty_tip.setVisibility(View.GONE);
			} else {
				listview.setBackgroundColor(0xfff3f3f3);
				listview.setOnTouchListener(listener);
				empty_tip.setVisibility(View.VISIBLE);
			}

		}

	}
	
	private SimpleCursorAdapter.ViewBinder viewBinder=new SimpleCursorAdapter.ViewBinder() {    
		   
		 @Override  
		 public boolean setViewValue(View view, Cursor cursor, int columnIndex) {  
		  // TODO Auto-generated method stub  
			 String key = key_edit.getText().toString();
			 if(cursor.getColumnIndex("address")==columnIndex){   
				   
				 	TextView tv=(TextView)view;  
				 	String mAddress = cursor.getString(cursor.getColumnIndexOrThrow("address"));
					String mContact = Contact.get(mAddress, false).getNameAndNumber();
				 	if (mContact != null) {
					 	SpannableStringBuilder buf = new SpannableStringBuilder(mContact);
					 	//int before = mAddress.indexOf(key);
						int before = mContact.toUpperCase().indexOf(key.toUpperCase());
					 	if( before >= 0 ){
					        buf.setSpan(new ForegroundColorSpan(
					                mContext.getResources().getColor(R.drawable.text_color_red)),
					                before, before + key.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
				        }
					 	tv.setText(buf); 
						
				 	}
				 	return true; 
			 }
			 
			 if(cursor.getColumnIndex("body")==columnIndex){   
				   
				 	TextView tv=(TextView)view;  
				 	String mBody = cursor.getString(cursor.getColumnIndexOrThrow("body"));
					//[ramos] added by liting 20151103 for BUG0009745 & BUG0009743
					if (mBody != null) {
					 	SpannableStringBuilder buf = new SpannableStringBuilder(mBody);
					 	//int before = mBody.indexOf(key);
						int before = mBody.toUpperCase().indexOf(key.toUpperCase());
						if (cursor.getInt(6) == 0) {
							if (mBody == null || mBody.equals("")) {
								mBody = mContext.getString(R.string.no_subject_view);
							} else {
								try {
									mBody = new String(mBody.getBytes("ISO-8859-1"), "UTF-8");
								} catch (UnsupportedEncodingException e) {
									 e.printStackTrace();
								}
							}
							tv.setText(mBody); 
						} else if( before >= 0 ){
					        buf.setSpan(new ForegroundColorSpan(
					                mContext.getResources().getColor(R.drawable.text_color_red)),
					                before, before + key.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
							if (before > 10) {
								buf.replace(0, before - 10, "...");
							}
							tv.setText(buf); 
				        } else {
							tv.setText(mBody); 
						}
					}
				   //[ramos] end liting
				   return true; 
			 }		
			 if(cursor.getColumnIndex("date")==columnIndex){   
				 	TextView mDateView=(TextView)view;  
				 	Long mDate = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
					//[ramos] added by liting 20151103 for BUG0009745 & BUG0009743
					if (cursor.getInt(6) == 0) {
						mDateView.setText(MessageUtils.formatTimeStampStringExtend(mContext, mDate * 1000L));
					} else {
						mDateView.setText(MessageUtils.formatTimeStampStringExtend(mContext, mDate));
					}
					//[ramos] end liting
				 	return true; 
			 }		
			 

			 return false;  
		 }  
	};  
}
