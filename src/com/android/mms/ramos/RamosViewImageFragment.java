package com.android.mms.ramos;

import com.android.mms.R;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class RamosViewImageFragment extends Fragment implements OnClickListener {
	Uri mUri;
	String mMimeType;
	private ImageView mImage;
	private TextView mButton; 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Bundle args = getArguments();
		mUri = Uri.parse(args.getString("data"));
		mMimeType = args.getString("type");
		super.onCreate(savedInstanceState);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {		
		return inflater.inflate(R.layout.ramos_view_image, null);
	}
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mImage = (ImageView) view.findViewById(R.id.image);
		mButton = (TextView) view.findViewById(R.id.set_image_as);
		
		mImage.setImageURI(mUri);
		
		mButton.setOnClickListener(this);
		super.onViewCreated(view, savedInstanceState);
	}
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub		
		Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
		intent.setDataAndType(mUri, mMimeType);
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra("mimeType", intent.getType());        
        getActivity().startActivity(Intent.createChooser(intent, getResources().getString(R.string.set_image_as)));
	}
}
