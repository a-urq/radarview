package com.ameliaWx.radarview.dataWrappers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public abstract class RadarScan {
	String locationOnDisk;
	HashMap<String, DataField> permaFields = new HashMap<>(); // very few fields, stored for rapid loading
	HashMap<String, DataField> swapFields = new HashMap<>(); // fields that can be swapped out. avoids loading all data at once to save memory
	
	public static RadarScan loadFromCdm(File f) throws IOException {
		return null;
	}
	
	public float dataFromField(String key) {
		return dataFromField(key, 0);
	}
	
	public float dataFromField(String key, int... indices) {
		if(permaFields.containsKey(key)) {
			return permaFields.get(key).getData(indices);
		} else {
			System.out.println("oopsies wip");
			return -1024;
		}
	}
}
