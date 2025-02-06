package com.ameliaWx.radarview.testScripts;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import javax.imageio.ImageIO;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.ameliaWx.radarview.dataWrappers.GoesImage;
import com.ameliaWx.radarview.dataWrappers.GoesMultibandImage;
import com.ameliaWx.radarview.gis.GeoCoord;
import com.ameliaWx.radarview.gis.GeostationaryProjection;
import com.ameliaWx.radarview.gis.LambertConformalProjection;
import com.ameliaWx.radarview.gis.VirtualCoord;

public class GoesTrueColor {
	// from GOES true color recipe
	// https://unidata.github.io/python-gallery/examples/mapping_GOES16_TrueColor.html#sphx-glr-download-examples-mapping-goes16-truecolor-py

	private static final DateTimeZone CST = DateTimeZone.forID("America/Chicago");
	private static final Font CAPTION_FONT_SMALL = new Font(Font.MONOSPACED, Font.BOLD, 48);

	public static void main(String[] args) throws IOException {
		BufferedImage HRRR_OVERLAY = ImageIO
				.read(new File("/home/a-urq/eclipse-workspace/RadarViewTakeFour/hrrrOverlay-2x.png"));

		List<File> conusImages = allFilesInDirectory(
				new File("/home/a-urq/eclipse-workspace/RadarViewTakeFour/goesTestData/fullDiskWest"));

		for (File f : conusImages) {
			System.out.println(f);

			GoesMultibandImage goes = GoesMultibandImage.loadFromFile(f);

			DateTime startTime = convertTimestampToDateTime(goes.field("time_start").getAnnotation());

			String filename = String.format("analysis_%04d%02d%02d%02d%02d%02d.png", startTime.getYear(),
					startTime.getMonthOfYear(), startTime.getDayOfMonth(), startTime.getHourOfDay(),
					startTime.getMinuteOfHour(), startTime.getSecondOfMinute());

			Color[][] geocolor = createComposite(goes);

			BufferedImage geocolorImg = new BufferedImage(geocolor.length, geocolor[0].length,
					BufferedImage.TYPE_3BYTE_BGR);
			Graphics2D g = geocolorImg.createGraphics();

			for (int i = 0; i < geocolorImg.getWidth(); i++) {
				for (int j = 0; j < geocolorImg.getHeight(); j++) {
					g.setColor(geocolor[i][j]);
					g.fillRect(i, j, 1, 1);
				}
			}

			g.setFont(CAPTION_FONT_SMALL);
			drawOutlinedString(dateStringAlt(startTime, CST, "CST"), g, 25, geocolorImg.getHeight() - 25, Color.WHITE);

			ImageIO.write(geocolorImg, "PNG", new File("geocolor-test/conus-afternoon-20250204/" + filename));

			BufferedImage geocolorImgEqui = new BufferedImage(18000, 9000, BufferedImage.TYPE_3BYTE_BGR);
			g = geocolorImgEqui.createGraphics();

			float[] x = goes.field("x").array1D();
			float[] y = goes.field("y").array1D();
			float dx = goes.dataFromField("dx");
			float dy = goes.dataFromField("dy");
			for (int i = 0; i < geocolor.length; i++) {
				for (int j = 0; j < geocolor[0].length; j++) {
					float x0 = -x[i];
					float y0 = y[j];

					GeoCoord latLon1 = GeostationaryProjection.GOES_WEST.projectXYToLatLon(x0 - dx / 2.0f,
							y0 - dy / 2.0f);
					GeoCoord latLon2 = GeostationaryProjection.GOES_WEST.projectXYToLatLon(x0 + dx / 2.0f,
							y0 - dy / 2.0f);
					GeoCoord latLon3 = GeostationaryProjection.GOES_WEST.projectXYToLatLon(x0 + dx / 2.0f,
							y0 + dy / 2.0f);
					GeoCoord latLon4 = GeostationaryProjection.GOES_WEST.projectXYToLatLon(x0 - dx / 2.0f,
							y0 + dy / 2.0f);

//					System.out.println("i: " + i + "\tj: " + j);
//					System.out.println("x0: " + x0 + "\ty0: " + y0);
//					System.out.println("latLon1: " + latLon1);
//					System.out.println("xy1: " + xy1);

					int x1 = (int) linScale(90, -90, 0, 9000, latLon1.getLat());
					int x2 = (int) linScale(90, -90, 0, 9000, latLon2.getLat());
					int x3 = (int) linScale(90, -90, 0, 9000, latLon3.getLat());
					int x4 = (int) linScale(90, -90, 0, 9000, latLon4.getLat());

					int y1 = (int) linScale(-180, 180, 0, 18000, latLon1.getLon());
					int y2 = (int) linScale(-180, 180, 0, 18000, latLon2.getLon());
					int y3 = (int) linScale(-180, 180, 0, 18000, latLon3.getLon());
					int y4 = (int) linScale(-180, 180, 0, 18000, latLon4.getLon());

					boolean allValid = !Float.isNaN(latLon1.getLat()) && !Float.isNaN(latLon2.getLat())
							&& !Float.isNaN(latLon3.getLat()) && !Float.isNaN(latLon4.getLat())
							&& !Float.isNaN(latLon1.getLon()) && !Float.isNaN(latLon2.getLon())
							&& !Float.isNaN(latLon3.getLon()) && !Float.isNaN(latLon4.getLon()) 
							&& x1 != 0
							&& x2 != 0
							&& x3 != 0
							&& x4 != 0
							&& y1 != 0
							&& y2 != 0
							&& y3 != 0
							&& y4 != 0;

					int[] yPoints = new int[] { x1, x2, x3, x4 };
					int[] xPoints = new int[] { y1, y2, y3, y4 };
					
					if(!allValid) {
						System.out.println(Arrays.toString(xPoints));
					}

					if(allValid) {
						g.setColor(geocolor[i][j]);
						g.fillPolygon(xPoints, yPoints, 4);
					}
				}
			}

			ImageIO.write(geocolorImgEqui, "PNG", new File("geocolor-test/fullDisk-west/" + filename));

			final int SCALE = 2;
			BufferedImage geocolorImgProj = new BufferedImage(SCALE * 1799, SCALE * 1059, BufferedImage.TYPE_3BYTE_BGR);
			g = geocolorImgProj.createGraphics();

//			float[] x = goes.field("x").array1D();
//			float[] y = goes.field("x").array1D();
//			float dx = goes.dataFromField("dx");
//			float dy = goes.dataFromField("dy");

			System.out.println("x[0]: " + x[0]);
			System.out.println("y[0]: " + y[0]);

			for (int i = 0; i < geocolor.length; i++) {
				for (int j = 0; j < geocolor[0].length; j++) {
					float x0 = -x[i];
					float y0 = y[j];

					GeoCoord latLon1 = GeostationaryProjection.GOES_WEST.projectXYToLatLon(x0 - dx / 2.0f,
							y0 - dy / 2.0f);
					GeoCoord latLon2 = GeostationaryProjection.GOES_WEST.projectXYToLatLon(x0 + dx / 2.0f,
							y0 - dy / 2.0f);
					GeoCoord latLon3 = GeostationaryProjection.GOES_WEST.projectXYToLatLon(x0 + dx / 2.0f,
							y0 + dy / 2.0f);
					GeoCoord latLon4 = GeostationaryProjection.GOES_WEST.projectXYToLatLon(x0 - dx / 2.0f,
							y0 + dy / 2.0f);

					VirtualCoord xy1 = LambertConformalProjection.HRRR_PROJ.projectLatLonToXY(latLon1);
					VirtualCoord xy2 = LambertConformalProjection.HRRR_PROJ.projectLatLonToXY(latLon2);
					VirtualCoord xy3 = LambertConformalProjection.HRRR_PROJ.projectLatLonToXY(latLon3);
					VirtualCoord xy4 = LambertConformalProjection.HRRR_PROJ.projectLatLonToXY(latLon4);

//					System.out.println("i: " + i + "\tj: " + j);
//					System.out.println("x0: " + x0 + "\ty0: " + y0);
//					System.out.println("latLon1: " + latLon1);
//					System.out.println("xy1: " + xy1);

					int[] xPoints = new int[] { (int) (SCALE * xy1.getX()), (int) (SCALE * xy2.getX()),
							(int) (SCALE * xy3.getX()), (int) (SCALE * xy4.getX()) };
					int[] yPoints = new int[] { (int) (SCALE * xy1.getY()), (int) (SCALE * xy2.getY()),
							(int) (SCALE * xy3.getY()), (int) (SCALE * xy4.getY()) };

					boolean allValid = !Float.isNaN(latLon1.getLat()) && !Float.isNaN(latLon2.getLat())
							&& !Float.isNaN(latLon3.getLat()) && !Float.isNaN(latLon4.getLat())
							&& !Float.isNaN(latLon1.getLon()) && !Float.isNaN(latLon2.getLon())
							&& !Float.isNaN(latLon3.getLon()) && !Float.isNaN(latLon4.getLon()) && xy1.getX() != 0
							&& xy2.getX() != 0
							&& xy3.getX() != 0
							&& xy4.getX() != 0
							&& xy1.getY() != 0
							&& xy2.getY() != 0
							&& xy3.getY() != 0
							&& xy4.getY() != 0;

//					for(int k = 0; k < xPoints.length; k++) {
//						allValid = allValid && !Double.isNaN(xPoints[k]) && !Double.isNaN(yPoints[k]);
//					}

					if (allValid) {
						g.setColor(geocolor[i][j]);
						g.fillPolygon(xPoints, yPoints, 4);
					}
				}
			}

			g.drawImage(HRRR_OVERLAY, 0, 0, null);

			ImageIO.write(geocolorImgProj, "PNG", new File("geocolor-test/conus-afternoon-20250204-proj/" + filename));
		}
	}

