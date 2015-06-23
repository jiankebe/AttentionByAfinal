package com.baidu.vsfinance.util.attention;
//同步上传model（可以根据需要添加相应的属性字段）
public class SyncItem {
		private String vendor_id;
		private String fund_code;
		private String is_delete;
		
		public String getVendor_id() {
			return vendor_id;
		}

		public void setVendor_id(String vendor_id) {
			this.vendor_id = vendor_id;
		}

		public String getFund_code() {
			return fund_code;
		}

		public void setFund_code(String fund_code) {
			this.fund_code = fund_code;
		}

		public String getIs_delete() {
			return is_delete;
		}

		public void setIs_delete(String is_delete) {
			this.is_delete = is_delete;
		}
	}