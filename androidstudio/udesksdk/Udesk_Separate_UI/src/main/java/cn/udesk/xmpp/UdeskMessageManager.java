package cn.udesk.xmpp;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;

import cn.udesk.UdeskConst;
import cn.udesk.UdeskUtil;
import cn.udesk.db.UdeskDBManager;
import udesk.core.UdeskCallBack;
import udesk.core.UdeskCoreConst;
import udesk.core.event.InvokeEventContainer;
import udesk.core.event.ReflectInvokeMethod;
import udesk.core.model.MessageInfo;
import udesk.core.xmpp.UdeskXmppManager;


public class UdeskMessageManager {
	
	private volatile static UdeskMessageManager instance;
	private UdeskXmppManager mUdeskXmppManager;
	private ExecutorService messageExecutor;

	public ReflectInvokeMethod  eventui_OnMessageReceived = new ReflectInvokeMethod(new Class<?>[]{String.class});
	public ReflectInvokeMethod  eventui_OnNewMessage = new ReflectInvokeMethod(new Class<?>[]{MessageInfo.class});
	public ReflectInvokeMethod  eventui_OnNewPresence = new ReflectInvokeMethod(new Class<?>[]{String.class ,Integer.class});
	public ReflectInvokeMethod  eventui_OnReqsurveyMsg = new ReflectInvokeMethod(new Class<?>[]{Boolean.class });
	
	private UdeskMessageManager() {
		bindEvent();
		mUdeskXmppManager = new UdeskXmppManager();
		ensureMessageExecutor();
	}

	public static UdeskMessageManager getInstance() {
		if (instance == null) {
			synchronized (UdeskMessageManager.class) {
				if (instance == null) {
					instance = new UdeskMessageManager();
				}
			}
		}
		return instance;
	}
	
	private void ensureMessageExecutor() {
		if (messageExecutor == null) {
			messageExecutor = Concurrents
					.newSingleThreadExecutor("messageExecutor");
		}
	}
	
	public void loginXmppWithNoCancel(){
		mUdeskXmppManager.startLoginXmpp(new UdeskCallBack() {
			
			@Override
			public void onSuccess(String message) {
				
				if(UdeskCoreConst.isDebug){
					Log.i("UdeskMessageManager ", message);
				}
			}
			
			@Override
			public void onFail(String message) {
				if(UdeskCoreConst.isDebug && message != null){
					Log.i("UdeskMessageManager ", message);
				}
			}
		});
	}

	
	public void loginXmpp(){
		mUdeskXmppManager.cancel(new UdeskCallBack() {

			@Override
			public void onSuccess(String message) {

				mUdeskXmppManager.startLoginXmpp(new UdeskCallBack() {

					@Override
					public void onSuccess(String message) {

						if (UdeskCoreConst.isDebug) {
							Log.i("UdeskMessageManager ", message);
						}
					}

					@Override
					public void onFail(String message) {
						if (UdeskCoreConst.isDebug && message != null) {
							Log.i("UdeskMessageManager ", message);
						}
					}
				});
			}

			@Override
			public void onFail(String message) {

			}
		});
	
	}
	
	public void cancelXmppConnect(){
		mUdeskXmppManager.cancel(new UdeskCallBack() {
			
			@Override
			public void onSuccess(String message) {
				
			}
			
			@Override
			public void onFail(String message) {
				
			}
		});
	}
	
	public void sendMessage(String type, String text, String msgId, String to,long duration){
		mUdeskXmppManager.sendMessage(type, text, msgId, to, duration);
	}

	private void bindEvent(){
		InvokeEventContainer.getInstance().event_OnNewMessage.bind(this,"onNewMessage");
		InvokeEventContainer.getInstance().event_OnMessageReceived.bind(this,"onMessageReceived");
		InvokeEventContainer.getInstance().event_OnNewPresence.bind(this,"onNewPresence");
		InvokeEventContainer.getInstance().event_OnReqsurveyMsg.bind(this, "onReqsurveyMsg");
	}


