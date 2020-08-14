package com.fxk.example;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Movie;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeView;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.ViewfinderView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;

public class ScanActivity extends AppCompatActivity {

    //세로모드 위한 변수 선언
    private IntentIntegrator qrScan;

    private QRDTO qrDTO;

    private TextView apiOutPut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        //가로모드
        //new IntentIntegrator(this).initiateScan();

        //세로모드
        qrScan = new IntentIntegrator(this);
        qrScan.setOrientationLocked(false); // default가 세로모드인데 휴대폰 방향에 따라 가로, 세로로 자동 변경됩니다.

        //default str : Place a barcode inside eth viewfinder rectangle to scan iT
        qrScan.setPrompt("FXK QR Scan Please!");

        qrScan.setBeepEnabled(false); //바코드 인식시 소리

        //qrScan.setCaptureActivity(QReaderActivity.class); //activity_qr_reader.xml 커스텀뷰 사용할 경우 주석 해제

        qrScan.initiateScan();



    }




    //QR SCAN 후 결과 받는 메소드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
                // fail todo
            } else {
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                //result.getContents() -> Scanned: {"name" : "xxxx", "address" : "xxx" , ...} json 형식의 QR을 SCAN할 경우

                // success todo , JSON 데이터 파싱 후 API 호출
                
                //SCAN DATA에 필수값이 포함되지 않으면 QR처리 X
                if(result.getContents().indexOf("ip") == -1
                        || result.getContents().indexOf("name") == -1
                        || result.getContents().indexOf("address") == -1){
                    return;
                }

                //QR DATA JSON 파싱
                qrDTO = jsonParsing(result.getContents());

                //API 호출
                // 위젯에 대한 참조.
                apiOutPut = (TextView) findViewById(R.id.apiOutPut);

                // URL 설정.
                String url = "https://recipes4dev.tistory.com/145";
                url += "?name="+qrDTO.getName()
                        +"&address="+qrDTO.getAddress()
                        +"&ip="+qrDTO.getIp();

                // AsyncTask를 통해 HttpURLConnection 수행.
                NetworkTask networkTask = new NetworkTask(url, null);
                networkTask.execute();


                //테스트용 코드로 주석필요 , JSON확인용 , HTTP 결과 받을 경우 onPostExecute 함수 apiOutPut.setText 주석 해제
                apiOutPut.setText("1) QR JSON DATA : "+result.getContents() + "\n\n2) API 호출된 URL : " +url);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    //JSON 파싱 메소드
    private QRDTO jsonParsing(String json)
    {
        try{
            JSONObject jsonObject = new JSONObject(json);

            QRDTO qrDTO = new QRDTO();

            qrDTO.setName(jsonObject.getString("name"));
            qrDTO.setAddress(jsonObject.getString("address"));
            qrDTO.setIp(jsonObject.getString("ip"));
            //... 파라미터 추가

            return qrDTO;

        }catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    //HTTP 통신
    public class NetworkTask extends AsyncTask<Void, Void, String> {

        private String url;
        private ContentValues values;

        public NetworkTask(String url, ContentValues values) {

            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... params) {

            String result; // 요청 결과를 저장할 변수.
            RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
            result = requestHttpURLConnection.request(url, values); // 해당 URL로 부터 결과물을 얻어온다.

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            //doInBackground()로 부터 리턴된 값이 onPostExecute()의 매개변수로 넘어오므로 s를 출력한다.

            //HTTP 결과를 받아서 텍스트 세팅할경우 주석해제
            //apiOutPut.setText(s);
        }
    }



}
