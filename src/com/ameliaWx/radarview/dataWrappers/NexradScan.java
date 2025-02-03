package com.ameliaWx.radarview.dataWrappers;

import java.io.File;
import java.io.IOException;

import ucar.nc2.NetcdfFile;

public class NexradScan extends RadarScan {
	public static void main(String[] args) throws IOException {
		NetcdfFile ncfile = NetcdfFile.open("/home/a-urq/eclipse-workspace/RadarViewTakeFour/KTLX20240925_010254_V06");
		
		System.out.println(ncfile);
		String station = ncfile.findGlobalAttribute("Station").getStringValue();
		
		NexradScan scan = NexradScan.loadFromCdm(new File("/home/a-urq/eclipse-workspace/RadarViewTakeFour/KTLX20240925_010254_V06"));
		
		System.out.println(scan.dataFromField("vcp"));
	}
	
	public static NexradScan loadFromCdm(File f) throws IOException {
		NexradScan scan = new NexradScan();
		
		scan.locationOnDisk = f.getAbsolutePath();
		
		@SuppressWarnings("deprecation")
		NetcdfFile ncfile = NetcdfFile.open(scan.locationOnDisk);
		
		System.out.println("load cdm: " + ncfile);
		
		scan.permaFields.put("reflectivity_sails", DataField.fromNexradVar(ncfile.findVariable("Reflectivity_HI")));
		scan.permaFields.get("reflectivity_sails").bundleField("time", DataField.fromNexradVar(ncfile.findVariable("timeR_HI")));
		scan.permaFields.get("reflectivity_sails").bundleField("elev", DataField.fromNexradVar(ncfile.findVariable("elevationR_HI")));
		scan.permaFields.get("reflectivity_sails").bundleField("azi", DataField.fromNexradVar(ncfile.findVariable("azimuthR_HI")));
		
		scan.permaFields.put("velocity_sails", DataField.fromNexradVar(ncfile.findVariable("RadialVelocity_HI")));
		scan.permaFields.get("velocity_sails").bundleField("time", DataField.fromNexradVar(ncfile.findVariable("timeV_HI")));
		scan.permaFields.get("velocity_sails").bundleField("elev", DataField.fromNexradVar(ncfile.findVariable("elevationV_HI")));
		scan.permaFields.get("velocity_sails").bundleField("azi", DataField.fromNexradVar(ncfile.findVariable("azimuthV_HI")));
		
		scan.permaFields.put("station", DataField.fromNexradAttrToStr(ncfile.findGlobalAttribute("Station")));
		scan.permaFields.put("station_name", DataField.fromNexradAttrToStr(ncfile.findGlobalAttribute("StationName")));
		scan.permaFields.put("station_lat", DataField.fromNexradAttr(ncfile.findGlobalAttribute("StationLatitude")));
		scan.permaFields.put("station_lon", DataField.fromNexradAttr(ncfile.findGlobalAttribute("StationLongitude")));
		scan.permaFields.put("station_elev", DataField.fromNexradAttr(ncfile.findGlobalAttribute("StationElevationInMeters")));
		scan.permaFields.put("vcp", DataField.fromNexradAttr(ncfile.findGlobalAttribute("VolumeCoveragePattern")));
		
		ncfile.close();
		return scan;
	}
}
