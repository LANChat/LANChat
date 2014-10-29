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
	private TextView chat_name;			//���ּ�IP
	private TextView chat_mood;			//����
	
	private Button chat_quit;			//�˳���ť

	
	private ListView chat_list;			//�����б�
	
	
	private EditText chat_input;		//���������
	private Button chat_send;			//���Ͱ�ť
	/* ����ѡ��ť
	 *jc
	 */
	private ImageView chat_face;
	private ChatListAdapter adapter;	//ListView��Ӧ��adapter
	private int[] imageIds = new int[107];
	private Dialog builder; //���Ʊ����ľ��
	
	
	private ArrayList<ChatMessage> msgList;	//��Ϣlist
	
	
	
	
	private String receiverName;			//Ҫ���ձ�activity�����͵���Ϣ���û�����
	private String receiverIp;			//Ҫ���ձ�activity�����͵���Ϣ���û�IP
	private String receiverGroup;			//Ҫ���ձ�activity�����͵���Ϣ���û�����

	private String selfName;
	private String selfGroup;
	
	private final static int MENU_ITEM_SENDFILE = Menu.FIRST;	//�����ļ�
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
		selfName = "android�ɸ�";
		selfGroup = "android";
		
		chat_name.setText(receiverName + "(" + receiverIp + ")");
		chat_mood.setText("������" + receiverGroup);
		chat_quit.setOnClickListener(this);
		chat_send.setOnClickListener(this);
		chat_face.setOnClickListener(this);
		Iterator<ChatMessage> it = netThreadHelper.getReceiveMsgQueue().iterator();
		while(it.hasNext()){	//ѭ����Ϣ���У���ȡ�������뱾����activity�����Ϣ
			ChatMessage temp = it.next();
			//����Ϣ�����еķ������뱾activity����Ϣ������IP��ͬ���������Ϣ�ó�����ӵ���activityҪ��ʾ����Ϣlist��
			if(receiverIp.equals(temp.getSenderIp())){ 
				msgList.add(temp);	//��ӵ���ʾlist
				it.remove();		//������Ϣ����Ϣ�������Ƴ�
			}
		}
		
		adapter = new ChatListAdapter(this,msgList);
		chat_list.setAdapter(adapter);
		
		netThreadHelper.addReceiveMsgListener(this);	//ע�ᵽlisteners
	}
	
	private void findViews(){
		chat_name = (TextView) findViewById(R.id.chat_name);
		chat_mood = (TextView) findViewById(R.id.chat_mood);
		chat_quit = (Button) findViewById(R.id.chat_quit);
		chat_list = (ListView) findViewById(R.id.chat_list);
		chat_input = (EditText) findViewById(R.id.chat_input);
		chat_send = (Button) findViewById(R.id.chat_send);
		chat_face=(ImageView)findViewById(R.id.chat_face);//�������ż�
	}

	@Override
	public void processMessage(Message msg) {
		// TODO Auto-generated method stub
		switch(msg.what){
		case IpMessageConst.IPMSG_SENDMSG:
			adapter.notifyDataSetChanged();	//ˢ��ListView
			break;
			
		case IpMessageConst.IPMSG_RELEASEFILES:{ //�ܾ������ļ�,ֹͣ�����ļ��߳�
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
			
		case UsedConst.FILESENDSUCCESS:{	//�ļ����ͳɹ�
			makeTextShort("�ļ����ͳɹ�");
		}
			break;	
		}	//end of switch
	}

	@Override
	public boolean receive(ChatMessage msg) {
		// TODO Auto-generated method stub
		if(receiverIp.equals(msg.getSenderIp())){	//����Ϣ�뱾activity�йأ������
			msgList.add(msg);	//������Ϣ��ӵ���ʾlist��
			sendEmptyMessage(IpMessageConst.IPMSG_SENDMSG); //ʹ��handle֪ͨ��������UI
			BasicActivity.playMsg();
			return true;
		}	
		return false;
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		//һ��Ҫ�Ƴ�����Ȼ��Ϣ���ջ��������
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
			/*���ֱ���Ȧѡ��
			 jc 
			 */
	
			createExpressionDialog();
			
			
		}
		
	}
	
	/**
	 * ������Ϣ��������Ϣ��ӵ�UI��ʾ
	 */
	private void sendAndAddMessage(){
		String msgStr = chat_input.getText().toString();
		if(!"".equals(msgStr)){
			//������Ϣ
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
				Log.e("MyFeiGeChatActivity", "���͵�ַ����");
			}
			if(sendto != null)
				netThreadHelper.sendUdpData(sendMsg.getProtocolString() + "\0", sendto, IpMessageConst.PORT);
			
			//�����Ϣ����ʾlist
			ChatMessage selfMsg = new ChatMessage("localhost", selfName, msgStr, new Date());
			selfMsg.setSelfMsg(true);	//����Ϊ������Ϣ
			
			adapter.addMessage(selfMsg);
			
			
		//	adapter.notifyDataSetChanged();//����UI	
			chat_input.setText("");
		
		}else{
			makeTextShort("���ܷ��Ϳ�����");
		}
		
	
	//	adapter.notifyDataSetChanged();//����UI
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_ITEM_SENDFILE, 0, "�����ļ�");
		menu.add(0, MENU_ITEM_EXIT, 0, "�˳�");
		
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
			//�õ������ļ���·��
			Bundle bundle = data.getExtras();
			
			String filePaths = bundle.getString("filePaths");	//�����ļ���Ϣ��,����ļ�ʹ��"\0"���зָ�
			String[] filePathArray = filePaths.split("\0");
			
			//���ʹ����ļ�UDP���ݱ�
			IpMessageProtocol sendPro = new IpMessageProtocol();
			sendPro.setVersion("" +IpMessageConst.VERSION);
			sendPro.setCommandNo(IpMessageConst.IPMSG_SENDMSG | IpMessageConst.IPMSG_FILEATTACHOPT);
			sendPro.setSenderName(selfName);
			sendPro.setSenderHost(selfGroup);
			String msgStr = "";	//���͵���Ϣ
			
			StringBuffer additionInfoSb = new StringBuffer();	//������ϸ����ļ���ʽ��sb
			for(String path:filePathArray){
				File file = new File(path);
				additionInfoSb.append("0:");
				additionInfoSb.append(file.getName() + ":");
				additionInfoSb.append(Long.toHexString(file.length()) + ":");		//�ļ���Сʮ�����Ʊ�ʾ
				additionInfoSb.append(Long.toHexString(file.lastModified()) + ":");	//�ļ�����ʱ�䣬������ʱ������޸�ʱ�����
				additionInfoSb.append(IpMessageConst.IPMSG_FILE_REGULAR + ":");
				byte[] bt = {0x07};		//���ڷָ���������ļ����ַ�
				String splitStr = new String(bt);
				additionInfoSb.append(splitStr);
			}
			
			sendPro.setAdditionalSection(msgStr + "\0" + additionInfoSb.toString() + "\0");
			
			InetAddress sendto = null;
			try {
				sendto = InetAddress.getByName(receiverIp);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				Log.e("MyFeiGeChatActivity", "���͵�ַ����");
			}
			if(sendto != null)
				netThreadHelper.sendUdpData(sendPro.getProtocolString(), sendto, IpMessageConst.PORT);
			
			//����2425�˿ڣ�׼������TCP��������
			Thread netTcpFileSendThread = new Thread(new NetTcpFileSendThread(filePathArray));
			netTcpFileSendThread.start();	//�����߳�
		}
	}
	/**
	 * ����һ������ѡ��Ի���
	 */
	private void createExpressionDialog() {
		builder = new Dialog(ChatActivity.this);
		GridView gridView = createGridView();
		Log.i("TAG", "333333333333333");
		builder.setContentView(gridView);
		builder.setTitle("Ĭ�ϱ���");
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
	 * ����һ������Ի����е�gridview
	 * @return
	 */
	private GridView createGridView() {
		final GridView view = new GridView(this);
		List<Map<String,Object>> listItems = new ArrayList<Map<String,Object>>();
		//����107�������id����װ
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
