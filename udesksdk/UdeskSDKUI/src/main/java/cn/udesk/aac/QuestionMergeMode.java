package cn.udesk.aac;

import cn.udesk.aac.MergeMode;

public class QuestionMergeMode extends MergeMode {
    String question;
    int questionId;
    int queryType;
    String msgId;
    int logId;

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

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
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

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }
}
