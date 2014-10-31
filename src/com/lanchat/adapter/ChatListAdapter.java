package com.lanchat.adapter;

import java.util.ArrayList;
import java.util.List;

import com.lanchat.activity.R;
import com.lanchat.data.ChatMessage;








import com.lanchat.data.ExpressionUtil;

import android.content.Context;
import android.content.res.Resources;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 聊天activity中的ListView的adapter，实现发送者名称对应TextView字体的颜色变化
 * 
 * 2014/10/19
 *
 */
/*
 *整个是去实现对话框的内容 
 **/
public class ChatListAdapter extends BaseAdapter {
	protected LayoutInflater mInflater;
	protected final ArrayList<ChatMessage> msgList;

	private Context context;
	public ChatListAdapter(Context c, ArrayList<ChatMessage> list){
		super();
		context = c;
		mInflater = LayoutInflater.from(c);
		msgList =list;

	}
	
	
	public void addMessage(ChatMessage msg){
		msgList.add(msg);
		notifyDataSetChanged();
	}
	
	public void addMessages(List<ChatMessage > msgList){
		msgList.addAll(msgList);
		notifyDataSetChanged();
	}
	
	public void clear(){
		msgList.clear();
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return msgList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return msgList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub	
		Log.i("TAG", "llllllllllllll");
		
		ChatMessage msg = msgList.get(position);
		
		final ViewHolder holder;
		if(convertView == null){
			convertView= mInflater.inflate(R.layout.team_layout_main_singlechat_item, null);
			
			holder=new ViewHolder(convertView);
			convertView.setTag(holder);
		}else{
			holder=(ViewHolder) convertView.getTag();
		}
	
		Log.i("TAG", "jjjjjjjjjjjjjjjjjjjjj");
	
			holder.setData(msg);
	
    	return convertView;
	}




private class ViewHolder{
	RelativeLayout lchat_layout;

	TextView text;
	public ViewHolder(View convertView){
		lchat_layout=(RelativeLayout) convertView.findViewById(R.id.team_singlechat_id_listiteam);
	//	image=(ImageView) convertView.findViewById(R.id.team_singlechat_id_listiteam_headicon);
		text=(TextView) convertView.findViewById(R.id.team_singlechat_id_listiteam_message);
	}
	public void setData(ChatMessage msg) {
	
		RelativeLayout.LayoutParams rl_chat_left=((RelativeLayout.LayoutParams)lchat_layout.getLayoutParams());
		RelativeLayout.LayoutParams rl_tv_msg_left=((RelativeLayout.LayoutParams)text.getLayoutParams());
		if(msg.isSelfMsg()){	//根据本地的ip地址来判断该条信息是属于本人所说还是对方所说
																			//如果是自己说的，则显示在右边；如果是对方所说，则显示在左边，在这里也可以利用IP地址来判断
			rl_chat_left.addRule(RelativeLayout.ALIGN_PARENT_LEFT,-1);
			rl_chat_left.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,0);
	
			rl_tv_msg_left.addRule(RelativeLayout.LEFT_OF,0);
			text.setBackgroundResource(R.drawable.balloon_l_selector);
		}else{
			rl_chat_left.addRule(RelativeLayout.ALIGN_PARENT_LEFT,0);
			rl_chat_left.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,-1);
		
			rl_tv_msg_left.addRule(RelativeLayout.RIGHT_OF,0);
			text.setBackgroundResource(R.drawable.balloon_r_selector);
		}
		/**********去掉头像**************/
	//	image.setImageResource(R.drawable.portrait_0);		//设置头像
		String str = msg.getMsg();														//消息具体内容 即获取相应位置的信息
		String zhengze = "f0[0-9]{2}|f10[0-7]";		//正则表达式，用来判断消息内是否有表情
	
		try {

	
			SpannableString spannableString = ExpressionUtil.getExpressionString(context, str, zhengze);//可以用来显示图片或者其它功能
			text.setText(spannableString);
	
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}
}
}

