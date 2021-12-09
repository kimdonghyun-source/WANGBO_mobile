package kr.co.wangbo.wms.model;

import java.util.List;

public class ShipPopModel extends ResultModel {

    List<ShipPopModel.Item> items;

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public class Item extends ResultModel{
        //현품표번호
        String pack_barcode;
        //수량
        int p_qty_r;

        public String getPack_barcode() {
            return pack_barcode;
        }

        public void setPack_barcode(String pack_barcode) {
            this.pack_barcode = pack_barcode;
        }

        public int getP_qty_r() {
            return p_qty_r;
        }

        public void setP_qty_r(int p_qty_r) {
            this.p_qty_r = p_qty_r;
        }
    }
}
