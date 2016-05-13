package com.android.mms.ramos;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Fragment;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.TimeFormatException;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.mms.R;

public class RamosViewVideoFragment extends Fragment implements OnCompletionListener,OnErrorListener,OnInfoListener,
OnPreparedListener, OnSeekCompleteListener,OnVideoSizeChangedListener,SurfaceHolder.Callback{
	private SurfaceView mSurfaceView;
	private ImageButton mButton;
	private ProgressBar mSeekbar;
	private TextView mStartTime;
	private TextView mEndTime;
	private MediaPlayer mPlayer;
	private Timer mTimer;
	private TimerTask mTimerTask;
	private Uri mUri;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Bundle args = getArguments();
		String data = args.getString("data");
		mUri = Uri.parse(data);
		super.onCreate(savedInstanceState);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.ramos_view_video, null);
	}
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub		
		mSurfaceView = (SurfaceView)view.findViewById(R.id.video_surface);
		mSurfaceView.setOnClickListener(mOnClickListener);
		mButton = (ImageButton)view.findViewById(R.id.play_pause);
		mSeekbar = (SeekBar) view.findViewById(R.id.progress);
		mStartTime = (TextView)view.findViewById(R.id.start_time);
		mEndTime = (TextView)view.findViewById(R.id.end_time);
		
		setupPlayer();	
		initSeekbar();
		
		mButton.setOnClickListener(mOnClickListener);
		super.onViewCreated(view, savedInstanceState);
	}

	void setupPlayer(){
		
		SurfaceHolder holder = mSurfaceView.getHolder();
		holder.addCallback(this);
		
		mPlayer = new MediaPlayer();
		mPlayer.setOnCompletionListener(this);
		mPlayer.setOnErrorListener(this);
		mPlayer.setOnInfoListener(this);
		mPlayer.setOnPreparedListener(this);
		mPlayer.setOnSeekCompleteListener(this);
		mPlayer.setOnVideoSizeChangedListener(this);

		try {
			mPlayer.setDataSource(getActivity(), mUri);
			mPlayer.prepare();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void initSeekbar(){
		  int mills = mPlayer.getDuration();
		  mSeekbar.setMax(mills);  
		  
		  String total = String.format("%2d:%2d", mills/60000, mills/1000);
		  mEndTime.setText(total);
		  
          mTimer = new Timer();    
          mTimerTask = new TimerTask() {    
              @Override    
              public void run() {
            	  if(null == mPlayer || !mPlayer.isPlaying())
            		  return;
            	  mHandler.sendEmptyMessage(0);
              }    
          };   
          mTimer.schedule(mTimerTask, 0, 500);
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
	}
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mPlayer.setDisplay(holder);
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}
	@Override
	public void onVideoSizeChanged(MediaPlayer arg0, int arg1, int arg2) {
	}
	@Override
	public void onSeekComplete(MediaPlayer arg0) {
		
	}
	@Override
	public void onPrepared(MediaPlayer player) {
	}
	@Override
	public boolean onInfo(MediaPlayer player, int whatInfo, int extra) {
		switch(whatInfo){
		case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
			break;
		case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
			
			break;
		case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
			
			break;
		case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
			
			break;
		}
		return false;
	}
	@Override
	public boolean onError(MediaPlayer player, int whatError, int extra) {
		switch (whatError) {
		case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
			break;
		case MediaPlayer.MEDIA_ERROR_UNKNOWN:
			break;
		default:
			break;
		}
		return false;
	}
	@Override
	public void onCompletion(MediaPlayer player) {
		updateUi();
	}
	
	private OnClickListener mOnClickListener=new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			if(null == mPlayer)
				return;
			boolean isPlaying = mPlayer.isPlaying();		
			try {
				if(!isPlaying){
					mPlayer.start();
				}else{
					mPlayer.pause();
				}
				updateUi();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	};
	void updateUi(){
		if(null == mPlayer)
			return;
		if(mPlayer.isPlaying()){
			mButton.setVisibility(View.GONE);
		}else{
			mButton.setVisibility(View.VISIBLE);
		}
	}
	Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
      	  int pos = mPlayer.getCurrentPosition();
          mSeekbar.setProgress(pos);
		  String start = String.format("%2d:%2d", pos/60000, pos/1000);
		  mStartTime.setText(start);
		}
	};
}
