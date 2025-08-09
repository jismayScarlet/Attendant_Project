package com.example.attendant_project.MemoBuddy.View;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.attendant_project.MemoBuddy.AIOption;
import com.example.attendant_project.MemoBuddy.ViewModel.EventViewModel;
import com.example.attendant_project.MemoBuddy.model.EventItem;
import com.example.attendant_project.R;

import java.util.ArrayList;
import java.util.Locale;

public class MemoBuddyFragment extends Fragment {
    private EventViewModel viewModel;
    private EventAdapter adapter;
    private ArrayAdapter<String> categoryAdapter;
    private Button btn_textInput, btn_voiceInnput, btn_photoInput;
    private Button btn_CM_add,btn_CM_edit,btn_CM_delete;
    private ImageButton btn_categoryManager;
    private EditText et_inputBox = null;
    private Spinner spi_categoryListView;
//    EventItem eventItem = new EventItem();
    private EventItem eventItem;
    private EditText ett_changeCategory;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.memobuddy_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view,Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        viewModel = new ViewModelProvider(this).get(EventViewModel.class);
        RecyclerView recyclerView = view.findViewById(R.id.rv_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EventAdapter(requireContext(),viewModel);
        recyclerView.setAdapter(adapter);
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(()-> {
                    adapter.itemFramgSize(recyclerView.getWidth(), recyclerView.getHeight());
                });

        spi_categoryListView = view.findViewById(R.id.spi_categoryList);
        spi_categoryListView.setSelection(0);//預設定
        spinnerViewUpdate();
        btn_textInput = view.findViewById(R.id.btn_text_input);
        btn_voiceInnput = view.findViewById(R.id.btn_voiceInputInBuddy);
        btn_photoInput = view.findViewById(R.id.btn_photo_input);
        btn_categoryManager = view.findViewById(R.id.btn_categoryManager);


        btn_textInput.setOnClickListener(v -> {
            textInputFunction();
        });

        btn_voiceInnput.setOnClickListener(v -> {
           voiceRecognizer();
        });

        btn_photoInput.setOnClickListener(v -> {
            CameraDialogFragment cameraFragment = new CameraDialogFragment();
            cameraFragment.show(getParentFragmentManager(), "camera_dialog");
        });



        spi_categoryListView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 取得選中的值
                String selected = (String) parent.getItemAtPosition(position);
                adapter.categoryItemChanged(selected);
                Log.d("Spinner", "選擇第 " + position + " 項：" + selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 使用者未選擇任何項目時觸發
                spi_categoryListView.setSelection(0);
                adapter.categoryItemChanged("common");
                Log.d("Spinner","default:" + spi_categoryListView.getSelectedItem().toString() );
            }
        });

        btn_categoryManager.setOnClickListener(v -> {
             new AlertDialog.Builder(requireContext())
                    .setTitle("類別")
                    .setMessage("下方輸入內容可新增或修改目前類別名稱")
                    .setView(categoryBundle())//做成函數，否則view在表層class建立會造成資源為釋放而重複呼叫view的錯誤，不呼叫removeView的解法
                    .setNegativeButton("關閉視窗",((dialog, which) -> {
                        dialog.cancel();
                    }))
                    .show();
        });


        viewModel.getAllEvents().observe(getViewLifecycleOwner(), events -> {
            adapter.setData(events, spi_categoryListView.getSelectedItem().toString());
        });

    }

    public void voiceRecognizer(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "請開始說話");

        try {
            startActivityForResult(intent,02);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(requireContext().getApplicationContext(), "您的裝置不支援語音輸入", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 02 && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                AIOption.sendMessage(results.get(0),callbackResult -> {
                    String selectedCategory = spi_categoryListView.getSelectedItem().toString();
                    String categoryConfirm;
                    if(selectedCategory.equals("All")){
                        categoryConfirm = "common";
                    }else{
                        categoryConfirm = selectedCategory;
                    }
                    eventItem = new EventItem
                            (results.get(0) + " " + callbackResult,
                                    categoryConfirm);
                    viewModel.addEvent(eventItem);
                });
                Log.i("voice recobnizer",results.get(0));
                // 處理語音辨識結果
            }
        }
    }

   private View categoryBundle(){
//       LayoutInflater categoryManager = LayoutInflater.from(requireContext());
//       View categoryManagerLayout = categoryManager.inflate(R.layout.category_manager_layout,null);
       View categoryManagerLayout = LayoutInflater.from(requireContext()).inflate(R.layout.category_manager_layout,null);
       btn_CM_edit = categoryManagerLayout.findViewById(R.id.btn_editCategory);
       btn_CM_delete = categoryManagerLayout.findViewById(R.id.btn_deleteCatgory);
       btn_CM_add = categoryManagerLayout.findViewById(R.id.btn_addCategory);
       ett_changeCategory = categoryManagerLayout.findViewById(R.id.ett_categoryText);

       btn_CM_add.setOnClickListener(v -> {
           String content = ett_changeCategory.getText().toString();
           Boolean exist = false;
           for(String i:adapter.getCategoryList()){
               if(content.equals(i)){
                   exist = true;
               }
           }
           if(TextUtils.isEmpty(content)){
               Toast.makeText(requireContext(),"輸入不能空白",Toast.LENGTH_SHORT).show();
           }else if(exist == true){
               Toast.makeText(requireContext(),"已經有相同的類別",Toast.LENGTH_SHORT).show();
           }else{
               adapter.insertCategory(requireContext(),content);
               categoryAdapter.notifyDataSetChanged();
               spinnerViewUpdate();
               Toast.makeText(requireContext(),"類別 " + content + "已新增",Toast.LENGTH_SHORT).show();
           }
       });

       btn_CM_edit.setOnClickListener(v -> {
           new AlertDialog.Builder(requireContext())
                   .setMessage("確定要更改選擇的類別名稱嗎?")
                   .setNegativeButton("取消",(dialog, which) -> {
                       dialog.cancel();
                   })
                   .setPositiveButton("確定",(dialog, which) -> {
                       adapter.editCategory(requireContext(),ett_changeCategory.getText().toString(),spi_categoryListView.getSelectedItem().toString());
                       spinnerViewUpdate();
                   })
                   .show();
       });

       btn_CM_delete.setOnClickListener(v -> {
           new AlertDialog.Builder(requireContext())
                   .setMessage("會連帶刪除類別下的資料，\n確定要刪除選擇的類別嗎?")
                   .setPositiveButton("確定",(dialog, which) -> {
                       adapter.deletCategory(requireContext(),spi_categoryListView.getSelectedItem().toString());
                       spinnerViewUpdate();
                   })
                   .setNegativeButton("取消",(dialog, which) -> {
                       dialog.cancel();
                   })
                   .show();
           Toast.makeText(requireContext(),"類別 " + spi_categoryListView.getSelectedItem().toString() + "已刪除",Toast.LENGTH_SHORT).show();
       });
       return categoryManagerLayout;
   }

   private void spinnerViewUpdate(){
       categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item,adapter.getCategoryList());
       spi_categoryListView.setAdapter(categoryAdapter);
   }

   public void textInputFunction(){
       et_inputBox = new EditText(requireContext());
       new AlertDialog.Builder(requireContext())
               .setMessage("輸入儲存事項")
               .setView(et_inputBox)
               .setPositiveButton("儲存", (dialog, which) -> {
                   if (!TextUtils.isEmpty(et_inputBox.getText())) {
                       AIOption.sendMessage(et_inputBox.getText().toString(), callbackResult -> {
                           String selectedCategory = spi_categoryListView.getSelectedItem().toString();
                           String categoryConfirm;
                           if (selectedCategory.equals("All")) {
                               categoryConfirm = "common";
                           } else {
                               categoryConfirm = selectedCategory;
                           }
                           eventItem = new EventItem
                                   (et_inputBox.getText().toString() + " " + callbackResult,
                                           categoryConfirm);
                           Log.d("Input", eventItem.getCategory());
                           viewModel.addEvent(eventItem);
                       });
                   }
               })
               .setNegativeButton("取消", (dialog, which) -> {
                   ViewGroup parent = (ViewGroup) et_inputBox.getParent();
                   if (parent != null) {
                       parent.removeView(et_inputBox);
                   }
                   dialog.dismiss();
               })
               .show();
   }
}
