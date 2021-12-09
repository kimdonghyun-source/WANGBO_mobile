package kr.co.wangbo.wms.menu.popup;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import kr.co.wangbo.wms.R;
import kr.co.wangbo.wms.common.Utils;
import kr.co.wangbo.wms.menu.ship.ShipFragment;
import kr.co.wangbo.wms.model.ResultModel;
import kr.co.wangbo.wms.model.ShipModel;
import kr.co.wangbo.wms.model.ShipPopModel;
import kr.co.wangbo.wms.model.ShipReqModel;
import kr.co.wangbo.wms.network.ApiClientService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.WIFI_SERVICE;

public class LocationSearchPopup {

    Activity mActivity;
    Dialog dialog;
    DatePickerDialog.OnDateSetListener callbackMethod;
    //List<CustomerInfoModel.CustomerInfo> mList = null;
    List<ShipModel.Item> mList = null;
    Handler mHandler;
    ImageButton bt_search;

    TextView item_date;
    EditText tv_cst_nm = null;
    ShipReqModel mShipReqModel;
    List<ShipReqModel.Item> mShipReqList;

    ListAdapter mAdapter;
    String mac;

    public LocationSearchPopup(Activity activity, int title, Handler handler) {
        mActivity = activity;
        mHandler = handler;
        showPopUpDialog(activity, title);

    }



    public void hideDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();

        }
    }

    public boolean isShowDialog() {
        if (dialog != null && dialog.isShowing()) {
            return true;
        } else {
            return false;
        }
    }

    private void showPopUpDialog(Activity activity, int title) {
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        dialog.setContentView(R.layout.popup_ship_search);

        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        //팝업을 맨 위로 올려야 함.
        wlp.gravity = Gravity.CENTER;
        window.setAttributes(wlp);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        ImageView iv_title = dialog.findViewById(R.id.iv_title);
        iv_title.setBackgroundResource(title);

        //tv_cst_nm = dialog.findViewById(R.id.tv_cst_nm);
        item_date = dialog.findViewById(R.id.item_date);
        bt_search = dialog.findViewById(R.id.bt_search);
        ListView listView = dialog.findViewById(R.id.list);
        mAdapter = new ListAdapter();
        listView.setAdapter(mAdapter);



        item_date.setOnClickListener(onClickListener);
        bt_search.setOnClickListener(onClickListener);

        //mAdapter = new ListAdapter();
        //listView.setAdapter(mAdapter);

        int year1 = Integer.parseInt(yearFormat.format(currentTime));
        int month1 = Integer.parseInt(monthFormat.format(currentTime));
        int day1 = Integer.parseInt(dayFormat.format(currentTime));

        String formattedMonth = "" + month1;
        String formattedDayOfMonth = "" + day1;
        if (month1 < 10) {

            formattedMonth = "0" + month1;
        }
        if (day1 < 10) {
            formattedDayOfMonth = "0" + day1;
        }

        item_date.setText(year1 + "-" + formattedMonth + "-" + formattedDayOfMonth);

        this.InitializeListener();


        dialog.findViewById(R.id.bt_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideDialog();
            }
        });

        dialog.show();
    }//Close Show popup

    public void InitializeListener() {
        callbackMethod = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");

                int month = monthOfYear + 1;
                String formattedMonth = "" + month;
                String formattedDayOfMonth = "" + dayOfMonth;

                if (month < 10) {

                    formattedMonth = "0" + month;
                }
                if (dayOfMonth < 10) {

                    formattedDayOfMonth = "0" + dayOfMonth;
                }

                item_date.setText(year + "-" + formattedMonth + "-" + formattedDayOfMonth);

            }
        };
    }

    Date currentTime = Calendar.getInstance().getTime();
    SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
    SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
    SimpleDateFormat monthFormat = new SimpleDateFormat("MM", Locale.getDefault());



    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.item_date:
                    //String m_date = item_date.getText().toString().replace("-", "");
                    int c_year = Integer.parseInt(item_date.getText().toString().substring(0, 4));
                    int c_month = Integer.parseInt(item_date.getText().toString().substring(5, 7));
                    int c_day = Integer.parseInt(item_date.getText().toString().substring(8, 10));

                    DatePickerDialog dialog = new DatePickerDialog(mActivity, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT, callbackMethod, c_year, c_month - 1, c_day);
                    dialog.show();
                    break;

                case R.id.bt_search:
                    /*if (tv_cst_nm.getText().toString().equals("")){
                        Utils.Toast(mActivity, "거래처를 입력해주세요.");
                        return;
                    }else{

                    }*/
                    sp_pda_reqlist_popup();
                    break;
            }
        }
    };

    /**
     * 출고지시서 조회
     */
    private void sp_pda_reqlist_popup() {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);
        String m_date = item_date.getText().toString().replace("-", "");
        Call<ShipReqModel> call = service.sp_pda_reqlist("sp_pda_reqlist", m_date);

        call.enqueue(new Callback<ShipReqModel>() {
            @Override
            public void onResponse(Call<ShipReqModel> call, Response<ShipReqModel> response) {
                if (response.isSuccessful()) {
                    mShipReqModel = response.body();
                    ShipReqModel model = response.body();
                    //Utils.Log("model ==> :" + new Gson().toJson(model));
                    if (model != null) {
                        if (model.getFlag() == ResultModel.SUCCESS) {
                            mShipReqList = model.getItems();
                            mAdapter.notifyDataSetChanged();

                        } else {
                            Utils.Toast(mActivity, model.getMSG());
                        }
                    }
                } else {
                    Utils.LogLine(response.message());
                    Utils.Toast(mActivity, response.code() + " : " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ShipReqModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mActivity, mActivity.getString(R.string.error_network));
            }
        });
    }//Close

    class ListAdapter extends BaseAdapter {
        LayoutInflater mInflater;

        public ListAdapter() {
            mInflater = LayoutInflater.from(mActivity);
        }

        @Override
        public int getCount() {
            if (mShipReqList == null) {
                return 0;
            }
            return mShipReqList.size();
        }


        @Override
        public ShipReqModel.Item getItem(int position){
            return mShipReqList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = convertView;
            final ViewHolder holder;
            if (v == null) {
                holder = new ViewHolder();
                v = mInflater.inflate(R.layout.cell_req_list, null);
                v.setTag(holder);

                holder.tv_no = v.findViewById(R.id.tv_no);
                holder.tv_cst_name = v.findViewById(R.id.tv_cst_name);
                holder.tv_date = v.findViewById(R.id.tv_date);
            } else {
                holder = (ViewHolder) v.getTag();
            }

            final ShipReqModel.Item data = mShipReqList.get(position);
            holder.tv_no.setText(Integer.toString(position + 1));
            holder.tv_cst_name.setText(data.getCst_name());
            holder.tv_date.setText(data.getArr_date());

            holder.tv_cst_name.setSelected(true);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Message msg = mHandler.obtainMessage();
                    msg.what = 1;
                    msg.obj = data;
                    mHandler.sendMessage(msg);
                    hideDialog();
                }
            });

            return v;
        }

        class ViewHolder {
            TextView tv_no;
            TextView tv_cst_name;
            TextView tv_date;
        }
    }//Close Adapter


}//Close popup