	private static String dateStringAlt(DateTime d, DateTimeZone tz, String tzCode) {
		DateTime c = d.toDateTime(tz);

		String daylightCode = tzCode.substring(0, tzCode.length() - 2) + "DT";

		boolean isPm = c.getHourOfDay() >= 12;
		boolean is12 = c.getHourOfDay() == 0 || c.getHourOfDay() == 12;
		return String.format("%04d", c.getYear()) + "-" + String.format("%02d", c.getMonthOfYear()) + "-"
				+ String.format("%02d", c.getDayOfMonth()) + " "
				+ String.format("%02d", c.getHourOfDay() % 12 + (is12 ? 12 : 0)) + ":"
				+ String.format("%02d", c.getMinuteOfHour()) + " " + (isPm ? "PM" : "AM") + " "
				+ (TimeZone.getTimeZone(tz.getID()).inDaylightTime(d.toDate()) ? daylightCode : tzCode);
	}

	private static void drawOutlinedString(String s, Graphics2D g, int x, int y, Color c) {
		g.setColor(Color.BLACK);
		g.drawString(s, x - 1, y - 1);
		g.drawString(s, x - 1, y);
		g.drawString(s, x - 1, y + 1);
		g.drawString(s, x, y - 1);
		g.drawString(s, x, y + 1);
		g.drawString(s, x + 1, y - 1);
		g.drawString(s, x + 1, y);
		g.drawString(s, x + 1, y + 1);

		g.setColor(c);
		g.drawString(s, x, y);
	}

