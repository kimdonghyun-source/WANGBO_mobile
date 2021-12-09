package kr.co.wangbo.wms.model;

import java.util.List;

public class ShipModel extends ResultModel {

    List<ShipModel.Item> items;

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public class Item extends ResultModel{
        //일자
        String req_date;
        //출하처명
        String cst_name;
        //운송회사명
        String buy_cst_code;
        //품목코드
        String itm_code;
        //품목명
        String itm_name;
        //품목사이즈
        String itm_size;
        //수량
        int chg_qty_r;
        //스캔수량
        int p_qty_r;

        public String getReq_date() {
            return req_date;
        }

        public void setReq_date(String req_date) {
            this.req_date = req_date;
        }

        public String getCst_name() {
            return cst_name;
        }

        public void setCst_name(String cst_name) {
            this.cst_name = cst_name;
        }

        public String getBuy_cst_code() {
            return buy_cst_code;
        }

        public void setBuy_cst_code(String buy_cst_code) {
            this.buy_cst_code = buy_cst_code;
        }

        public String getItm_code() {
            return itm_code;
        }

        public void setItm_code(String itm_code) {
            this.itm_code = itm_code;
        }

        public String getItm_name() {
            return itm_name;
        }

        public void setItm_name(String itm_name) {
            this.itm_name = itm_name;
        }

        public String getItm_size() {
            return itm_size;
        }

        public void setItm_size(String itm_size) {
            this.itm_size = itm_size;
        }

        public int getChg_qty_r() {
            return chg_qty_r;
        }

        public void setChg_qty_r(int chg_qty_r) {
            this.chg_qty_r = chg_qty_r;
        }

        public int getP_qty_r() {
            return p_qty_r;
        }

        public void setP_qty_r(int p_qty_r) {
            this.p_qty_r = p_qty_r;
        }
    }
}
