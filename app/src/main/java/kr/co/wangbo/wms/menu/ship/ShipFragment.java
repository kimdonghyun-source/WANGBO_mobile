package kr.co.wangbo.wms.menu.ship;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.honeywell.aidc.BarcodeReadEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kr.co.wangbo.wms.GlobalApplication;
import kr.co.wangbo.wms.R;
import kr.co.wangbo.wms.common.SharedData;
import kr.co.wangbo.wms.common.Utils;
import kr.co.wangbo.wms.custom.CommonFragment;
import kr.co.wangbo.wms.honeywell.AidcReader;
import kr.co.wangbo.wms.menu.main.MainActivity;
import kr.co.wangbo.wms.menu.popup.LocationSearchPopup;
import kr.co.wangbo.wms.menu.popup.OneBtnPopup;
import kr.co.wangbo.wms.menu.popup.ShipListPopup;
import kr.co.wangbo.wms.menu.popup.TwoBtnPopup;
import kr.co.wangbo.wms.model.ResultModel;
import kr.co.wangbo.wms.model.ShipDetailModel;
import kr.co.wangbo.wms.model.ShipModel;
import kr.co.wangbo.wms.model.ShipReqModel;
import kr.co.wangbo.wms.model.UserInfoModel;
import kr.co.wangbo.wms.network.ApiClientService;
import okhttp3.internal.Util;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.WIFI_SERVICE;

public class ShipFragment extends CommonFragment {

    ImageButton bt_search, bt_next;
    LocationSearchPopup mLocationSearchPopup;
    ShipListPopup mShipListPopup;

    ShipModel mShipModel;
    List<ShipModel.Item> mShipList;
    ListAdapter mAdapter;

    ShipDetailModel mShipDetailModel;
    List<ShipDetailModel.Item> mShipDetailList;

    String barcodeScan, beg_barcode;
    List<String> mIncode;
    EditText et_search, et_barcode;
    TextView tv_buy_code, tv_cst_name;
    RecyclerView Ship_list;
    OneBtnPopup mOneBtnPopup;
    TwoBtnPopup mTwoBtnPopup;
    String req_date, req_code, mac;

    ShipReqModel.Item mShipReqModel;
    List<ShipReqModel.Item> mShipReqList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        mIncode = new ArrayList<>();

