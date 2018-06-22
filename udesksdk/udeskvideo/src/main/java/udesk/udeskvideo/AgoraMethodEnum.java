package udesk.udeskvideo;

/**
 * author : ${揭军平}
 * time   : 2017/11/23
 * desc   :
 * version: 1.0
 */

public enum AgoraMethodEnum {



    onFirstRemoteVideoDecoded("onFirstRemoteVideoDecoded"),
    onUserJoined("onUserJoined"),
    onUserOffline("onUserOffline"),
    onLeaveChannel("onLeaveChannel"),
    onJoinChannelSuccess("onJoinChannelSuccess"),
    onError("onError"),
    Defualt("defualt");

    String name;

    AgoraMethodEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static AgoraMethodEnum getByValue(String operator) {

        for (AgoraMethodEnum operatorEnum : AgoraMethodEnum.values()) {
            if (operatorEnum.name.equals(operator)) {
                return operatorEnum;
            }
        }
        return Defualt;
    }
}
