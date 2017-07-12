package cn.udesk.model;

/**
 * Created by user on 2016/12/27.
 */

public class SDKIMSetting {

    //    {"code"=>"1000",
//            "message"=>"success",
//            "result"=>
//        {"enable_im_group"=>false,
//                "im_session_closed"=>false,
//                "is_worktime"=>true,
//                "has_robot"=>true,
//                "enable_robot"=>false,
//                "enable_sdk_robot"=>false,
//                "enable_agent"=>false,
//                "investigation_when_leave"=>false,
//                "enable_web_im_feedback"=>false,
//                "no_reply_hint"=>"对不起，当前无客户在线"}}
    private int code;
    private Object message;

    //是否配置了引导页
    private Object enable_im_group;
    //会话是否关闭
    private Object in_session;
    //是否在工作时间
    private Object is_worktime;
    private Object has_robot;
    private Object enable_robot;
    private Object enable_sdk_robot;
    private Object enable_agent;
    private Object enable_web_im_feedback;
    private Object no_reply_hint;
    private Object robot;
    private Object investigation_when_leave;
    private Object enable_im_survey;
    //'msg', 'form'
    private String leave_message_type;


    public String getLeave_message_type() {
        return leave_message_type;
    }

    public void setLeave_message_type(String leave_message_type) {
        this.leave_message_type = leave_message_type;
    }

    public boolean isInvestigation_when_leave() {
        if (investigation_when_leave instanceof Boolean) {
            return (boolean) investigation_when_leave;
        }
        return false;
    }

    public void setInvestigation_when_leave(Object investigation_when_leave) {
        this.investigation_when_leave = investigation_when_leave;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public boolean getEnable_im_group() {
        if (enable_im_group instanceof Boolean) {
            return (boolean) enable_im_group;
        }
        return false;
    }

    public void setEnable_im_group(Object enable_im_group) {
        this.enable_im_group = enable_im_group;
    }

    public boolean getIn_session() {
        if (in_session instanceof Boolean) {
            return (boolean) in_session;
        }
        return false;

    }

    public void setIn_session(Object in_session) {
        this.in_session = in_session;
    }

    public boolean getIs_worktime() {
        if (is_worktime instanceof Boolean) {
            return (boolean) is_worktime;
        }
        return false;
    }

    public void setIs_worktime(Object is_worktime) {
        this.is_worktime = is_worktime;
    }

    public boolean getHas_robot() {
        if (has_robot instanceof Boolean) {
            return (boolean) has_robot;
        }
        return false;

    }

    public void setHas_robot(Object has_robot) {
        this.has_robot = has_robot;
    }

    public boolean getEnable_robot() {
        if (enable_robot instanceof Boolean) {
            return (boolean) enable_robot;
        }
        return false;
    }

    public void setEnable_robot(Object enable_robot) {
        this.enable_robot = enable_robot;
    }

    public boolean getEnable_sdk_robot() {
        if (enable_sdk_robot instanceof Boolean) {
            return (boolean) enable_sdk_robot;
        }
        return false;
    }

    public void setEnable_sdk_robot(Object enable_sdk_robot) {
        this.enable_sdk_robot = enable_sdk_robot;
    }

    public String getEnable_agent() {
        if (enable_agent instanceof Boolean) {
            boolean transfer = (boolean) enable_agent;
            if (transfer) {
                return "true";
            } else {
                return "false";
            }
        }
        return "false";
    }

    public void setEnable_agent(Object enable_agent) {
        this.enable_agent = enable_agent;
    }

    public boolean getEnable_web_im_feedback() {
        if (enable_web_im_feedback instanceof Boolean) {
            return (boolean) enable_web_im_feedback;
        }
        return false;
    }

    public void setEnable_web_im_feedback(Object enable_web_im_feedback) {
        this.enable_web_im_feedback = enable_web_im_feedback;
    }

    public boolean getEnable_im_survey() {
        if (enable_im_survey instanceof Boolean) {
            return (boolean) enable_im_survey;
        }
        return false;
    }

    public void setEnable_im_survey(Object enable_im_survey) {
        this.enable_im_survey = enable_im_survey;
    }

    public String getNo_reply_hint() {
        if (no_reply_hint instanceof String) {
            return (String) no_reply_hint;
        }
        return "";
    }

    public void setNo_reply_hint(Object no_reply_hint) {
        this.no_reply_hint = no_reply_hint;
    }

    public String getRobot() {
        if (robot instanceof String) {
            return (String) robot;
        }
        return "";
    }

    public void setRobot(Object robot) {
        this.robot = robot;
    }
}
