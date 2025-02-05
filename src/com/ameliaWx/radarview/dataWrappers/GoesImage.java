package com.ameliaWx.radarview.dataWrappers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import ucar.nc2.NetcdfFile;

public class GoesImage extends CdmFile implements SatelliteImage {
	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws IOException {
//		NetcdfFile ncfile = NetcdfFile.open("/home/a-urq/eclipse-workspace/RadarViewTakeFour/OR_ABI-L2-MCMIPC-M6_G16_s20250352331170_e20250352333543_c20250352334059.nc");
		
		NetcdfFile ncfile1 = NetcdfFile.open("/home/a-urq/eclipse-workspace/RadarViewTakeFour/OR_ABI-L1b-RadC-M6C01_G16_s20250352301170_e20250352303543_c20250352303585.nc");
		NetcdfFile ncfile2 = NetcdfFile.open("/home/a-urq/eclipse-workspace/RadarViewTakeFour/OR_ABI-L1b-RadC-M6C02_G16_s20250352301170_e20250352303543_c20250352303579.nc");
		NetcdfFile ncfile3 = NetcdfFile.open("/home/a-urq/eclipse-workspace/RadarViewTakeFour/OR_ABI-L1b-RadC-M6C03_G16_s20250352301170_e20250352303543_c20250352304034.nc");
		GoesImage band13 = GoesImage.loadFromFile(new File("/home/a-urq/eclipse-workspace/RadarViewTakeFour/OR_ABI-L1b-RadC-M6C13_G16_s20250352301170_e20250352303555_c20250352304037.nc"));
		
		System.out.println(ncfile1);
		
		GoesImage band3 = GoesImage.loadFromFile(new File(ncfile3.getLocation()));
		
		System.out.println(band3.dataFromField("wavelength"));
	}

	@SuppressWarnings("deprecation")
	public static GoesImage loadFromFile(File f) throws IOException {
		GoesImage image = new GoesImage();
		
		image.locationOnDisk = f.getAbsolutePath();
		
		NetcdfFile ncfile = NetcdfFile.open(image.locationOnDisk);
		
//		System.out.println(ncfile);

		double scaleFactor = ncfile.findVariable("Rad").findAttributeDouble("scale_factor", -1024);
		double addOffset = ncfile.findVariable("Rad").findAttributeDouble("add_offset", -1024);
		double fillValue = ncfile.findVariable("Rad").findAttributeDouble("_FillValue", -1024);
		
		image.permaFields.put("rad", DataField.fromCdmVar(ncfile.findVariable("Rad")));
		image.permaFields.get("rad").bundleField("scale_factor", DataField.fromNumber(scaleFactor));
		image.permaFields.get("rad").bundleField("add_offset", DataField.fromNumber(addOffset));
		image.permaFields.get("rad").bundleField("fill_value", DataField.fromNumber(fillValue));
		image.permaFields.get("rad").processOffsets();
		
		image.permaFields.put("wavelength", DataField.fromCdmVar(ncfile.findVariable("band_wavelength")));
		image.permaFields.get("wavelength").setAnnotation("units are micrometers");
		
		ncfile.close();
		return image;
	}
	
	public DataField field(String key) {
		if(permaFields.containsKey(key)) {
			return permaFields.get(key);
		} else {
			if (!swapFields.containsKey(key)) {
				try {
					loadIntoSwap(key);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			return swapFields.get(key);
		}
	}
	
	public float dataFromField(String key) {
		return dataFromField(key, 0);
	}
	
	public float dataFromField(String key, int... indices) {
		if(permaFields.containsKey(key)) {
			return permaFields.get(key).getData(indices);
		} else {
			return fromSwap(key, indices);
		}
	}
	
	public float fromSwap(String key) {
		return fromSwap(key, 0);
	}
	
	public float fromSwap(String key, int... indices) {
		if (!swapFields.containsKey(key)) {
			try {
				loadIntoSwap(key);
			} catch (IOException e) {
				// pass up the chain actually this just makes it compile for now
			}
		}
		
		return swapFields.get(key).getData(indices);
	}
	
	public void loadIntoSwap(String... keys) throws IOException {
		// no swap fields to load
	}
}
