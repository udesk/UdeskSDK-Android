package cn.udesk.aac;

public class QuestionMergeMode extends MergeMode {
    String question;
    long questionId;
    int queryType;
    String msgId;
    String logId;

    public QuestionMergeMode(int type, String id) {
        super(type, id);
    }

    public QuestionMergeMode(int type, Object data, String id) {
        super(type, data, id);
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(long questionId) {
        this.questionId = questionId;
    }

    public int getQueryType() {
        return queryType;
    }

    public void setQueryType(int queryType) {
        this.queryType = queryType;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }
}
