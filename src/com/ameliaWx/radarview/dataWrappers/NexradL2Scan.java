package com.ameliaWx.radarview.dataWrappers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import ucar.nc2.NetcdfFile;

public class NexradL2Scan extends CdmFile implements RadarScan {
	public static void main(String[] args) throws IOException {
		NetcdfFile ncfile = NetcdfFile.open("/home/a-urq/eclipse-workspace/RadarViewTakeFour/KTLX20240925_010254_V06");
		
		System.out.println(ncfile);
		String station = ncfile.findGlobalAttribute("Station").getStringValue();
		
		NexradL2Scan scan = NexradL2Scan.loadFromFile(new File("/home/a-urq/eclipse-workspace/RadarViewTakeFour/KTLX20240925_010254_V06"));
		
		long usedMemory = Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory();
		
		System.out.println("usedMemory: " + usedMemory);
		
		System.out.println(scan.dataFromField("vcp"));
	}
	
	public static NexradL2Scan loadFromFile(File f) throws IOException {
		NexradL2Scan scan = new NexradL2Scan();
		
		scan.locationOnDisk = f.getAbsolutePath();
		
		@SuppressWarnings("deprecation")
		NetcdfFile ncfile = NetcdfFile.open(scan.locationOnDisk);
		
		System.out.println("load cdm: " + ncfile);
		
		scan.permaFields.put("reflectivity_sails", DataField.fromCdmVar(ncfile.findVariable("Reflectivity_HI")));
		scan.permaFields.get("reflectivity_sails").bundleField("time", DataField.fromCdmVar(ncfile.findVariable("timeR_HI")));
		scan.permaFields.get("reflectivity_sails").bundleField("elev", DataField.fromCdmVar(ncfile.findVariable("elevationR_HI")));
		scan.permaFields.get("reflectivity_sails").bundleField("azi", DataField.fromCdmVar(ncfile.findVariable("azimuthR_HI")));
		
		scan.permaFields.put("velocity_sails", DataField.fromCdmVar(ncfile.findVariable("RadialVelocity_HI")));
		scan.permaFields.get("velocity_sails").bundleField("time", DataField.fromCdmVar(ncfile.findVariable("timeV_HI")));
		scan.permaFields.get("velocity_sails").bundleField("elev", DataField.fromCdmVar(ncfile.findVariable("elevationV_HI")));
		scan.permaFields.get("velocity_sails").bundleField("azi", DataField.fromCdmVar(ncfile.findVariable("azimuthV_HI")));
		
		scan.permaFields.put("station", DataField.fromNexradAttrToStr(ncfile.findGlobalAttribute("Station")));
		scan.permaFields.put("station_name", DataField.fromNexradAttrToStr(ncfile.findGlobalAttribute("StationName")));
		scan.permaFields.put("station_lat", DataField.fromNexradAttr(ncfile.findGlobalAttribute("StationLatitude")));
		scan.permaFields.put("station_lon", DataField.fromNexradAttr(ncfile.findGlobalAttribute("StationLongitude")));
		scan.permaFields.put("station_elev", DataField.fromNexradAttr(ncfile.findGlobalAttribute("StationElevationInMeters")));
		scan.permaFields.put("vcp", DataField.fromNexradAttr(ncfile.findGlobalAttribute("VolumeCoveragePattern")));
		
		ncfile.close();
		return scan;
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
		swapFields = new HashMap<>();
		
		NetcdfFile ncfile = NetcdfFile.open(locationOnDisk);
		
		final String specWdthSAILS = "spectrum_width_sails";
		final String diffReflSAILS = "differential_reflectivity_sails";
		final String corrCoefSAILS = "correlation_coefficient_sails";
		final String diffPhseSAILS = "differential_phase_sails";
		final String refl = "reflectivity";
		final String vlcy = "velocity";
		final String specWdth = "spectrum_width";
		final String diffRefl = "differential_reflectivity";
		final String corrCoef = "correlation_coefficient";
		final String diffPhse = "differential_phase";
				
		for(String key : keys) {
			if(specWdthSAILS.equals(key)) {
				swapFields.put(key, DataField.fromCdmVar(ncfile.findVariable("SpectrumWidth_HI")));
				swapFields.get(key).bundleField("time", DataField.fromCdmVar(ncfile.findVariable("timeV_HI")));
				swapFields.get(key).bundleField("elev", DataField.fromCdmVar(ncfile.findVariable("elevationV_HI")));
				swapFields.get(key).bundleField("azi", DataField.fromCdmVar(ncfile.findVariable("azimuthV_HI")));
			}
		}
		
		ncfile.close();
	}
}
