package com.mahong.tool;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * @ClassName: MatrixImageView
 * @Description: 带放大、缩小、移动效果的ImageView
 * @author LinJ
 * @date 2015-1-7 上午11:15:07
 * 
 */
public class MatrixImageView extends ImageView {
	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();
	/** 位图对象 */
	private Bitmap bitmap = null;
	/** 屏幕的分辨率 */
	private DisplayMetrics dm;

	/** 最小缩放比例 */
	float minScaleR = 1.0f;

	/** 最大缩放比例 */
	static final float MAX_SCALE = 15f;

	/** 初始状态 */
	static final int NONE = 0;
	/** 拖动 */
	static final int DRAG = 1;
	/** 缩放 */
	static final int ZOOM = 2;

	/** 当前模式 */
	int mode = NONE;

	/** 存储float类型的x，y值，就是你点下的坐标的X和Y */
	PointF prev = new PointF();
	PointF mid = new PointF();
	float dist = 1f;
	private GestureDetector mGestureDetector;

	public MatrixImageView(Context context) {
		super(context);
		mGestureDetector=new GestureDetector(getContext(), new GestureListener());
		
		setupView();
	}

	public MatrixImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setupView();
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		super.setImageBitmap(bm);
		bitmap = bm;
		fitScale();
		center();
		this.setImageMatrix(matrix);
	}

	public void setupView() {
		Context context = getContext();
		// 获取屏幕分辨率,需要根据分辨率来使用图片居中
		dm = context.getResources().getDisplayMetrics();
		// 设置ScaleType为ScaleType.MATRIX，这一步很重要
		this.setScaleType(ScaleType.MATRIX);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		/*switch (event.getAction() & MotionEvent.ACTION_MASK) {
		// 主点按下
		case MotionEvent.ACTION_DOWN:
			savedMatrix.set(matrix);
			prev.set(event.getX(), event.getY());
			mode = DRAG;
			break;
		// 副点按下
		case MotionEvent.ACTION_POINTER_DOWN:
			dist = spacing(event);
			// 如果连续两点距离大于10，则判定为多点模式
			if (spacing(event) > 10f) {
				savedMatrix.set(matrix);
				midPoint(mid, event);
				mode = ZOOM;
			}
			break;
		case MotionEvent.ACTION_UP: {
			break;
		}
		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;
			// savedMatrix.set(matrix);
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode == DRAG) {
				matrix.set(savedMatrix);
				matrix.postTranslate(event.getX() - prev.x,
						event.getY() - prev.y);
			} else if (mode == ZOOM) {
				float newDist = spacing(event);
				if (newDist > 10f) {
					matrix.set(savedMatrix);
					float tScale = newDist / dist;
					matrix.postScale(tScale, tScale, mid.x, mid.y);
				}
			}
			break;
		}
		MatrixImageView.this.setImageMatrix(matrix);
		CheckView();*/
		mGestureDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}
	
	/**
	 * 横向、纵向居中
	 */
	protected void center() {
		Matrix m = new Matrix();
		m.set(matrix);
		RectF rect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
		m.mapRect(rect);

		float height = rect.height();
		float width = rect.width();

		float deltaX = 0, deltaY = 0;

		// 图片小于屏幕大小，则居中显示。大于屏幕，上方留空则往上移，下方留空则往下移
		int screenHeight = dm.heightPixels;
		if (height < screenHeight) {
			deltaY = (screenHeight - height) / 2 - rect.top;
		} else if (rect.top > 0) {
			deltaY = -rect.top;
		} else if (rect.bottom < screenHeight) {
			deltaY = this.getHeight() - rect.bottom;
		}

		int screenWidth = dm.widthPixels;
		if (width < screenWidth) {
			deltaX = (screenWidth - width) / 2 - rect.left;
		} else if (rect.left > 0) {
			deltaX = -rect.left;
		} else if (rect.right < screenWidth) {
			deltaX = screenWidth - rect.right;
		}
		matrix.postTranslate(deltaX, deltaY);
	}

	protected void fitScale() {
		Matrix m = new Matrix();
		m.set(matrix);
		RectF rect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
		m.mapRect(rect);

		float height = rect.height();
		float width = rect.width();
		float hs = 1f, ws = 1f, ss = 1f;
		if (height > dm.heightPixels)
			hs = dm.heightPixels / height;
		if (width > dm.widthPixels)
			ws = dm.widthPixels / width;

		if (height * ws <= dm.heightPixels)
			ss = ws;
		if (width * hs <= dm.widthPixels)
			ss = hs;

		minScaleR = ss;
		matrix.setScale(ss, ss);
	}

	/**
	 * 限制最大最小缩放比例，自动居中
	 */
	private void CheckView() {
		float p[] = new float[9];
		matrix.getValues(p);
		if (mode == ZOOM) {
			if (p[0] < minScaleR) {
				// Log.d("", "当前缩放级别:"+p[0]+",最小缩放级别:"+minScaleR);
				matrix.setScale(minScaleR, minScaleR);
			}
			if (p[0] > MAX_SCALE) {
				// Log.d("", "当前缩放级别:"+p[0]+",最大缩放级别:"+MAX_SCALE);
				matrix.set(savedMatrix);
			}
		}
		center();
	}

	/**
	 * 两点的距离
	 */
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	/**
	 * 两点的中点
	 */
	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	private void onDoubleClick() {
		float p[] = new float[9];
		matrix.getValues(p);
		if (Math.abs(p[0] - minScaleR) <= 0.001)
			matrix.setScale(MAX_SCALE, MAX_SCALE);
		else
			matrix.setScale(minScaleR, minScaleR);
		center();
		mode = NONE;
	}

	private class GestureListener extends SimpleOnGestureListener {
		public GestureListener()
		{
		}
		
		@Override
        public boolean onDown(MotionEvent e) {
            //捕获Down事件
            return true;
        }
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            //触发双击事件
            onDoubleClick();
            return true;
        }
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            // TODO Auto-generated method stub
            return super.onSingleTapUp(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            // TODO Auto-generated method stub
            super.onLongPress(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                float distanceX, float distanceY) {
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY) {
            // TODO Auto-generated method stub

            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public void onShowPress(MotionEvent e) {
            // TODO Auto-generated method stub
            super.onShowPress(e);
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            // TODO Auto-generated method stub
            return super.onDoubleTapEvent(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            // TODO Auto-generated method stub
            return super.onSingleTapConfirmed(e);
        }

	}
}