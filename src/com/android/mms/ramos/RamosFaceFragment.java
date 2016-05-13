package com.android.mms.ramos;


import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import com.android.inputmethod.accessibility.AccessibilityUtils;
import com.android.inputmethod.event.Event;
import com.android.inputmethod.event.InputTransaction;
import com.android.inputmethod.keyboard.Keyboard;
import com.android.inputmethod.keyboard.KeyboardId;
import com.android.inputmethod.keyboard.KeyboardLayoutSet;
import com.android.inputmethod.keyboard.KeyboardSwitcher;
import com.android.inputmethod.keyboard.KeyboardTheme;
import com.android.inputmethod.keyboard.MainKeyboardView;
import com.android.inputmethod.keyboard.KeyboardActionListener.Adapter;
import com.android.inputmethod.keyboard.emoji.EmojiPalettesView;
import com.android.inputmethod.keyboard.internal.KeyVisualAttributes;
import com.android.inputmethod.keyboard.internal.KeyboardTextsSet;
import com.android.inputmethod.latin.AudioAndHapticFeedbackManager;
import com.android.inputmethod.latin.Constants;
import com.android.inputmethod.latin.InputPointers;
import com.android.inputmethod.latin.InputView;
import com.android.inputmethod.latin.RichInputMethodManager;
import com.android.inputmethod.latin.SubtypeSwitcher;
import com.android.inputmethod.latin.SuggestedWords;
import com.android.inputmethod.latin.settings.Settings;
import com.android.inputmethod.latin.utils.ResourceUtils;
import com.android.inputmethod.latin.utils.StringUtils;
import com.android.inputmethod.keyboard.KeyboardActionListener;
import com.android.inputmethod.latin.Constants;
import com.android.mms.R;
import com.android.mms.ui.ComposeMessageActivity;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class RamosFaceFragment extends Fragment{

	private EmojiPalettesView mEmojiPalettesView;
	private Context mThemeContext;
	private LayoutInflater mInflater;
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		  mThemeContext = new ContextThemeWrapper(activity, R.style.KeyboardTheme_LXX_Light);		  
		  mInflater = LayoutInflater.from(mThemeContext);
		  
		  Settings.init(activity);
          PreferenceManager.getDefaultSharedPreferences(activity);
//        RichInputMethodManager.init(activity);
//        RichInputMethodManager.getInstance();
//        SubtypeSwitcher.init(activity);
//        //KeyboardSwitcher.init(activity);
		  AudioAndHapticFeedbackManager.init(activity);
          AccessibilityUtils.init(activity);
//        //StatsUtils.init(activity);
        
		super.onAttach(activity);
	}	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

//		View v = inflater.inflate(R.layout.ramos_fragment_face, null);
		View v = mInflater.inflate(R.layout.emoji_palettes_view,null);			
		mEmojiPalettesView = (EmojiPalettesView)v;
		mEmojiPalettesView.setKeyboardActionListener(new Adapter());
		setEmojiKeyboard();		
		return v;
	}
	
    public void setEmojiKeyboard() {        
        mEmojiPalettesView.startEmojiPalettes(getMinKeyboardView());        
    }
//    private KeyboardIconsSet getIconSet(){
//        final KeyboardLayoutSet.Builder builder = new KeyboardLayoutSet.Builder(
//        		mThemeContext, null /* editorInfo */);        
//        final Resources res = mThemeContext.getResources();
//        mEmojiLayoutParams = new EmojiLayoutParams(res);        
//        builder.setSubtype(SubtypeSwitcher.getInstance().getEmojiSubtype());
//        builder.setKeyboardGeometry(ResourceUtils.getDefaultKeyboardWidth(res),
//                mEmojiLayoutParams.mEmojiKeyboardHeight);        
//        final KeyboardLayoutSet layoutSet = builder.build();    	
//        final Keyboard keyboard = layoutSet.getKeyboard(KeyboardId.ELEMENT_ALPHABET); 
//        return keyboard.mIconsSet;
//    }
    private KeyVisualAttributes getMinKeyboardView(){        
    	InputView mCurrentInputView = (InputView)mInflater.inflate(R.layout.input_view, null);
        MainKeyboardView mKeyboardView = (MainKeyboardView) mCurrentInputView.findViewById(R.id.keyboard_view);
        return mKeyboardView.getKeyVisualAttribute();
    }    
    
    public class Adapter implements KeyboardActionListener {
        @Override
        public void onPressKey(int primaryCode, int repeatCount, boolean isSinglePointer) {}
        @Override
		public void onReleaseKey(int primaryCode, boolean withSliding) {

		}
        @Override
        public void onCodeInput(int primaryCode, int x, int y, boolean isKeyRepeat) {
			Activity a = getActivity();
			if (null != a && a instanceof ComposeMessageActivity) {
				EditText et = ((ComposeMessageActivity) a).getMessageEditor();
				if(primaryCode == Constants.CODE_DELETE){
					int selection = et.getSelectionStart();					
					if (selection > 0) {
						final String text = et.getText().toString();
						final int lastCodePoint = text.codePointBefore(selection);
						et.getText().delete(selection - Character.charCount(lastCodePoint), selection);
					}
				}else{
					String s = StringUtils.newSingleCodePointString(primaryCode);	
					int selection = et.getSelectionStart();					
					if (selection > 0) {
						et.getEditableText().insert(selection, s);
					}else{
						et.append(s);
					}
				}
			}
        }
        @Override
        public void onTextInput(String text) {
			Activity a = getActivity();
			if (null != a && a instanceof ComposeMessageActivity) {
				EditText et = ((ComposeMessageActivity) a).getMessageEditor();				
				int selection = et.getSelectionStart();					
				if (selection > 0) {
					et.getEditableText().insert(selection, text);
				}else{
					et.append(text);
				}	
			}
        }
        @Override
        public void onStartBatchInput() {}
        @Override
        public void onUpdateBatchInput(InputPointers batchPointers) {}
        @Override
        public void onEndBatchInput(InputPointers batchPointers) {}
        @Override
        public void onCancelBatchInput() {}
        @Override
        public void onCancelInput() {}
        @Override
        public void onFinishSlidingInput() {}
        @Override
        public boolean onCustomRequest(int requestCode) {
            return false;
        }
    } 
}