	public  void clean(){
		InvokeEventContainer.getInstance().event_OnNewMessage.unBind(this);
		InvokeEventContainer.getInstance().event_OnMessageReceived.unBind(this);
		InvokeEventContainer.getInstance().event_OnNewPresence.unBind(this);
		InvokeEventContainer.getInstance().event_OnReqsurveyMsg.unBind(this);
	}

	public void onMessageReceived(final String msgId) {
		
		ensureMessageExecutor();
		messageExecutor.submit(new Runnable() {
			@Override
			public void run() {
				UdeskDBManager.getInstance().updateMsgSendFlag(msgId,UdeskConst.SendFlag.RESULT_SUCCESS);
				UdeskDBManager.getInstance().deleteSendingMsg(msgId);
				eventui_OnMessageReceived.invoke(msgId);
			}
		});
	}


	public void onNewMessage(final String type, final String msgId, final String content,
			final Long duration) {
		final MessageInfo msginfo = buildReceiveMessage(type, msgId, content, duration);
		if(UdeskDBManager.getInstance().hasReceviedMsg(msgId)){
			return;
		}
		ensureMessageExecutor();
		if(!type.equals(UdeskConst.ChatMsgTypeString.TYPE_AUDIO)){
			if(!type.equals(UdeskConst.ChatMsgTypeString.TYPE_REDIRECT)){
				messageExecutor.submit(new Runnable() {

					@Override
					public void run() {

						UdeskDBManager.getInstance().addMessageInfo(msginfo);
					}
				});
			}
			eventui_OnNewMessage.invoke(msginfo);
		}else{
			messageExecutor.submit(new DownAudioTask(content,
					msginfo));
		}
		
	}
	
	
	
	public MessageInfo buildReceiveMessage(String msgType, String msgId,
			String content, long duration) {
		MessageInfo msg = new MessageInfo();
		msg.setMsgtype(msgType);
		msg.setTime(System.currentTimeMillis());
		msg.setMsgId(msgId);
		msg.setDirection(UdeskConst.ChatMsgDirection.Recv);
		msg.setSendFlag(UdeskConst.SendFlag.RESULT_SUCCESS);
		msg.setReadFlag(UdeskConst.ChatMsgReadFlag.read);
		msg.setMsgContent(content);
		msg.setPlayflag(UdeskConst.PlayFlag.NOPLAY);
		msg.setLocalPath("");
		msg.setDuration(duration);
		return msg;
	}
	
	
	private class DownAudioTask implements Runnable {
		private String urlStr; // 下载链接
		private File fvoice;
		private MessageInfo info;

		public DownAudioTask(String urlStr, MessageInfo info) {
			this.urlStr = urlStr;
			this.info = info;
		}

		public void run() {
			OutputStream output = null;
			InputStream input = null;
			try {
				fvoice = UdeskUtil.getAudioFile(urlStr);
				if (fvoice == null) {
					return;
				}
				URL audioUrl = new URL(urlStr);
				HttpURLConnection conn = (HttpURLConnection) audioUrl
						.openConnection();
				conn.setDoInput(true);
				conn.connect();
				input = conn.getInputStream();
				output = new FileOutputStream(fvoice);
				// 读取大文件
				byte[] voice_bytes = new byte[1024];
				int len = -1;
				while ((len = input.read(voice_bytes)) != -1) {
					output.write(voice_bytes, 0, len);
					output.flush();
				}
				output.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (output != null) {
						output.close();
						output = null;
					}
					if (input != null) {
						input.close();
						input = null;
					}
					if (fvoice != null) {
						info.setLocalPath(fvoice.getPath());
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				UdeskDBManager.getInstance().addMessageInfo(info);
				eventui_OnNewMessage.invoke(info);
			}
		}
	}


	public void onNewPresence(String jid, Integer onlineflag) {
		eventui_OnNewPresence.invoke(jid, onlineflag);
		
	}

	public void onReqsurveyMsg(Boolean isSurvey) {
		eventui_OnReqsurveyMsg.invoke(isSurvey);
	}


	

}
