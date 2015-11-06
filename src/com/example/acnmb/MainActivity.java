package com.example.acnmb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mahong.http.BitmapCache;
import com.mahong.http.GetData;
import com.mahong.http.ResponseListener;
import com.mahong.http.SingleContent;
import com.mahong.tool.ImageAsync;
import com.mahong.tool.MatrixImageView;

public class MainActivity extends Activity implements OnClickListener{

	private static final int SHOWGET = 0;
	private static long lastBackTime;

	private PullToRefreshListView listView;
	private List<SingleContent> list = new ArrayList<SingleContent>();
	private MListAdapter adapter;
	private MatrixImageView image;
	
	private ImageView add;
	private ResponseListener listener = new ResponseListener() {

		@Override
		public void onError() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onCheck(String s) {
			Message msg = new Message();
			msg.what = SHOWGET;
			msg.obj = s;
			handler.sendMessage(msg);
		}
	};

	private int currBlock, currPage = 1;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SHOWGET:
				if (currPage == 1)
					list.clear();
				try {
					JSONArray jsonArray = new JSONArray((String) msg.obj);
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject jsonObject = jsonArray.getJSONObject(i);
						String id = jsonObject.getString("id");
						String img = jsonObject.getString("img");
						String ext = jsonObject.getString("ext");
						String now = jsonObject.getString("now");
						String userid = jsonObject.getString("userid");
						String content = jsonObject.getString("content");
						String replyCount = jsonObject.getString("replyCount");
						Boolean admin = jsonObject.getString("admin").equals(
								"0") ? false : true;

						SingleContent item = new SingleContent(id, img, ext,
								now, userid, content, replyCount, admin);
						list.add(item);
					}
					adapter.notifyDataSetChanged();
					listView.onRefreshComplete();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
		currBlock = pref.getInt("lastBlock", 4);
		
		add = (ImageView) findViewById(R.id.add);
		add.setOnClickListener(this);

		image = (MatrixImageView) findViewById(R.id.big_image);
		listView = (PullToRefreshListView) findViewById(R.id.list_view);
		listView.setMode(Mode.BOTH);
		adapter = new MListAdapter(MainActivity.this,
				R.layout.single_title_item, list);
		listView.setAdapter(adapter);

		listView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				// TODO Auto-generated method stub
				currPage = 1;
				Map<String, String> m = new HashMap<String, String>();
				m.put("id", currBlock + "");
				m.put("page", currPage + "");
				GetData.httpLink(m, GetData.GETSHOW, listener);
			}

			@Override
			public void onPullUpToRefresh(
					PullToRefreshBase<ListView> refreshView) {
				// TODO Auto-generated method stub
				currPage++;
				Map<String, String> m = new HashMap<String, String>();
				m.put("id", currBlock + "");
				m.put("page", currPage + "");
				GetData.httpLink(m, GetData.GETSHOW, listener);
			}

		});

		Map<String, String> m = new HashMap<String, String>();
		m.put("id", currBlock + "");
		m.put("page", currPage + "");

		GetData.httpLink(m, GetData.GETSHOW, listener);
	}
	
	@Override
	public void onClick(View v)
	{
		switch(v.getId())
		{
		case R.id.add:
			Intent intent = new Intent(MainActivity.this, AddActivity.class);
			startActivity(intent);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (image.getVisibility() == View.VISIBLE)
				image.setVisibility(View.GONE);
			else
			{
				long currBackTime = System.currentTimeMillis();
				if (currBackTime - lastBackTime <= 1000)
					finish();
				lastBackTime = currBackTime;
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private class MListAdapter extends ArrayAdapter<SingleContent> {
		private int resourceId;
		private RequestQueue queue;
		private ImageLoader imageLoader;

		public MListAdapter(Context context, int resource,
				List<SingleContent> objects) {
			super(context, resource, objects);
			resourceId = resource;
			queue = Volley.newRequestQueue(context);
			imageLoader = new ImageLoader(queue, new BitmapCache());
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final SingleContent item = getItem(position);
			View view;
			ViewHolder viewHolder;
			if (convertView == null) {
				view = LayoutInflater.from(getContext()).inflate(resourceId,
						null);
				viewHolder = new ViewHolder();
				viewHolder.id = (TextView) view.findViewById(R.id.id);
				viewHolder.userid = (TextView) view.findViewById(R.id.userid);
				viewHolder.img = (NetworkImageView) view
						.findViewById(R.id.image);
				viewHolder.now = (TextView) view.findViewById(R.id.time);
				viewHolder.content = (TextView) view.findViewById(R.id.content);
				viewHolder.replyCount = (TextView) view
						.findViewById(R.id.reply);
				view.setTag(viewHolder);
			} else {
				view = convertView;
				viewHolder = (ViewHolder) view.getTag();
			}

			viewHolder.id.setText(item.getId());
			viewHolder.userid.setText(item.getUserid());
			if (item.getAdmin())
				viewHolder.userid.setTextColor(Color.RED);
			if (item.getImg().equals(""))
				viewHolder.img.setVisibility(View.GONE);
			else {
				viewHolder.img.setVisibility(View.VISIBLE);
				viewHolder.img.setImageUrl(GetData.IMAGEAPI + "/thumb" + "/"
						+ item.getImg() + item.getExt(), imageLoader);
			}
			viewHolder.now.setText(item.getNow());
			viewHolder.content.setText(item.getContent());
			viewHolder.replyCount.setText(item.getReplyCount());
			
			view.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(MainActivity.this, ContentDetail.class);
					intent.putExtra("id", item.getId());
					startActivity(intent);
				}
			});

			viewHolder.img.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) { 
					listView.setEnabled(false);
					String url = GetData.IMAGEAPI + "/image" + "/"
							+ item.getImg() + item.getExt();
					new ImageAsync(MainActivity.this, url).execute(image);
				}

			});
			return view;
		}
	}

	private class ViewHolder {
		private TextView id;
		private NetworkImageView img;
		private TextView now;
		private TextView userid;
		private TextView content;
		private TextView replyCount;
	}
}
