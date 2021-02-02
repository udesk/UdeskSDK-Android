package cn.udesk;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

import cn.udesk.config.UdeskBaseInfo;
import cn.udesk.model.Agent;
import cn.udesk.model.AgentGroupNode;
import cn.udesk.model.Customer;
import cn.udesk.model.IMInfo;
import cn.udesk.model.ImSetting;
import cn.udesk.model.InitCustomerBean;
import cn.udesk.model.OptionsModel;
import cn.udesk.model.StructModel;
import cn.udesk.model.PreSession;
import cn.udesk.model.Robot;
import cn.udesk.model.SurveyOptionsModel;
import cn.udesk.model.Tag;
import cn.udesk.model.TicketReplieMode;
import udesk.core.model.AllMessageMode;
import udesk.core.model.BaseMode;
import udesk.core.model.Content;
import udesk.core.model.DataBean;
import udesk.core.model.InfoListBean;
import udesk.core.model.InviterAgentInfo;
import udesk.core.model.LinkBean;
import udesk.core.model.LogBean;
import udesk.core.model.OptionsListBean;
import udesk.core.model.OrderBean;
import udesk.core.model.ProductListBean;
import udesk.core.model.RobotInit;
import udesk.core.model.RobotTipBean;
import udesk.core.model.ShowProductBean;
import udesk.core.model.StrucTableBean;
import udesk.core.model.TemplateMsgBean;
import udesk.core.model.TopAskBean;
import cn.udesk.model.UploadService;
import cn.udesk.model.UploadToken;
import udesk.core.model.AgentInfo;
import udesk.core.model.Product;
import udesk.core.model.TraceBean;
import udesk.core.model.TraceInitBean;
import udesk.core.model.TracesModel;
import udesk.core.model.UDHelperArticleContentItem;
import udesk.core.model.UDHelperItem;
import udesk.core.model.UploadBean;
import udesk.core.model.WebConfigBean;
import udesk.core.model.WechatImageBean;
import udesk.core.utils.UdeskUtils;

public class JsonUtils {