	private static DateTime convertTimestampToDateTime(String timestamp) {
		int year = Integer.valueOf(timestamp.substring(0, 4));
		int month = Integer.valueOf(timestamp.substring(5, 7));
		int day = Integer.valueOf(timestamp.substring(8, 10));
		int hour = Integer.valueOf(timestamp.substring(11, 13));
		int minute = Integer.valueOf(timestamp.substring(14, 16));
		int second = Integer.valueOf(timestamp.substring(17, 19));

		DateTime dt = new DateTime(year, month, day, hour, minute, second, DateTimeZone.UTC);

		return dt;
	}

	private static List<File> allFilesInDirectory(File dir) {
		List<File> allFiles = new ArrayList<>();

		File[] files = dir.listFiles();

		if (files == null)
			return allFiles;

		for (File file : files) {
			if (file.isDirectory()) {
				List<File> subdirFiles = allFilesInDirectory(file);

				allFiles.addAll(subdirFiles);
			} else {
				allFiles.add(file);
			}
		}

		return allFiles;
	}

	// temu geocolor
	private static Color[][] createComposite(GoesMultibandImage goes) {
		Color[][] trueColor = createTrueColorGoes(goes);
		Color[][] irColor = createIRGoes(goes);

		Color[][] goesComposite = new Color[trueColor.length][trueColor[0].length];

		for (int i = 0; i < goesComposite.length; i++) {
			for (int j = 0; j < goesComposite[0].length; j++) {
				goesComposite[i][j] = maxTristims(trueColor[i][j], irColor[i][j]);
			}
		}

		return goesComposite;
	}

