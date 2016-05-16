package com.example.hw3;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

public class MainActivity extends Activity {

    // 미리 변수들 선언
    String s,n="";// s = 메세지 내용, n = 전화번호
    WebView wv;
    Message message = new Message();// 자바스크립트와 데이터를 주고 받을 클래스를 만듬
    Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wv = (WebView)findViewById(R.id.wv);// layout의 WebView와 연결
        wv.getSettings().setJavaScriptEnabled(true);// 자바스크립트 사용을 허용해준다
        wv.addJavascriptInterface(message, "message");// 자바스크립트 인터페이스들이 쓰일 클래스를 알려줌
        wv.loadUrl("file:///android_asset/test.html");// 내가 만든 html 파일을 WebView로 불러온다
    }

    class Message {// 자바스크립트 인터페이스 메소드를 담은 클래스

        @JavascriptInterface
        public String numSetting(String num){ // html로부터 입력된 숫자들을 받고 이어붙여서
                                               // 다시 html로 보내서 화면에 이어붙여진 번호들을 보여준다
            n += num;// html로부터 받은 숫자 저장
            return n;// html 화면에 띄워주기 위해 다시 돌려줌
        }
        @JavascriptInterface
        public void getText(String data) { // 텍스트 메시지 받고 메세지 보내는 메소드 실행
            s = data;// html로 부터 입력된 텍스트를 받는다

            if (n.length()>0 && s.length()>0) {
                sendSMS(n, s);// 메세지 보내는 메소드 실행
            }
            // 번호가 9자리 이하로 쓰였거나, 메세지 내용이 입력되지 않았을 경우 경고 토스트 메시지를 띄워준다
            else if(n.length() < 9)
                Toast.makeText(MainActivity.this, "번호를 제대로 입력해 주세요", Toast.LENGTH_SHORT).show();
            else if(s.length() == 0)
                Toast.makeText(MainActivity.this, "메세지 내용을 입력해 주세요", Toast.LENGTH_SHORT).show();
            n = "";// html에서 submit버튼을 눌렀을 경우 화면에 쓰여진 번호를 초기화 시켜준다
        }
        @JavascriptInterface
        public String giveBackNum(){// html로부터 받은 전화번호를 다시 보내준다
            return n;
        }
        @JavascriptInterface
        public String clear(){// submit 버튼 눌렀을때 쓰여진 번호 초기화를 해줌
            n = "";
            return n;
        }
        public void sendSMS(String smsNumber, String smsText){// 실제로 메세지를 보내주는 메소드
            PendingIntent sentIntent = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent("SMS_SENT_ACTION"), 0);
            PendingIntent deliveredIntent = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent("SMS_DELIVERED_ACTION"), 0);

            /**
             * SMS가 발송될때 실행
             * When the SMS massage has been sent
             */
            registerReceiver(new BroadcastReceiver() {// 메시지를 보내고나서 일어날 수 있는 이벤트를 다뤄줌
                @Override
                public void onReceive(Context context, Intent intent) {
                    switch(getResultCode()){
                        case Activity.RESULT_OK:
                            // 전송 성공
                            Toast.makeText(mContext, "전송 완료", Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            // 전송 실패
                            Toast.makeText(mContext, "전송 실패", Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            // 서비스 지역 아님
                            Toast.makeText(mContext, "서비스 지역이 아닙니다", Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            // 무선 꺼짐
                            Toast.makeText(mContext, "무선(Radio)가 꺼져있습니다", Toast.LENGTH_SHORT).show();
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            // PDU 실패
                            Toast.makeText(mContext, "PDU Null", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }, new IntentFilter("SMS_SENT_ACTION"));

            SmsManager mSmsManager = SmsManager.getDefault();
            mSmsManager.sendTextMessage(smsNumber, null, smsText, sentIntent, deliveredIntent);
        }
    }
}
