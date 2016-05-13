package com.android.mms.ramos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.android.mms.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;


public class RamosFaceUtil {

	private static final int PAGE_SIZE = 27;
	public static final int PAGE_COLUMNS = 7;
	private HashMap<String, String> mEmojiMap = new HashMap<String, String>();
	private List<ChatEmoji> mEmojiList = new ArrayList<ChatEmoji>();
	public List<List<ChatEmoji>> mEmojiPages = new ArrayList<List<ChatEmoji>>();
	private RamosFaceUtil() {}
	private static RamosFaceUtil instance;
	public static RamosFaceUtil getInstace() {
		if (instance == null) {
			instance = new RamosFaceUtil();
		}
		return instance;
	}

	public SpannableString getExpressionString(Context context, String str) {
		SpannableString spannableString = new SpannableString(str);
		String zhengze = "\\[[^\\]]+\\]";
		Pattern sinaPatten = Pattern.compile(zhengze, Pattern.CASE_INSENSITIVE);
		try {
			dealExpression(context, spannableString, sinaPatten, 0);
		} catch (Exception e) {
			Log.e("dealExpression", e.getMessage());
		}
		return spannableString;
	}

	public SpannableString addFace(Context context, int imgId,
			String spannableString) {
		if (TextUtils.isEmpty(spannableString)) {
			return null;
		}
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),imgId);
		int px = (int)context.getResources().getDimension(R.dimen.emoji_size);
		bitmap = Bitmap.createScaledBitmap(bitmap, px, px, true);
		ImageSpan imageSpan = new ImageSpan(context, bitmap);
		SpannableString spannable = new SpannableString(spannableString);
		spannable.setSpan(imageSpan, 0, spannableString.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return spannable;
	}
	public static int dip2px(Context context, float dipValue){ 
        final float scale = context.getResources().getDisplayMetrics().density; 
        return (int)(dipValue * scale + 0.5f); 
	} 
	private void dealExpression(Context context,
			SpannableString spannableString, Pattern patten, int start)
			throws Exception {
		Matcher matcher = patten.matcher(spannableString);
		while (matcher.find()) {
			String key = matcher.group();
			if (matcher.start() < start) {
				continue;
			}
			String value = mEmojiMap.get(key);
			if (TextUtils.isEmpty(value)) {
				continue;
			}
			int resId = context.getResources().getIdentifier(value, "drawable",
					context.getPackageName());
			if (resId != 0) {
				Bitmap bitmap = BitmapFactory.decodeResource(
						context.getResources(), resId);
				int px = (int)context.getResources().getDimension(R.dimen.emoji_size);
				bitmap = Bitmap.createScaledBitmap(bitmap, px, px, true);
				ImageSpan imageSpan = new ImageSpan(bitmap);
				int end = matcher.start() + key.length();
				spannableString.setSpan(imageSpan, matcher.start(), end,
						Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
				if (end < spannableString.length()) {
					dealExpression(context, spannableString, patten, end);
				}
				break;
			}
		}
	}

	public void getFileText(Context context) {
		if(mEmojiPages.size() > 0)
			return;
		List<String> data = getEmojiFile(context);
		ParseData(data, context);
	}

	private void ParseData(List<String> data, Context context) {
		if (data == null) {
			return;
		}
		ChatEmoji emojEentry;
		try {
			for (String str : data) {
				String[] text = str.split(",");
				String fileName = text[0]
						.substring(0, text[0].lastIndexOf("."));
				mEmojiMap.put(text[1], fileName);
				int resID = context.getResources().getIdentifier(fileName,
						"drawable", context.getPackageName());

				if (resID != 0) {
					emojEentry = new ChatEmoji();
					emojEentry.setId(resID);
					emojEentry.setCharacter(text[1]);
					emojEentry.setFaceName(fileName);
					mEmojiList.add(emojEentry);
				}
			}
			int pageCount = (int) Math.ceil(mEmojiList.size() / PAGE_SIZE + 0.1);

			for (int i = 0; i < pageCount; i++) {
				mEmojiPages.add(getData(i));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<ChatEmoji> getData(int page) {
		int startIndex = page * PAGE_SIZE;
		int endIndex = startIndex + PAGE_SIZE;

		if (endIndex > mEmojiList.size()) {
			endIndex = mEmojiList.size();
		}
		List<ChatEmoji> list = new ArrayList<ChatEmoji>();
		list.addAll(mEmojiList.subList(startIndex, endIndex));
		addDeleteButton(list);
		if (list.size() < PAGE_SIZE) {
			for (int i = list.size(); i < PAGE_SIZE; i++) {
				ChatEmoji object = new ChatEmoji();
				list.add(object);
			}
		}
		return list;
	}
	
	private void addDeleteButton(List<ChatEmoji> list) {
		if(null == list || list.size() <= 0)
			return;		
		ChatEmoji object = new ChatEmoji();
		object.setId(R.drawable.ramos_face_del_icon);
		
		if(list.size() < PAGE_COLUMNS){
			list.add(object);
		}else{
			list.add(PAGE_COLUMNS-1, object);
		}
	}

	public static List<String> getEmojiFile(Context context) {
		try {
			List<String> list = new ArrayList<String>();
			InputStream in = context.getResources().getAssets().open("emoji");
			BufferedReader br = new BufferedReader(new InputStreamReader(in,
					"UTF-8"));
			String str = null;
			while ((str = br.readLine()) != null) {
				list.add(str);
			}

			return list;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public class ChatEmoji {
	    private int id;
	    private String character;
	    private String faceName;
	    public int getId() {
	        return id;
	    }
	    public void setId(int id) {
	        this.id=id;
	    }
	    public String getCharacter() {
	        return character;
	    }
	    public void setCharacter(String character) {
	        this.character=character;
	    }
	    public String getFaceName() {
	        return faceName;
	    }
	    public void setFaceName(String faceName) {
	        this.faceName=faceName;
	    }
	}
}