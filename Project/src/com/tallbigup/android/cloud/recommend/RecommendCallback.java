package com.tallbigup.android.cloud.recommend;

import java.util.Map;

public interface RecommendCallback {
	void result(final boolean success,final Map<Integer,String[]> recommendList);
}
