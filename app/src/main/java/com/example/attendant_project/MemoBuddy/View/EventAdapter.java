package com.example.attendant_project.MemoBuddy.View;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.attendant_project.MemoBuddy.ViewModel.EventViewModel;
import com.example.attendant_project.MemoBuddy.model.EventItem;
import com.example.attendant_project.R;
import com.example.attendant_project.time_task.PrefsManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<EventItem> eventList = new ArrayList<>();
    private final EventViewModel viewModel;
    private final List<String> categoryList = new ArrayList<>(Arrays.asList("All","common","picture"));
    private static List<String> categoryListExtra = new ArrayList<>();
    private List<String> categoryMix = new ArrayList<>();
    PrefsManager prefs = new PrefsManager();
    private final String categoryStorageKey = "category_storage";
    View onCreateView = null;
    private int parentWidth,parentHeight = 0;



    public EventAdapter(Context context,EventViewModel viewModel) {
        String[] categoryData = prefs.getString(context,categoryStorageKey,"nonStoredData").split("/");
        categoryListExtra = new ArrayList<>();
            for(int i=0;i<categoryData.length;i++){
                Log.d("categoryData",categoryData[i]);
                if(!categoryData[i].equals("nonStoredData")) {
                    categoryListExtra.add(categoryData[i]);
                }
            }
        this.viewModel = viewModel;

    }


    // ✅ setData：提供 LiveData 傳入新資料
    public void setData(List<EventItem> newList,String selectedCategory) {
        newList.sort((a, b) -> {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime aTime = datetimeCuter(a.getText());
            LocalDateTime bTime = datetimeCuter(b.getText());

            long aDiff = Duration.between(now, aTime).abs().toMillis();
            long bDiff = Duration.between(now, bTime).abs().toMillis();

            return Long.compare(aDiff, bDiff); // 差距小的排前面
        });
        eventList.clear();
        for(EventItem i:newList){
            i.checkVisible(selectedCategory);
            eventList.add(i);
            Log.d("eventList content check new",String.valueOf(i.isVisible()));
        }
//        this.eventList = newList;//用newList在這邊執行一次date sort

        notifyDataSetChanged(); // ❗效能不佳但簡單可靠，可用 DiffUtil 最佳化，通知所有訂閱
    }

    private void storeCategoryData(Context context,List<String> list){
        String cs=null;
        StringBuilder builder = new StringBuilder();
        for(String s:list){
            builder.append(s).append("/");
            cs = new String(builder);
            Log.d("debug",s + " " + cs );
        }
        prefs.setString(context, categoryStorageKey,cs);
    }

    @Override
    public int getItemViewType(int position) {
        EventItem item = eventList.get(position);
//        Log.d("getType",String.valueOf(item.isVisible()));
        if (item.isVisible()) {
            return 1; // 可見
        } else {
            return 0; // 不可見
        }
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        if (viewType == 0) {
            onCreateView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.picture_event_item, parent, false);
            ;
            onCreateView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            onCreateView.setVisibility(View.GONE);
        }else{

            onCreateView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.picture_event_item, parent, false);
            onCreateView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            onCreateView.setVisibility(View.VISIBLE);
        }

        Log.d("onCreateViewHolder", String.valueOf(onCreateView.getVisibility()));
        return new EventViewHolder(onCreateView);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventItem item = eventList.get(position);
        holder.bind(item, position);
