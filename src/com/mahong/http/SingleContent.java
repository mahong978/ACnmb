package com.mahong.http;

import android.text.Html;
import android.text.Spanned;

public class SingleContent {
	private String id;
	private String img;
	private String ext;
	private String now;
	private String userid;
	private String refer;
	private String content;
	private String replyCount;
	private Boolean admin;
	public SingleContent(String id, String img, String ext, String now,
			String userid, String content, String replyConut, Boolean admin) {
		super();
		this.id = id;
		this.img = img;
		this.ext = ext;
		this.now = now;
		this.userid = userid;
		this.replyCount= replyCount;
		this.admin = admin;
		
		String temp = Html.fromHtml(content).toString();
		if (!temp.equals("") && temp.charAt(0) == '>')
		{
			String[] temps = temp.split("\\r?\\n", 2);
			this.refer = temps[0];
			this.content = temps[1];
		}
		else
		{
			this.refer = null;
			this.content = content;
		}
	}
	public String getId() {
		return id;
	}
	public String getImg() {
		return img;
	}
	public String getExt() {
		return ext;
	}
	public String getNow() {
		return now;
	}
	public String getUserid() {
		return userid;
	}
	public Spanned getRefer() {
		if (refer == null)
			return null;
		else
			return Html.fromHtml(refer);
	}
	public Spanned getContent() {
		return Html.fromHtml(content);
	}
	public String getReplyCount() {
		return replyCount;
	}
	public Boolean getAdmin() {
		return admin;
	}
	
	
}
