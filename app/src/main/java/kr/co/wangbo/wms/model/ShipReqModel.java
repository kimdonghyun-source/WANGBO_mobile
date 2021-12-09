package kr.co.wangbo.wms.model;

import java.util.List;

public class ShipReqModel extends ResultModel {

    List<ShipReqModel.Item> items;

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public class Item extends ResultModel{
        //req_code
        String req_code;
        //거래처명
        String cst_name;
        //도착일자
        String arr_date;

        public String getReq_code() {
            return req_code;
        }

        public void setReq_code(String req_code) {
            this.req_code = req_code;
        }

        public String getCst_name() {
            return cst_name;
        }

        public void setCst_name(String cst_name) {
            this.cst_name = cst_name;
        }

        public String getArr_date() {
            return arr_date;
        }

        public void setArr_date(String arr_date) {
            this.arr_date = arr_date;
        }
    }
}
