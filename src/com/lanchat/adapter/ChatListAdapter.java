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
 * ����activity�е�ListView��adapter��ʵ�ַ��������ƶ�ӦTextView�������ɫ�仯
 * 
 * 2014/10/19
 *
 */
/*
 *������ȥʵ�ֶԻ�������� 
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
		if(msg.isSelfMsg()){	//���ݱ��ص�ip��ַ���жϸ�����Ϣ�����ڱ�����˵���ǶԷ���˵
																			//������Լ�˵�ģ�����ʾ���ұߣ�����ǶԷ���˵������ʾ����ߣ�������Ҳ��������IP��ַ���ж�
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
		/**********ȥ��ͷ��**************/
	//	image.setImageResource(R.drawable.portrait_0);		//����ͷ��
		String str = msg.getMsg();														//��Ϣ�������� ����ȡ��Ӧλ�õ���Ϣ
		String zhengze = "f0[0-9]{2}|f10[0-7]";		//������ʽ�������ж���Ϣ���Ƿ��б���
	
		try {

	
			SpannableString spannableString = ExpressionUtil.getExpressionString(context, str, zhengze);//����������ʾͼƬ������������
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

