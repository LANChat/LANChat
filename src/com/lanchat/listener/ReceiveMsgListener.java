package com.lanchat.listener;

import com.lanchat.data.*;

/**
 * 接收消息监听的listener接口
 *
 */
public interface ReceiveMsgListener {
	public boolean receive(ChatMessage msg);

}
