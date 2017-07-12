package cn.udesk.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cn.udesk.UdeskUtil;

/**
 * Created by user on 2017/4/24.
 */

public class TicketReplieMode {


    /**
     * status : 0
     * message : 成功
     * size : 10
     * total : 21
     * total_pages : 3
     * contents : [{"reply_id":168,"user_avatar":"","reply_content":"{\"type\":\"message\",\"data\":{\"content\":\"123\"}}","reply_content_type":"api","reply_type":"external","reply_user":"Alex","reply_user_type":"agent","reply_created_at":"2017-04-17 17:52:19 +0800","reply_updated_at":"2017-04-17 17:52:19 +0800"},{"reply_id":167,"user_avatar":"","reply_content":"{\"type\":\"message\",\"data\":{\"content\":\"123\"}}","reply_content_type":"api","reply_type":"external","reply_user":"Alex","reply_user_type":"agent","reply_created_at":"2017-04-17 17:45:33 +0800","reply_updated_at":"2017-04-17 17:45:33 +0800"},{"reply_id":166,"user_avatar":"","reply_content":"{\"type\":\"message\",\"data\":{\"content\":\"123\"}}","reply_content_type":"api","reply_type":"external","reply_user":"Alex","reply_user_type":"agent","reply_created_at":"2017-04-17 17:38:38 +0800","reply_updated_at":"2017-04-17 17:38:38 +0800"},{"reply_id":165,"user_avatar":"","reply_content":"{\"type\":\"message\",\"data\":{\"content\":\"123\"}}","reply_content_type":"api","reply_type":"external","reply_user":"Alex","reply_user_type":"agent","reply_created_at":"2017-04-17 17:37:42 +0800","reply_updated_at":"2017-04-17 17:37:42 +0800"},{"reply_id":163,"user_avatar":"","reply_content":"{\"type\":\"message\",\"data\":{\"content\":\"123\"}}","reply_content_type":"api","reply_type":"external","reply_user":"Alex","reply_user_type":"agent","reply_created_at":"2017-04-17 17:18:24 +0800","reply_updated_at":"2017-04-17 17:18:24 +0800"},{"reply_id":162,"user_avatar":"","reply_content":"{\"type\":\"message\",\"data\":{\"content\":\"123\"}}","reply_content_type":"api","reply_type":"external","reply_user":"Alex","reply_user_type":"agent","reply_created_at":"2017-04-17 17:05:16 +0800","reply_updated_at":"2017-04-17 17:05:16 +0800"},{"reply_id":164,"user_avatar":"","reply_content":"{\"type\":\"message\",\"data\":{\"content\":\"123\"}}","reply_content_type":"api","reply_type":"external","reply_user":"Alex","reply_user_type":"agent","reply_created_at":"2017-04-17 01:31:57 +0800","reply_updated_at":"2017-04-17 01:31:57 +0800"},{"reply_id":160,"user_avatar":"","reply_content":"{\"type\":\"message\",\"data\":{\"content\":\"fasdfasdsadf\"}}","reply_content_type":"api","reply_type":"external","reply_user":"Alex","reply_user_type":"agent","reply_created_at":"2017-04-14 16:30:07 +0800","reply_updated_at":"2017-04-14 16:30:07 +0800"},{"reply_id":159,"user_avatar":"","reply_content":"{\"type\":\"message\",\"data\":{\"content\":\"fasdfasdsadf\"}}","reply_content_type":"api","reply_type":"external","reply_user":"Alex","reply_user_type":"agent","reply_created_at":"2017-04-14 16:29:57 +0800","reply_updated_at":"2017-04-14 16:29:57 +0800"},{"reply_id":158,"user_avatar":"","reply_content":"{\"type\":\"message\",\"data\":{\"content\":\"fasdfasdsadf\"}}","reply_content_type":"api","reply_type":"external","reply_user":"Alex","reply_user_type":"agent","reply_created_at":"2017-04-14 16:20:12 +0800","reply_updated_at":"2017-04-14 16:20:12 +0800"}]
     */

    private Object status;
    private Object message;
    private Object size;
    private Object total;
    private Object total_pages;
    private List<ContentsBean> contents;

    public int getStatus() {
        return UdeskUtil.objectToInt(status);
    }

    public void setStatus(Object status) {
        this.status = status;
    }

    public String getMessage() {
        return UdeskUtil.objectToString(message);
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public int getSize() {
        return UdeskUtil.objectToInt(size);
    }

    public void setSize(Object size) {
        this.size = size;
    }

    public int getTotal() {
        return UdeskUtil.objectToInt(total);
    }

    public void setTotal(Object total) {
        this.total = total;
    }

    public int getTotal_pages() {
        return UdeskUtil.objectToInt(total_pages);
    }

    public void setTotal_pages(Object total_pages) {
        this.total_pages = total_pages;
    }

    public List<ContentsBean> getContents() {
        return contents;
    }

    public void setContents(List<ContentsBean> contents) {
        this.contents = contents;
    }

    public static class ContentsBean {
        /**
         * reply_id : 168
         * user_avatar :
         * reply_content : {"type":"message","data":{"content":"123"}}
         * reply_content_type : api
         * reply_type : external
         * reply_user : Alex
         * reply_user_type : agent
         * reply_created_at : 2017-04-17 17:52:19 +0800
         * reply_updated_at : 2017-04-17 17:52:19 +0800
         */

        private Object reply_id;
        private Object user_avatar;
        private Object reply_content;
        private Object reply_content_type;
        private Object reply_type;
        private Object reply_user;
        private Object reply_user_type;
        private Object reply_created_at;
        private Object reply_updated_at;

        public int getReply_id() {
            return UdeskUtil.objectToInt(reply_id);
        }

        public void setReply_id(Object reply_id) {
            this.reply_id = reply_id;
        }

        public String getUser_avatar() {
            return UdeskUtil.objectToString(user_avatar);
        }

        public void setUser_avatar(Object user_avatar) {
            this.user_avatar = user_avatar;
        }

        public String getReply_content() {
            try {
                JSONObject object = new JSONObject(UdeskUtil.objectToString(reply_content));
                if (object.has("data")) {
                    JSONObject dataObj = object.getJSONObject("data");
                    if (dataObj.has("content")) {
                        return dataObj.getString("content");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return UdeskUtil.objectToString(reply_content);
        }

        public void setReply_content(Object reply_content) {
            this.reply_content = reply_content;
        }

        public String getReply_content_type() {
            return UdeskUtil.objectToString(reply_content_type);
        }

        public void setReply_content_type(Object reply_content_type) {
            this.reply_content_type = reply_content_type;
        }

        public String getReply_type() {
            return UdeskUtil.objectToString(reply_type);
        }

        public void setReply_type(Object reply_type) {
            this.reply_type = reply_type;
        }

        public String getReply_user() {
            return UdeskUtil.objectToString(reply_user);
        }

        public void setReply_user(Object reply_user) {
            this.reply_user = reply_user;
        }

        public String getReply_user_type() {
            return UdeskUtil.objectToString(reply_user_type);
        }

        public void setReply_user_type(Object reply_user_type) {
            this.reply_user_type = reply_user_type;
        }

        public String getReply_created_at() {
            return UdeskUtil.objectToString(reply_created_at);
        }

        public void setReply_created_at(Object reply_created_at) {
            this.reply_created_at = reply_created_at;
        }

        public String getReply_updated_at() {
            return UdeskUtil.objectToString(reply_updated_at);
        }

        public void setReply_updated_at(Object reply_updated_at) {
            this.reply_updated_at = reply_updated_at;
        }
    }
}
