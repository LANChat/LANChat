package com.lanchat.data;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lanchat.activity.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;


public class ExpressionUtil {
	/**
	 * 对spanableString进行正则判断，如果符合要求，则以表情图片代替
	 * @param context
	 * @param spannableString
	 * @param patten
	 * @param start
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws NumberFormatException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
    public static void dealExpression(Context context,SpannableString spannableString, Pattern patten, int start) throws SecurityException, NoSuchFieldException, NumberFormatException, IllegalArgumentException, IllegalAccessException {
    	Matcher matcher = patten.matcher(spannableString);
        while (matcher.find()) {
 
            String key = matcher.group();
            if (matcher.start() < start) {
                continue;
            }
            
            
   
            Field field = R.drawable.class.getDeclaredField(key);
 
			int resId = Integer.parseInt(field.get(null).toString());	
	
            if (resId != 0) {
            	
                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);	
   
                ImageSpan imageSpan = new ImageSpan(bitmap);
           
                int end = matcher.start() + key.length();//通过图片资源id来得到bitmap，用丿تImageSpan来包裿
                spannableString.setSpan(imageSpan, matcher.start(), end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);	//ݫكͼƬͦۻؖػԮאڦ֨քλ׃א
                if (end < spannableString.length()) {						//如果整个字符串还未验证完，则继续。䀧n保承܉的表情都能够被添加到
                    dealExpression(context,spannableString,  patten, end);
                }
    
                break;
            }
        }
    }
    
    /**
     * 得到丿تSpanableString对象，ꀨ߇传入的字符丿并进行正则判斿     * @param context
     * @param str
     * @return
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     * @throws NoSuchFieldException 
     * @throws SecurityException 
     * @throws NumberFormatException 
     */
    public static SpannableString getExpressionString(Context context,String str,String zhengze){
    	SpannableString spannableString = new SpannableString(str);//相应的类

 
        Pattern sinaPatten = Pattern.compile(zhengze, Pattern.CASE_INSENSITIVE);		//通过传入的正则表达式来生成一个pattern
    
            try {
				dealExpression(context,spannableString, sinaPatten, 0);
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

        return spannableString;
    }
	

}