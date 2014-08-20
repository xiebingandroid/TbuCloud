package com.tallbigup.android.cloud;

import java.util.Map;

public interface RankInfoCallback {
	void result(final boolean success,final Map<Integer,Map<String,String>> callback);
}
