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
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mahong.http.BitmapCache;
import com.mahong.http.GetData;
import com.mahong.http.ResponseListener;
import com.mahong.http.SingleContent;
import com.mahong.tool.ImageAsync;
import com.mahong.tool.MatrixImageView;

public class ContentDetail extends Activity{
	
	private static final int SHOWGET = 0;

	private PullToRefreshListView listView;
	private List<SingleContent> list = new ArrayList<SingleContent>();
	private MListAdapter adapter;
	private MatrixImageView image;
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

	private String id;
	private int currPage = 1;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SHOWGET:
				if (currPage == 1)
					list.clear();
				try {
					JSONArray jsonArray = new JSONArray("["+(String) msg.obj+"]");
					JSONObject jsonObject = jsonArray.getJSONObject(0);
					String id = jsonObject.getString("id");
					String img = jsonObject.getString("img");
					String ext = jsonObject.getString("ext");
					String now = jsonObject.getString("now");
					String userid = jsonObject.getString("userid");
					String content = jsonObject.getString("content");
					Boolean admin = jsonObject.getString("admin").equals(
							"0") ? false : true;
					String reply = jsonObject.getString("replys");

					SingleContent item = new SingleContent(id, img, ext,
							now, userid, content, null, admin);
					list.add(item);
					
					jsonArray = new JSONArray(reply);
					for (int i=0; i<jsonArray.length(); i++)
					{
						jsonObject = jsonArray.getJSONObject(i);
						id = jsonObject.getString("id");
						img = jsonObject.getString("img");
						ext = jsonObject.getString("ext");
						now = jsonObject.getString("now");
						userid = jsonObject.getString("userid");
						content = jsonObject.getString("content");
						admin = jsonObject.getString("admin").equals(
								"0") ? false : true;
						
						item = new SingleContent(id, img, ext,
								now, userid, content, null, admin);
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
		setContentView(R.layout.content_detail);

		Intent intent = getIntent();
		id = intent.getStringExtra("id");

		image = (MatrixImageView) findViewById(R.id.big_image);
		listView = (PullToRefreshListView) findViewById(R.id.list_view);
		listView.setMode(Mode.PULL_FROM_END);
		adapter = new MListAdapter(ContentDetail.this,
				R.layout.single_title_item, list);
		listView.setAdapter(adapter);

		listView.setOnRefreshListener(new OnRefreshListener<ListView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				// TODO Auto-generated method stub
				currPage++;
				Map<String, String> m = new HashMap<String, String>();
				m.put("id", id + "");
				m.put("page", currPage + "");
				GetData.httpLink(m, GetData.GETCONTENT, listener);
			}
			
		});

		Map<String, String> m = new HashMap<String, String>();
		m.put("id", id + "");
		m.put("page", currPage + "");
		GetData.httpLink(m, GetData.GETCONTENT, listener);
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
				viewHolder.refer = (TextView) view.findViewById(R.id.refer);
				viewHolder.content = (TextView) view.findViewById(R.id.content);
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
			viewHolder.refer.setText(item.getRefer());
			viewHolder.content.setText(item.getContent());

			viewHolder.img.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					String url = GetData.IMAGEAPI + "/image" + "/"
							+ item.getImg() + item.getExt();
					new ImageAsync(ContentDetail.this, url).execute(image);
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
		private TextView refer;
		private TextView content;
	}

}
