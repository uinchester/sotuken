package com.example.getcoordinate;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.getcoordinate.MainActivity;

public class SubActivity extends MainActivity implements SensorEventListener {
	private SensorManager manager;
	private TextView values;
	private float acX, acY, acZ = 0; // 加速度センサーの値を受け取る変数
	int activitymode = 0;

	// private BallSurFaceView mSurFaceView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sub);
		// ver3.0から追加されたStrictModeに対するエラーの対処
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
				.permitAll().build());
		// mSurFaceView = new BallSurFaceView(this);
		// setContentView(mSurFaceView);
		Intent intent = getIntent();
		activitymode = intent.getIntExtra("activitymode", activitymode);
		if (activitymode == 0) {
			sendX = intent.getIntExtra("y", y);
			sendY = intent.getIntExtra("x", x);
			sendZ = intent.getIntExtra("z", z);
			activitymode = 1;
		} else if (activitymode == 2) {
			sendX = intent.getIntExtra("vcX", vcX);
			sendY = intent.getIntExtra("vcY", vcY);
			sendZ = intent.getIntExtra("vcZ", vcZ);
			activitymode = 1;
		}
		/* ボタンの実装 */
		// ボタンの取得
		Button button5 = (Button) findViewById(R.id.button5); // つかむ/はなすボタン
		Button button6 = (Button) findViewById(R.id.button6); // 加速度センサーのON/OFF切り替え用ボタン
		Button button7 = (Button) findViewById(R.id.button7);
		Button button8 = (Button) findViewById(R.id.button8);
		Button button9 = (Button) findViewById(R.id.button9);
		Button button10 = (Button) findViewById(R.id.button10);
		// 加速度センサー
		values = (TextView) findViewById(R.id.value_id);
		manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		// リスナーの登録
		button5.setOnClickListener(this);
		button6.setOnClickListener(this);
		button7.setOnClickListener(this);
		button8.setOnClickListener(this);
		button9.setOnClickListener(this);
		button10.setOnClickListener(this);
	}

	float previousDistance = 0;
	// タッチした時の時間
	long time = 0;
	float SacX = 0;
	float SacY = 0;
	int acmove = 1;
	float acflont = -3;
	float acback = 3;
	float acleft = -3;
	float acright = 3;
	int openclose = 0;

	public boolean onTouchEvent(MotionEvent event) {
		return false;

	}

	// ボタンが押された時のアクション
	public void onClick(View v) {
		Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		vib.vibrate(50);
		if (v.getId() == R.id.button5) {
			if (openclose == 0) {
				Toast toast = Toast.makeText(this, "アームを閉じます。",
						Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 46, 90);
				toast.show();
				i = 7;
				connect(null, null, null);
				i = 0;
				openclose = 1;
			} else {
				Toast toast = Toast.makeText(this, "アームを開きます。",
						Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 46, 90);
				toast.show();
				i = 8;
				connect(null, null, null);
				i = 0;
				openclose = 0;
			}

		} else if (v.getId() == R.id.button6) {
			if (acmove == 0) {
				Toast toast = Toast.makeText(this, "無効", Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.BOTTOM | Gravity.RIGHT, 28, 90);
				toast.show();
				acmove = 1;
			} else {
				Toast toast = Toast.makeText(this, "有効", Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.BOTTOM | Gravity.RIGHT, 28, 90);
				toast.show();
				acmove = 0;
			}

		} else if (v.getId() == R.id.button7) {
			acflont = acX;
			String flont = Float.toString(acflont);
			Toast.makeText(this, "前のしきい値を" + flont + "に変更しました。",
					Toast.LENGTH_SHORT).show();

		} else if (v.getId() == R.id.button8) {
			acleft = acY;
			String left = Float.toString(acleft);
			Toast.makeText(this, "左のしきい値を" + left + "に変更しました。",
					Toast.LENGTH_SHORT).show();
		} else if (v.getId() == R.id.button9) {

			acright = acY;
			String right = Float.toString(acright);
			Toast.makeText(this, "右のしきい値を" + right + "に変更しました。",
					Toast.LENGTH_SHORT).show();

		} else if (v.getId() == R.id.button10) {
			acback = acX;
			String back = Float.toString(acback);
			Toast.makeText(this, "後のしきい値を" + back + "に変更しました。",
					Toast.LENGTH_SHORT).show();

		}
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
		getMenuInflater().inflate(R.menu.activity_sub, menu);
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
					"com.example.getcoordinate.MainActivity");
			intent.putExtra("sendX", sendX);
			intent.putExtra("sendY", sendY);
			intent.putExtra("sendZ", sendZ);
			intent.putExtra("activitymode", activitymode);
			startActivity(intent);
			break;
		case R.id.menu7:
			Intent intent2 = new Intent();
			intent2.setClassName("com.example.getcoordinate",
					"com.example.getcoordinate.VCActivity");
			intent2.putExtra("sendX", sendX);
			intent2.putExtra("sendY", sendY);
			intent2.putExtra("sendZ", sendZ);
			intent2.putExtra("activitymode", activitymode);
			startActivity(intent2);
			break;
		case R.id.menu6:
			getAC();
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

	void getAC() {
		AlertDialog.Builder diag2 = new AlertDialog.Builder(this);
		diag2.setTitle("しきい値の初期化").setMessage("しきい値を初期化しますか？");
		diag2.setPositiveButton("キャンセル", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {

			}
		});
		diag2.setNegativeButton("初期化する", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				acflont = -3;
				acback = 3;
				acleft = -3;
				acright = 3;
				Toast.makeText(SubActivity.this, "しきい値をデフォルト(全て3.0)に戻しました。",
						Toast.LENGTH_SHORT).show();
			}

		});
		diag2.show();
	}

	// ///////////////加速度センサー//////////////////
	@Override
	protected void onResume() {
		// TODO 自動生成されたメソッド・スタブ
		super.onResume();
		// Listenerの登録
		List<Sensor> sensors = manager.getSensorList(Sensor.TYPE_ACCELEROMETER);
		if (sensors.size() > 0) {
			Sensor s = sensors.get(0);
			manager.registerListener(this, s, SensorManager.SENSOR_DELAY_GAME);
		}
	}

	@Override
	protected void onStop() {
		// TODO 自動生成されたメソッド・スタブ
		super.onStop();
		// Listenerの登録解除
		manager.unregisterListener(this);
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO 自動生成されたメソッド・スタブ
	}

	public void onSensorChanged(SensorEvent event) {
		// TODO 自動生成されたメソッド・スタブ
		if (acmove == 0) { // 加速度センサーによる操作の可・不可切り替え
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				acX = event.values[0]; // 加速度センサー値
				acY = event.values[1];
				acZ = event.values[2];
				// mSurFaceView.drawBall((int) acX, (int)acY);
				SacX = SacX + acX; // センサーの値を足していく
				SacY = SacY + acY;
				count++; // 何回センサーで値を取得したかカウントする
				// 現在の時間
				long nowAC = System.currentTimeMillis();
				// 座標を送った時間と現在の時間を比べる(ミリ秒)
				if ((nowAC - time) > 250) {
					SacX = (float) (SacX / count); // センサの値を取得した回数で合計を割る
					SacY = (float) (SacY / count);
					if (SacX < acflont) {
						if (z >= 140) {
							if (sendX >= 135) {
								sendX = 140;
							} else {
								sendX = sendX + 5;
							}
						} else {
							if (sendX >= 165) {
								sendX = 170;
							} else {
								sendX = sendX + 5;
							}
						}

					} else if (SacX >= acflont) {
						if (SacX <= acback) {
							sendX = sendX;
						}
						if (SacX > acback) {
							if (sendX <= 79) {
								sendX = 74;
							} else {
								sendX = sendX - 5;
							}
						}
					}

					if (SacY < acleft) {
						if (sendY >= 46) {
							sendY = 56;
						} else {
							sendY = sendY + 5;
						}
					} else if (SacY >= acleft) {
						if (SacY <= acright) {
							sendY = sendY;
						}
						if (SacY > acright) {
							if (sendY <= -46) {
								sendY = -56;
							} else {
								sendY = sendY - 5;
							}
						}
					}
					x = sendY; // 現在地情報を統一
					y = sendX;
					String X = Integer.toString(sendX);
					String Y = Integer.toString(sendY);
					String Z = Integer.toString(sendZ);

					connect(X, Y, Z); // 座標の送信
					String str = "加速度センサー値:" + "\nX軸:" + acX + "\nY軸:" + acY;
					values.setText(str); // 画面に表示

					SacX = 0; // 初期化
					SacY = 0;
					count = 0;
					time = nowAC;
				}
			}
		}
	}

}
