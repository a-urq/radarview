package com.ameliaWx.radarview.gis;

public class LambertConformalProjection implements MapProjection {
	// ALL ANGLES IN DEGREES; -180 = 180W, 180 = 180E
	public double refLongitude;
	public double refLatitude;
	public static final double R = 6371.14035848;
	public double standardParallel1;
	public double standardParallel2;
	public double dx;
	public double dy;
	public double offsetX;
	public double offsetY;
	public double nx;
	public double ny;
	
	public static final LambertConformalProjection HRRR_PROJ = new LambertConformalProjection(-97.5, 21.138, 38.5, 38.5, 3, 3, 899.178564262, -123.970196814, 1799, 1059);
	public static final LambertConformalProjection RAP32_PROJ = new LambertConformalProjection(-107.0, 1.000, 50, 50, 32.46341, 32.46341, 173.5, -45, 349, 277);
	public static final LambertConformalProjection HRDPS_CONT_PROJ = new LambertConformalProjection(-110.3, 60, 50, 68, 2.5, 2.5, 756, 835, 2540, 1290);
	public static final LambertConformalProjection RTMA_RU_PROJ = new LambertConformalProjection(-95, 25, 25, 25, 2.539703, 2.539703, 1289, 103, 2345, 1597);
	
	public LambertConformalProjection(double refLongitude, double refLatitude, double standardParallel1,
			double standardParallel2, double dx, double dy, double offsetX, double offsetY, double nx, double ny) {
		this.refLongitude = refLongitude;
		this.refLatitude = refLatitude;
		this.standardParallel1 = standardParallel1;
		this.standardParallel2 = standardParallel2;
		this.dx = dx;
		this.dy = dy;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.nx = nx;
		this.ny = ny;
	}

	public VirtualCoord projectLatLonToXY(double longitude, double latitude) {

		double n;
		if (standardParallel1 == standardParallel2) {
			n = sin(standardParallel1);
		} else {
			n = (Math.log(cos(standardParallel1) * sec(standardParallel2)))
					/ (Math.log(tan(0.25 * 180 + 0.5 * standardParallel2) * cot(0.25 * 180 + 0.5 * standardParallel1)));
		}
		double F = (cos(standardParallel1) * Math.pow(tan(0.25 * 180 + 0.5 * standardParallel1), n)) / n;
		double rho = R * F * Math.pow(cot(0.25 * 180 + 0.5 * latitude), n);
		double rho0 = R * F * Math.pow(cot(0.25 * 180 + 0.5 * refLatitude), n);

		//System.out.println(n);

		double x = rho * sin(n * (longitude - refLongitude));
		double y = rho0 - rho * cos(n * (longitude - refLongitude));

		//System.out.println(new PointD(x, y));
		return new VirtualCoord(x / dx + offsetX, (ny - 1) - (y / dy + offsetY));
	}

	public VirtualCoord projectLatLonToXY(GeoCoord p) {
		return projectLatLonToXY(p.getLon(), p.getLat());
	}

	@Override
	public boolean inDomain(double i, double j) {
		if(i >= 0 && i <= nx - 1 && j >= 0 && j <= ny - 1) return true;
		
		return false;
	}

	@Override
	public boolean inDomain(VirtualCoord p) {
		return inDomain(p.getX(), p.getY());
	}

	@Override
	public GeoCoord projectXYToLatLon(double i, double j) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GeoCoord projectXYToLatLon(VirtualCoord p) {
		// TODO Auto-generated method stub
		return null;
	}
}
