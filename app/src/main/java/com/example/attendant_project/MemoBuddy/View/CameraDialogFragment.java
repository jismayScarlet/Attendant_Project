package com.example.attendant_project.MemoBuddy.View;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import com.example.attendant_project.MemoBuddy.AIOption;
import com.example.attendant_project.MemoBuddy.ViewModel.EventViewModel;
import com.example.attendant_project.MemoBuddy.model.EventItem;
import com.example.attendant_project.R;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CameraDialogFragment extends DialogFragment implements LifecycleOwner {
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ImageCapture imageCapture;
    private PreviewView previewView;
    private EditText cameraEt_ImageDetail;
    String fileName,imageParseResponse;
    private Button cameraBtn_capture;
    private CheckBox cameraCb_autoDeptail;
    private static final int REQUEST_CAMERA_PERMISSION = 1001;
    private static final int REQUEST_CREATE_IMAGE = 1002;
//    private Uri outputUri;

    public CameraDialogFragment(){}


//    public interface PhotoInfoCallBack {//將photo訊息用callback方式回傳
//        void getPhotoInformation();
//    }
//    private PhotoInfoCallBack photoInfoCallBack;
//    public CameraDialogFragment(PhotoInfoCallBack photoInfoCallBack){
//        this.photoInfoCallBack = photoInfoCallBack;
//    }
//
//    public void getCameraInfo(){
//        new Thread(() -> {
//            try {
//
//                // 回呼 callback
//                photoInfoCallBack.getPhotoInformation();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }).start();
//    }
    //---------------callback

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.camera_layout, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        previewView = view.findViewById(R.id.previewView);
        cameraBtn_capture = view.findViewById(R.id.cameraBtn_capture);
        cameraCb_autoDeptail = view.findViewById(R.id.cameraCb_autoDeptail);
        cameraEt_ImageDetail = view.findViewById(R.id.cameraEt_ImageDetail);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.d("camera permissions","request is not permission");
        }else if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            Log.d("camera permissions","request is permission");
        }//授權檢查
        startCamera(previewView);

        cameraBtn_capture.setOnClickListener(v -> {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedTime = " " + now.format(formatter);

            if(TextUtils.isEmpty(cameraEt_ImageDetail.getText())){
                fileName = formattedTime + "_" + System.currentTimeMillis() + ".jpg";
            }else{
                fileName = cameraEt_ImageDetail.getText().toString() + ".jpg";
            }

            takePhoto();
        });


    }



    public void startCamera(PreviewView previewView) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                imageCapture = new ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageCapture
                );
                Log.i("CameraX", "camera provider is complete");
            } catch (Exception e) {
                Log.e("CameraX", "Failed to start camera", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    public void takePhoto() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedTime = " " + now.format(formatter);

        if(imageCapture == null){
            startCamera(previewView);
        }

        if(cameraCb_autoDeptail.isChecked()){
            EventViewModel viewModel = new ViewModelProvider(this).get(EventViewModel.class);
            File photoFile = new File(getOutputDirectory(),
                    fileName);

            ImageCapture.OutputFileOptions outputOptions =
                    new ImageCapture.OutputFileOptions.Builder(photoFile).build();

            URI filePath = photoFile.toURI();
            android.net.Uri androidUri = android.net.Uri.parse(filePath.toString());
                imageCapture.takePicture(outputOptions,
                        ContextCompat.getMainExecutor(requireContext()),
                        new ImageCapture.OnImageSavedCallback() {
                            @Override
                            public void onImageSaved(ImageCapture.OutputFileResults output) {
                                //傳給GPT進行圖片判斷
                                AIOption.sendImage(requireContext(), androidUri,callbackResult -> {
                                    String categoryConfirm = "picture";
                                    EventItem eventItem = new EventItem(callbackResult + formattedTime,categoryConfirm,androidUri);
                                    Log.d("Input", eventItem.getCategory() + " " + androidUri.toString());
                                    viewModel.addEvent(eventItem);
                                });
                                Toast.makeText(requireContext(), "拍照完成 位置:\n 內部共用儲存空間\\Android\\data\\com.example.attendant_project\\files\\CameraXPhotos \n\n直接查看照片需連接電腦。\n自動註記需要花費時間，請燒等", Toast.LENGTH_SHORT).show();
                                Log.d("CameraX", "Photo saved: " + photoFile.getAbsolutePath());
                            }

                            @Override
                            public void onError(ImageCaptureException exc) {
                                Log.e("CameraX", "Photo capture failed: " + exc.getMessage(), exc);
                            }
                        });



        }else{
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
// 可選：指定儲存資料夾（Android 10+）
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/MemoBuddy");

            ImageCapture.OutputFileOptions outputOptions =
                    new ImageCapture.OutputFileOptions.Builder(
                            requireContext().getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues
                    ).build();

            Log.d("CameraX","\n contentValues " + contentValues);
                imageCapture.takePicture(outputOptions,
                        ContextCompat.getMainExecutor(requireContext()),
                        new ImageCapture.OnImageSavedCallback() {
                            @Override
                            public void onImageSaved(ImageCapture.OutputFileResults output) {
                                textInsert(output.getSavedUri(),formattedTime);
                                Toast.makeText(requireContext(), "拍照完成 位置:\n" + output.getSavedUri(), Toast.LENGTH_SHORT).show();
                                Log.d("CameraX", "Photo saved: " + output.getSavedUri());
                            }

                            @Override
                            public void onError(ImageCaptureException exc) {
                                Log.e("CameraX", "Photo capture failed: " + exc.getMessage(), exc);
                            }
                        });
        }
        Log.i("AutoDetail_chip",String.valueOf(cameraCb_autoDeptail.isChecked()));
    }

    private File getOutputDirectory() {
        File mediaDir = new File(requireContext().getExternalFilesDir(null), "CameraXPhotos");
        if (!mediaDir.exists()) mediaDir.mkdirs();
        return mediaDir;
    }

    private void textInsert(Uri imageUri,String dateTime){
        EditText et_cameraText = new EditText(requireContext());
        new AlertDialog.Builder(requireContext())
                .setMessage("輸入圖片註記")
                .setView(et_cameraText)
                .setPositiveButton("儲存", (dialog, which) -> {
                    String pictureDetail = null;
                    if (TextUtils.isEmpty(et_cameraText.getText())) {
                            pictureDetail = "picture captured" + dateTime;
                    }else{
                        pictureDetail = et_cameraText.getText().toString() + " ";
                    }
                    String categoryConfirm = "picture";
                    EventItem eventItem = new EventItem
                            (pictureDetail,
                                    categoryConfirm,imageUri);
                    Log.d("Input", eventItem.getCategory() + " " + imageUri.toString());
                    EventViewModel viewModel = new ViewModelProvider(this).get(EventViewModel.class);
                    viewModel.addEvent(eventItem);
                })
                .setNeutralButton("儲存並註記當前時間",((dialog, which) -> {
                    String pictureDetail = null;
                    if (TextUtils.isEmpty(et_cameraText.getText())) {
                        pictureDetail = "picture captured" + dateTime;
                    }else{
                        pictureDetail = et_cameraText.getText().toString() + " " + dateTime;
                    }
                    String categoryConfirm = "picture";
                    EventItem eventItem = new EventItem
                            (pictureDetail,
                                    categoryConfirm,imageUri);
                    Log.d("Input", eventItem.getCategory() + " " + imageUri.toString());
                    EventViewModel viewModel = new ViewModelProvider(this).get(EventViewModel.class);
                    viewModel.addEvent(eventItem);
                }))
                .setNegativeButton("取消", (dialog, which) -> {
                    ViewGroup parent = (ViewGroup) et_cameraText.getParent();
                    if (parent != null) {
                        parent.removeView(et_cameraText);
                    }
                    dialog.dismiss();
                })
                .show();

    }


}
