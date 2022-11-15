package kr.co.base64imagetest;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import kr.co.base64imagetest.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    private JSONObject jsonRecipe;

    // 임시 Json 데이터 가져오기
    public JSONObject getTempJson() {
        try {
            InputStream is = getAssets().open("test.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            return new JSONObject(new String(buffer, "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void initView() {
        binding.btnTempLoad.setOnClickListener(v -> {
            // Json 데이터에서 Base64 로 인코딩 된 이미지 가져오기
            String imageString = jsonRecipe.optString("image");
            // 이미지를 Bitmap 으로 디코딩
            Bitmap bitmap = getBitmapFromBase64(imageString);
            // ImageView 에 Bitmap 형식의 이미지를 설정
            binding.imageView.setImageBitmap(bitmap);
        });

        binding.btnLoad.setOnClickListener(v -> loadImage());
        binding.btnClear.setOnClickListener(v -> binding.imageView.setImageBitmap(null));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /*
            서버에 업로드 하기 전 JsonObject 를 만드는 방법과,
            서버에서 받은 Json 데이터를 파싱하는 방법을 보여드리기 위해 작업 한 코드입니다

            실제로는 서버에서 받은 Json 데이터를 파싱하여 이미지를 불러오는 방법을 사용하시면 됩니다
         */

        if (requestCode == 100 && resultCode == RESULT_OK) {
            try {
                // 이미지를 불러온 후 Bitmap 으로 변환
                InputStream is = getContentResolver().openInputStream(data.getData());
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                is.close();

                /*
                    서버에 업로드 하기 전 JsonObject 를 만드는 방법
                 */

                // Bitmap 을 blob 으로 변경 후 Base64 로 인코딩
                String imageString = encodeBitmapToBase64(bitmap);
                // JsonObject 에 이미지를 추가
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("image", imageString);

                /*
                    서버에서 받은 Json 데이터를 파싱하는 방법
                 */

                // Json 데이터에서 Base64 로 인코딩 된 이미지 가져오기
                String imageString2 = jsonObject.optString("image");
                // 이미지를 Bitmap 으로 디코딩
                Bitmap bitmap2 = getBitmapFromBase64(imageString2);
                // ImageView 에 Bitmap 형식의 이미지를 설정
                binding.imageView.setImageBitmap(bitmap2);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 임시 Json 데이터 가져오기
        jsonRecipe = getTempJson();

        // View 초기화
        initView();
    }


    // Base 64 로 인코딩
    private String encodeBase64(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    // Base 64 로 디코딩
    private byte[] decodeBase64(String str) {
        return Base64.decode(str, Base64.DEFAULT);
    }

    // Bitmap 을 blob 으로 변경 후 Base64 로 인코딩
    private String encodeBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return encodeBase64(byteArray);
    }

    // 로컬 이미지 불러오기
    private void loadImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 100);
    }

    // Base64 로 인코딩된 이미지를 Bitmap 으로 디코딩
    public Bitmap getBitmapFromBase64(String base64) {
        byte[] decodedString = decodeBase64(base64);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

}