	private static Color[][] createTrueColorGoes(GoesMultibandImage goes) {
//		System.out.println("band 1 wavelength: " + goes.field("band_1").getBundledField("wavelength").getData() + " um");
//		System.out.println("band 2 wavelength: " + goes.field("band_2").getBundledField("wavelength").getData() + " um");
//		System.out.println("band 3 wavelength: " + goes.field("band_3").getBundledField("wavelength").getData() + " um");

		float[][] band1Rad = goes.field("band_1").array2D();
		float[][] band2Rad = goes.field("band_2").array2D();
		float[][] band3Rad = goes.field("band_3").array2D();

//		System.out.println("band 1 shape: " + band1Rad.length + "\t" + band1Rad[0].length);
//		System.out.println("band 2 shape: " + band2Rad.length + "\t" + band2Rad[0].length);
//		System.out.println("band 3 shape: " + band3Rad.length + "\t" + band3Rad[0].length);
//		
//		System.out.println("band 1 min/max: " + min(band1Rad) + "\t" + max2(band1Rad));
//		System.out.println("band 2 min/max: " + min(band2Rad) + "\t" + max2(band2Rad));
//		System.out.println("band 3 min/max: " + min(band3Rad) + "\t" + max2(band3Rad));

		float[][] band1Clip = clip(band1Rad, 0, 1);
		float[][] band2Clip = clip(band2Rad, 0, 1);
		float[][] band3Clip = clip(band3Rad, 0, 1);

		final float GAMMA = 2.2f;

		float[][] band1NormG = gammaCorrect(band1Clip, GAMMA);
		float[][] band2NormG = gammaCorrect(band2Clip, GAMMA);
		float[][] band3NormG = gammaCorrect(band3Clip, GAMMA);

		float[][] syntheticGreen = new float[band3Rad.length][band3Rad[0].length];

		for (int i = 0; i < syntheticGreen.length; i++) {
			for (int j = 0; j < syntheticGreen[i].length; j++) {
				// calculate the "true" green
				syntheticGreen[i][j] = 0.325f * band2NormG[i][j] + 0.35f * band3NormG[i][j] + 0.325f * band1NormG[i][j];
				// original formula
//				syntheticGreen[i][j] = 0.45f * band2NormG[i][j] + 0.1f * band3NormG[i][j] + 0.45f * band1NormG[i][j];
			}
		}

		syntheticGreen = clip(syntheticGreen, 0, 1);

		float[][] red = band2NormG;
		float[][] green = syntheticGreen;
		float[][] blue = band1NormG;

		boolean hiResRed = false;
		if (red.length >= 2 * blue.length) {
			hiResRed = true;
		}

		Color[][] goesComposite = new Color[green[0].length][green.length];

		for (int i = 0; i < goesComposite[0].length; i++) {
			for (int j = 0; j < goesComposite.length; j++) {
				int r;
				if (hiResRed) {
					r = (int) (255 * red[2 * i][2 * j]);
				} else {
					r = (int) (255 * red[i][j]);
				}

				int gr = (int) (255 * green[i][j]);
				int b = (int) (255 * blue[i][j]);

				Color c = new Color(r, gr, b);

				goesComposite[j][i] = contrast(c, 48);
			}
		}

		return goesComposite;
	}

