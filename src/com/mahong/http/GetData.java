package com.mahong.http;

import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.mahong.tool.Tool;

public class GetData {
	private static final String API = "http://h.nimingban.com/Api";
	public static final String IMAGEAPI = "http://hacfun-tv.n1.yun.tf:8999/Public/Upload/";
	private static final String[] SUBAPI = new String[]{"/getCookie", "/getForumList", "/showf", "/thread", "/feed",
		"/addFeed", "/delFeed"
	};
	
	public static final int GETCOOKIE = 0;
	public static final int GETLIST = 1;
	public static final int GETSHOW = 2;
	public static final int GETCONTENT = 3;
	public static final int SHOWSUBSCRIBE = 4;
	public static final int ADDSUBSCRIBE = 5;
	public static final int DELSUBSCRIBE = 6;
	
	public static void httpLink(final Map<String, String> m, final int flag, final ResponseListener listener)
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					String address = API + SUBAPI[flag];
					if (m!=null)
						for(Map.Entry<String, String> entry:m.entrySet())
							address += "/" + entry.getKey() + "/" + entry.getValue();
						
					HttpClient httpClient = new DefaultHttpClient();
					HttpGet httpGet = new HttpGet(address);
					HttpResponse httpResponse = httpClient.execute(httpGet);
					if (httpResponse.getStatusLine().getStatusCode() == 200)
					{
						HttpEntity entity = httpResponse.getEntity();
						String response = EntityUtils.toString(entity);
						
						if (listener != null)
						{
							listener.onCheck(response);
						}
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}).start();
	}

}
