package com.android.mms.ramos;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;

import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ramos.RamosBottomPanel.OnHandleMessage;
import com.android.mms.R;

public class RamosQuickReplyFragment extends ListFragment implements OnClickListener, OnItemClickListener {
	private static String DATA_FILE_PATH = "/data/data/com.android.mms/quick_reply/quick_reply.dat";
	
	private ArrayList<String> mListData; 
	private ArrayAdapter<String> mAdapter;
	private OnHandleMessage mHandler;
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mHandler = (OnHandleMessage)activity;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		File f = new File(DATA_FILE_PATH);
		if(!f.exists()){
			List<String> defaultData = loadFromResource();
			WriteToFile(new ArrayList<String>(defaultData));
		}
		super.onCreate(savedInstanceState);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.ramos_fragment_quick_reply, null);
		v.findViewById(R.id.edit_quick_reply).setOnClickListener(this);		
		return v;
	}	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		getListView().setOnItemClickListener(this);
		super.onViewCreated(view, savedInstanceState);
	}
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		mListData = readFromFile();
		mAdapter = new ArrayAdapter<String>(getActivity(), R.layout.ramos_quick_reply_simple_list_item_1, mListData);
		setListAdapter(mAdapter);
		//[ramos] added by liting 20151019 for BUG0009029 
		if(mListData.size() == 0) {
			getListView().setVisibility(View.GONE);
		} else {
			getListView().setVisibility(View.VISIBLE);
		}
		//[ramos] end liting BUG0009029
		
		super.onResume();
	}
	private List<String> loadFromResource(){
		String[] s = getResources().getStringArray(R.array.default_quick_reply);
		return Arrays.asList(s);		
	}
	public static ArrayList<String> readFromFile(){
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		ArrayList<String> listData = null;
		try {			
			File file = new File(DATA_FILE_PATH);
			if (!file.exists()) {
				throw new FileNotFoundException();
			}		
			fis = new FileInputStream(file);
			ois = new ObjectInputStream(fis);
			listData = (ArrayList<String>) ois.readObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (ois != null) {
					ois.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(null == listData)
			listData = new ArrayList<String>();
		return listData;
	}	
	public static void WriteToFile(ArrayList<String> listData){
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try{
			File file = new File(DATA_FILE_PATH);
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}		
			fos = new FileOutputStream(file);
			oos= new ObjectOutputStream(fos);
			oos.writeObject(listData);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (oos != null) {
					oos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	@Override
	public void onClick(View arg0) {
		if(arg0.getId() == R.id.edit_quick_reply){
			Intent i = new Intent(getActivity(), RamosEditQuickActivity.class);
			startActivity(i);
		}
	}
	@Override
	public void onItemClick(AdapterView<?> paramAdapterView, View paramView,
			int position, long paramLong) {
		String str = mListData.get(position);
//		ComposeMessageActivity cma = (ComposeMessageActivity)getActivity();
//		cma.appendContent(str);
		Message msg = Message.obtain();
		msg.what = 3;//ComposeMessageActivity.java : MSG_REPLY
		msg.obj = str;
		mHandler.handleMessage(msg);
	}
}
