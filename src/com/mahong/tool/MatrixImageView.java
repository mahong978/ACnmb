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
 * @Description: ���Ŵ���С���ƶ�Ч����ImageView
 * @author LinJ
 * @date 2015-1-7 ����11:15:07
 * 
 */
public class MatrixImageView extends ImageView {
	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();
	/** λͼ���� */
	private Bitmap bitmap = null;
	/** ��Ļ�ķֱ��� */
	private DisplayMetrics dm;

	/** ��С���ű��� */
	float minScaleR = 1.0f;

	/** ������ű��� */
	static final float MAX_SCALE = 15f;

	/** ��ʼ״̬ */
	static final int NONE = 0;
	/** �϶� */
	static final int DRAG = 1;
	/** ���� */
	static final int ZOOM = 2;

	/** ��ǰģʽ */
	int mode = NONE;

	/** �洢float���͵�x��yֵ����������µ������X��Y */
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
		// ��ȡ��Ļ�ֱ���,��Ҫ���ݷֱ�����ʹ��ͼƬ����
		dm = context.getResources().getDisplayMetrics();
		// ����ScaleTypeΪScaleType.MATRIX����һ������Ҫ
		this.setScaleType(ScaleType.MATRIX);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		/*switch (event.getAction() & MotionEvent.ACTION_MASK) {
		// ���㰴��
		case MotionEvent.ACTION_DOWN:
			savedMatrix.set(matrix);
			prev.set(event.getX(), event.getY());
			mode = DRAG;
			break;
		// ���㰴��
		case MotionEvent.ACTION_POINTER_DOWN:
			dist = spacing(event);
			// �����������������10�����ж�Ϊ���ģʽ
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
	 * �����������
	 */
	protected void center() {
		Matrix m = new Matrix();
		m.set(matrix);
		RectF rect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
		m.mapRect(rect);

		float height = rect.height();
		float width = rect.width();

		float deltaX = 0, deltaY = 0;

		// ͼƬС����Ļ��С���������ʾ��������Ļ���Ϸ������������ƣ��·�������������
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
	 * ���������С���ű������Զ�����
	 */
	private void CheckView() {
		float p[] = new float[9];
		matrix.getValues(p);
		if (mode == ZOOM) {
			if (p[0] < minScaleR) {
				// Log.d("", "��ǰ���ż���:"+p[0]+",��С���ż���:"+minScaleR);
				matrix.setScale(minScaleR, minScaleR);
			}
			if (p[0] > MAX_SCALE) {
				// Log.d("", "��ǰ���ż���:"+p[0]+",������ż���:"+MAX_SCALE);
				matrix.set(savedMatrix);
			}
		}
		center();
	}

	/**
	 * ����ľ���
	 */
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	/**
	 * ������е�
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
            //����Down�¼�
            return true;
        }
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            //����˫���¼�
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