package com.ogp.cputableau2.providers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import android.annotation.SuppressLint;

import com.ogp.cputableau2.results.RPCResult;
import com.ogp.cputableau2.settings.LocalSettings;
import com.ogp.cputableau2.su.RootCaller;


public abstract class HWProvider {
	private RootCaller.RootExecutor rootExecutor;

	String readFileStringRoot(String path) {
		if (null == rootExecutor) {
			return null;
		}


		String command = String.format("cat %s", path);
		RPCResult result = rootExecutor.executeOnRoot(command);
		if (result.isError()) {
			return null;
		}

		if (result.isList()) {
			return (String)result.get(0);
		} else {
			return null;
		}
	}


	@SuppressLint("DefaultLocale")
	static String temperatureDouble2StringString(double dres) {
		if (LocalSettings.isFahrenheit()) {
			return String.format("%.1fºF", dres * 1.8f + 32.0f);
		} else {
			return String.format("%.1fºC", dres);
		}
	}

	public void init(RootCaller.RootExecutor rootExecutor) {
		this.rootExecutor = rootExecutor;
	}


	public String getData() {
		return null;
	}

	public abstract void clear();
}