        WifiManager mng = (WifiManager) mContext.getSystemService(WIFI_SERVICE);
        WifiInfo info = mng.getConnectionInfo();
        mac = info.getMacAddress();

    }//Close onCreate

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.frag_ship, container, false);

        bt_search = v.findViewById(R.id.bt_search);
        et_search = v.findViewById(R.id.et_search);
        tv_buy_code = v.findViewById(R.id.tv_buy_code);
        tv_cst_name = v.findViewById(R.id.tv_cst_name);
        Ship_list = v.findViewById(R.id.Ship_list);
        et_barcode = v.findViewById(R.id.et_barcode);
        bt_next = v.findViewById(R.id.bt_next);

        Ship_list.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mAdapter = new ListAdapter(getActivity());
        Ship_list.setAdapter(mAdapter);

        bt_search.setOnClickListener(onClickListener);
        bt_next.setOnClickListener(onClickListener);

        /*getCurrentMacAddress();
        Log.d("맥주소???", getCurrentMacAddress());*/

        return v;

    }//Close onCreateView

    /*public String getCurrentMacAddress(){
        String macAddress="";
        boolean bIsWifiOff=false;

        WifiManager wfManager = (WifiManager)mContext.getSystemService(mContext.WIFI_SERVICE);
        if(!wfManager.isWifiEnabled()){
            wfManager.setWifiEnabled(true);
            bIsWifiOff = true;
        }

        WifiInfo wfInfo = wfManager.getConnectionInfo();
        macAddress = wfInfo.getMacAddress();

        if(bIsWifiOff){
            wfManager.setWifiEnabled(false);
            bIsWifiOff = false;
        }

        return macAddress;
    }*/


    @Override
    public void onResume() {
        super.onResume();
        AidcReader.getInstance().claim(mContext);
        AidcReader.getInstance().setListenerHandler(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {

                    BarcodeReadEvent event = (BarcodeReadEvent) msg.obj;
                    String barcode = event.getBarcodeData();
                    barcodeScan = barcode;


                    /*if (mIncode != null) {
                        if (mIncode.contains(barcode)) {
                            Utils.Toast(mContext, "동일한 바코드를 스캔하였습니다.");
                            return;
                        }
                    }*/

                    if (barcode.indexOf("-") == 1) {
                        /*if (mAdapter.itemsList != null) {
                            Utils.Toast(mContext, "작업중인 출하지시서가 있습니다.");
                            return;
                        }*/
                        requesItmlist(barcodeScan);
                        et_search.setText(barcodeScan);

                    } else {
                        if (mAdapter.itemsList == null) {
                            Utils.Toast(mContext, "출하지시서를 먼저 스캔해주세요.");
                            return;
                        } else {
                            requesDetailScanYN();
                            et_barcode.setText(barcodeScan);
                        }

                    }


                }
            }
        });

    }//Close onResume

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.bt_search:
                    mLocationSearchPopup = new LocationSearchPopup(getActivity(), R.drawable.popup_title_ship_search, new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == 1) {
                                ShipReqModel.Item item = (ShipReqModel.Item) msg.obj;
                                mShipReqModel = item;
                                et_search.setText(mShipReqModel.getReq_code());
                                req_code = mShipReqModel.getReq_code();
                                requesItmlist(req_code);
                                /*WhModel.Item order = (WhModel.Item) msg.obj;
                                et_from2.setText("[" + order.getWh_code() + "] " + order.getWh_name());
                                wh_code2 = order.getWh_code();*/
                            }
                        }
                    });
                    break;

                case R.id.bt_next:
                    if (mAdapter != null) {

                        for (int i = 0; i < mAdapter.getItemCount(); i++) {
                            if (mShipList.get(i).getP_qty_r() == mShipList.get(i).getChg_qty_r()) {
                                if (mAdapter.getItemCount() > 1){
                                    mTwoBtnPopup = new TwoBtnPopup(getActivity(), "'" + tv_cst_name.getText().toString() + "'으로 '" + mShipModel.getItems().get(0).getItm_name()
                                            + "'외" + mAdapter.getItemCount() + "개 품목 출하처리를 하시겠습니까?"
                                            , R.drawable.popup_title_alert, new Handler() {
                                        @Override
                                        public void handleMessage(Message msg) {
                                            if (msg.what == 1) {
                                                requestShipSave();
                                                mTwoBtnPopup.hideDialog();

                                            }
                                        }
                                    });
                                }else{
                                    mTwoBtnPopup = new TwoBtnPopup(getActivity(), "'" + tv_cst_name.getText().toString() + "'으로 '" + mShipModel.getItems().get(0).getItm_name()
                                            + " 품목 출하처리를 하시겠습니까?"
                                            , R.drawable.popup_title_alert, new Handler() {
                                        @Override
                                        public void handleMessage(Message msg) {
                                            if (msg.what == 1) {
                                                requestShipSave();
                                                mTwoBtnPopup.hideDialog();

                                            }
                                        }
                                    });
                                }

                            } else {
                                mTwoBtnPopup = new TwoBtnPopup(getActivity(), "지시수량과 출하수량이 다릅니다.\n출하처리를 하시겠습니까?", R.drawable.popup_title_alert, new Handler() {
                                    @Override
                                    public void handleMessage(Message msg) {
                                        if (msg.what == 1) {
                                            requestShipSave();
                                            mTwoBtnPopup.hideDialog();
                                        }
                                    }
                                });
                            }
                        }


                    }
            }

        }
    };//Close onClick



    /**
     * 제품출하 출하지지서 조회
     */
    private void requesItmlist(String req_code) {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<ShipModel> call = service.pda_req_itmlist("sp_pda_req_itmlist", mac, req_code);

        call.enqueue(new Callback<ShipModel>() {
            @Override
            public void onResponse(Call<ShipModel> call, Response<ShipModel> response) {
                if (response.isSuccessful()) {
                    mShipModel = response.body();
                    ShipModel model = response.body();
                    //Utils.Log("model ==> :" + new Gson().toJson(model));
                    if (model != null) {
                        if (model.getFlag() == ResultModel.SUCCESS) {
                            if (mAdapter.itemsList != null) {
                                mAdapter.itemsList.clear();
                                mAdapter.clearData();
                            }

                            for (int i = 0; i < model.getItems().size(); i++) {
                                ShipModel.Item item = (ShipModel.Item) model.getItems().get(i);
                                mAdapter.addData(item);
                            }

                            mShipList = model.getItems();
                            tv_cst_name.setText(mShipModel.getItems().get(0).getCst_name());
                            tv_buy_code.setText(mShipModel.getItems().get(0).getBuy_cst_code());
                            req_date = mShipModel.getItems().get(0).getReq_date();
                            mAdapter.notifyDataSetChanged();

                        } else {
                            Utils.Toast(mContext, model.getMSG());
                        }
                    }
                } else {
                    Utils.LogLine(response.message());
                    Utils.Toast(mContext, response.code() + " : " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ShipModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
            }
        });
    }//Close

    /**
     * 제품출하 현품표스캔 결과값
     */
    private void requesDetailScanYN() {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<ShipModel> call = service.pda_req_Scan("sp_pda_stopchk", mac, et_search.getText().toString(), barcodeScan);

        call.enqueue(new Callback<ShipModel>() {
            @Override
            public void onResponse(Call<ShipModel> call, Response<ShipModel> response) {
                if (response.isSuccessful()) {
                    mShipModel = response.body();
                    ShipModel model = response.body();
                    //Utils.Log("model ==> :" + new Gson().toJson(model));
                    if (model != null) {
                        if (model.getFlag() == ResultModel.SUCCESS) {
                            mIncode.add(barcodeScan);
                            requesItmDetailList();

                        } else {
                            mOneBtnPopup = new OneBtnPopup(getActivity(), model.getMSG(), R.drawable.popup_title_alert, new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    if (msg.what == 1) {
                                        mOneBtnPopup.hideDialog();
                                    }
                                }
                            });
                            //Utils.Toast(mContext, model.getMSG());
                        }
                    }
                } else {
                    Utils.LogLine(response.message());
                    Utils.Toast(mContext, response.code() + " : " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ShipModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
            }
        });
    }//Close

    /**
     * 제품출하 현품표스캔 후 출하지시서 재호출
     */
    private void requesItmDetailList() {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);

        Call<ShipModel> call = service.pda_req_itmDetailList("sp_pda_itm", mac, et_search.getText().toString());

        call.enqueue(new Callback<ShipModel>() {
            @Override
            public void onResponse(Call<ShipModel> call, Response<ShipModel> response) {
                if (response.isSuccessful()) {
                    mShipModel = response.body();
                    ShipModel model = response.body();
                    //Utils.Log("model ==> :" + new Gson().toJson(model));
                    if (model != null) {
                        if (model.getFlag() == ResultModel.SUCCESS) {
                            mAdapter.itemsList.clear();
                            mAdapter.clearData();
                            mAdapter.notifyDataSetChanged();

                            for (int i = 0; i < model.getItems().size(); i++) {
                                ShipModel.Item item = (ShipModel.Item) model.getItems().get(i);
                                mAdapter.addData(item);
                            }

                            mShipList = model.getItems();
                            mAdapter.notifyDataSetChanged();

                        } else {
                            Utils.Toast(mContext, model.getMSG());
                        }
                    }
                } else {
                    Utils.LogLine(response.message());
                    Utils.Toast(mContext, response.code() + " : " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ShipModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
            }
        });
    }//Close

    //제품출하등록
    private void requestShipSave() {
        ApiClientService service = ApiClientService.retrofit.create(ApiClientService.class);
        String userID = (String) SharedData.getSharedData(mContext, SharedData.UserValue.USER_ID.name(), "");
        Call<ShipModel> call = service.requestShipSave("sp_sal_ship_save",mac, req_date, userID, et_search.getText().toString());

        call.enqueue(new Callback<ShipModel>() {
            @Override
            public void onResponse(Call<ShipModel> call, Response<ShipModel> response) {
                if (response.isSuccessful()) {
                    ShipModel model = response.body();
                    if (model != null) {
                        //mShipScanModel.getFlag().equals("OK")
                        if (model.getFlag() == ResultModel.SUCCESS) {
                            mOneBtnPopup = new OneBtnPopup(getActivity(), "출고처리 등록되었습니다.",
                                    R.drawable.popup_title_alert, new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    if (msg.what == 1) {
                                        mOneBtnPopup.hideDialog();
                                    }
                                }
                            });

                            et_barcode.setText("");
                            et_search.setText("");
                            mAdapter.itemsList.clear();
                            mAdapter.clearData();
                            tv_buy_code.setText("");
                            tv_cst_name.setText("");
                            mShipModel = null;
                            mShipList = null;
                            mIncode = null;
                        } else {
                            Utils.Toast(mContext, model.getMSG());
                        }
                    }
                } else {
                    Utils.LogLine(response.message());
                    Utils.Toast(mContext, response.code() + " : " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ShipModel> call, Throwable t) {
                Utils.LogLine(t.getMessage());
                Utils.Toast(mContext, getString(R.string.error_network));
            }
        });
    }//Close 제품출하등록

    //출하시지서 어댑터
    public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

        List<ShipModel.Item> itemsList;
        Activity mActivity;
        Handler mHandler = null;

        public ListAdapter(Activity context) {
            mActivity = context;
        }

        public void setData(List<ShipModel.Item> list) {
            itemsList = list;
        }

        public void clearData() {
            if (itemsList != null) itemsList.clear();
        }

        public void setRetHandler(Handler h) {
            this.mHandler = h;
        }

        public List<ShipModel.Item> getData() {
            return itemsList;
        }

        public void addData(ShipModel.Item item) {
            if (itemsList == null) itemsList = new ArrayList<>();
            itemsList.add(item);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, final int z) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cell_ship_list, viewGroup, false);
            ViewHolder holder = new ViewHolder(v);
            return holder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            final ShipModel.Item item = itemsList.get(position);

            holder.tv_itm_code.setText(item.getItm_code() + "    " + item.getItm_name());
            //holder.tv_itm_name.setText(item.getItm_name());
            holder.tv_itm_size.setText(item.getItm_size());
            holder.tv_tot_qty.setText(Integer.toString(item.getChg_qty_r()) + "(R/L)");
            if (item.getP_qty_r() == 0) {
                holder.tv_qty.setText(Integer.toString(item.getP_qty_r()));
            } else {
                holder.tv_qty.setText(Integer.toString(item.getP_qty_r()) + "(R/L)");
            }
            if (item.getP_qty_r() > 0) {
            holder.bt_search.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mShipListPopup = new ShipListPopup(getActivity(), mShipModel.getItems().get(0).getItm_name(), et_search.getText().toString(), itemsList.get(position).getItm_code(), mac, new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            if (msg.what == 1) {
                                requesItmDetailList();
                                mAdapter.notifyDataSetChanged();

                            }

                        }
                    });
                }
            });
            }

        }


        @Override
        public int getItemCount() {
            return (null == itemsList ? 0 : itemsList.size());
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView tv_itm_code;
            //TextView tv_itm_name;
            TextView tv_itm_size;
            TextView tv_tot_qty;
            TextView tv_qty;
            ImageButton bt_search;

            public ViewHolder(View view) {
                super(view);

                tv_itm_code = view.findViewById(R.id.tv_itm_code);
                //tv_itm_name = view.findViewById(R.id.tv_itm_name);
                tv_itm_size = view.findViewById(R.id.tv_itm_size);
                tv_tot_qty = view.findViewById(R.id.tv_tot_qty);
                tv_qty = view.findViewById(R.id.tv_qty);
                bt_search = view.findViewById(R.id.bt_search);

                /*view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Message msg = new Message();
                        msg.obj = itemsList.get(getAdapterPosition());
                        msg.what= getAdapterPosition();
                        mHandler.sendMessage(msg);
                    }
                });*/
            }
        }
    }//Close Adapter

}