	private static Color[][] createIRGoes(GoesMultibandImage goes) {
//		System.out.println("band 7 wavelength: " + goes.field("band_7").getBundledField("wavelength").getData() + " um");
//		System.out.println("band 13 wavelength: " + goes.field("band_13").getBundledField("wavelength").getData() + " um");

		float[][] band7Temp = goes.field("band_7").array2D();
		float[][] band13Temp = goes.field("band_13").array2D();

//		System.out.println("band 7 shape: " + band7Temp.length + "\t" + band7Temp[0].length);
//		System.out.println("band 13 shape: " + band13Temp.length + "\t" + band13Temp[0].length);
//		System.out.println("band 7 min/max: " + min(band7Temp) + "\t" + max2(band7Temp));
//		System.out.println("band 13 min/max: " + min(band13Temp) + "\t" + max2(band13Temp));
//		System.out.println("band 13 corner: " + band13Temp[0][0] + " K");

		float[][] band13Clip = clip(band13Temp, 90, 273);
		float[][] band13Norm = clip(invNormalize(band13Clip, 0, 500), 0, 255);

		Color[][] goesComposite = new Color[band13Temp[0].length][band13Temp.length];

		for (int i = 0; i < goesComposite[0].length; i++) {
			for (int j = 0; j < goesComposite.length; j++) {
				float fog = band13Temp[i][j] - band7Temp[i][j];

				float fogBlue = clip(linScale(0, 5, 0, 200, fog), 0, 200);

				Color fogColor = new Color((int) (0.5 * fogBlue), (int) (0.75 * fogBlue), (int) (1.0 * fogBlue));
				Color band13Color = new Color((int) band13Norm[i][j], (int) band13Norm[i][j],
						(int) Double.max(band13Norm[i][j], fogBlue));

				goesComposite[j][i] = maxTristims(fogColor, band13Color);

				if (band13Temp[i][j] == -1024) {
					goesComposite[j][i] = Color.BLACK;
				}
			}
		}

		return goesComposite;
	}

	private static Color maxTristims(Color a, Color b) {
		int rA = a.getRed();
		int gA = a.getGreen();
		int bA = a.getBlue();

		int rB = b.getRed();
		int gB = b.getGreen();
		int bB = b.getBlue();

		Color comp = new Color(Integer.max(rA, rB), Integer.max(gA, gB), Integer.max(bA, bB));

		return comp;
	}

	private static Color contrast(Color c, float contrast) {
		float factor = 259 * (contrast + 255) / (255 * (259 - contrast));

		int r = (int) (factor * (c.getRed() - 128) + 128);
		int g = (int) (factor * (c.getGreen() - 128) + 128);
		int b = (int) (factor * (c.getBlue() - 128) + 128);

		r = clip(r, 0, 255);
		g = clip(g, 0, 255);
		b = clip(b, 0, 255);

		return new Color(r, g, b);
	}

	private static int clip(int val, int min, int max) {
		if (val < min) {
			return min;
		} else if (val > max) {
			return max;
		} else if (val == -1024) {
			return -1024;
		} else {
			return val;
		}
	}

	private static float clip(float val, float min, float max) {
		if (val < min) {
			return min;
		} else if (val > max) {
			return max;
		} else if (val == -1024) {
			return -1024;
		} else {
			return val;
		}
	}

