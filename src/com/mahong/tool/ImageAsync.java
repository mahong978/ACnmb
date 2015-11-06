package com.mahong.tool;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class ImageAsync extends AsyncTask<MatrixImageView, Integer, Bitmap>{
	private Context context;
	private ProgressDialog pd;
    private MatrixImageView image;
    private String url;
    
    public ImageAsync(Context context, String url) {
        this.context = context;
        this.url = url;
    }

    @Override
    protected void onPreExecute() {
        pd = new ProgressDialog(context);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setMessage("下载中....");
        pd.setCancelable(true);
        pd.setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface arg0) {
				// TODO Auto-generated method stub
				cancel(true);
			}
		});
        pd.show();
    }
    
    @Override
    protected Bitmap doInBackground(MatrixImageView... params) {
        this.image = params[0];
        Bitmap bitmap = null;
        HttpClient httpClient = new DefaultHttpClient();
        try {
            HttpGet httpPost = new HttpGet(url);
            HttpResponse httpResponse = httpClient.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = httpResponse.getEntity();
                final long size = entity.getContentLength();
                CountingInputStream cis = new CountingInputStream(
                        entity.getContent(), new ProgressListener() {

                            @Override
                            public void transferred(long transferedBytes) {
                                Log.i("FileDownLoadAsyncTask", "总字节数：" + size
                                        + " 已下载字节数：" + transferedBytes);
                                publishProgress((int) (100 * transferedBytes / size));
                            }
                        });
                
                bitmap = BitmapFactory.decodeStream(cis);  
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (ConnectTimeoutException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (httpClient != null && httpClient.getConnectionManager() != null) {
                httpClient.getConnectionManager().shutdown();
            }
        }
        return bitmap;
    }
    
    @Override
    protected void onProgressUpdate(Integer... progress) {
        pd.setProgress((int) (progress[0]));
    }

    @Override
    protected void onPostExecute(Bitmap bm) {
        pd.dismiss();
        if (bm != null) {
        	image.setVisibility(View.VISIBLE);
            image.setImageBitmap(bm);
        } else {
            Toast.makeText(context, "图片下载失败", Toast.LENGTH_SHORT).show();
        }
    }
    
    public byte[] toByteArray(InputStream instream, int contentLength)
            throws IOException {
        if (instream == null) {
            return null;
        }
        try {
            if (contentLength < 0) {
                contentLength = 4096;
            }
            final ByteArrayBuffer buffer = new ByteArrayBuffer(contentLength);
            final byte[] tmp = new byte[4096];
            int l;
            while ((l = instream.read(tmp)) != -1) {
                buffer.append(tmp, 0, l);
            }
            return buffer.toByteArray();
        } finally {
            instream.close();
        }
    }
}
