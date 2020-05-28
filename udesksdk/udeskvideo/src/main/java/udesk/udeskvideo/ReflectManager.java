package udesk.udeskvideo;

import android.content.Context;

public class ReflectManager {
    private void OnConnectWebsocket(Context context){
        UdeskVideoCallManager.getInstance().OnConnectWebsocket(context);
    }
}
