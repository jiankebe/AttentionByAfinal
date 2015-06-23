package com.baidu.vsfinance.util.attention;

/**
*
*  @author jianchuanli
* 
*
*/


import java.util.Map;


//根据需求，利用接口添加字段
public interface AttentionInterface {
	public Map<String, Class<?>> createSyncModel();
	public Map<String, Class<?>> createDbModel();
}
