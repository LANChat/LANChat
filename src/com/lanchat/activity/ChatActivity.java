package com.lanchat.activity;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.lanchat.adapter.ChatListAdapter;
import com.lanchat.data.ChatMessage;
import com.lanchat.listener.ReceiveMsgListener;
import com.lanchat.network.NetTcpFileSendThread;
import com.lanchat.util.IpMessageConst;
import com.lanchat.util.IpMessageProtocol;
import com.lanchat.util.UsedConst;





import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ChatActivity extends BasicActivity implements OnClickListener,ReceiveMsgListener{
	private TextView chat_name;			//名字及IP
	private TextView chat_mood;			//组名
	
	private Button chat_quit;			//退出按钮

	
	private ListView chat_list;			//聊天列表
	
	
	private EditText chat_input;		//聊天输入框
	private Button chat_send;			//发送按钮
	/* 表情选择按钮
	 *jc
	 */
	private ImageView chat_face;
	private ChatListAdapter adapter;	//ListView对应的adapter
	private int[] imageIds = new int[107];
	private Dialog builder; //控制表情框的句柄
	
	
	private ArrayList<ChatMessage> msgList;	//消息list
	
	
	
	
	private String receiverName;			//要接收本activity所发送的消息的用户名字
	private String receiverIp;			//要接收本activity所发送的消息的用户IP
	private String receiverGroup;			//要接收本activity所发送的消息的用户组名

	private String selfName;
	private String selfGroup;
	
	private final static int MENU_ITEM_SENDFILE = Menu.FIRST;	//发送文件
	private final static int MENU_ITEM_EXIT = Menu.FIRST + 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat);
		findViews();
		msgList = new ArrayList<ChatMessage>();
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		receiverName = bundle.getString("receiverName");
		receiverIp = bundle.getString("receiverIp");
		receiverGroup = bundle.getString("receiverGroup");
		selfName = "android飞鸽";
		selfGroup = "android";
		
		chat_name.setText(receiverName + "(" + receiverIp + ")");
		chat_mood.setText("组名：" + receiverGroup);
		chat_quit.setOnClickListener(this);
		chat_send.setOnClickListener(this);
		chat_face.setOnClickListener(this);
		Iterator<ChatMessage> it = netThreadHelper.getReceiveMsgQueue().iterator();
		while(it.hasNext()){	//循环消息队列，获取队列中与本聊天activity相关信息
			ChatMessage temp = it.next();
			//若消息队列中的发送者与本activity的消息接收者IP相同，则将这个消息拿出，添加到本activity要显示的消息list中
			if(receiverIp.equals(temp.getSenderIp())){ 
				msgList.add(temp);	//添加到显示list
				it.remove();		//将本消息从消息队列中移除
			}
		}
		
		adapter = new ChatListAdapter(this,msgList);
		chat_list.setAdapter(adapter);
		
		netThreadHelper.addReceiveMsgListener(this);	//注册到listeners
	}
	
	private void findViews(){
		chat_name = (TextView) findViewById(R.id.chat_name);
		chat_mood = (TextView) findViewById(R.id.chat_mood);
		chat_quit = (Button) findViewById(R.id.chat_quit);
		chat_list = (ListView) findViewById(R.id.chat_list);
		chat_input = (EditText) findViewById(R.id.chat_input);
		chat_send = (Button) findViewById(R.id.chat_send);
		chat_face=(ImageView)findViewById(R.id.chat_face);//表情扩张键
	}

	@Override
	public void processMessage(Message msg) {
		// TODO Auto-generated method stub
		switch(msg.what){
		case IpMessageConst.IPMSG_SENDMSG:
			adapter.notifyDataSetChanged();	//刷新ListView
			break;
			
		case IpMessageConst.IPMSG_RELEASEFILES:{ //拒绝接受文件,停止发送文件线程
			if(NetTcpFileSendThread.server != null){
				try {
					NetTcpFileSendThread.server.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
			break;
			
		case UsedConst.FILESENDSUCCESS:{	//文件发送成功
			makeTextShort("文件发送成功");
		}
			break;	
		}	//end of switch
	}

	@Override
	public boolean receive(ChatMessage msg) {
		// TODO Auto-generated method stub
		if(receiverIp.equals(msg.getSenderIp())){	//若消息与本activity有关，则接收
			msgList.add(msg);	//将此消息添加到显示list中
			sendEmptyMessage(IpMessageConst.IPMSG_SENDMSG); //使用handle通知，来更新UI
			BasicActivity.playMsg();
			return true;
		}	
		return false;
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		//一定要移除，不然信息接收会出现问题
		netThreadHelper.removeReceiveMsgListener(this);
		super.finish();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v == chat_send){
			sendAndAddMessage();	
		}else if(v == chat_quit){
			finish();
		}
		else if(v == chat_face)
		{
			/*出现表情圈选择
			 jc 
			 */
	
			createExpressionDialog();
			
			
		}
		
	}
	
	/**
	 * 发送消息并将该消息添加到UI显示
	 */
	private void sendAndAddMessage(){
		String msgStr = chat_input.getText().toString();
		if(!"".equals(msgStr)){
			//发送消息
			IpMessageProtocol sendMsg = new IpMessageProtocol();
			sendMsg.setVersion(String.valueOf(IpMessageConst.VERSION));
			sendMsg.setSenderName(selfName);
			sendMsg.setSenderHost(selfGroup);
			sendMsg.setCommandNo(IpMessageConst.IPMSG_SENDMSG);
			sendMsg.setAdditionalSection(msgStr.trim());
			InetAddress sendto = null;
			try {
				sendto = InetAddress.getByName(receiverIp);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				Log.e("MyFeiGeChatActivity", "发送地址有误");
			}
			if(sendto != null)
				netThreadHelper.sendUdpData(sendMsg.getProtocolString() + "\0", sendto, IpMessageConst.PORT);
			
			//添加消息到显示list
			ChatMessage selfMsg = new ChatMessage("localhost", selfName, msgStr, new Date());
			selfMsg.setSelfMsg(true);	//设置为自身消息
			
			adapter.addMessage(selfMsg);
			
			
		//	adapter.notifyDataSetChanged();//更新UI	
			chat_input.setText("");
		
		}else{
			makeTextShort("不能发送空内容");
		}
		
	
	//	adapter.notifyDataSetChanged();//更新UI
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_ITEM_SENDFILE, 0, "发送文件");
		menu.add(0, MENU_ITEM_EXIT, 0, "退出");
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case MENU_ITEM_SENDFILE:
			Intent intent = new Intent(this, FileActivity.class);
			startActivityForResult(intent, 0);
			
			break;
		case MENU_ITEM_EXIT:
			finish();
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			//得到发送文件的路径
			Bundle bundle = data.getExtras();
			
			String filePaths = bundle.getString("filePaths");	//附加文件信息串,多个文件使用"\0"进行分隔
			String[] filePathArray = filePaths.split("\0");
			
			//发送传送文件UDP数据报
			IpMessageProtocol sendPro = new IpMessageProtocol();
			sendPro.setVersion("" +IpMessageConst.VERSION);
			sendPro.setCommandNo(IpMessageConst.IPMSG_SENDMSG | IpMessageConst.IPMSG_FILEATTACHOPT);
			sendPro.setSenderName(selfName);
			sendPro.setSenderHost(selfGroup);
			String msgStr = "";	//发送的消息
			
			StringBuffer additionInfoSb = new StringBuffer();	//用于组合附加文件格式的sb
			for(String path:filePathArray){
				File file = new File(path);
				additionInfoSb.append("0:");
				additionInfoSb.append(file.getName() + ":");
				additionInfoSb.append(Long.toHexString(file.length()) + ":");		//文件大小十六进制表示
				additionInfoSb.append(Long.toHexString(file.lastModified()) + ":");	//文件创建时间，现在暂时已最后修改时间替代
				additionInfoSb.append(IpMessageConst.IPMSG_FILE_REGULAR + ":");
				byte[] bt = {0x07};		//用于分隔多个发送文件的字符
				String splitStr = new String(bt);
				additionInfoSb.append(splitStr);
			}
			
			sendPro.setAdditionalSection(msgStr + "\0" + additionInfoSb.toString() + "\0");
			
			InetAddress sendto = null;
			try {
				sendto = InetAddress.getByName(receiverIp);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				Log.e("MyFeiGeChatActivity", "发送地址有误");
			}
			if(sendto != null)
				netThreadHelper.sendUdpData(sendPro.getProtocolString(), sendto, IpMessageConst.PORT);
			
			//监听2425端口，准备接受TCP连接请求
			Thread netTcpFileSendThread = new Thread(new NetTcpFileSendThread(filePathArray));
			netTcpFileSendThread.start();	//启动线程
		}
	}
	/**
	 * 创建一个表情选择对话框
	 */
	private void createExpressionDialog() {
		builder = new Dialog(ChatActivity.this);
		GridView gridView = createGridView();
		Log.i("TAG", "333333333333333");
		builder.setContentView(gridView);
		builder.setTitle("默认表情");
		builder.show();
		Log.i("TAG", "221111111111111111");
		gridView.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Bitmap bitmap = null;
				bitmap = BitmapFactory.decodeResource(getResources(), imageIds[arg2 % imageIds.length]);
				ImageSpan imageSpan = new ImageSpan(ChatActivity.this, bitmap);
				String str = null;
				if(arg2<10){
					str = "f00"+arg2;
				}else if(arg2<100){
					str = "f0"+arg2;
				}else{
					str = "f"+arg2;
				}
				SpannableString spannableString = new SpannableString(str);
				spannableString.setSpan(imageSpan, 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				chat_input.append(spannableString);
				builder.dismiss();
			}
		});
	}
	
	
	/**
	 * 生成一个表情对话框中的gridview
	 * @return
	 */
	private GridView createGridView() {
		final GridView view = new GridView(this);
		List<Map<String,Object>> listItems = new ArrayList<Map<String,Object>>();
		//生成107个表情的id，封装
		for(int i = 0; i < 107; i++){
			try {
				if(i<10){
					Field field = R.drawable.class.getDeclaredField("f00" + i);
					int resourceId = Integer.parseInt(field.get(null).toString());
					imageIds[i] = resourceId;
				}else if(i<100){
					Field field = R.drawable.class.getDeclaredField("f0" + i);
					int resourceId = Integer.parseInt(field.get(null).toString());
					imageIds[i] = resourceId;
				}else{
					Field field = R.drawable.class.getDeclaredField("f" + i);
					int resourceId = Integer.parseInt(field.get(null).toString());
					imageIds[i] = resourceId;
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
	        Map<String,Object> listItem = new HashMap<String,Object>();
			listItem.put("image", imageIds[i]);
			listItems.add(listItem);
		}
		
		SimpleAdapter simpleAdapter =new SimpleAdapter(this, listItems, R.layout.team_layout_single_expression_cell, new String[]{"image"}, new int[]{R.id.image});
		view.setAdapter(simpleAdapter);
	
	
		view.setAdapter(simpleAdapter);
		view.setNumColumns(6);
		view.setBackgroundColor(Color.rgb(214, 211, 214));
		view.setHorizontalSpacing(1);
		view.setVerticalSpacing(1);
		view.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		view.setGravity(Gravity.CENTER);
		return view;
	}

}