	private static float[][] normalize(float[][] arr, float newMin, float newMax) {
		float[][] normArr = new float[arr.length][arr[0].length];

		float max = max2(arr);
		float min = min(arr);

		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
				normArr[i][j] = linScale(min, max, newMin, newMax, arr[i][j]);

				// overshoot correction
				if (normArr[i][j] > newMax) {
					normArr[i][j] = newMax;
				}
			}
		}

		return normArr;
	}

	private static float[][] invNormalize(float[][] arr, float newMin, float newMax) {
		float[][] normArr = new float[arr.length][arr[0].length];

		float max = max2(arr);
		float min = min(arr);

		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
				normArr[i][j] = linScale(min, max, newMax, newMin, arr[i][j]);

				// overshoot correction
				if (normArr[i][j] > newMax) {
					normArr[i][j] = newMax;
				}
			}
		}

		return normArr;
	}

	private static float[][] gammaCorrect(float[][] arr, float gamma) {
		float[][] gammaCorr = new float[arr.length][arr[0].length];

		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
				gammaCorr[i][j] = (float) Math.pow(arr[i][j], 1 / gamma);
			}
		}

		return gammaCorr;
	}

	private static float[][] clip(float[][] arr, float min, float max) {
		float[][] clipped = new float[arr.length][arr[0].length];

		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
				clipped[i][j] = clip(arr[i][j], min, max);
			}
		}

		return clipped;
	}

	private static float minNomiss(float[][] arr) {
		float min = Float.MAX_VALUE;

		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
				min = Float.min(arr[i][j], min);
			}
		}

		return min;
	}

	private static float min(float[][] arr) {
		float min = Float.MAX_VALUE;

		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
				if (arr[i][j] != -1024) {
					min = Float.min(arr[i][j], min);
				}
			}
		}

		return min;
	}

	// gets the second highest value
	private static float max2(float[][] arr) {
		float maxO = max(arr);

		float max = -Float.MAX_VALUE;

		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
				if (arr[i][j] != maxO) {
					max = Float.max(arr[i][j], max);
				}
			}
		}

		return max;
	}

	private static float max(float[][] arr) {
		float max = -Float.MAX_VALUE;

		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
				max = Float.max(arr[i][j], max);
			}
		}

		return max;
	}

	private static float linScale(float preMin, float preMax, float postMin, float postMax, float value) {
		float slope = (postMax - postMin) / (preMax - preMin);

		return slope * (value - preMin) + postMin;
	}

	private static BufferedImage createTrueColorGoes(GoesImage band1, GoesImage band2, GoesImage band3) {
//		System.out.println("band 1 wavelength: " + band1.dataFromField("wavelength") + " um");
//		System.out.println("band 2 wavelength: " + band2.dataFromField("wavelength") + " um");
//		System.out.println("band 3 wavelength: " + band3.dataFromField("wavelength") + " um");

		float[][] band1Rad = band1.field("rad").array2D();
		float[][] band2Rad = band2.field("rad").array2D();
		float[][] band3Rad = band3.field("rad").array2D();

//		System.out.println("band 1 shape: " + band1Rad.length + "\t" + band1Rad[0].length);
//		System.out.println("band 2 shape: " + band2Rad.length + "\t" + band2Rad[0].length);
//		System.out.println("band 3 shape: " + band3Rad.length + "\t" + band3Rad[0].length);
//		
//		System.out.println("band 1 min/max: " + min(band1Rad) + "\t" + max2(band1Rad));
//		System.out.println("band 2 min/max: " + min(band2Rad) + "\t" + max2(band2Rad));
//		System.out.println("band 3 min/max: " + min(band3Rad) + "\t" + max2(band3Rad));

		float[][] band1Clip = clip(band1Rad, 0, 1000000);
		float[][] band2Clip = clip(band2Rad, 0, 1000000);
		float[][] band3Clip = clip(band3Rad, 0, 1000000);

		float[][] band1Norm = normalize(band1Clip, 0, 1);
		float[][] band2Norm = normalize(band2Clip, 0, 1);
		float[][] band3Norm = normalize(band3Clip, 0, 1);

		final float GAMMA = 2.2f;

		float[][] band1NormG = gammaCorrect(band1Norm, GAMMA);
		float[][] band2NormG = gammaCorrect(band2Norm, GAMMA);
		float[][] band3NormG = gammaCorrect(band3Norm, GAMMA);

		float[][] syntheticGreen = new float[band3Rad.length][band3Rad[0].length];

		for (int i = 0; i < syntheticGreen.length; i++) {
			for (int j = 0; j < syntheticGreen[i].length; j++) {
				// calculate the "true" green
				syntheticGreen[i][j] = 0.325f * band2NormG[2 * i][2 * j] + 0.35f * band3NormG[i][j]
						+ 0.325f * band1NormG[i][j];
				// original formula
//				syntheticGreen[i][j] = 0.45f * band2NormG[2 * i][2 * j] + 0.1f * band3NormG[i][j] + 0.45f * band1NormG[i][j];
			}
		}

		syntheticGreen = normalize(syntheticGreen, 0, 1);

		float[][] red = band2NormG;
		float[][] green = syntheticGreen;
		float[][] blue = band1NormG;

		boolean hiResRed = false;
		if (red.length >= 2 * blue.length) {
			hiResRed = true;
		}

		BufferedImage goesComposite = new BufferedImage(green[0].length, green.length, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g = goesComposite.createGraphics();

		for (int i = 0; i < goesComposite.getHeight(); i++) {
			for (int j = 0; j < goesComposite.getWidth(); j++) {
				int r;
				if (hiResRed) {
					r = (int) (255 * red[2 * i][2 * j]);
				} else {
					r = (int) (255 * red[i][j]);
				}

				int gr = (int) (255 * green[i][j]);
				int b = (int) (255 * blue[i][j]);

				Color c = new Color(r, gr, b);
				g.setColor(contrast(c, 32));
				g.fillRect(j, i, 1, 1);
			}
		}

		return goesComposite;
	}
}
