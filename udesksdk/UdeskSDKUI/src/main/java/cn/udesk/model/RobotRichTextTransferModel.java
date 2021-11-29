package cn.udesk.model;

public class RobotRichTextTransferModel {
    private boolean isTransfer;
    private String content;

    public boolean isTransfer() {
        return isTransfer;
    }

    public void setTransfer(boolean transfer) {
        isTransfer = transfer;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
