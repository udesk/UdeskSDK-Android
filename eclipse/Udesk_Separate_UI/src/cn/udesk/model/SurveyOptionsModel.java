package cn.udesk.model;

import java.io.Serializable;
import java.util.List;

public class SurveyOptionsModel implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	String title ="";
	
	String desc = "";
	
	List<OptionsModel> options;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public List<OptionsModel> getOptions() {
		return options;
	}

	public void setOptions(List<OptionsModel> options) {
		this.options = options;
	}
	
	

}
