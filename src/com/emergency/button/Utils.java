package com.emergency.button;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class Utils {
	public static String stacktrace(Exception exception) {
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		exception.printStackTrace(printWriter);
		return result.toString();
	}

	public static String packageInfo(Context context) {
		String info = "package info";
		PackageManager pm = context.getPackageManager();
		PackageInfo pi;
		// Version
		try {
			pi = pm.getPackageInfo(context.getPackageName(), 0);
			info += "\napp version: " + pi.versionName;
			info += "\npackage name: " + pi.packageName;
			info += "\nphone model: " + android.os.Build.MODEL;
			info += "\nandroid version: " + android.os.Build.VERSION.RELEASE;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			info += "\ngetPackageInfo failed.";
		}

		return info;
	}
}
