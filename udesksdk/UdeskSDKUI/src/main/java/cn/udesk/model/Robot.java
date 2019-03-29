package cn.udesk.model;

import udesk.core.utils.UdeskUtils;

public class Robot {

    private Object enable; //机器人是否开启
    private Object url;//机器人接口请求 前面路径
    private Object robot_name;//机器人名字
    private Object show_robot_times; //客户发送多少条后显示转人工
    private Object enable_agent;//是否显示转人工按钮


    public boolean getEnable() {
        return UdeskUtils.objectToBoolean(enable);
    }

    public void setEnable(Object enable) {
        this.enable = enable;
    }

    public Object getUrl() {
        return url;
    }

    public void setUrl(Object url) {
        this.url = url;
    }

    public String getRobot_name() {
        return UdeskUtils.objectToString(robot_name);
    }

    public void setRobot_name(Object robot_name) {
        this.robot_name = robot_name;
    }

    public int getShow_robot_times() {
        return UdeskUtils.objectToInt(show_robot_times);
    }

    public void setShow_robot_times(Object show_robot_times) {
        this.show_robot_times = show_robot_times;
    }

    public boolean getEnable_agent() {
        return UdeskUtils.objectToBoolean(enable_agent);
    }

    public void setEnable_agent(Object enable_agent) {
        this.enable_agent = enable_agent;
    }
}
