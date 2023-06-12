package com.atguigu.common.constant;

/**
 * @Author:luosheng
 * @Date:2023-05-11 10:42
 * @Description:
 */
public class ProductConstant {
    public enum AttrEnm {
        ATTR_TYPE_BASE(1, "基本属性"), ATTR_TYPE_SALE(0, "销售属性");
        private int code;
        private String msg;

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }

        AttrEnm(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

    }

    public enum StatusEnum {
        //上架状态[0 - 下架，1 - 上架]
        NEW_SPU(0, "新建"), SPU_UP(1, "商品上架"), SPU_DOWN(2, "商品下架");
        private int code;
        private String msg;

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }

        StatusEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

    }
}
