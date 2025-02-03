package com.ameliaWx.radarview.dataWrappers;

import java.io.IOException;
import java.util.HashMap;

import ucar.ma2.Array;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

public class DataField {
	private int[] shape;
	private int[] shapeMult;
	private float[] data;
	private String annotation;
	private HashMap<String, DataField> bundledFields = new HashMap<>();
	
	public static DataField fromNexradVar(Variable var) {
		DataField field = new DataField();
		Array arr = null;
		try {
			arr = var.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		field.shape = var.getShape();
		field.shapeMult = new int[field.shape.length];
		int runningMult = 1;
		for(int i = field.shape.length - 1; i >= 0; i--) {
			field.shapeMult[i] = runningMult;
			runningMult = field.shape[i];
		}
		
		field.data = new float[(int) var.getSize()];
		for(int i = 0; i < var.getSize(); i++) {
			field.data[i] = arr.getFloat(i);
		}
		
		return field;
	}

	public static DataField fromNexradAttr(Attribute attr) {
		DataField field = new DataField();
		field.shape = new int[1];
		field.shapeMult = new int[field.shape.length];
		field.shapeMult[0] = 1;
		
		Object value = attr.getValue(0);

		field.data = new float[1];
		if(value.getClass() == Integer.valueOf(0).getClass()) {
			field.data[0] = (float) (int) value;
		} else if(value.getClass() == Double.valueOf(0).getClass()) {
			field.data[0] = (float) (double) value;
		}
		
		return field;
	}
	
	public static DataField fromNexradAttrToStr(Attribute attr) {
		DataField field = new DataField();
		field.shape = new int[1];
		field.shapeMult = new int[field.shape.length];
		field.shapeMult[0] = 1;
		
		String value = attr.getStringValue();

		field.data = new float[1];
		field.data[0] = -1024.0f;
		
		field.annotation = value;
		
		return field;
	}
	
	public float getData() {
		return getData(0);
	}
	
	public float getData(int... indices) {
		int idx = 0;
		
		int minLength = Integer.min(indices.length, shapeMult.length);
		for(int i = 0; i < indices.length; i++) {
			int idxComp = indices[minLength - 1 - i];
			int mult = shapeMult[minLength - 1 - i];
			
			idx += idxComp * mult;
		}
		
		return data[idx];
	}

	public String getAnnotation() {
		return annotation;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}
	
	public void bundleField(String str, DataField f) {
		this.bundledFields.put(str, f);
	}
	
	public DataField getBundledField(String str) {
		return this.bundledFields.get(str);
	}
}
