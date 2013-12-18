package com.example.getcoordinate;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class BallSurFaceView extends SurfaceView implements
		SurfaceHolder.Callback {

	private int mCircleX = 0;
	private int mCircleY = 0;
	private SurfaceHolder mHolder;
	private Canvas mCanvas = null;
	private Paint mPaint = null;
	private float mBallSize = 10.0f;

	public BallSurFaceView(Context context) {
		super(context);
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setFixedSize(getWidth(), getHeight());

	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO 自動生成されたメソッド・スタブ

	}

	public void surfaceCreated(SurfaceHolder holder) {
		// TODO 自動生成されたメソッド・スタブ
		mPaint = new Paint();
		mPaint.setColor(Color.GREEN);
		mPaint.setAntiAlias(true);

		mCircleX = getWidth() / 2;
		mCircleY = getHeight() / 2;
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO 自動生成されたメソッド・スタブ

	}

	public void drawBall(int x, int y) {
		mCircleX -= x * 2;
		mCircleY += y * 2;
		if (mCircleX > getWidth()) {
			mCircleX = 10;
			newBall();
		}
		if (mCircleY > getHeight()) {
			mCircleY = 10;
			newBall();
		}
		if (mCircleX < 0) {
			mCircleX = getWidth() - 10;
			newBall();
		}
		if (mCircleY < 0) {
			mCircleX = getHeight() - 10;
			newBall();
		}
		try {
			mCanvas = getHolder().lockCanvas();
			mCanvas.drawColor(Color.LTGRAY);
			mCanvas.drawCircle(mCircleX, mCircleY, mBallSize, mPaint);
			getHolder().unlockCanvasAndPost(mCanvas);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void newBall() {
		mBallSize *= 1.1f;
	}
}