    //解析帮助中心接口  请求获取到文章列表字符串
    public static List<UDHelperItem> parseListArticlesResult(String result) {
        List<UDHelperItem> mList = new ArrayList<UDHelperItem>();
        try {
            JSONObject json = new JSONObject(result);
            if (json.has("contents")&&!TextUtils.isEmpty(json.optString("contents"))){
                JSONArray kownlegeArray = json.optJSONArray("contents");
                if (kownlegeArray != null && kownlegeArray.length() > 0) {
                    for (int i = 0; i < kownlegeArray.length(); i++) {
                        JSONObject data = kownlegeArray.optJSONObject(i);
                        UDHelperItem helpItem = new UDHelperItem();
                        helpItem.id = data.optInt("id");
                        helpItem.subject = data.optString("subject");
                        mList.add(helpItem);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mList;
    }

    //解析帮助中心文章内容
    public static UDHelperArticleContentItem parseArticleContentItem(
            String result) {
        UDHelperArticleContentItem item = null;
        try {
            JSONObject json = new JSONObject(result);
            if (json.has("contents")&&!TextUtils.isEmpty(json.optString("contents"))) {
                JSONObject contents = json.optJSONObject("contents");
                if (contents != null) {
                    item = new UDHelperArticleContentItem();
                    item.subject = contents.optString("subject");
                    item.content = contents.optString("content");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return item;
    }

    //解析请求到客服的信息
    public static AgentInfo parseAgentResult(String response) {
        AgentInfo agentInfo = new AgentInfo();
        if (TextUtils.isEmpty(response)) {
            return agentInfo;
        }
        try {
            JSONObject json = new JSONObject(response);
            if (json.has("result")&&!TextUtils.isEmpty(json.optString("result"))) {
                JSONObject result = json.getJSONObject("result");
                if (result.has("code")) {
                    agentInfo.setAgentCode(result.getInt("code"));
                }
                if (result.has("message")) {
                    agentInfo.setMessage(result.getString("message"));
                }
                if (result.has("im_sub_session_id")) {
                    agentInfo.setIm_sub_session_id(result.getString("im_sub_session_id"));
                }
                if (result.has("agent")) {
                    JSONObject agentJson = result.getJSONObject("agent");

                    if (agentJson.has("nick")) {
                        agentInfo.setAgentNick(agentJson.getString("nick"));
                    }
                    if (agentJson.has("jid")) {
                        agentInfo.setAgentJid(agentJson.getString("jid"));
                        UdeskBaseInfo.sendMsgTo = agentJson.getString("jid");
                    }
                    if (agentJson.has("agent_id")) {
                        agentInfo.setAgent_id(agentJson.getString("agent_id"));
                    }
                    if (agentJson.has("avatar")) {
                        agentInfo.setHeadUrl(agentJson.getString("avatar"));
                    }
                }
            } else {
                if (json.has("code")) {
                    agentInfo.setAgentCode(json.getInt("code"));
                }
                if (json.has("message")) {
                    agentInfo.setMessage(json.getString("message"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return agentInfo;

    }

    //解析设置的满意度调查选项
    public static SurveyOptionsModel parseSurveyOptions(String response) {
        SurveyOptionsModel optionsMode = new SurveyOptionsModel();
        if (TextUtils.isEmpty(response)) {
            return optionsMode;
        }
        try {
            JSONObject json = new JSONObject(response);
            if (json.has("result") && !TextUtils.isEmpty(json.optString("result"))) {
                JSONObject result = json.getJSONObject("result");
                if (result.has("enabled")) {
                    optionsMode.setEnabled(result.opt("enabled"));
                }
                if (result.has("remark_enabled")) {
                    optionsMode.setEnabled(result.opt("remark_enabled"));
                }
                if (result.has("remark")) {
                    optionsMode.setRemark(result.opt("remark"));
                }
                if (result.has("name")) {
                    optionsMode.setName(result.opt("name"));
                }
                if (result.has("title")) {
                    optionsMode.setTitle(result.opt("title"));
                }
                if (result.has("desc")) {
                    optionsMode.setDesc(result.opt("desc"));
                }
                if (result.has("show_type")) {
                    optionsMode.setType(result.opt("show_type"));
                }

                JSONObject contextObject = null;
                if (result.has("text")) {
                    contextObject = result.getJSONObject("text");
                } else if (result.has("expression")) {
                    contextObject = result.getJSONObject("expression");
                } else if (result.has("star")) {
                    contextObject = result.getJSONObject("star");
                }

                if (contextObject != null) {

                    if (contextObject.has("default_option_id")) {
                        optionsMode.setDefault_option_id(contextObject.opt("default_option_id"));
                    }
                    if (contextObject.has("options")) {
                        List<OptionsModel> options = new ArrayList<OptionsModel>();
                        JSONArray optionsArray = contextObject.optJSONArray("options");
                        if (optionsArray != null && optionsArray.length() > 0) {
                            for (int i = 0; i < optionsArray.length(); i++) {
                                JSONObject data = optionsArray.optJSONObject(i);
                                OptionsModel optionItem = new OptionsModel();
                                optionItem.setId(data.opt("id"));
                                optionItem.setEnabled(data.opt("enabled"));
                                if (!optionItem.getEnabled() && optionsMode.getType().equals("text")) {
                                    continue;
                                }
                                optionItem.setText(data.opt("text"));
                                optionItem.setDesc(data.opt("desc"));
                                optionItem.setRemark_option(data.opt("remark_option"));

                                if (data.has("tags")) {
                                    List<Tag> tags = new ArrayList<Tag>();
                                    String tagStirng = UdeskUtils.objectToString(data.opt("tags"));
                                    if (!TextUtils.isEmpty(tagStirng)) {
                                        String[] tagsArray = tagStirng.split(",");
                                        for (int k = 0; k < tagsArray.length; k++) {
                                            Tag tag = new Tag();
                                            tag.setText(tagsArray[k]);
                                            tags.add(tag);
                                        }
                                    }
                                    optionItem.setTags(tags);
                                }
                                options.add(optionItem);
                            }
                        }
                        optionsMode.setOptions(options);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return optionsMode;

    }


    public static StructModel parserStructMsg(String jsonString) {
        StructModel structModel = new StructModel();
        try {
            JSONObject rootJson = new JSONObject(jsonString);
            if (rootJson.has("title")) {
                structModel.setTitle(rootJson.getString("title"));
            }
            if (rootJson.has("description")) {
                structModel.setDescription(rootJson.getString("description"));
            }
            if (rootJson.has("img_url")) {
                structModel.setImg_url(rootJson.getString("img_url"));
            }

            if (rootJson.has("buttons")) {
                JSONArray btnArray = rootJson.optJSONArray("buttons");
                List<StructModel.ButtonsBean> structBtns = new ArrayList<StructModel.ButtonsBean>();
                if (btnArray != null && btnArray.length() > 0) {
                    for (int i = 0; i < btnArray.length(); i++) {
                        JSONObject data = btnArray.optJSONObject(i);
                        StructModel.ButtonsBean buttonsBean = new StructModel.ButtonsBean();
                        if (data.has("type")) {
                            buttonsBean.setType(data.getString("type"));
                        }
                        if (data.has("text")) {
                            buttonsBean.setText(data.getString("text"));
                        }
                        if (data.has("value")) {
                            buttonsBean.setValue(data.getString("value"));
                        }
                        structBtns.add(buttonsBean);
                    }
                }
                structModel.setButtons(structBtns);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return structModel;
    }

    public static TicketReplieMode parserTicketReplie(String jsonString) {
        TicketReplieMode replieMode = new TicketReplieMode();
        try {
            JSONObject rootJson = new JSONObject(jsonString);
            if (rootJson.has("size")) {
                replieMode.setSize(rootJson.getInt("size"));
            }
            if (rootJson.has("total")) {
                replieMode.setTotal(rootJson.getInt("total"));
            }
            if (rootJson.has("total_pages")) {
                replieMode.setTotal_pages(rootJson.getInt("total_pages"));
            }

            if (rootJson.has("contents")&&!TextUtils.isEmpty(rootJson.optString("contents"))) {
                JSONArray contentsArray = rootJson.optJSONArray("contents");
                List<TicketReplieMode.ContentsBean> contents = new ArrayList<TicketReplieMode.ContentsBean>();
                if (contentsArray != null && contentsArray.length() > 0) {
                    for (int i = 0; i < contentsArray.length(); i++) {
                        JSONObject data = contentsArray.optJSONObject(i);
                        TicketReplieMode.ContentsBean contentsBean = new TicketReplieMode.ContentsBean();
                        if (data.has("reply_id")) {
                            contentsBean.setReply_id(data.get("reply_id"));
                        }
                        if (data.has("user_avatar")) {
                            contentsBean.setUser_avatar(data.get("user_avatar"));
                        }
                        if (data.has("reply_content")) {
                            contentsBean.setReply_content(data.get("reply_content"));
                        }
                        if (data.has("reply_content_type")) {
                            contentsBean.setReply_content_type(data.get("reply_content_type"));
                        }
                        if (data.has("reply_type")) {
                            contentsBean.setReply_type(data.get("reply_type"));
                        }
                        if (data.has("reply_user")) {
                            contentsBean.setReply_user(data.get("reply_user"));
                        }
                        if (data.has("reply_created_at")) {
                            contentsBean.setReply_created_at(data.get("reply_created_at"));
                        }
                        if (data.has("reply_updated_at")) {
                            contentsBean.setReply_updated_at(data.get("reply_updated_at"));
                        }
                        contents.add(contentsBean);
                    }
                }
                replieMode.setContents(contents);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return replieMode;
    }


    public static JSONObject getProduceJson(Product mProduct) {
        JSONObject jsonObject = new JSONObject();
        try {
            if (!TextUtils.isEmpty(mProduct.getName())) {
                jsonObject.put("name", mProduct.getName());
            }
            if (!TextUtils.isEmpty(mProduct.getUrl())) {
                jsonObject.put("url", mProduct.getUrl());
            }
            if (!TextUtils.isEmpty(mProduct.getImgUrl())) {
                jsonObject.put("imgUrl", mProduct.getImgUrl());
            }
            if (mProduct.getCustomParameters()!=null) {
                jsonObject.put("customParameters",mProduct.getCustomParameters());
            }
            List<Product.ParamsBean> params = mProduct.getParams();
            if (params != null && params.size() > 0) {
                JSONArray jsonsArray = new JSONArray();
                for (Product.ParamsBean paramsBean : params) {
                    JSONObject param = new JSONObject();
                    param.put("text", paramsBean.getText());
                    param.put("color", paramsBean.getColor());
                    param.put("fold", paramsBean.isFold());
                    param.put("break", paramsBean.isBreakX());
                    param.put("size", paramsBean.getSize());
                    jsonsArray.put(param);
                }

                jsonObject.put("params", jsonsArray);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static JSONObject getReplyProductJson(ProductListBean bean) {
        JSONObject jsonObject = new JSONObject();
        try {
            if (!TextUtils.isEmpty(bean.getName())) {
                jsonObject.put("name", bean.getName());
            }
            if (!TextUtils.isEmpty(bean.getUrl())) {
                jsonObject.put("url", bean.getUrl());
            }
            if (!TextUtils.isEmpty(bean.getImage())) {
                jsonObject.put("image", bean.getImage());
            }
            if (0 != bean.getId()) {
                jsonObject.put("id", bean.getId());
            }
            if (bean.getInfoList() != null && bean.getInfoList().size() > 0) {
                List<InfoListBean> infoList = bean.getInfoList();
                JSONArray jsonsArray = new JSONArray();
                for (InfoListBean infoListBean : infoList) {
                    JSONObject param = new JSONObject();
                    param.put("boldFlag", infoListBean.getBoldFlag());
                    param.put("color", infoListBean.getColor());
                    param.put("info", infoListBean.getInfo());
                    jsonsArray.put(param);
                }
                jsonObject.put("infoList", jsonsArray);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    //解析设置的满意度调查选项
    public static RobotInit parseRobotInit(String response) {

        RobotInit robotInit = new RobotInit();
        if (TextUtils.isEmpty(response)) {
            return robotInit;
        }
        try {
            JSONObject root = new JSONObject(response);
            robotInit.setCode(root.opt("code"));
            robotInit.setMessage(root.opt("message"));
            if (root.has("session_info")&&!TextUtils.isEmpty(root.optString("session_info"))) {
                JSONObject sessionInfo = root.getJSONObject("session_info");
                robotInit.setCode(sessionInfo.opt("code"));
                robotInit.setMessage(sessionInfo.opt("message"));
                robotInit.setStatus(sessionInfo.opt("status"));
                robotInit.setSessionId(sessionInfo.opt("sessionId"));
                robotInit.setLogId(sessionInfo.opt("logId"));
                robotInit.setSwitchStaffTips(sessionInfo.opt("switchStaffTips"));
                robotInit.setSwitchStaffType(sessionInfo.opt("switchStaffType"));
                robotInit.setSwitchStaffAnswer(sessionInfo.opt("switchStaffAnswer"));
                if (sessionInfo.has("webConfig")) {
                    WebConfigBean webConfigBean = new WebConfigBean();
                    JSONObject webConfig = sessionInfo.getJSONObject("webConfig");
                    webConfigBean.setRobotName(webConfig.opt("robotName"));
                    webConfigBean.setLogoUrl(webConfig.opt("logoUrl"));
                    webConfigBean.setLeadingWord(webConfig.opt("leadingWord"));
                    webConfigBean.setHelloWord(webConfig.opt("helloWord"));
                    robotInit.setWebConfig(webConfigBean);
                }
                if (sessionInfo.has("topAsk")) {
                    List<TopAskBean> topAskList = new ArrayList<>();
                    JSONArray topAskArray = sessionInfo.getJSONArray("topAsk");
                    if (topAskArray != null && topAskArray.length() > 0) {
                        for (int i = 0; i < topAskArray.length(); i++) {
                            JSONObject topAsk = topAskArray.optJSONObject(i);
                            TopAskBean topAskBean = new TopAskBean();
                            topAskBean.setQuestionType(topAsk.opt("questionType"));
                            topAskBean.setQuestionTypeId(topAsk.opt("questionTypeId"));
                            if (topAsk.has("optionsList")) {
                                List<OptionsListBean> optionsLists = new ArrayList<>();
                                JSONArray optionsListArray = topAsk.getJSONArray("optionsList");
                                if (optionsListArray != null && optionsListArray.length() > 0) {
                                    for (int k = 0; k < optionsListArray.length(); k++) {
                                        JSONObject optionsList = optionsListArray.optJSONObject(k);
                                        OptionsListBean optionsListBean = new OptionsListBean();
                                        optionsListBean.setQuestion(optionsList.opt("question"));
                                        optionsListBean.setQuestionId(optionsList.opt("questionId"));

                                        optionsLists.add(optionsListBean);
                                    }
                                }
                                topAskBean.setOptionsList(optionsLists);
                            }
                            topAskList.add(topAskBean);

                        }
                    }
                    robotInit.setTopAsk(topAskList);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return robotInit;

    }

    public static RobotTipBean parseRobotTip(String response) {
        RobotTipBean bean = new RobotTipBean();
        if (TextUtils.isEmpty(response)) {
            return bean;
        }
        try {
            JSONObject root = new JSONObject(response);
            bean.setCode(root.opt("code"));
            bean.setMessage(root.opt("message"));
            if (root.has("list") && !TextUtils.isEmpty(root.optString("list"))) {
                List<RobotTipBean.ListBean> tipList = new ArrayList<>();
                JSONArray tipArray = root.getJSONArray("list");
                if (tipArray != null && tipArray.length() > 0) {
                    for (int i = 0; i < tipArray.length(); i++) {
                        JSONObject tip = tipArray.optJSONObject(i);
                        RobotTipBean.ListBean tipListBean = new RobotTipBean.ListBean();
                        tipListBean.setQuestionId(tip.opt("questionId"));
                        tipListBean.setQuestion(tip.opt("question"));
                        tipListBean.setType(tip.opt("type"));
                        tipList.add(tipListBean);
                    }
                }
                bean.setList(tipList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bean;
    }

    //AllMessageMode
    public static AllMessageMode parseMessage(String response) {
        AllMessageMode allMessageMode = new AllMessageMode();
        if (TextUtils.isEmpty(response)) {
            return allMessageMode;
        }
        try {
            JSONObject root = new JSONObject(response);
            allMessageMode.setCode(root.opt("code"));
            allMessageMode.setMore_marking(root.opt("more_marking"));
            allMessageMode.setMessage(root.opt("message"));
            if (root.has("messages") && !TextUtils.isEmpty(root.optString("messages"))) {
                List<LogBean> logMessages = new ArrayList<>();
                JSONArray messagesArray = root.getJSONArray("messages");
                if (messagesArray != null && messagesArray.length() > 0) {
                    for (int i = 0; i < messagesArray.length(); i++) {
                        JSONObject log = messagesArray.getJSONObject(i);
                        LogBean logBean = new LogBean();
                        logBean.setAgent_avatar(log.opt("agent_avatar"));
                        logBean.setAgent_id(log.opt("agent_id"));
                        logBean.setAgent_jid(log.opt("agent_jid"));
                        logBean.setAgent_nick_name(log.opt("agent_nick_name"));
                        logBean.setContent_raw(log.opt("content_raw"));
                        logBean.setCreated_at(log.opt("created_at"));
                        logBean.setCreated_time(log.opt("created_time"));
                        logBean.setCustomer_id(log.opt("customer_id"));
                        logBean.setId(log.opt("id"));
                        logBean.setLog_type(log.opt("log_type"));
                        logBean.setLogId(log.opt("logId"));
                        logBean.setMessage_id(log.opt("message_id"));
                        logBean.setNow(log.opt("now"));
                        logBean.setSender(log.opt("sender"));
                        logBean.setSession_type(log.opt("session_type"));
                        logBean.setUpdated_at(log.opt("updated_at"));
                        logBean.setSend_status(log.opt("send_status"));
                        logBean.setIm_sub_session_id(log.opt("im_sub_session_id"));
                        logBean.setOrder(log.opt("order"));

                        if (log.has("content")) {
                            JSONObject content = log.getJSONObject("content");
                            Content contentBean = new Content();
                            contentBean.setType(content.opt("type"));
                            contentBean.setPush_type(content.opt("push_type"));
                            contentBean.setFont(content.opt("font"));
                            contentBean.setPlatform(content.opt("platform"));
                            contentBean.setVersion(content.opt("version"));
                            contentBean.setAuto(content.opt("auto"));
                            contentBean.setSeq_num(content.opt("seq_num"));
                            contentBean.setIm_sub_session_id(content.opt("im_sub_session_id"));
                            contentBean.setFilename(content.opt("filename"));
                            contentBean.setFilesize(content.opt("filesize"));
                            contentBean.setLocalPath(content.opt("localPath"));
                            contentBean.setFile_policy(content.opt("file_policy"));
                            if (content.has("data")) {
                                DataBean dataBean = new DataBean();
                                JSONObject data = content.getJSONObject("data");
                                dataBean.setContent(data.opt("content"));
                                dataBean.setSwitchStaffType(data.opt("switchStaffType"));
                                dataBean.setSwitchStaffTips(data.opt("switchStaffTips"));
                                dataBean.setSwitchStaffAnswer(data.opt("switchStaffAnswer"));
                                dataBean.setQuesition_id(data.opt("question_id"));
                                dataBean.setTimeout_freq(data.opt("timeout_freq"));
                                dataBean.setDuration(data.opt("duration"));

                                dataBean.setFlowId(data.opt("flowId"));
                                dataBean.setFlowTitle(data.opt("flowTitle"));
                                dataBean.setFlowContent(data.opt("flowContent"));
                                dataBean.setRecommendationGuidance(data.opt("recommendationGuidance"));
                                if (data.has("topAsk")) {
                                    if (!TextUtils.isEmpty(data.optString("topAsk"))) {
                                        List<TopAskBean> topAskList = parseTopAsk(data);
                                        dataBean.setTopAsk(topAskList);
                                    }
                                }
                                contentBean.setData(dataBean);
                            }
                            logBean.setContent(contentBean);
                        }
                        if (log.has("inviter")&&!TextUtils.isEmpty(log.optString("inviter"))){
                            JSONObject inviter = log.getJSONObject("inviter");
                            InviterAgentInfo inviterAgentInfo=new InviterAgentInfo();
                            inviterAgentInfo.setAvatar(inviter.opt("avatar"));
                            inviterAgentInfo.setId(inviter.opt("id"));
                            inviterAgentInfo.setJid(inviter.opt("jid"));
                            inviterAgentInfo.setNick_name(inviter.opt("nick_name"));
                            logBean.setInviterAgentInfo(inviterAgentInfo);
                        }
                        logMessages.add(logBean);
                    }
                }
                allMessageMode.setMessages(logMessages);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return allMessageMode;

    }

    public static List<TopAskBean> parseTopAsk(JSONObject data) {
        List<TopAskBean> topAskList = new ArrayList<>();
        if (data == null) {
            return topAskList;
        }
        try {
            if (data.has("topAsk") && !TextUtils.isEmpty(data.optString("topAsk"))) {
                Object topAskObject = new JSONTokener(data.optString("topAsk")).nextValue();
                if (topAskObject instanceof JSONArray) {
                    JSONArray topAskArray = data.getJSONArray("topAsk");
                    if (topAskArray != null && topAskArray.length() > 0) {
                        for (int k = 0; k < topAskArray.length(); k++) {
                            JSONObject topAsk = topAskArray.optJSONObject(k);
                            TopAskBean topAskBean = new TopAskBean();
                            topAskBean.setQuestionType(topAsk.opt("questionType"));
                            topAskBean.setQuestionTypeId(topAsk.opt("questionTypeId"));
                            if (topAsk.has("optionsList")) {
                                List<OptionsListBean> optionsLists = new ArrayList<>();
                                JSONArray optionsListArray = topAsk.getJSONArray("optionsList");
                                if (optionsListArray != null && optionsListArray.length() > 0) {
                                    for (int j = 0; j < optionsListArray.length(); j++) {
                                        JSONObject optionsList = optionsListArray.optJSONObject(j);
                                        OptionsListBean optionsListBean = new OptionsListBean();
                                        optionsListBean.setQuestion(optionsList.opt("question"));
                                        optionsListBean.setQuestionId(optionsList.opt("questionId"));

                                        optionsLists.add(optionsListBean);
                                    }
                                }
                                topAskBean.setOptionsList(optionsLists);
                            }
                            topAskList.add(topAskBean);
                        }
                    }

                } else if (topAskObject instanceof JSONObject) {
                    JSONObject topAsk = data.getJSONObject("topAsk");
                    TopAskBean topAskBean = new TopAskBean();
                    topAskBean.setQuestionType(topAsk.opt("questionType"));
                    topAskBean.setQuestionTypeId(topAsk.opt("questionTypeId"));
                    if (topAsk.has("optionsList")) {
                        List<OptionsListBean> optionsLists = new ArrayList<>();
                        JSONArray optionsListArray = topAsk.getJSONArray("optionsList");
                        if (optionsListArray != null && optionsListArray.length() > 0) {
                            for (int j = 0; j < optionsListArray.length(); j++) {
                                JSONObject optionsList = optionsListArray.optJSONObject(j);
                                OptionsListBean optionsListBean = new OptionsListBean();
                                optionsListBean.setQuestion(optionsList.opt("question"));
                                optionsListBean.setQuestionId(optionsList.opt("questionId"));
                                optionsLists.add(optionsListBean);
                            }
                        }
                        topAskBean.setOptionsList(optionsLists);
                    }
                    topAskList.add(topAskBean);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return topAskList;
    }
    public static LogBean parseLogBean(String response) {
        LogBean logBean = new LogBean();
        if (TextUtils.isEmpty(response)) {
            return logBean;
        }
        try {
            JSONObject root = new JSONObject(response);
            logBean.setCode(root.opt("code"));
            logBean.setMessage(root.opt("message"));
            if (!TextUtils.isEmpty(root.optString("log"))&&root.has("log")) {
                JSONObject log = root.getJSONObject("log");
                logBean.setAgent_avatar(log.opt("agent_avatar"));
                logBean.setAgent_id(log.opt("agent_id"));
                logBean.setAgent_jid(log.opt("agent_jid"));
                logBean.setAgent_nick_name(log.opt("agent_nick_name"));
                logBean.setContent_raw(log.opt("content_raw"));
                logBean.setCreated_at(log.opt("created_at"));
                logBean.setCreated_time(log.opt("created_time"));
                logBean.setCustomer_id(log.opt("customer_id"));
                logBean.setId(log.opt("id"));
                logBean.setLog_type(log.opt("log_type"));
                logBean.setLogId(log.opt("logId"));
                logBean.setMessage_id(log.opt("message_id"));
                logBean.setNow(log.opt("now"));
                logBean.setSender(log.opt("sender"));
                logBean.setSession_type(log.opt("session_type"));
                logBean.setUpdated_at(log.opt("updated_at"));
                logBean.setSend_status(log.opt("send_status"));
                logBean.setIm_sub_session_id(log.opt("im_sub_session_id"));
                logBean.setOrder(log.opt("order"));
                if (log.has("content")) {
                    JSONObject content = log.getJSONObject("content");
                    Content contentBean = new Content();
                    contentBean.setType(content.opt("type"));
                    contentBean.setPush_type(content.opt("push_type"));
                    contentBean.setFont(content.opt("font"));
                    contentBean.setPlatform(content.opt("platform"));
                    contentBean.setVersion(content.opt("version"));
                    contentBean.setAuto(content.opt("auto"));
                    contentBean.setSeq_num(content.opt("seq_num"));
                    contentBean.setIm_sub_session_id(content.opt("im_sub_session_id"));
                    contentBean.setFilename(content.opt("filename"));
                    contentBean.setFilesize(content.opt("filesize"));
                    contentBean.setLocalPath(content.opt("localPath"));
                    contentBean.setFile_policy(content.opt("file_policy"));
                    if (content.has("data")) {
                        DataBean dataBean = new DataBean();
                        JSONObject data = content.getJSONObject("data");
                        dataBean.setContent(data.opt("content"));
                        dataBean.setSwitchStaffType(data.opt("switchStaffType"));
                        dataBean.setSwitchStaffTips(data.opt("switchStaffTips"));
                        dataBean.setSwitchStaffAnswer(data.opt("switchStaffAnswer"));
                        dataBean.setQuesition_id(data.opt("question_id"));
                        dataBean.setTimeout_freq(data.opt("timeout_freq"));
                        dataBean.setDuration(data.opt("duration"));
                        dataBean.setFlowId(data.opt("flowId"));
                        dataBean.setFlowTitle(data.opt("flowTitle"));
                        dataBean.setFlowContent(data.opt("flowContent"));
                        dataBean.setRecommendationGuidance(data.opt("recommendationGuidance"));
                        if (data.has("topAsk")) {
                            if (!TextUtils.isEmpty(data.optString("topAsk"))) {
                                List<TopAskBean> topAskList = parseTopAsk(data);
                                dataBean.setTopAsk(topAskList);
                            }
                        }
                        contentBean.setData(dataBean);
                    }
                    logBean.setContent(contentBean);
                }
                if (log.has("inviter")&&!TextUtils.isEmpty(log.optString("inviter"))){
                    JSONObject inviter = log.getJSONObject("inviter");
                    InviterAgentInfo inviterAgentInfo=new InviterAgentInfo();
                    inviterAgentInfo.setAvatar(inviter.opt("avatar"));
                    inviterAgentInfo.setId(inviter.opt("id"));
                    inviterAgentInfo.setJid(inviter.opt("jid"));
                    inviterAgentInfo.setNick_name(inviter.opt("nick_name"));
                    logBean.setInviterAgentInfo(inviterAgentInfo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return logBean;
    }

    //解析设置的满意度调查选项
    public static BaseMode parseAnswerSurvey(String response) {

        BaseMode baseMode = new BaseMode();
        if (TextUtils.isEmpty(response)) {
            return baseMode;
        }
        try {
            JSONObject root = new JSONObject(response);
            baseMode.setCode(root.opt("code"));
            baseMode.setMessage(root.opt("message"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return baseMode;

    }

    public static StrucTableBean parseStrucTable(String response) {
        StrucTableBean strucTableBean = new StrucTableBean();
        if (TextUtils.isEmpty(response)) {
            return strucTableBean;
        }
        try {
            JSONObject root = new JSONObject(response);
            strucTableBean.setTitle(root.opt("title"));
            strucTableBean.setColumnNumber(root.opt("columnNumber"));
            strucTableBean.setRowNumber(root.opt("rowNumber"));
            if (root.has("optionList")) {
                List<StrucTableBean.OptionListBean> listBeanList = new ArrayList<>();
                JSONArray optionList = root.getJSONArray("optionList");
                if (optionList != null && optionList.length() > 0) {
                    for (int i = 0; i < optionList.length(); i++) {
                        StrucTableBean.OptionListBean optionListBean = new StrucTableBean.OptionListBean();
                        JSONObject object = optionList.getJSONObject(i);
                        optionListBean.setId(object.opt("id"));
                        optionListBean.setValue(object.opt("value"));
                        listBeanList.add(optionListBean);
                    }
                }
                strucTableBean.setOptionList(listBeanList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strucTableBean;
    }

    public static TemplateMsgBean parseTemplateMsg(String response){
        TemplateMsgBean templateMsgBean =new TemplateMsgBean();
        if (TextUtils.isEmpty(response)){
            return templateMsgBean;
        }

        try {
            JSONObject root = new JSONObject(response);
            templateMsgBean.setTitle(root.opt("title"));
            templateMsgBean.setContent(root.opt("content"));
            if (root.has("btns")){
                List<TemplateMsgBean.BtnsBean> btnsBeans= new ArrayList<>();
                JSONArray btnsArray = root.getJSONArray("btns");
                if (btnsArray!=null && btnsArray.length()>0){
                    for (int i= 0; i< btnsArray.length();i++){
                        TemplateMsgBean.BtnsBean btnsBean = new TemplateMsgBean.BtnsBean();
                        JSONObject beanJson = btnsArray.getJSONObject(i);
                        btnsBean.setName(beanJson.opt("name"));
                        btnsBean.setType(beanJson.opt("type"));
                        if (beanJson.has("data")){
                            JSONObject data = beanJson.getJSONObject("data");
                            TemplateMsgBean.BtnsBean.DataBean dataBean = new TemplateMsgBean.BtnsBean.DataBean();
                            dataBean.setUrl(data.opt("url"));
                            btnsBean.setData(dataBean);
                        }
                        btnsBeans.add(btnsBean);
                    }
                }
                templateMsgBean.setBtns(btnsBeans);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return templateMsgBean;
    }
    public static UploadBean parseUploadBean(String response) {
        UploadBean uploadBean = new UploadBean();
        if (TextUtils.isEmpty(response)) {
            return uploadBean;
        }
        try {
            JSONObject root = new JSONObject(response);
            uploadBean.setCode(root.opt("code"));
            uploadBean.setMessage(root.opt("message"));
            if (root.has("upload_service")) {
                JSONObject upload_service = root.getJSONObject("upload_service");
                uploadBean.setKey(upload_service.opt("key"));
                uploadBean.setMarking(upload_service.opt("marking"));
                uploadBean.setReferer(upload_service.opt("referer"));
                if (upload_service.has("upload_token")) {
                    JSONObject upload_token = upload_service.getJSONObject("upload_token");
                    UploadBean.UploadTokenBean uploadTokenBean = new UploadBean.UploadTokenBean();
                    uploadTokenBean.setAccessid(upload_token.opt("accessid"));
                    uploadTokenBean.setBucket(upload_token.opt("bucket"));
                    uploadTokenBean.setCallback(upload_token.opt("callback"));
                    uploadTokenBean.setDir(upload_token.opt("dir"));
                    uploadTokenBean.setExpire(upload_token.opt("expire"));
                    uploadTokenBean.setFields(upload_token.opt("fields"));
                    uploadTokenBean.setHost(upload_token.opt("host"));
                    uploadTokenBean.setPolicy(upload_token.opt("policy"));
                    uploadTokenBean.setSignature(upload_token.opt("signature"));
                    uploadTokenBean.setStorage_policy(upload_token.opt("storage_policy"));
                    uploadTokenBean.setToken(upload_token.opt("token"));
                    uploadTokenBean.setDownload_host(upload_token.opt("download_host"));
                    uploadBean.setUpload_token(uploadTokenBean);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uploadBean;
    }

    public static ShowProductBean parseShowProduct(String response) {
        ShowProductBean showProductBean = new ShowProductBean();
        if (TextUtils.isEmpty(response)) {
            return showProductBean;
        }
        try {
            JSONObject root = new JSONObject(response);
            showProductBean.setShowSize(root.opt("showSize"));
            showProductBean.setTitle(root.opt("title"));
            showProductBean.setTurnFlag(root.opt("turnFlag"));
            if (root.has("productList")) {
                List<ProductListBean> productListBeanList = new ArrayList<>();
                JSONArray productList = root.getJSONArray("productList");
                if (productList != null && productList.length() > 0) {
                    for (int i = 0; i < productList.length(); i++) {
                        ProductListBean productListBean = parseReplyProduct(productList.getJSONObject(i).toString());
                        productListBeanList.add(productListBean);
                    }
                }
                showProductBean.setProductList(productListBeanList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return showProductBean;
    }

    public static LinkBean parseLinkBean(String response) {
        LinkBean linkBean = new LinkBean();
        if (TextUtils.isEmpty(response)) {
            return linkBean;
        }
        try {
            JSONObject root = new JSONObject(response);
            linkBean.setAnswerUrl(root.opt("answerUrl"));
            linkBean.setTitle(root.opt("title"));
            linkBean.setFaviconUrl(root.opt("faviconUrl"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return linkBean;
    }

    public static WechatImageBean parseWechatImage(String response) {
        WechatImageBean wechatImageBean = new WechatImageBean();
        if (TextUtils.isEmpty(response)) {
            return wechatImageBean;
        }
        try {
            JSONObject root = new JSONObject(response);
            wechatImageBean.setAnswerUrl(root.opt("answerUrl"));
            wechatImageBean.setContent(root.opt("content"));
            wechatImageBean.setCoverUrl(root.opt("coverUrl"));
            wechatImageBean.setDescription(root.opt("description"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return wechatImageBean;
    }

    public static ProductListBean parseReplyProduct(String response) {
        ProductListBean productListBean = new ProductListBean();
        if (TextUtils.isEmpty(response)) {
            return productListBean;
        }
        try {
            JSONObject productListJSONObject = new JSONObject(response);
            productListBean.setId(productListJSONObject.opt("id"));
            productListBean.setImage(productListJSONObject.opt("image"));
            productListBean.setName(productListJSONObject.opt("name"));
            productListBean.setUrl(productListJSONObject.opt("url"));
            if (productListJSONObject.has("infoList")) {
                JSONArray infoList = productListJSONObject.getJSONArray("infoList");
                List<InfoListBean> infoListBeanList = new ArrayList<>();
                if (infoList != null && infoList.length() > 0) {
                    for (int j = 0; j < infoList.length(); j++) {
                        JSONObject infoListJSONObject = infoList.getJSONObject(j);
                        InfoListBean infoListBean = new InfoListBean();
                        infoListBean.setBoldFlag(infoListJSONObject.opt("boldFlag"));
                        infoListBean.setColor(infoListJSONObject.opt("color"));
                        infoListBean.setInfo(infoListJSONObject.opt("info"));
                        infoListBeanList.add(infoListBean);
                    }
                }
                productListBean.setInfoList(infoListBeanList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return productListBean;
    }

    public static InitCustomerBean parseInitCustomer(String response) {
        InitCustomerBean initCustomerBean = new InitCustomerBean();
        if (TextUtils.isEmpty(response)) {
            return initCustomerBean;
        }
        try {
            JSONObject root = new JSONObject(response);
            initCustomerBean.setCode(root.opt("code"));
            initCustomerBean.setMessage(root.opt("message"));

            if (root.has("im")) {
                IMInfo imInfo = new IMInfo();
                JSONObject imObject = root.optJSONObject("im");
                imInfo.setUsername(imObject.opt("username"));
                imInfo.setPassword(imObject.opt("password"));
                imInfo.setServer(imObject.opt("server"));
                imInfo.setPort(imObject.opt("port"));
                initCustomerBean.setIm(imInfo);
            }

            if (root.has("upload_service")) {
                JSONObject uploadServiceObject = root.optJSONObject("upload_service");
                UploadService uploadService = new UploadService();
                uploadService.setKey(uploadServiceObject.opt("key"));
                uploadService.setMarking(uploadServiceObject.opt("marking"));
                uploadService.setReferer(uploadServiceObject.opt("referer"));
                if (uploadServiceObject.has("upload_token")) {
                    JSONObject uploadTokenObject = uploadServiceObject.optJSONObject("upload_token");
                    UploadToken uploadToken = new UploadToken();
                    uploadToken.setAccessid(uploadTokenObject.opt("accessid"));
                    uploadToken.setBucket(uploadServiceObject.opt("bucket"));
                    uploadToken.setCallback(uploadServiceObject.opt("callback"));
                    uploadToken.setDir(uploadServiceObject.opt("dir"));
                    uploadToken.setExpire(uploadServiceObject.opt("expire"));
                    uploadToken.setHost(uploadServiceObject.opt("host"));
                    uploadToken.setPolicy(uploadServiceObject.opt("policy"));
                    uploadToken.setSignature(uploadServiceObject.opt("signature"));
                    uploadToken.setStorage_policy(uploadServiceObject.opt("storage_policy"));
                    uploadToken.setToken(uploadTokenObject.opt("token"));
                    uploadService.setUpload_token(uploadToken);
                }
                initCustomerBean.setUploadService(uploadService);
            }

            if (root.has("im_survey")) {
                SurveyOptionsModel optionsMode = new SurveyOptionsModel();
                JSONObject result = root.getJSONObject("im_survey");
                if (result.has("enabled")) {
                    optionsMode.setEnabled(result.opt("enabled"));
                }
                if (result.has("remark_enabled")) {
                    optionsMode.setEnabled(result.opt("remark_enabled"));
                }
                if (result.has("remark")) {
                    optionsMode.setRemark(result.opt("remark"));
                }
                if (result.has("name")) {
                    optionsMode.setName(result.opt("name"));
                }
                if (result.has("title")) {
                    optionsMode.setTitle(result.opt("title"));
                }
                if (result.has("desc")) {
                    optionsMode.setDesc(result.opt("desc"));
                }
                if (result.has("show_type")) {
                    optionsMode.setType(result.opt("show_type"));
                }

                JSONObject contextObject = null;
                if (result.has("text")) {
                    contextObject = result.getJSONObject("text");
                } else if (result.has("expression")) {
                    contextObject = result.getJSONObject("expression");
                } else if (result.has("star")) {
                    contextObject = result.getJSONObject("star");
                }

                if (contextObject != null) {

                    if (contextObject.has("default_option_id")) {
                        optionsMode.setDefault_option_id(contextObject.opt("default_option_id"));
                    }
                    if (contextObject.has("options")) {
                        List<OptionsModel> options = new ArrayList<OptionsModel>();
                        JSONArray optionsArray = contextObject.optJSONArray("options");
                        if (optionsArray != null && optionsArray.length() > 0) {
                            for (int i = 0; i < optionsArray.length(); i++) {
                                JSONObject data = optionsArray.optJSONObject(i);
                                OptionsModel optionItem = new OptionsModel();
                                optionItem.setId(data.opt("id"));
                                optionItem.setEnabled(data.opt("enabled"));
                                if (!optionItem.getEnabled() && optionsMode.getType().equals("text")) {
                                    continue;
                                }
                                optionItem.setText(data.opt("text"));
                                optionItem.setDesc(data.opt("desc"));
                                optionItem.setRemark_option(data.opt("remark_option"));

                                if (data.has("tags")) {
                                    List<Tag> tags = new ArrayList<Tag>();
                                    String tagStirng = UdeskUtils.objectToString(data.opt("tags"));
                                    if (!TextUtils.isEmpty(tagStirng)) {
                                        String[] tagsArray = tagStirng.split(",");
                                        for (int k = 0; k < tagsArray.length; k++) {
                                            Tag tag = new Tag();
                                            tag.setText(tagsArray[k]);
                                            tags.add(tag);
                                        }
                                    }
                                    optionItem.setTags(tags);
                                }
                                options.add(optionItem);
                            }
                        }
                        optionsMode.setOptions(options);
                    }
                }
                initCustomerBean.setIm_survey(optionsMode);
            }

            if (root.has("im_settings")) {
                JSONObject settingsJsonObject = root.getJSONObject("im_settings");
                ImSetting imSetting = new ImSetting();
                imSetting.setEnable_im_group(settingsJsonObject.opt("enable_im_group"));
                imSetting.setLeave_message_type(settingsJsonObject.opt("leave_message_type"));
                imSetting.setIs_worktime(settingsJsonObject.opt("is_worktime"));
                imSetting.setEnable_web_im_feedback(settingsJsonObject.opt("enable_web_im_feedback"));
                imSetting.setEnable_im_survey(settingsJsonObject.opt("enable_im_survey"));
                imSetting.setIm_survey_show_type(settingsJsonObject.opt("im_survey_show_type"));
                imSetting.setInvestigation_when_leave(settingsJsonObject.opt("investigation_when_leave"));
                imSetting.setNo_reply_hint(settingsJsonObject.opt("no_reply_hint"));
                imSetting.setVcall(settingsJsonObject.opt("vcall"));
                imSetting.setSdk_vcall(settingsJsonObject.opt("sdk_vcall"));
                imSetting.setVc_app_id(settingsJsonObject.opt("vc_app_id"));
                imSetting.setVcall_token_url(settingsJsonObject.opt("vcall_token_url"));
                imSetting.setAgora_app_id(settingsJsonObject.opt("agora_app_id"));
                imSetting.setServer_url(settingsJsonObject.opt("server_url"));
                imSetting.setLeave_message_guide(settingsJsonObject.opt("leave_message_guide"));
                if (settingsJsonObject.has("robot")) {
                    JSONObject robotJsonObject = settingsJsonObject.getJSONObject("robot");
                    Robot robot = new Robot();
                    robot.setEnable(robotJsonObject.opt("enable"));
                    robot.setUrl(robotJsonObject.opt("url"));
                    robot.setRobot_name(robotJsonObject.opt("robot_name"));
                    robot.setShow_robot_times(robotJsonObject.opt("show_robot_times"));
                    robot.setEnable_agent(robotJsonObject.opt("enable_agent"));
                    imSetting.setRobot(robot);
                }
                initCustomerBean.setImSetting(imSetting);
            }
            if (root.has("im_group")) {
                List<AgentGroupNode> groupsModel = new ArrayList<AgentGroupNode>();
                JSONArray optionsArray = root.optJSONArray("im_group");
                if (optionsArray != null && optionsArray.length() > 0) {
                    for (int i = 0; i < optionsArray.length(); i++) {
                        JSONObject data = optionsArray.optJSONObject(i);
                        AgentGroupNode model = new AgentGroupNode();
                        model.setId(data.optString("id"));
                        model.setHas_next(data.optString("has_next"));
                        model.setItem_name(data.optString("item_name"));
                        model.setLink(data.optString("link"));
                        model.setParentId(data.optString("parentId"));
                        groupsModel.add(model);
                    }
                }
                initCustomerBean.setIm_group(groupsModel);
            }

            if (root.has("company")) {
                JSONObject companyObject = root.optJSONObject("company");
                initCustomerBean.setBlack_list_notice(companyObject.opt("black_list_notice"));
            }

            if (root.has("customer")) {
                JSONObject customerObject = root.optJSONObject("customer");
                Customer customer = new Customer();
                customer.setId(customerObject.opt("id"));
                customer.setIs_blocked(customerObject.opt("nick_name"));
                customer.setIs_blocked(customerObject.opt("is_blocked"));
                initCustomerBean.setCustomer(customer);
            }

            if (root.has("pre_session")) {
                JSONObject presessionObject = root.optJSONObject("pre_session");
                PreSession preSession = new PreSession();
                preSession.setPre_session_id(presessionObject.opt("pre_session_id"));
                preSession.setPre_session_title(presessionObject.opt("pre_session_title"));
                preSession.setShow_pre_session(presessionObject.opt("show_pre_session"));
                preSession.setPre_session(presessionObject.opt("pre_session"));
                initCustomerBean.setPre_session(preSession);
            }

            if (root.has("status")) {
                initCustomerBean.setStatus(root.opt("status"));
            }

            if (root.has("agent")) {
                JSONObject agentObject = root.optJSONObject("agent");
                Agent agent = new Agent();
                agent.setAgent_id(agentObject.opt("jid"));
                agent.setAvatar(agentObject.opt("avatar"));
                agent.setAgent_id(agentObject.opt("agent_id"));
                agent.setNick(agentObject.opt("nick"));
                initCustomerBean.setAgent(agent);
            }

            initCustomerBean.setIm_sub_session_id(root.opt("im_sub_session_id"));


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return initCustomerBean;

    }

    public static TraceInitBean parseTraceInit(String response) {
        TraceInitBean traceInitBean = new TraceInitBean();
        if (TextUtils.isEmpty(response)) {
            return traceInitBean;
        }
        try {
            JSONObject root = new JSONObject(response);
            traceInitBean.setCode(root.opt("code"));
            traceInitBean.setBehavoir_trace(root.opt("behavoir_trace"));
            traceInitBean.setCustomer_order(root.opt("customer_order"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return traceInitBean;
    }
    public static TracesModel parseTracesModel(String response) {
        TracesModel tracesModel = new TracesModel();
        if (TextUtils.isEmpty(response)) {
            return tracesModel;
        }
        try {
            JSONObject root = new JSONObject(response);
            tracesModel.setCode(root.opt("code"));
           if (root.has("traces")){
               List<TracesModel.TracesBean> tracesBeanList = new ArrayList<>();
               JSONArray traces = root.optJSONArray("traces");
               if (traces!=null && traces.length()>0){
                   for (int i=0;i<traces.length();i++){
                       TracesModel.TracesBean tracesBean=new TracesModel.TracesBean();
                       JSONObject jsonObject = traces.optJSONObject(i);
                       tracesBean.setId(jsonObject.opt("id"));
                       tracesBean.setVist_num(jsonObject.opt("vist_num"));
                       if (jsonObject.has("trace")){
                           TraceBean traceBean = new TraceBean();
                           JSONObject trace = jsonObject.optJSONObject("trace");
                           traceBean.setType(trace.opt("type"));
                           if (trace.has("data")){
                               TraceBean.DataBean dataBean=new TraceBean.DataBean();
                               JSONObject data = trace.optJSONObject("data");
                               dataBean.setName(data.opt("name"));
                               dataBean.setImgUrl(data.opt("imgUrl"));
                               dataBean.setUrl(data.opt("url"));
                               dataBean.setDate(data.opt("date"));
                               if (data.has("params")){
                                   List<TraceBean.DataBean.ParamsBean> paramsBeanList =new ArrayList<>();
                                   JSONArray params = data.optJSONArray("params");
                                   if (params!=null && params.length()>0){
                                       for (int j=0;j<params.length();j++){
                                           TraceBean.DataBean.ParamsBean paramsBean=new TraceBean.DataBean.ParamsBean();
                                           JSONObject object = params.optJSONObject(j);
                                           paramsBean.setBreakX(object.opt("break"));
                                           paramsBean.setColor(object.opt("color"));
                                           paramsBean.setFold(object.opt("fold"));
                                           paramsBean.setText(object.opt("text"));
                                           paramsBean.setSize(object.opt("size"));
                                           paramsBeanList.add(paramsBean);
                                       }
                                   }
                                   dataBean.setParams(paramsBeanList);
                               }
                               traceBean.setData(dataBean);
                           }
                           tracesBean.setTrace(traceBean);
                       }
                       tracesBeanList.add(tracesBean);
                   }
               }
               tracesModel.setTraces(tracesBeanList);
           }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return tracesModel;
    }
    public static JSONObject getTraceJson(TraceBean traceBean) {
        JSONObject jsonObject = new JSONObject();
        if (traceBean == null) {
            return jsonObject;
        }
        try {
            jsonObject.put("type", traceBean.getType());
            if (traceBean.getData() != null) {
                JSONObject dataJson = new JSONObject();
                TraceBean.DataBean data = traceBean.getData();
                dataJson.put("name", data.getName());
                dataJson.put("url", data.getUrl());
                dataJson.put("date", data.getDate());
                dataJson.put("imgUrl", data.getImgUrl());

                if (data.getParams() != null && data.getParams().size() > 0) {
                    JSONArray paramListJson = new JSONArray();
                    List<TraceBean.DataBean.ParamsBean> params = data.getParams();
                    for (TraceBean.DataBean.ParamsBean paramsBean : params) {
                        JSONObject paramJson = new JSONObject();
                        paramJson.put("text", paramsBean.getText());
                        paramJson.put("color", paramsBean.getColor());
                        paramJson.put("fold", paramsBean.getFold());
                        paramJson.put("break", paramsBean.getBreakX());
                        paramJson.put("size", paramsBean.getSize());
                        paramListJson.put(paramJson);
                    }
                    dataJson.put("params", paramListJson);
                }
                jsonObject.put("data", dataJson);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static JSONObject getOrderJson(OrderBean orderBean){
        JSONObject jsonObject =new JSONObject();
        if (orderBean==null){
            return jsonObject;
        }
        try {
            jsonObject.put("name", orderBean.getName());
            jsonObject.put("order_at", orderBean.getOrder_at());
            jsonObject.put("order_no", orderBean.getOrder_no());
            jsonObject.put("pay_at", orderBean.getPay_at());
            jsonObject.put("price", orderBean.getPrice());
            jsonObject.put("remark", orderBean.getRemark());
            jsonObject.put("status", orderBean.getStatus());
            jsonObject.put("url", orderBean.getUrl());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static JSONObject parseTopAskToJson(List<TopAskBean> topAsk) {
        JSONObject jsonObject = new JSONObject();
        if (topAsk==null){
            return jsonObject;
        }
        try {
            JSONArray jsonArray = new JSONArray();
            int count=topAsk.size();
            for (int i=0;i<count;i++){
                JSONObject topAskBean = new JSONObject();
                topAskBean.put("questionType",topAsk.get(i).getQuestionType());
                topAskBean.put("questionTypeId",topAsk.get(i).getQuestionTypeId());
                List<OptionsListBean> optionsListBeanList = topAsk.get(i).getOptionsList();
                if (optionsListBeanList !=null){
                    topAskBean.put("optionsList",parseOptionListToJson(optionsListBeanList));
                }
                jsonArray.put(topAskBean);
            }
            jsonObject.put("topAsk",jsonArray);
        }catch (Exception e){
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static JSONArray parseOptionListToJson(List<OptionsListBean> optionsListBeanList){
        JSONArray jsonArray = new JSONArray();
        if (optionsListBeanList == null){
            return jsonArray;
        }
        try {
            int count=optionsListBeanList.size();
            for (int i=0;i<count;i++){
                JSONObject optionsListBean = new JSONObject();
                optionsListBean.put("question",optionsListBeanList.get(i).getQuestion());
                optionsListBean.put("questionId",optionsListBeanList.get(i).getQuestionId());
                jsonArray.put(optionsListBean);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return jsonArray;
    }

    public static JSONObject parseWebConfigBeanToJson(WebConfigBean webConfig){
        JSONObject jsonObject = new JSONObject();
        if (webConfig==null){
            return jsonObject;
        }
        try {
            JSONObject webConfigJson = new JSONObject();
            webConfigJson.put("helloWord",webConfig.getHelloWord());
            webConfigJson.put("robotName",webConfig.getRobotName());
            webConfigJson.put("logoUrl",webConfig.getLogoUrl());
            webConfigJson.put("leadingWord",webConfig.getLeadingWord());
            jsonObject.put("webConfig",webConfigJson);
        }catch (Exception e){
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static WebConfigBean parseWebConfigBean(String webConfigJson){
        WebConfigBean webConfigBean = new WebConfigBean();
        if (TextUtils.isEmpty(webConfigJson)){
            return webConfigBean;
        }
        try {
            JSONObject jsonObject = new JSONObject(webConfigJson);
            if (jsonObject.has("webConfig")&& !TextUtils.isEmpty(jsonObject.optString("webConfig"))){
                JSONObject object = new JSONObject(jsonObject.optString("webConfig"));
                webConfigBean.setHelloWord(object.opt("helloWord"));
                webConfigBean.setRobotName(object.opt("robotName"));
                webConfigBean.setLogoUrl(object.opt("logoUrl"));
                webConfigBean.setLeadingWord(object.opt("leadingWord"));
            }
        }catch (Exception e){

        }
        return webConfigBean;
    }

}
