package com.android.mms.ramos;

import com.android.mms.R;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RamosViewAudioFragment extends Fragment {
	private Uri mUri;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Bundle args = getArguments();
		String data = args.getString("data");
		mUri = Uri.parse(data);
		super.onCreate(savedInstanceState);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.ramos_view_audio, null);
	}
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);
	}
}
