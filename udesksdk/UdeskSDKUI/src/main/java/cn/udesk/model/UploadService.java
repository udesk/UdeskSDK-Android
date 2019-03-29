package cn.udesk.model;

import udesk.core.utils.UdeskUtils;

public class UploadService {

    private Object key;
    private Object marking;
    private Object referer;
    private UploadToken upload_token;

    public Object getKey() {
        return key;
    }

    public void setKey(Object key) {
        this.key = key;
    }

    public Object getMarking() {
        return marking;
    }

    public void setMarking(Object marking) {
        this.marking = marking;
    }

    public String getReferer() {
        return UdeskUtils.objectToString(referer);
    }

    public void setReferer(Object referer) {
        this.referer = referer;
    }

    public UploadToken getUpload_token() {
        return upload_token;
    }

    public void setUpload_token(UploadToken upload_token) {
        this.upload_token = upload_token;
    }
}
