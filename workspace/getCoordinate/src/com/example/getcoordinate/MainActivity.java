package com.example.getcoordinate;

import java.io.*;
import java.net.*;

import com.example.getcoordinate.R.id;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	int i = 0;
	int j = 0;
	int k = 0;
	int x = 0;
	int y = 90;
	float tempX = 0;
	float tempY = 0;
	int tempZ = 160;
	int z = 160;
	float previousDistance = 0;
	// タッチした時の時間
	long time = 0;

	float temp2X = 0;
	float temp2Y = 0;
	float temp2Z = 0;
	float SacX = 0;
	float SacY = 0;

	int acmove = 1;
	float count = 0;
	int activitymode = 0;
	int vcX = 0;
	int vcY = 0;
	int vcZ = 0;
	int sendX = 0;
	int sendY = 0;
	int sendZ = 0;
	int openclose1 = 0;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// ver3.0から追加されたStrictModeに対するエラーの対処
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
				.permitAll().build());
		Intent intent = getIntent();
		activitymode = intent.getIntExtra("activitymode", activitymode);
		if (activitymode == 1) { // 位置情報を各アクティビティから受け取り統一する
			x = intent.getIntExtra("sendY", y);
			y = intent.getIntExtra("sendX", x);
			z = intent.getIntExtra("sendZ", z);
			activitymode = 0;
		} else if (activitymode == 2) {
			x = intent.getIntExtra("vcY", y);
			y = intent.getIntExtra("vcX", x);
			z = intent.getIntExtra("vcZ", z);
			activitymode = 0;
		}

		/* ボタンの実装 */
		// ボタンの取得
		Button button1 = (Button) findViewById(R.id.button1); // 上昇ボタン
		Button button2 = (Button) findViewById(R.id.button2); // 下降ボタン
		Button button11 = (Button) findViewById(R.id.button11);

		// リスナーの登録
		button1.setOnClickListener(this);
		button2.setOnClickListener(this);
		button11.setOnClickListener(this);

	}

	// スクリーンがタッチされたかどうかの判定
	public boolean onTouchEvent(MotionEvent event) {
		move(event);
		int mx = y, mz = 0;
		String[] strs = trans(mx, mz);
		String actionX = strs[0];
		String actionY = strs[1];
		String actionZ = strs[2];
		String actionXY = strs[3];
		// 座標の表示
		TextView coordinate = (TextView) findViewById(id.text);
		TextView coordinate2 = (TextView) findViewById(id.text2);
		coordinate.setText(actionXY);
		coordinate2.setText(actionZ);
		// 現在の時間
		long now = System.currentTimeMillis();
		// タッチした時間と現在の時間を比べる(ミリ秒)
		if ((now - time) > 400) {
			connect(actionX, actionY, actionZ);
			time = now;
		}
		return super.onTouchEvent(event);
	}

	// ボタンが押された時のアクション
	public void onClick(View v) {
		// X,Y,Zをconnect用に変換
		String superX = Integer.toString(x);
		String superY = Integer.toString(y);
		String superZ = Integer.toString(z);
		Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		vib.vibrate(50);
		// ボタン1(上昇)が押された場合
		 if (v.getId() == R.id.button1) {
			Toast toast = Toast.makeText(this, "上昇！", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.BOTTOM | Gravity.RIGHT, 75, 75);
			toast.show();

			tempZ += 10;
			if (tempZ > 160) {
				tempZ = 160;
			}
			superZ = Integer.toString(tempZ);
			z = tempZ;
			connect(superY, superX, superZ);
			// ボタン2(下降)が押された場合
		} else if (v.getId() == R.id.button2) {
			Toast toast = Toast.makeText(this, "下降！", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.BOTTOM | Gravity.RIGHT, 75, 75);
			toast.show();
			tempZ -= 10;
			if (tempZ < 60) {
				tempZ = 60;
			}
			superZ = Integer.toString(tempZ);
			z = tempZ;
			connect(superY, superX, superZ);
		}else if (v.getId() == R.id.button11) {
		
			if (openclose1 == 0) {
				Toast toast = Toast.makeText(this, "アームを閉じます。",
						Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 36, 70);
				toast.show();
				i = 7;
				connect(null, null, null);
				i = 0;
				openclose1 = 1;
			} else {
				Toast toast = Toast.makeText(this, "アームを開きます。",
						Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 36, 70);
				toast.show();
				i = 8;
				connect(null, null, null);
				i = 0;
				openclose1 = 0;
			}
		}
	}

	@SuppressLint("FloatMath")
	private void move(MotionEvent event) {
		// シングルタッチ
		// if(event.getPointerCount()==1) {
		// 座標の取得
		tempY = (int) ((event.getY() - 400));
		tempX = (int) ((event.getX() - 640));
		/* 8画素を，1座標とする */
		y = (int) ((tempY / 8) * (-1));
		x = (int) ((tempX / 8) * (-1));

		// yを正の値にする
		if (z != 140) {
			if (x > 50) {
				x = 50;
			} else if (x < -50) {
				x = -50;
			}
			if (y < 0) {
				y = 0;
			}
		}
		y += 90;
		// }
		// マルチタッチ
		/*
		 * else { int pointer1=event.getPointerId(0); int
		 * pointer2=event.getPointerId(1);
		 * 
		 * int pointer_index_1 = event.findPointerIndex( pointer1 ); int
		 * pointer_index_2 = event.findPointerIndex( pointer2 );
		 * 
		 * float tempX = event.getX(pointer_index_2) -
		 * event.getX(pointer_index_1); float tempY =
		 * event.getY(pointer_index_2) - event.getY(pointer_index_1); //
		 * ボタン間の距離を求める float distance = (float) Math.sqrt(tempX * tempX + tempY
		 * * tempY); switch ( event.getAction() ) { case
		 * MotionEvent.ACTION_MOVE: //距離を狭めた場合 if ( (tempZ > 95 )&&(tempZ < 165)
		 * ) { if( previousDistance > distance ) { tempZ += 5; } else if(
		 * previousDistance < distance ) { tempZ -= 5; } // zを140に固定する // tempZ
		 * = 140; z = tempZ; } } previousDistance = distance; }
		 */
	}

	String[] trans(int mx, int mz) {
		String[] strs = new String[4];
		if (i == 0) {
			// 座標系に合わせる
			strs[0] = String.valueOf(y);
			strs[1] = String.valueOf(x);
			strs[2] = String.valueOf(z);
			strs[3] = strs[1] + " * " + strs[0];
		} else {
			// 座標変換
			double exRadians = Math.toRadians(i);
			y = (int) ((y - 90) * Math.cos(exRadians) - (-(230 - z))
					* Math.sin(exRadians));
			mz = (int) ((mx - 90) * Math.sin(exRadians) + (-(230 - z))
					* Math.cos(exRadians));
			y = mx + y;
			mz = z + 70 - (-mz);
			strs[0] = String.valueOf(y);
			strs[1] = String.valueOf(x);
			strs[2] = String.valueOf(mz);
			strs[3] = strs[0] + " * " + strs[1];
		}
		return strs;
	}

	void connect(String actionX, String actionY, String actionZ) {
		// PCとの接続
		Socket socket = null;
		try {
			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(this);
			String ip = sp.getString("SaveString", null);
			int port = 10000;
			socket = new Socket(ip, port);
			// 座標の送信
			PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
			String action;
			if (i == -1) {
				action = "exit";
			} else if (i == 7) {
				action = "move1";// つかむ
			} else if (i == 8) {
				action = "move2";// はなす
			} else if (i == 9) {// 加速度測定
				action = actionX + ", " + actionY + ", " + actionZ + ", -180, "
						+ i;
			} else if (i == 0) {
				action = actionX + ", " + actionY + ", " + actionZ + ", -180, "
						+ i;
			} else {
				action = Integer.toString(i);// 1から6までの位置へ移動する命令を送る
			}
			pw.println(action);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (socket != null) {
			try {
				socket.close();
				socket = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		final EditText input = new EditText(this);
		final EditText input1 = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_NUMBER);
		switch (item.getItemId()) {
		case R.id.menu1:
			getTrans(input);
			break;
		case R.id.menu2:
			getIP(input1);
			break;
		case R.id.menu4:
			Intent intent = new Intent();
			intent.setClassName("com.example.getcoordinate",
					"com.example.getcoordinate.SubActivity");
			intent.putExtra("x", x);
			intent.putExtra("y", y);
			intent.putExtra("z", z);
			intent.putExtra("activitymode", activitymode);
			startActivity(intent);
			break;
		case R.id.menu7:
			Intent intent2 = new Intent();
			intent2.setClassName("com.example.getcoordinate",
					"com.example.getcoordinate.VCActivity");
			intent2.putExtra("x", x);
			intent2.putExtra("y", y);
			intent2.putExtra("z", z);
			intent2.putExtra("activitymode", activitymode);
			startActivity(intent2);
			break;
		case R.id.menu3:
			i = -1;
			connect(null, null, null);
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	void getIP(final EditText input1) {
		AlertDialog.Builder diag1 = new AlertDialog.Builder(this);
		diag1.setTitle("IPアドレスの設定");
		diag1.setView(input1);
		diag1.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				SharedPreferences sp = PreferenceManager
						.getDefaultSharedPreferences(getBaseContext());
				sp.edit().putString("SaveString", input1.getText().toString())
						.commit();
			}
		});
		diag1.show();
	}

	void getTrans(final EditText input) {
		AlertDialog.Builder diag = new AlertDialog.Builder(this);
		diag.setTitle("回転する角度");
		diag.setMessage("0から90の間で入力");
		diag.setView(input);
		diag.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				String stri = input.getText().toString();
				// 座標変換するための数値に変換
				i = Integer.parseInt(stri);
				// 範囲外が選択された場合ノーマルに戻る
				if ((i > 90) || (i < 0)) {
					i = 0;
				}
			}
		});
		diag.setNegativeButton("cancel", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {

			}
		});
		diag.show();
	}

}
