package com.example.getcoordinate;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import com.example.getcoordinate.MainActivity;

public class VCActivity extends MainActivity {
	private static final int REQUEST_CODE = 0;
	int mode = 0;
	int activitymode = 0;
	int voiceI = 0;
	String voiceJ = null;
	String textJ = null;
	String[][] voice = { { "162", "51", "75" }, { "166", "9", "75" },
			{ "167", "-33", "75" }, { "123", "49", "75" },
			{ "124", "7", "75" }, { "124", "-35", "75" } };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_vc);
		// ver3.0から追加されたStrictModeに対するエラーの対処
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
				.permitAll().build());
		Intent intent = getIntent();
		activitymode = intent.getIntExtra("activitymode", activitymode);
		if (activitymode == 0) {

			vcX = intent.getIntExtra("y", y);
			vcY = intent.getIntExtra("x", x);
			vcZ = intent.getIntExtra("z", z);
			activitymode = 2;
		} else if (activitymode == 1) {
			vcX = intent.getIntExtra("sendX", sendX);
			vcY = intent.getIntExtra("sendY", sendY);
			vcZ = intent.getIntExtra("sendZ", sendZ);
			activitymode = 2;
		}
		Button button3 = (Button) findViewById(R.id.button3); // 音声認識ボタン
		button3.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Vibrator vib=(Vibrator)getSystemService(VIBRATOR_SERVICE);
				vib.vibrate(50);
				try {
					// インテント作成
					Intent intent = new Intent(
							RecognizerIntent.ACTION_RECOGNIZE_SPEECH); // ACTION_WEB_SEARCH
					intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
							RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
					intent.putExtra(
							RecognizerIntent.EXTRA_PROMPT,
							"「上」「下」「左」「右」「前」「後ろ」「つかむ」「はなす」のうち一つを発声してください。\nまた、「1」「2」「3」「4」「5」「6」により特定の位置にボールを運ぶことができます。"); // 音声認識時に表示する文字
					intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1); // 返す候補を１つにする
					// インテント発行
					startActivityForResult(intent, REQUEST_CODE);
				} catch (ActivityNotFoundException e) {
					// このインテントに応答できるアクティビティがインストールされていない場合(エミュレータでの実行時に発生する例外処理)
					Toast.makeText(VCActivity.this,
							"ActivityNotFoundException", Toast.LENGTH_LONG)
							.show();
				}
			}
		});
		
		final Activity activity = this ;
		String[] arr = {"認識可能な言葉一覧"};
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,arr);
		final ListView List =(ListView)this.findViewById(R.id.listView1);
		List.setAdapter(adapter);
		List.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapter, View view, int arg2,
					long arg3) {
				TextView text1 = (TextView)view;
				
				
			}});
		
		

	}
	public boolean onTouchEvent(MotionEvent event) {
		return false;

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
		getMenuInflater().inflate(R.menu.activity_vc, menu);
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
		case R.id.menu5:
			Intent intent = new Intent();
			intent.setClassName("com.example.getcoordinate",
					"com.example.getcoordinate.MainActivity");
			intent.putExtra("vcX", vcX);
			intent.putExtra("vcY", vcY);
			intent.putExtra("vcZ", vcZ);
			intent.putExtra("activitymode", activitymode);
			startActivity(intent);
			break;
		case R.id.menu4:
			Intent intent2 = new Intent();
			intent2.setClassName("com.example.getcoordinate",
					"com.example.getcoordinate.SubActivity");
			intent2.putExtra("vcX", vcX);
			intent2.putExtra("vcY", vcY);
			intent2.putExtra("vcZ", vcZ);
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

	// ///////////音声認識/////////////
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// 自分が投げたインテントであれば応答する
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
			String resultsString = "";

			// 結果文字列リスト
			ArrayList<String> results = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

			for (int i = 0; i < results.size(); i++) {
				// ここでは、文字列が複数あった場合に結合しています
				resultsString += results.get(i);
			}

			// 以外の入力のみ結果を表示する
			String patternString = "[1-6]+"; // 1から6のどれかひとつ以上
			String patternString2 = "[前後右左上下]+";
			Pattern p = Pattern.compile(patternString);
			Pattern p2 = Pattern.compile(patternString2);
			Matcher m = p.matcher(resultsString);
			Matcher m2 = p2.matcher(resultsString);
			String[] hikaku = resultsString.split("");// 音声認識により入力された文字を位置文字ずつ配列に格納

			Toast.makeText(this, resultsString, Toast.LENGTH_LONG).show();
			if (m2.find() == true) {
				for (j = 0; j < hikaku.length; j++) {
					if ("上".equals(hikaku[j])) { // 「上」と入力があった場合
						Toast.makeText(this, "上に移動します。", Toast.LENGTH_LONG)
								.show();
						for (j = 0; j < hikaku.length; j++) {
							vcZ += 10;
							if (vcZ >= 160) {
								vcZ = 160;
							}
						}
					} else if ("下".equals(hikaku[j])) { // 「下」と入力があった場合
						Toast.makeText(this, "下に移動します。", Toast.LENGTH_LONG)
								.show();
						vcZ -= 10;
						if (vcZ <= 60) {
							vcZ = 60;
						}
					} else if ("左".equals(hikaku[j])) { // 「左」と入力があった場合
						Toast.makeText(this, "左に移動します。", Toast.LENGTH_LONG)
								.show();
						vcY += 10;
						if (vcY >= 56) {
							vcY = 56;
						}
					} else if ("右".equals(hikaku[j])) { // 「右」と入力があった場合
						Toast.makeText(this, "右に移動します。", Toast.LENGTH_LONG)
								.show();
						vcY -= 10;
						if (vcY <= -56) {
							vcY = -56;
						}
					} else if ("前".equals(hikaku[j])) { // 「前」と入力があった場合
						Toast.makeText(this, "前に移動します。", Toast.LENGTH_LONG)
								.show();
						vcX += 10;
						if (vcX >= 140) {
							vcX = 140;
						}
					} else if ("後".equals(hikaku[j])) { // 「後ろ」と入力があった場合
						Toast.makeText(this, "後に移動します。", Toast.LENGTH_LONG)
								.show();
						vcX -= 10;
						if (vcX <= 40) {
							vcX = 40;
						}
					}
				}
			} else if ("つかむ".equals(resultsString)) { // 「つかむ」と入力があった場合
				Toast.makeText(this, "アームを閉じます。", Toast.LENGTH_LONG).show();
				i = 7;
				connect(null, null, null);
				i = 0;
				mode = 1;
			} else if ("話す".equals(resultsString)) { // 「はなす」と入力があった場合
				Toast.makeText(this, "アームを開きます。", Toast.LENGTH_LONG).show();
				i = 8;
				connect(null, null, null);
				i = 0;
				mode = 1;
			} else if (m.find() == true) { // 1から6のどれかに移動する命令の時
				for (j = 0; j < hikaku.length; j++) {
					if (voiceI < 1) { // 一回目のみ処理を行い、それ以降は処理を行わない。ここの値を変えることで数字をいくつ認識するか変える事ができる。
						for (k = 1; k < 7; k++) { // 1から6までの中からどの数字と一致するか確認する
							voiceJ = Integer.toString(k); // 確認する数字の方をStringにする
							if (voiceJ.equals(hikaku[j])) { // 数字が等しいかどうか判定
								if (voiceI == 0) { // 一回目の数字の一致があった場合のみ処理を行い、それ以降はfor文を終了する
									textJ = hikaku[j]; // 画面に表示する文字の代入
									i = Integer.parseInt(hikaku[j]); // 移動する位置の番号をiに代入
									connect(null, null, null); // ダミーの座標情報（実際には送信しない）を使い、connectを呼び出す
									i = 0; // iを座標送信時の値に戻す
									voiceI++; // 数字を一回認識した
									x = Integer.parseInt(voice[k - 1][0]); // 現在地の統一
									y = Integer.parseInt(voice[k - 1][1]);
									z = Integer.parseInt(voice[k - 1][2]);
								} else { // 二回目以降は処理を行わない
									break;
								}
							}
						}
					} else { // 二回目以降は処理を行わない
						break;
					}
				}
				Toast.makeText(this, "現在地から" + textJ + "にボールを移動します。",
						Toast.LENGTH_LONG).show(); // どこにボールを移動するか画面に表示
				textJ = null; // 変数の初期化
				voiceI = 0; // 変数の初期化
				mode = 1; // 座標を送信しない
			} else {
				Toast.makeText(
						this,
						"もう一度お願いします。\n認識できる単語は「上」「下」「左」「右」「前」「後」「つかむ」「はなす」です。\nまた、「1」「2」「3」「4」「5」「6」により特定の位置にボールを運ぶことができます。",
						Toast.LENGTH_LONG).show();
				mode = 1;
			}
			if (mode == 0) { // 「上」「下」「前」「後ろ」「右」「左」と入力があった場合処理を行う
				String voiceX = Integer.toString(vcX);
				String voiceY = Integer.toString(vcY);
				String voiceZ = Integer.toString(vcZ);
				connect(voiceX, voiceY, voiceZ); // 座標の送信
				x = vcY;
				y = vcX;
				z = vcZ;
			}
			mode = 0; // 座標を送信する（初期値に戻す）
		}

		super.onActivityResult(requestCode, resultCode, data);
	}
}