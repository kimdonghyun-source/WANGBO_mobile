package kr.co.wangbo.wms.menu.popup;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
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
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import kr.co.wangbo.wms.R;
import kr.co.wangbo.wms.common.Utils;
import kr.co.wangbo.wms.menu.main.MainActivity;
import kr.co.wangbo.wms.model.ResultModel;
import kr.co.wangbo.wms.model.ShipDetailModel;
import kr.co.wangbo.wms.model.ShipModel;
import kr.co.wangbo.wms.model.ShipPopModel;
import kr.co.wangbo.wms.network.ApiClientService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShipListPopup {

    Activity mActivity;
    Dialog dialog;
    List<ShipPopModel.Item> mShipPopList = null;
    ShipPopModel mShipPopModel = null;
    Handler mHandler;
    ListView listView;
    TextView tv_req_code, tv_itm_code;
    TwoBtnPopup mTwoBtnPopup;
    ListAdapter mAdapter;
    String MAC;

    public ShipListPopup(Activity activity, String title, String bar, String itm_code, String mac, Handler handler) {
        mActivity = activity;
        mHandler = handler;
        showPopUpDialog(activity, title, bar, itm_code, mac);

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

    private void showPopUpDialog(Activity activity, String title, String bar, String itm_code, String mac) {
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        dialog.setContentView(R.layout.popup_ship_list);

        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        //팝업을 맨 위로 올려야 함.
        wlp.gravity = Gravity.CENTER;
        window.setAttributes(wlp);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        TextView iv_title = dialog.findViewById(R.id.iv_title);
        tv_req_code = dialog.findViewById(R.id.tv_req_code);
        tv_itm_code = dialog.findViewById(R.id.tv_itm_code);
        iv_title.setText(title);
        tv_req_code.setText(bar);
        tv_itm_code.setText(itm_code);
        MAC = mac;

        listView = dialog.findViewById(R.id.list);
        mAdapter = new ListAdapter();
        listView.setAdapter(mAdapter);


        dialog.findViewById(R.id.bt_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = mHandler.obtainMessage();
                msg.what = 1;
                msg.obj = "확인";
                mHandler.sendMessage(msg);
                hideDialog();
            }
        });


        DetailList();
        dialog.show();
    }//Close Show popup


    /**
     * 현품표조회 리스트
     */
    private void DetailList() {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<ShipPopModel> call = service.sp_pda_barlist("sp_pda_barlist", MAC, tv_req_code.getText().toString(), tv_itm_code.getText().toString());

        call.enqueue(new Callback<ShipPopModel>() {
            @Override
            public void onResponse(Call<ShipPopModel> call, Response<ShipPopModel> response) {
                if (response.isSuccessful()) {
                    mShipPopModel = response.body();
                    ShipPopModel model = response.body();
                    //Utils.Log("model ==> :" + new Gson().toJson(model));
                    if (model != null) {
                        if (model.getFlag() == ResultModel.SUCCESS) {
                            mShipPopList = model.getItems();
                            mAdapter.notifyDataSetChanged();
                        } else {
                            Utils.Toast(mActivity, model.getMSG());
                            //mAdapter.clearData();
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                } else {
                    Utils.LogLine(response.message());
                    Utils.Toast(mActivity, response.code() + " : " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ShipPopModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mActivity, mActivity.getString(R.string.error_network));
            }
        });
    }//Close

    /**
     * 현품표조회 삭제
     */
    private void DetailDelete(String barcode) {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<ShipPopModel> call = service.sp_pda_pack_del("sp_pda_pack_del", MAC, tv_req_code.getText().toString(), barcode);

        call.enqueue(new Callback<ShipPopModel>() {
            @Override
            public void onResponse(Call<ShipPopModel> call, Response<ShipPopModel> response) {
                if (response.isSuccessful()) {
                    mShipPopModel = response.body();
                    ShipPopModel model = response.body();
                    //Utils.Log("model ==> :" + new Gson().toJson(model));
                    if (model != null) {
                        if (model.getFlag() == ResultModel.SUCCESS) {
                            mShipPopList = model.getItems();
                            //mAdapter.notifyDataSetChanged();
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
            public void onFailure(Call<ShipPopModel> call, Throwable t) {
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
            if (mShipPopList == null) {
                return 0;
            }
            return mShipPopList.size();
        }


        @Override
        public ShipPopModel.Item getItem(int position) {
            return mShipPopList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void clearData() {
            mShipPopList.clear();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = convertView;
            final ViewHolder holder;
            if (v == null) {
                holder = new ViewHolder();
                v = mInflater.inflate(R.layout.cell_ship_popup, null);
                v.setTag(holder);

                holder.tv_itm_name = v.findViewById(R.id.tv_itm_name);
                holder.tv_qty = v.findViewById(R.id.tv_qty);
                holder.bt_search = v.findViewById(R.id.bt_search);



            } else {
                holder = (ViewHolder) v.getTag();
            }

            final ShipPopModel.Item data = mShipPopList.get(position);
            holder.tv_itm_name.setText(data.getPack_barcode());
            holder.tv_qty.setText(Integer.toString(data.getP_qty_r()));

            holder.bt_search.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTwoBtnPopup = new TwoBtnPopup(mActivity, data.getPack_barcode() + "  " + data.getP_qty_r() + "(R/L) 를 삭제하시겠습니까?", R.drawable.popup_title_list_del, new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == 1) {
                                DetailDelete(data.getPack_barcode());
                                mShipPopList.remove(position);
                                mAdapter.notifyDataSetChanged();
                                //DetailList();
                                mTwoBtnPopup.hideDialog();
                            }
                        }
                    });

                }
            });

            return v;
        }

        class ViewHolder {
            TextView tv_itm_name;
            TextView tv_qty;
            ImageButton bt_search;
        }
    }//Close Adapter


}//Close popup