//        Log.d("onBindViewHolder","work");
    }

    @Override
    public int getItemCount() {
        return eventList != null ? eventList.size() : 0;
    }


    class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tv_event;
        ImageButton btn_event_options;
        ImageView iv_eventImage;


        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_event = itemView.findViewById(R.id.tv_event);
            btn_event_options = itemView.findViewById(R.id.btn_event_options);
            iv_eventImage = itemView.findViewById(R.id.iv_eventImage);

            tv_event.setWidth(parentWidth - 100);
        }

        public void bind(EventItem item, int position) {
            tv_event.setText(item.getText());

            btn_event_options.setOnClickListener(v -> {
                viewModel.itemConfig(item,itemView.getContext());
            });

            if(!TextUtils.isEmpty(item.getImageUri())){
                iv_eventImage.setVisibility(View.VISIBLE);
                Uri imageUri = Uri.parse(item.getImageUri());
//                iv_eventImage.setImageDrawable(null);
//                iv_eventImage.setImageURI(imageUri);
//                iv_eventImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
//                onCreateView.requestLayout();
                Glide.with(itemView.getContext())
                        .load(imageUri)
                        .centerCrop()
                        .fitCenter()
                        .into(iv_eventImage);
                Log.i("Bind","Image already in.\n" + imageUri);
//                for (UriPermission perm : itemView.getContext().getContentResolver().getPersistedUriPermissions()) {
//                    Log.d("UriPermission", "URI: " + perm.getUri() + " | Read: " + perm.isReadPermission() + " | Write: " + perm.isWritePermission());
//                }
            }else{
                iv_eventImage.setVisibility(View.GONE);
            }
        }
    }

    private LocalDateTime datetimeCuter(String input){
        if(TextUtils.isEmpty(input)){
            input = "本輸入異常=空值";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String refrenceTime = LocalDateTime.now().format(formatter).toString();
        // 日期格式：YYYY-MM-DD
        Pattern datePattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
        Matcher dateMatcher = datePattern.matcher(input);
        String date = dateMatcher.find() ? dateMatcher.group() : refrenceTime;

        // 時間格式：HH:mm
        Pattern timePattern = Pattern.compile("\\d{2}:\\d{2}");
        Matcher timeMatcher = timePattern.matcher(input);
        String time = timeMatcher.find() ? timeMatcher.group() : "00:00";

        return LocalDateTime.parse(date + "T" + time); // e.g., 2025-07-08T14:30;
    }



    public List<String> getCategoryList(){
        categoryMix = new ArrayList<>(categoryList);
        categoryMix.addAll(categoryListExtra);
        return new ArrayList<>(categoryMix);
    }


    public void categoryItemChanged(String selectCategory){
        if(eventList != null) {
                List<EventItem> eventListProcess = new ArrayList<>();
                for (EventItem items : eventList) {
                    items.checkVisible(selectCategory);
                    eventListProcess.add(items);
                }
                eventList.clear();
                eventList = eventListProcess;
        }
        notifyDataSetChanged();
    }

    public void insertCategory(Context context,String category){
        categoryListExtra.add(category);
        storeCategoryData(context,categoryListExtra);
    }

    public void editCategory(Context context,String setCategory,String categoryEdited){
        int position = categoryListExtra.indexOf(categoryEdited);
        if(!TextUtils.isEmpty(categoryEdited)) {
            if (position != -1) {
                categoryListExtra.remove(position);
                categoryListExtra.add(setCategory);
                storeCategoryData(context,categoryListExtra);
            } else {
                Toast.makeText(context, "預設類別無法更改名稱", Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(context,"沒有輸入名稱",Toast.LENGTH_LONG).show();
        }
    }

    public void deletCategory(Context context,String categoryContent){
        int position = categoryListExtra.indexOf(categoryContent);
        if(position != -1) {
            categoryListExtra.remove(position);
            storeCategoryData(context,categoryListExtra);
            Iterator<EventItem> iterator = eventList.iterator();
            // 遍歷時刪除元素 可避免 ConcurrentModificationException
            // = 避免異步對儲存空間內容修改
            while (iterator.hasNext()) {
                EventItem item = iterator.next();
                if (item.getCategory().equals(categoryContent)) {
                    iterator.remove();
                    viewModel.deleteEventItem(item);
                    break;
                }
            }
        }else{
            Toast.makeText(context,"預設類別無法刪除",Toast.LENGTH_LONG).show();
            Log.e("category exchange","deleteCategory " + categoryListExtra.indexOf(categoryContent));
        }
    }

    public List<String> checkCategorys(){//未完成
        List<String> categorys = new ArrayList<>();
        return categorys;
    }

    public void itemFramgSize(int width,int height){
        parentWidth = width;
        parentHeight = height;
    }

}
