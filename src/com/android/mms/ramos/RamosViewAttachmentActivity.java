package com.android.mms.ramos;

import java.io.IOException;

import com.android.mms.R;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.LinearLayout;

public class RamosViewAttachmentActivity extends Activity {
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		setContentView(R.layout.ramos_view_attachment);
		Uri uri = getIntent().getData();		
		String type = getIntent().getType();
		if(null == uri)
			finish();
		
		Log.v("shuyong", "uri="+uri.toString()+",mimeType="+type);
		
		Fragment fragment;
		if(type.contains("video")){
			fragment = new RamosViewVideoFragment();
		}else if(type.contains("image")){
			fragment = new RamosViewImageFragment();
		}else{
			fragment = new RamosViewAudioFragment();
		}
		
		Bundle args = new Bundle();
		args.putString("data", uri.toString());
		args.putString("type", type);
		fragment.setArguments(args);
		
		FragmentTransaction fts = getFragmentManager().beginTransaction();
		fts.add(R.id.content, fragment);
		fts.commit();		
		super.onCreate(arg0);
	}	
}

