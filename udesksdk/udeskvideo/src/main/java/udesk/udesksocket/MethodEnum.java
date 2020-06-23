package udesk.udesksocket;

/**
 * author : ${揭军平}
 * time   : 2017/11/23
 * desc   :
 * version: 1.0
 */

public enum MethodEnum {



    LOGIN("login"),
    LOGOUT("logout"),
    SETSTATE("setState"),
    GETSTATE("getState"),
    GETUSERSTATE("getUserState"),
    SETATTR("setAttr"),
    GETATTR("getAttr"),
    GETATTRALL("getAttrAll"),
    INVITE("invite"),
    CANCEL("cancel"),
    GETCHANNELTOKEN("get_channel_token"),
    ANSWER("answer"),
    BYE("bye"),
    PRE_ANSWER("pre_answer"),
    Defualt("defualt");

    String name;

    MethodEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static MethodEnum getByValue(String operator) {

        for (MethodEnum operatorEnum : MethodEnum.values()) {
            if (operatorEnum.name.equals(operator)) {
                return operatorEnum;
            }
        }
        return Defualt;
    }
}
