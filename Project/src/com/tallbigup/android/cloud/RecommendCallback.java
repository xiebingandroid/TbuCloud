package com.tallbigup.android.cloud;

import java.util.List;
import java.util.Map;

public interface RecommendCallback {
	void result(final boolean success,final List<Map<String,String>> recommendList);
}
