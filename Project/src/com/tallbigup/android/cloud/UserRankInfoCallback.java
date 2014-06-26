package com.tallbigup.android.cloud;

import java.util.Map;

public interface UserRankInfoCallback {
	void result(final boolean success,final Map<String,Map<String,String>> callback);
}
