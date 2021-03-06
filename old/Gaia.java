package gaia;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;

public class Gaia {
	private StringBuffer sb;
	private static final String GAIA_SOURCE_URL = "http://cdn.gea.esac.esa.int/Gaia/gdr2/gaia_source/csv/";
	private static final String TEMP_DIR_PATH = "d:\\gaia\\";
	private static final String TEMP_FILE_PATH = TEMP_DIR_PATH + "temp.gz";
	private Color[] color = { new Color(0, 0, 0), new Color(32, 32, 32), new Color(64, 64, 64), new Color(96, 96, 96), new Color(128, 128, 128), new Color(160, 160, 160), new Color(192, 192, 192), new Color(224, 224, 224), new Color(255, 255, 255) };
	public Color[] c16 = { new Color(255, 255, 255), new Color(224, 224, 224), new Color(192, 192, 192), new Color(128, 128, 128), new Color(96, 96, 96),
			new Color(64, 64, 64), new Color(56, 56, 56), new Color(48, 48, 48), new Color(44, 44, 44), new Color(40, 40, 40),
			new Color(36, 36, 36), new Color(32, 32, 32), new Color(28, 28, 28), new Color(24, 24, 24), new Color(20, 20, 20), new Color(16, 16, 16) };

	public Gaia() {
		step20();
	}

	public void step1() {
		if (input(GAIA_SOURCE_URL, false)) {
			String[] str = sb.toString().split("<a href=\"", 0);
			int len = str.length;
			int i = 0;
			Pattern p = Pattern.compile(".*.csv.gz\"");
			List<String> list = new ArrayList<String>();
			File file = null;

			for (i = 0; i < len; i++) {
				Matcher m = p.matcher(str[i]);
				while (m.find()) {
					list.add(m.group().replace("\"", ""));
				}
			}

			len = list.size();

			for (i = 0; i < len; i++) {
				file = new File(TEMP_DIR_PATH + list.get(i).replace(".gz", ""));

				if (!file.exists()) {
					if (input(GAIA_SOURCE_URL + list.get(i), true)) {
						output(file);
						System.out.println(i + ":OK:" + list.get(i));
					} else {
						System.out.println(i + ":NG:" + list.get(i));
					}
				} else {
					System.out.println(i + ":Skip:" + list.get(i));
				}
			}
		}
	}

	public void step2() {
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File file, String str) {
				if (str.length() > 53) {
					if (str.indexOf("GaiaSource_6") != -1) {
						return true;
					} else {
						return false;
					}
				} else {
					return false;
				}
			}
		};
		File[] list = new File(TEMP_DIR_PATH).listFiles(filter);

		if (list != null) {
			int i, m, n = 0, len = list.length;
			float ra = 0, ra_max = -1, ra_min = 400, ra_max_i = -1, ra_min_i = 400, dec = 0, dec_max = -100, dec_min = 100, dec_max_i = -100, dec_min_i = 100;
			PrintWriter pw = null;

			try {
				pw = new PrintWriter(new BufferedWriter(new FileWriter(new File("d:\\homepage\\astro.starfree.jp\\gaia\\table.txt"))));
			} catch (IOException e) {
				e.printStackTrace();
			}

			java.util.Arrays.sort(list, new java.util.Comparator<File>() {
				public int compare(File file1, File file2) {
					return file1.getName().compareTo(file2.getName());
				}
			});

			for (i = 0; i < len; i++) {
				if (list[i].isFile()) {
					try {
						File file = new File(list[i].toString());
						BufferedReader br = new BufferedReader(new FileReader(file));
						String line = null;
						String name = list[i].toString().replace(TEMP_DIR_PATH, "");
						String[] data = null;
						m = 0;
						ra_max_i = -1;
						ra_min_i = 400;
						dec_max_i = -100;
						dec_min_i = 100;
						System.out.println(name);

						while ((line = br.readLine()) != null) {
							m++;
							n++;
							data = line.split(",", 0);
							ra = Float.parseFloat(data[1] + "." + data[2]);
							dec = Float.parseFloat(data[3] + "." + data[4]);

							if (ra > ra_max) {
								ra_max = ra;
							} else if (ra < ra_min) {
								ra_min = ra;
							}

							if (ra > ra_max_i) {
								ra_max_i = ra;
							} else if (ra < ra_min_i) {
								ra_min_i = ra;
							}

							if (dec > dec_max) {
								dec_max = dec;
							} else if (dec < dec_min) {
								dec_min = dec;
							}

							if (dec > dec_max_i) {
								dec_max_i = dec;
							} else if (dec < dec_min_i) {
								dec_min_i = dec;
							}
						}

						br.close();
						pw.println(name + "," + String.format("%.3f", ra_min_i / 15.0f) + "," + String.format("%.3f", ra_max_i / 15.0f) + ","
								+ String.format("%+.3f", dec_min_i) + "," + String.format("%+.3f", dec_max_i) + "," + m);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			pw.println("total," + String.format("%.3f", ra_min / 15.0f) + "," + String.format("%.3f", ra_max / 15.0f) + "," + String.format("%+.3f", dec_min) + "," + String.format("%+.3f", dec_max) + "," + n);
			pw.close();

		} else {
			System.out.println("NG:File not found.");
		}
	}

	public void step3() {
		File[] list = new File(TEMP_DIR_PATH).listFiles();

		if (list != null) {
			int i, m, len = list.length;
			double ra, ra_max, ra_min, dec, dec_max, dec_min, mag, mag_max, mag_min;
			PrintWriter pw = null;

			try {
				pw = new PrintWriter(new BufferedWriter(new FileWriter(new File("d:\\homepage\\astro.starfree.jp\\gaia\\table.txt"))));
			} catch (IOException e) {
				e.printStackTrace();
			}

			java.util.Arrays.sort(list, new java.util.Comparator<File>() {
				public int compare(File file1, File file2) {
					return file1.getName().compareTo(file2.getName());
				}
			});

			for (i = 0; i < len; i++) {
				if (list[i].isFile()) {
					try {
						String line = list[i].toString();
						File file = new File(line);
						BufferedReader br = new BufferedReader(new FileReader(file));
						String name = line.replace(TEMP_DIR_PATH + "GaiaSource_", "");
						name = name.replace(".csv", "");
						String[] data = null;
						m = 0;
						ra_max = -1;
						ra_min = 400;
						dec_max = -100;
						dec_min = 100;
						mag_max = -100;
						mag_min = 100;

						while ((line = br.readLine()) != null) {
							m++;
							data = line.split(",", 0);
							ra = Double.parseDouble(data[1] + "." + data[2]);
							dec = Double.parseDouble(data[3] + "." + data[4]);
							mag = Double.parseDouble(data[5] + "." + data[6]);

							if (ra > ra_max) {
								ra_max = ra;
							} else if (ra < ra_min) {
								ra_min = ra;
							}

							if (dec > dec_max) {
								dec_max = dec;
							} else if (dec < dec_min) {
								dec_min = dec;
							}

							if (mag > mag_max) {
								mag_max = mag;
							} else if (mag < mag_min) {
								mag_min = mag;
							}
						}

						br.close();
						pw.println(name + "," + String.format("%.12f", ra_min / 15.0) + "," + String.format("%.12f", ra_max / 15.0) + ","
								+ String.format("%.12f", dec_min) + "," + String.format("%.12f", dec_max) + "," + String.format("%.6f", mag_min) + "," + String.format("%.6f", mag_max) + "," + m);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}

					System.out.println(i);
				}
			}

			pw.close();

		} else {
			System.out.println("NG:File not found.");
		}
	}

	public void step4() {
		File[] list = new File(TEMP_DIR_PATH).listFiles();

		if (list != null) {
			int i, j, ra, dec, len = list.length;
			PrintWriter pw = null;
			int map[][] = new int[182][361];
			String line = null;

			try {
				pw = new PrintWriter(new BufferedWriter(new FileWriter(new File("d:\\homepage\\astro.starfree.jp\\gaia\\table.csv"))));
			} catch (IOException e) {
				e.printStackTrace();
			}

			for (i = 0; i < len; i++) {
				if (list[i].isFile()) {
					try {
						File file = new File(list[i].toString());
						BufferedReader br = new BufferedReader(new FileReader(file));
						String[] data = null;
						System.out.println(i);

						while ((line = br.readLine()) != null) {
							data = line.split(",", 0);
							ra = Integer.parseInt(data[1]);

							if ("-0".equals(data[3])) {
								map[90][ra]++;
							} else {
								dec = Integer.parseInt(data[3]);
								if (dec < 0) {
									dec += 90;
								} else {
									dec += 91;
								}
								map[dec][ra]++;
							}
						}

						br.close();

					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			for (i = 0; i < 182; i++) {
				line = "";
				for (j = 0; j < 361; j++) {
					line += map[i][j] + ",";
				}
				pw.println(line);
			}

			pw.println("---");

			for (i = 181; i >= 0; i--) {
				line = "";
				for (j = 360; j >= 0; j--) {
					line += map[i][j] + ",";
				}
				pw.println(line);
			}

			pw.close();

		} else {
			System.out.println("NG:File not found.");
		}
	}

	public void step5() {
		int i = 0, j;
		int map[][] = new int[181][360];
		String line = null;
		String[] data = null;

		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("d:\\homepage\\astro.starfree.jp\\gaia\\gdr2_map_asc.csv")));
			br.readLine();

			while ((line = br.readLine()) != null) {
				data = line.split(",", 0);

				for (j = 1; j < 361; j++) {
					map[i][j - 1] = Integer.parseInt(data[j]);
				}

				i++;
			}

			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		BufferedImage bi = new BufferedImage(360, 180, BufferedImage.TYPE_INT_BGR);
		Graphics2D g = bi.createGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, 360, 180);

		for (i = 0; i < 180; i++) {
			for (j = 0; j < 360; j++) {
				g.setColor(color[color_id(map[i][j])]);
				g.fillRect(j, i, 1, 1);
			}
		}

		try {
			ImageIO.write(bi, "png", new File("d:\\homepage\\astro.starfree.jp\\gaia\\map_asc.png"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void step6() {
		int i, len;
		double ra, dec, mag;
		List<Double[]> star = new ArrayList<Double[]>();

		File[] list = new File(TEMP_DIR_PATH).listFiles();

		if (list != null) {
			len = list.length;

			for (i = 0; i < len; i++) {
				if (list[i].isFile()) {
					try {
						String line = list[i].toString();
						BufferedReader br = new BufferedReader(new FileReader(new File(line)));
						String[] data = null;

						while ((line = br.readLine()) != null) {
							data = line.split(",", 0);
							ra = Double.parseDouble(data[1] + "." + data[2]);
							dec = Double.parseDouble(data[3] + "." + data[4]);

							if (101.0 < ra && ra < 102.0 && -16.8 < dec && dec < -16.7) {
								mag = Float.parseFloat(data[5] + "." + data[6]);
								star.add(new Double[] { ra, dec, mag });
							}
						}

						br.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			System.out.println("NG:File not found.");
		}

		BufferedImage bi = new BufferedImage(600, 600, BufferedImage.TYPE_INT_BGR);
		Graphics2D g = bi.createGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, 600, 600);
		len = star.size();

		for (i = 0; i < len; i++) {
			g.setColor(color[color_mag(star.get(i)[2])]);
			g.fillRect((int) ((star.get(i)[0] - 101.0) * 600.0), (int) ((star.get(i)[1] + 16.8) * 6000.0), 1, 1);
		}

		try {
			ImageIO.write(bi, "png", new File("d:\\homepage\\astro.starfree.jp\\gaia\\sirius.png"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void step7() {
		int i, len;
		float ra, dec, mag;
		double r, a;
		BufferedImage bi = new BufferedImage(1800, 1800, BufferedImage.TYPE_INT_BGR);
		Graphics2D g = bi.createGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, 1800, 1800);
		File[] list = new File(TEMP_DIR_PATH).listFiles();

		if (list != null) {
			len = list.length;

			for (i = 0; i < len; i++) {
				if (list[i].isFile()) {
					try {
						String line = list[i].toString();
						BufferedReader br = new BufferedReader(new FileReader(new File(line)));
						String[] data = null;

						while ((line = br.readLine()) != null) {
							data = line.split(",", 0);
							dec = Float.parseFloat(data[3] + "." + data[4]);

							if (dec >= 89.9f) {
								ra = Float.parseFloat(data[1] + "." + data[2]);
								mag = Float.parseFloat(data[5] + "." + data[6]);
								r = 9000.0 * (90.0f - dec);
								a = Math.toRadians(ra);
								g.setColor(color[color_mag(mag)]);
								g.fillRect((int) (r * Math.cos(a)) + 900, (int) (r * Math.sin(a)) + 900, 1, 1);
							}
						}

						br.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			System.out.println("NG:File not found.");
		}

		try {
			ImageIO.write(bi, "png", new File("d:\\homepage\\astro.starfree.jp\\gaia\\a.png"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void step8() {
		File[] list = new File(TEMP_DIR_PATH).listFiles();

		if (list != null) {
			int i, n, ra_min = 99, ra_max = 0, dec_min = 99, dec_max = 0, mag_min = 99, mag_max = 0, len = list.length;
			String line = "";
			String[] data = null;
			BufferedReader br = null;

			for (i = 0; i < len; i++) {
				if (list[i].isFile()) {
					try {
						br = new BufferedReader(new FileReader(new File(list[i].toString())));

						while ((line = br.readLine()) != null) {
							data = line.split(",", 0);
							n = data[2].length();

							if (n > ra_max) {
								ra_max = n;
							}

							if (n < ra_min) {
								ra_min = n;
							}

							n = data[4].length();

							if (n > dec_max) {
								dec_max = n;
							}

							if (n < dec_min) {
								dec_min = n;
							}

							n = data[6].length();

							if (n > mag_max) {
								mag_max = n;
							}

							if (n < mag_min) {
								mag_min = n;
							}
						}

						br.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}

					System.out.println(i);
				}
			}

			System.out.println("---");
			System.out.println(ra_min + "," + ra_max + "," + dec_min + "," + dec_max + "," + mag_min + "," + mag_max);
		} else {
			System.out.println("NG:File not found.");
		}
	}

	public void step9() {
		File[] list = new File(TEMP_DIR_PATH).listFiles();

		if (list != null) {
			int i, j, ra_i, ra_f, dec_i, dec_f, mag, len = list.length;
			double ra, dec;
			String line;
			String[] data;
			BufferedReader br = null;
			BufferedOutputStream[][] bos = new BufferedOutputStream[180][360];

			for (i = 0; i < 90; i++) {
				for (j = 0; j < 360; j++) {
					bos[89 - i][j] = openBOS("-" + i + "_" + j);
					bos[i + 90][j] = openBOS("+" + i + "_" + j);
				}
			}

			for (i = 0; i < len; i++) {
				if (list[i].isFile()) {
					try {
						br = new BufferedReader(new FileReader(new File(list[i].toString())));

						while ((line = br.readLine()) != null) {
							data = line.split(",", 0);
							ra = Double.parseDouble(data[1] + "." + data[2]);
							ra_f = Integer.parseInt(String.format("%.9f", ra).split("\\.")[1]);
							ra_i = Integer.parseInt(data[1]);
							dec = Double.parseDouble(data[3] + "." + data[4]);
							dec_f = Integer.parseInt(String.format("%.9f", dec).split("\\.")[1]);

							if ("-0".equals(data[3])) {
								dec_i = 89;
							} else {
								dec_i = Integer.parseInt(data[3]);

								if (dec_i < 0) {
									dec_i += 89;
								} else {
									dec_i += 90;
								}
							}

							mag = Integer.parseInt(data[5]) - 1;
							if (mag > 15) {
								mag = 15;
							}
							writeBOS(encode8B(ra_f, dec_f, mag), bos[dec_i][ra_i]);
						}

						br.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}

					System.out.println(i);
				}
			}

			for (i = 0; i < 180; i++) {
				for (j = 0; j < 360; j++) {
					closeBOS(bos[i][j]);
					System.out.println("close:" + i + "," + j);
				}
			}
		} else {
			System.out.println("NG:File not found.");
		}
	}

	public void step10() {
		BufferedInputStream fis = null;
		int nbyte = 12;
		int i, j, k, l, len;
		byte[] rows = null;
		byte[] row = new byte[nbyte];
		int[] res = null;
		HashMap<String, Boolean> flag = null;
		String key = "";

		for (i = 89; i >= 0; i--) {
			for (j = 0; j < 360; j++) {
				try {
					File file = new File("d:\\gdr2bit\\" + "-" + i + "_" + j + ".dat");
					fis = new BufferedInputStream(new FileInputStream(file));
					rows = new byte[fis.available()];
					fis.read(rows);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (fis != null) {
							fis.close();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				len = rows.length / nbyte;
				flag = new HashMap<String, Boolean>();

				for (k = 0; k < len; k++) {
					row = new byte[nbyte];
					for (l = 0; l < nbyte; l++) {
						row[l] = rows[k * nbyte + l];
					}
					res = decode12B(row);
					key = String.format("%09d", res[1]).substring(0, 6) + "," + String.format("%09d", res[2]).substring(0, 6);
					if (flag.get(key) == null) {
						flag.put(key, true);
					} else {
						System.out.println("ng," + i + "," + j + "," + key);
						break;
					}
				}
			}
			System.out.println(i);
		}
	}

	public void step11() {
		File[] list = new File(TEMP_DIR_PATH).listFiles();

		if (list != null) {
			int i, j, id, ra_i, ra_f, dec_i, dec_f, mag, len = list.length;
			double ra, dec;
			String line;
			String[] data;
			BufferedReader br = null;
			BufferedOutputStream[][] bos = new BufferedOutputStream[180][360];

			for (i = 0; i < 90; i++) {
				for (j = 0; j < 360; j++) {
					bos[89 - i][j] = openBOS("-" + i + "_" + j);
					bos[i + 90][j] = openBOS("+" + i + "_" + j);
				}
			}

			for (i = 0; i < len; i++) {
				if (list[i].isFile()) {
					try {
						br = new BufferedReader(new FileReader(new File(list[i].toString())));

						while ((line = br.readLine()) != null) {
							data = line.split(",", 0);
							id = Integer.parseInt(data[0]);
							ra = Double.parseDouble(data[1] + "." + data[2]);
							ra_f = Integer.parseInt(String.format("%.6f", ra).split("\\.")[1]);
							ra_i = Integer.parseInt(data[1]);
							dec = Double.parseDouble(data[3] + "." + data[4]);
							dec_f = Integer.parseInt(String.format("%.6f", dec).split("\\.")[1]);

							if ("-0".equals(data[3])) {
								dec_i = 89;
							} else {
								dec_i = Integer.parseInt(data[3]);

								if (dec_i < 0) {
									dec_i += 89;
								} else {
									dec_i += 90;
								}
							}

							mag = Integer.parseInt(data[5]);
							writeBOS(encode12B(id, ra_f, dec_f, mag), bos[dec_i][ra_i]);
						}

						br.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}

					System.out.println(i);
				}
			}

			for (i = 0; i < 180; i++) {
				for (j = 0; j < 360; j++) {
					closeBOS(bos[i][j]);
					System.out.println("close:" + i + "," + j);
				}
			}
		} else {
			System.out.println("NG:File not found.");
		}
	}

	public void step12() {
		BufferedInputStream fis = null;
		int nbyte = 8;
		int mag_max = 14;
		int i, j, k, l, len;
		byte[] rows = null;
		byte[] row = new byte[nbyte];
		int[] res = null;
		boolean white = false;
		double ra, dec, r;
		double gAlpha = 0.0;
		double gDelta = 90.0;
		double gRad = Math.PI / 180;
		ArrayList<ArrayList<Integer>> x = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> y = new ArrayList<ArrayList<Integer>>();

		for (i = 0; i < mag_max; i++) {
			x.add(new ArrayList<Integer>());
			y.add(new ArrayList<Integer>());
		}

		for (i = 88; i < 90; i++) {
			for (j = 0; j < 360; j++) {
				try {
					File file = new File("d:\\gdr2bit\\+" + i + "_" + j + ".dat");
					fis = new BufferedInputStream(new FileInputStream(file));
					rows = new byte[fis.available()];
					fis.read(rows);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (fis != null) {
							fis.close();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				len = rows.length / nbyte;

				for (k = 0; k < len; k++) {
					row = new byte[nbyte];
					for (l = 0; l < nbyte; l++) {
						row[l] = rows[k * nbyte + l];
					}
					res = decode8B(row);

					if (res[2] < mag_max) {
						ra = j + res[0] / 100000000.0;
						dec = i + res[1] / 100000000.0;
						r = 45000.0 / (1 + Math.sin(gDelta * gRad) * Math.sin(dec * gRad) + Math.cos(gDelta * gRad) * Math.cos(dec * gRad) * Math.cos((ra - gAlpha) * gRad));
						x.get(res[2]).add((int) (-r * Math.cos(dec * gRad) * Math.sin((ra - gAlpha) * gRad) + 400.0));
						y.get(res[2]).add((int) (-r * (Math.cos(gDelta * gRad) * Math.sin(dec * gRad) - Math.sin(gDelta * gRad) * Math.cos(dec * gRad) * Math.cos((ra - gAlpha) * gRad)) + 400.0));
					}
				}
			}
			System.out.println(i);
		}

		for (i = 0; i < mag_max; i++) {
			BufferedImage bi = new BufferedImage(800, 800, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = bi.createGraphics();

			if (white) {
				g.setColor(Color.WHITE);
			} else {
				g.setColor(Color.BLACK);
			}

			g.fillRect(0, 0, 800, 800);

			for (j = i; j >= 0; j--) {
				len = x.get(j).size();
				for (k = 0; k < len; k++) {
					fill_star(x.get(j).get(k), y.get(j).get(k), j, g);
					draw_star(x.get(j).get(k), y.get(j).get(k), g);
				}
			}

			try {
				ImageIO.write(bi, "png", new File("d:\\m" + i + ".png"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void step13() {
		BufferedImage bi = new BufferedImage(500, 100, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bi.createGraphics();
		g.setColor(new Color(0, 0, 0));
		//g.setColor(new Color(255, 255, 255));
		g.fillRect(0, 0, 500, 100);
		//g.setColor(new Color(0, 0, 0));
		g.setColor(new Color(255, 255, 255));

		fill_star(20, 20, 0, g);
		fill_star(50, 20, 1, g);
		fill_star(80, 20, 2, g);
		fill_star(110, 20, 3, g);
		fill_star(130, 20, 4, g);
		fill_star(150, 20, 5, g);
		fill_star(170, 20, 6, g);
		fill_star(190, 20, 7, g);
		fill_star(210, 20, 8, g);
		fill_star(225, 20, 9, g);
		fill_star(240, 20, 10, g);
		fill_star(250, 20, 11, g);
		fill_star(260, 20, 12, g);
		fill_star(270, 20, 13, g);
		fill_star(280, 20, 14, g);
		fill_star(290, 20, 15, g);

		try {
			ImageIO.write(bi, "png", new File("d:\\mag.png"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void step14() {
		BufferedInputStream fis = null;
		int nbyte = 12;
		int i, j, len;
		byte[] rows = null;
		byte[] row = new byte[nbyte];
		int[] res = null;
		int[] ra = null;
		int[] dec = null;
		byte[] mag = null;
		boolean white = false;
		BufferedImage bi = new BufferedImage(750, 750, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bi.createGraphics();

		if (white) {
			g.setColor(Color.WHITE);
		} else {
			g.setColor(Color.BLACK);
		}

		g.fillRect(0, 0, 750, 750);

		try {
			File file = new File("d:\\gdr2bit\\-32_256.dat");
			fis = new BufferedInputStream(new FileInputStream(file));
			rows = new byte[fis.available()];
			fis.read(rows);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		len = rows.length / nbyte;
		ra = new int[len];
		dec = new int[len];
		mag = new byte[len];

		if (white) {
			g.setColor(Color.LIGHT_GRAY);
		} else {
			g.setColor(Color.DARK_GRAY);
		}

		for (i = 0; i < len; i++) {
			row = new byte[nbyte];
			for (j = 0; j < nbyte; j++) {
				row[j] = rows[i * nbyte + j];
			}
			res = decode8B(row);
			ra[i] = (int) (res[0] / 100000.0f);
			dec[i] = (int) (res[1] / 100000.0f);
			mag[i] = (byte) res[2];
			fill_star(ra[i], dec[i], mag[i], g);
		}

		if (white) {
			g.setColor(Color.BLACK);
		} else {
			g.setColor(Color.WHITE);
		}

		for (i = 0; i < len; i++) {
			draw_star(ra[i], dec[i], g);
		}

		try {
			ImageIO.write(bi, "png", new File("d:\\n.png"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void step15() {
		int i, len;
		double ra, dec, r;
		double gAlpha = 0.0;
		double gDelta = 90.0;
		double gRad = Math.PI / 180;
		BufferedImage bi = new BufferedImage(800, 800, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bi.createGraphics();
		List<Integer> x = new ArrayList<Integer>();
		List<Integer> y = new ArrayList<Integer>();
		List<String> s = new ArrayList<String>();

		g.setColor(Color.BLACK);
		g.fillRect(0, 0, 800, 800);

		for (dec = 87; dec < 90; dec += 0.1) {
			for (ra = 0; ra < 360; ra += 1.0) {
				r = 90000.0 / (1 + Math.sin(gDelta * gRad) * Math.sin(dec * gRad) + Math.cos(gDelta * gRad) * Math.cos(dec * gRad) * Math.cos((ra - gAlpha) * gRad));
				x.add((int) (-r * Math.cos(dec * gRad) * Math.sin((ra - gAlpha) * gRad) + 400.0));
				y.add((int) (-r * (Math.cos(gDelta * gRad) * Math.sin(dec * gRad) - Math.sin(gDelta * gRad) * Math.cos(dec * gRad) * Math.cos((ra - gAlpha) * gRad)) + 400.0));
				s.add("" + (int) ra);
			}
		}

		len = x.size();
		g.setColor(Color.DARK_GRAY);
		g.setFont(new Font("Arial", Font.PLAIN, 8));

		for (i = 0; i < len; i++) {
			g.drawString(s.get(i), x.get(i), y.get(i));
		}

		g.setColor(Color.WHITE);

		for (i = 0; i < len; i++) {
			draw_star(x.get(i), y.get(i), g);
		}

		try {
			ImageIO.write(bi, "png", new File("d:\\sample.png"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void step16() {
		BufferedInputStream fis = null;
		int nbyte = 8;
		int i, j, k, l, len;
		byte[] rows = null;
		byte[] row = new byte[nbyte];
		int[] res = null;
		boolean white = false;
		double ra, dec, r;
		double gAlpha = 0.0;
		double gDelta = -90.0;
		double gRad = Math.PI / 180;
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> y = new ArrayList<Integer>();
		ArrayList<Integer> m = new ArrayList<Integer>();

		for (i = 88; i < 90; i++) {
			for (j = 0; j < 360; j++) {
				try {
					File file = new File("d:\\gdr2bit\\-" + i + "_" + j + ".dat");
					fis = new BufferedInputStream(new FileInputStream(file));
					rows = new byte[fis.available()];
					fis.read(rows);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (fis != null) {
							fis.close();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				len = rows.length / nbyte;

				for (k = 0; k < len; k++) {
					row = new byte[nbyte];
					for (l = 0; l < nbyte; l++) {
						row[l] = rows[k * nbyte + l];
					}
					res = decode8B(row);
					ra = j + res[0] / 100000000.0;
					dec = i + res[1] / 100000000.0;
					if (gDelta < 0) {
						dec *= -1;
					}
					r = 45000.0 / (1 + Math.sin(gDelta * gRad) * Math.sin(dec * gRad) + Math.cos(gDelta * gRad) * Math.cos(dec * gRad) * Math.cos((ra - gAlpha) * gRad));
					x.add((int) (-r * Math.cos(dec * gRad) * Math.sin((ra - gAlpha) * gRad) + 400.0));
					y.add((int) (-r * (Math.cos(gDelta * gRad) * Math.sin(dec * gRad) - Math.sin(gDelta * gRad) * Math.cos(dec * gRad) * Math.cos((ra - gAlpha) * gRad)) + 400.0));
					m.add(res[2]);
				}
			}
		}

		len = x.size();
		BufferedImage bi = new BufferedImage(800, 800, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bi.createGraphics();

		if (white) {
			g.setColor(Color.WHITE);
		} else {
			g.setColor(Color.BLACK);
		}

		g.fillRect(0, 0, 800, 800);

		if (white) {
			g.setColor(Color.LIGHT_GRAY);
		} else {
			g.setColor(Color.DARK_GRAY);
		}

		for (i = 0; i < len; i++) {
			fill_star(x.get(i), y.get(i), m.get(i), g);
		}

		if (white) {
			g.setColor(Color.BLACK);
		} else {
			g.setColor(Color.WHITE);
		}

		for (i = 0; i < len; i++) {
			draw_star(x.get(i), y.get(i), g);
		}

		try {
			ImageIO.write(bi, "png", new File("d:\\map_" + gAlpha + "_" + gDelta + ".png"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void step17() {
		int hd;
		float ra = 0, dec = 0;
		PrintWriter pw;
		BufferedReader br;
		String line, str, bayer, cst, mag;
		StringBuffer sb = null;
		HashMap<String, String> greek = new HashMap<String, String>();
		greek.put("alf", "\u03b1");
		greek.put("bet", "\u03b2");
		greek.put("gam", "\u03b3");
		greek.put("del", "\u03b4");
		greek.put("eps", "\u03b5");
		greek.put("zet", "\u03b6");
		greek.put("eta", "\u03b7");
		greek.put("the", "\u03b8");
		greek.put("iot", "\u03b9");
		greek.put("kap", "\u03ba");
		greek.put("lam", "\u03bb");
		greek.put("mu.", "\u03bc");
		greek.put("nu.", "\u03bd");
		greek.put("ksi", "\u03be");
		greek.put("omi", "\u03bf");
		greek.put("pi.", "\u03c0");
		greek.put("rho", "\u03c1");
		greek.put("sig", "\u03c3");
		greek.put("tau", "\u03c4");
		greek.put("ups", "\u03c5");
		greek.put("phi", "\u03c6");
		greek.put("chi", "\u03c7");
		greek.put("psi", "\u03c8");
		greek.put("ome", "\u03c9");

		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(new File("d:\\name.csv"))));
			br = new BufferedReader(new FileReader(new File("d:\\catalog.dat")));
			while ((line = br.readLine()) != null) {
				sb = new StringBuffer(line);
				hd = Integer.parseInt(sb.substring(0, 6).replaceAll(" ", ""));
				ra = (Byte.parseByte(sb.substring(38, 40)) + Byte.parseByte(sb.substring(40, 42)) / 60.0f + (Byte.parseByte(sb.substring(42, 44)) + Byte.parseByte(sb.substring(45, 47).replaceAll(" ", "0")) / 100.0f) / 3600.0f) * 15.0f;
				dec = Byte.parseByte(sb.substring(49, 51)) + Byte.parseByte(sb.substring(51, 53)) / 60.0f + (Byte.parseByte(sb.substring(53, 55)) + Byte.parseByte(sb.substring(56, 57).replaceAll(" ", "0")) / 10.0f) / 3600.0f;
				if ("-".equals(sb.substring(48, 49))) {
					dec *= -1;
				}
				str = sb.substring(58, 63).replaceAll(" ", "");
				if ("".equals(str)) {
					mag = "99.99";
				} else {
					mag = str;
				}
				cst = sb.substring(74, 77).replaceAll(" ", "");
				str = sb.substring(68, 73).replaceAll(" ", "");
				if ("".equals(str)) {
					str = sb.substring(64, 67).replaceAll(" ", "");
					if ("".equals(str)) {
						bayer = "";
					} else {
						bayer = "" + Integer.parseInt(str);
					}

				} else {
					if (str.length() >= 3) {
						bayer = greek.get(str.substring(0, 3));
						if (bayer == null) {
							if ("0".equals(str.substring(1, 2))) {
								bayer = str.replaceAll("0", "");
							} else {
								bayer = str;
							}
						} else {
							if (str.length() >= 4) {
								if ("0".equals(str.substring(3, 4))) {
									bayer += str.substring(4, 5);
								} else {
									bayer += str.substring(3, 5);
								}
							}
						}
					} else {
						bayer = str;
					}
				}

				pw.println(hd + "," + String.format("%.9f", ra) + "," + String.format("%.9f", dec) + "," + mag + "," + bayer + " " + cst);
			}
			br.close();
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void step18() {
		int i, len;
		String line;
		String[] data;
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> y = new ArrayList<Integer>();
		ArrayList<Integer> m = new ArrayList<Integer>();
		ArrayList<String> s = new ArrayList<String>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("d:\\name.csv")));
			br.readLine();
			while ((line = br.readLine()) != null) {
				data = line.split(",", 0);
				x.add((int) ((360.0f - Float.parseFloat(data[1])) * 10.0f));
				y.add((int) ((180.0f - (Float.parseFloat(data[2]) + 90.0f)) * 10.0f));
				m.add((int) (Float.parseFloat(data[3]) + 2.0f));
				s.add(data[4]);
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		len = x.size();
		BufferedImage bi = new BufferedImage(3600, 1800, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bi.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, 3600, 1800);
		g.setColor(Color.BLACK);

		for (i = 0; i < len; i++) {
			fill_star2(x.get(i), y.get(i), m.get(i), g);
		}

		g.setColor(Color.GRAY);
		g.setFont(new Font("Arial", Font.PLAIN, 12));

		for (i = 0; i < len; i++) {
			g.drawString(s.get(i), x.get(i), y.get(i));
		}

		try {
			ImageIO.write(bi, "png", new File("d:\\name.png"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void step19() {
		int i, j, k, l, len;
		String line;
		String[] data;
		ArrayList<Double> sr = new ArrayList<Double>();
		ArrayList<Double> sd = new ArrayList<Double>();
		ArrayList<String> ss = new ArrayList<String>();
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Integer> y = new ArrayList<Integer>();
		ArrayList<Integer> m = new ArrayList<Integer>();
		BufferedInputStream fis = null;
		int nbyte = 8;
		byte[] rows = null;
		byte[] row = new byte[nbyte];
		int[] res = null;

		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("d:\\name.csv")));
			br.readLine();
			while ((line = br.readLine()) != null) {
				data = line.split(",", 0);
				sr.add(Double.parseDouble(data[1]));
				sd.add(Double.parseDouble(data[2]));
				ss.add(data[4]);
			}
			br.close();

			for (i = 0; i < 90; i++) {
				for (j = 0; j < 360; j++) {
					File file = new File("d:\\gdr2bit\\+" + i + "_" + j + ".dat");
					fis = new BufferedInputStream(new FileInputStream(file));
					rows = new byte[fis.available()];
					fis.read(rows);
					fis.close();
					len = rows.length / nbyte;
					x = new ArrayList<Integer>();
					y = new ArrayList<Integer>();
					m = new ArrayList<Integer>();

					for (k = 0; k < len; k++) {
						row = new byte[nbyte];
						for (l = 0; l < nbyte; l++) {
							row[l] = rows[k * nbyte + l];
						}
						res = decode8B(row);
						x.add((int) ((1000000000 - res[0]) / 1000000.0));
						y.add((int) ((1000000000 - res[1]) / 1000000.0));
						m.add(res[2]);
					}

					BufferedImage bi = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
					Graphics2D g = bi.createGraphics();
					g.setColor(Color.WHITE);
					g.fillRect(0, 0, 1000, 1000);
					g.setColor(Color.LIGHT_GRAY);
					len = x.size();

					for (k = 0; k < len; k++) {
						fill_star(x.get(k), y.get(k), m.get(k), g);
					}

					g.setColor(Color.BLACK);
					len = x.size();

					for (k = 0; k < len; k++) {
						draw_star(x.get(k), y.get(k), g);
					}

					g.setColor(Color.DARK_GRAY);
					g.setFont(new Font("Arial", Font.PLAIN, 12));
					len = ss.size();

					for (k = 0; k < len; k++) {
						if (j <= sr.get(k) && sr.get(k) <= j + 1 && i <= sd.get(k) && sd.get(k) <= i + 1) {
							g.drawString(ss.get(k), (int) ((1000000000.0 - (sr.get(k) - j) * 1000000000.0) / 1000000.0), (int) ((1000000000.0 - (sd.get(k) - i) * 1000000000.0) / 1000000.0));
						}
					}

					try {
						ImageIO.write(bi, "png", new File("d:\\gdr2img\\+" + i + "_" + j + ".png"));
					} catch (Exception e) {
						e.printStackTrace();
					}

					System.out.println(j + "," + i);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void step20() {
		int a, b, c, d, i, j, k, l, m, s, x, y, w = 3840, h = 2160;
		String line, str;
		String[] data;
		ArrayList<Integer> sx = null;
		ArrayList<Integer> sy = null;
		ArrayList<Rectangle> rs = null;
		ArrayList<Boolean> sb = null;
		ArrayList<Double> sr = new ArrayList<Double>();
		ArrayList<Double> sd = new ArrayList<Double>();
		ArrayList<String> ss = null;
		ArrayList<String> sss = new ArrayList<String>();
		BufferedInputStream fis = null;
		BufferedReader br = null;
		int nbyte = 8;
		byte[] rows = null;
		byte[] row = new byte[nbyte];
		int[] res = null;
		boolean label_flag = false;

		try {
			if (label_flag) {
				br = new BufferedReader(new FileReader(new File("d:\\name.csv")));
				br.readLine();

				while ((line = br.readLine()) != null) {
					data = line.split(",", 0);
					sr.add(Double.parseDouble(data[1]));
					sd.add(Double.parseDouble(data[2]));
					sss.add(data[4]);
				}

				br.close();
				br = new BufferedReader(new FileReader(new File("d:\\ni.csv")));
				br.readLine();

				while ((line = br.readLine()) != null) {
					data = line.split(",", 0);
					sr.add(Double.parseDouble(data[0]));
					sd.add(Double.parseDouble(data[1]));
					str = "";
					if ("N".equals(data[2])) {
						str = "NGC";
					} else if ("I".equals(data[2])) {
						str = "IC";
					} else {
						str = "";
					}
					str = str + data[3];
					if (data.length > 4) {
						if (!"".equals(data[4])) {
							str = str + "(" + data[4] + ")";
						}
					}
					sss.add(str);
				}

				br.close();
			}

			for (i = 0; i < 216; i++) {
				BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
				Graphics2D g = bi.createGraphics();
				//g.setColor(Color.WHITE);
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, w, h);
				//g.setColor(Color.LIGHT_GRAY);
				g.setColor(Color.DARK_GRAY);
				a = i / 24 * 10;
				b = a + 10;
				c = i % 24 * 15;
				d = c + 15;
				sx = new ArrayList<Integer>();
				sy = new ArrayList<Integer>();

				for (j = a; j < b; j++) {
					for (k = c; k < d; k++) {
						File file = new File("d:\\gdr2bit\\-" + j + "_" + k + ".dat");
						fis = new BufferedInputStream(new FileInputStream(file));
						rows = new byte[fis.available()];
						fis.read(rows);
						fis.close();
						s = rows.length / nbyte;

						for (l = 0; l < s; l++) {
							row = new byte[nbyte];
							for (m = 0; m < nbyte; m++) {
								row[m] = rows[l * nbyte + m];
							}
							res = decode8B(row);
							x = (int) ((1.0 - (k + res[0] / 1000000000.0 - c) / 15.0) * w);
							y = (int) (((j + res[1] / 1000000000.0 - a) / 10.0) * h);
							if (res[2] == 15) {
								g.fillRect(x, y, 1, 1);
							} else {
								sx.add(x);
								sy.add(y);
							}
						}
					}
				}

				s = sx.size();
				//g.setColor(Color.BLACK);
				g.setColor(Color.WHITE);

				for (j = 0; j < s; j++) {
					g.fillRect(sx.get(j), sy.get(j), 1, 1);
				}

				if (label_flag) {
					g.setFont(new Font("Arial", Font.PLAIN, 9));
					FontMetrics fm = g.getFontMetrics();
					Rectangle rect = null;
					s = sss.size();
					rs = new ArrayList<Rectangle>();
					ss = new ArrayList<String>();
					sb = new ArrayList<Boolean>();

					for (j = 0; j < s; j++) {
						if (c <= sr.get(j) && sr.get(j) <= d && a <= sd.get(j) && sd.get(j) <= b) {
							x = (int) ((1.0 - (sr.get(j) - c) / 15.0) * w);
							y = (int) ((1.0 - (sd.get(j) - a) / 10.0) * h);
							rect = fm.getStringBounds(sss.get(j), g).getBounds();
							rect.setLocation(x - (int) (rect.getWidth() / 2.0), y + 6);
							rs.add(rect);
							ss.add(sss.get(j));
							sb.add(true);
						}
					}

					s = ss.size();

					if (s > 0) {
						g.setColor(Color.RED);
						if (s > 1) {
							for (j = 0; j < s; j++) {
								for (k = j; k < s; k++) {
									if (j != k && rs.get(j).intersects(rs.get(k))) {
										sb.set(k, false);
									}
								}
							}
							for (j = 0; j < s; j++) {
								if (sb.get(j)) {
									g.drawString(ss.get(j), rs.get(j).x, rs.get(j).y);
								}
							}
						} else {
							g.drawString(ss.get(0), rs.get(0).x, rs.get(0).y);
						}
					}
				}

				line = "d:\\gdr2img\\-" + a + "-" + b + "_" + c + "-" + d + ".png";

				try {
					ImageIO.write(bi, "png", new File(line));
				} catch (Exception e) {
					e.printStackTrace();
				}

				System.out.println(line);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void fill_star(int x, int y, int mag, Graphics2D g) {
		if (mag < 15) {
			if (mag > 12) {
				g.fillOval(x - 2, y - 2, 4, 4);
				g.drawOval(x - 2, y - 2, 4, 4);
			} else if (mag > 10) {
				g.fillOval(x - 3, y - 3, 6, 6);
				g.drawOval(x - 3, y - 3, 6, 6);
			} else if (mag > 8) {
				g.fillOval(x - 4, y - 4, 8, 8);
				g.drawOval(x - 4, y - 4, 8, 8);
			} else if (mag > 6) {
				g.fillOval(x - 5, y - 5, 10, 10);
				g.drawLine(x - 5, y - 1, x - 5, y + 1);
				g.drawLine(x - 4, y - 3, x - 3, y - 4);
				g.drawLine(x - 4, y + 3, x - 3, y + 4);
				g.drawLine(x - 1, y - 5, x + 1, y - 5);
				g.drawLine(x - 1, y + 5, x + 1, y + 5);
				g.drawLine(x + 3, y - 4, x + 4, y - 3);
				g.drawLine(x + 3, y + 4, x + 4, y + 3);
				g.drawLine(x + 5, y - 1, x + 5, y + 1);
			} else if (mag > 4) {
				g.fillOval(x - 6, y - 6, 12, 12);
				g.drawOval(x - 6, y - 6, 12, 12);
				g.drawLine(x - 5, y - 4, x - 4, y - 5);
				g.drawLine(x - 5, y + 4, x - 4, y + 5);
				g.drawLine(x + 4, y - 5, x + 5, y - 4);
				g.drawLine(x + 4, y + 5, x + 5, y + 4);
			} else if (mag > 2) {
				g.fillOval(x - 7, y - 7, 14, 14);
				g.drawOval(x - 7, y - 7, 14, 14);
			} else if (mag > 1) {
				g.fillOval(x - 8, y - 8, 16, 16);
				g.drawOval(x - 8, y - 8, 16, 16);
			} else if (mag > 0) {
				g.fillOval(x - 9, y - 9, 18, 18);
				g.drawOval(x - 9, y - 9, 18, 18);
			} else {
				g.fillOval(x - 10, y - 10, 20, 20);
				g.drawOval(x - 10, y - 10, 20, 20);
			}
		}
	}

	private void fill_star2(int x, int y, int mag, Graphics2D g) {
		if (mag > 8) {
			g.fillRect(x, y, 1, 1);
		} else if (mag > 7) {
			g.fillOval(x - 2, y - 2, 4, 4);
			g.drawOval(x - 2, y - 2, 4, 4);
		} else if (mag > 6) {
			g.fillOval(x - 3, y - 3, 6, 6);
			g.drawOval(x - 3, y - 3, 6, 6);
		} else if (mag > 5) {
			g.fillOval(x - 4, y - 4, 8, 8);
			g.drawOval(x - 4, y - 4, 8, 8);
		} else if (mag > 4) {
			g.fillOval(x - 5, y - 5, 10, 10);
			g.drawLine(x - 5, y - 1, x - 5, y + 1);
			g.drawLine(x - 4, y - 3, x - 3, y - 4);
			g.drawLine(x - 4, y + 3, x - 3, y + 4);
			g.drawLine(x - 1, y - 5, x + 1, y - 5);
			g.drawLine(x - 1, y + 5, x + 1, y + 5);
			g.drawLine(x + 3, y - 4, x + 4, y - 3);
			g.drawLine(x + 3, y + 4, x + 4, y + 3);
			g.drawLine(x + 5, y - 1, x + 5, y + 1);
		} else if (mag > 3) {
			g.fillOval(x - 6, y - 6, 12, 12);
			g.drawOval(x - 6, y - 6, 12, 12);
			g.drawLine(x - 5, y - 4, x - 4, y - 5);
			g.drawLine(x - 5, y + 4, x - 4, y + 5);
			g.drawLine(x + 4, y - 5, x + 5, y - 4);
			g.drawLine(x + 4, y + 5, x + 5, y + 4);
		} else if (mag > 2) {
			g.fillOval(x - 7, y - 7, 14, 14);
			g.drawOval(x - 7, y - 7, 14, 14);
		} else if (mag > 1) {
			g.fillOval(x - 8, y - 8, 16, 16);
			g.drawOval(x - 8, y - 8, 16, 16);
		} else if (mag > 0) {
			g.fillOval(x - 9, y - 9, 18, 18);
			g.drawOval(x - 9, y - 9, 18, 18);
		} else {
			g.fillOval(x - 10, y - 10, 20, 20);
			g.drawOval(x - 10, y - 10, 20, 20);
		}
	}

	private void draw_star(int x, int y, Graphics2D g) {
		g.fillRect(x, y, 1, 1);
	}

	private int color_mag(double n) {
		int ret = 0;

		if (n < 6) {
			ret = 8;
		} else if (n < 9) {
			ret = 7;
		} else if (n < 12) {
			ret = 6;
		} else if (n < 14) {
			ret = 5;
		} else if (n < 16) {
			ret = 4;
		} else if (n < 18) {
			ret = 3;
		} else if (n < 20) {
			ret = 2;
		} else {
			ret = 1;
		}

		return ret;
	}

	private int color_id(int n) {
		int ret = 0;

		if (n >= 500000) {
			ret = 8;
		} else if (n >= 100000) {
			ret = 7;
		} else if (n >= 50000) {
			ret = 6;
		} else if (n >= 10000) {
			ret = 5;
		} else if (n >= 5000) {
			ret = 4;
		} else if (n >= 1000) {
			ret = 3;
		} else if (n >= 500) {
			ret = 2;
		} else if (n >= 100) {
			ret = 1;
		} else {
			ret = 0;
		}

		return ret;
	}

	private boolean input(String url_str, boolean save) {
		boolean ret = false;
		HttpURLConnection con = null;

		try {
			Thread.sleep(3000);
			URL url = new URL(url_str);
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.connect();
			final int status = con.getResponseCode();

			if (status == HttpURLConnection.HTTP_OK) {
				final InputStream is = con.getInputStream();

				if (save) {
					DataInputStream dis = new DataInputStream(is);
					FileOutputStream fos = new FileOutputStream(TEMP_FILE_PATH);
					BufferedOutputStream bos = new BufferedOutputStream(fos);
					DataOutputStream dos = new DataOutputStream(bos);
					byte[] b = new byte[4096];
					int n = 0;

					while (-1 != (n = dis.read(b))) {
						dos.write(b, 0, n);
					}

					dos.close();
					bos.close();
					fos.close();
					dis.close();

				} else {
					String encoding = con.getContentEncoding();

					if (encoding == null) {
						encoding = "UTF-8";
					}

					final InputStreamReader isr = new InputStreamReader(is, encoding);
					final BufferedReader br = new BufferedReader(isr);
					String line = null;
					sb = new StringBuffer();

					while ((line = br.readLine()) != null) {
						sb.append(line);
					}

					br.close();
					isr.close();
				}

				is.close();
				ret = true;
			} else {
				System.out.println(status);
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}

		return ret;
	}

	private void output(File fo) {
		File fi = new File(TEMP_FILE_PATH);

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new BufferedInputStream(new FileInputStream(fi)))));
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(fo)));
			String line = br.readLine();
			String[] data = null;

			while ((line = br.readLine()) != null) {
				data = line.split(",", 0);
				pw.println(data[3] + "," + data[5].replace(".", ",") + "," + data[7].replace(".", ",") + "," + data[50].replace(".", ","));
			}

			br.close();
			pw.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		fi.delete();
	}

	private BufferedOutputStream openBOS(String fileName) {
		BufferedOutputStream bos = null;
		FileOutputStream fos = null;
		File file = new File("d:\\gdr2bit\\" + fileName + ".dat");

		try {
			fos = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		bos = new BufferedOutputStream(fos);
		return bos;
	}

	private void closeBOS(BufferedOutputStream bos) {
		if (bos != null) {
			try {
				bos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void writeBOS(byte[] data, BufferedOutputStream bos) {
		int i = 0;
		int len = data.length;

		try {
			for (i = 0; i < len; i++) {
				bos.write(data[i]);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private byte[] encode8B(int ra, int dec, int mag) {
		byte[] ret = new byte[8];
		int a = 0;
		int b = 0;
		ret[0] = (byte) ((ra >>> 22) & 0x00ff);
		ret[1] = (byte) ((ra >>> 14) & 0x00ff);
		ret[2] = (byte) ((ra >>> 6) & 0x00ff);
		a = (ra << 2) & 0x00fc;
		b = (dec >>> 28) & 0x0003;
		ret[3] = (byte) ((a | b) & 0x00ff);
		ret[4] = (byte) ((dec >>> 20) & 0x00ff);
		ret[5] = (byte) ((dec >>> 12) & 0x00ff);
		ret[6] = (byte) ((dec >>> 4) & 0x00ff);
		a = (dec << 4) & 0x00f0;
		b = mag & 0x000f;
		ret[7] = (byte) ((a | b) & 0x00ff);
		return ret;
	}

	private byte[] encode12B(int id, int ra, int dec, int mag) {
		byte[] ret = new byte[12];
		int a = 0;
		int b = 0;
		ret[0] = (byte) ((id >>> 23) & 0x00ff);
		ret[1] = (byte) ((id >>> 15) & 0x00ff);
		ret[2] = (byte) ((id >>> 7) & 0x00ff);
		a = (id << 1) & 0x00fe;
		b = (ra >>> 29) & 0x0001;
		ret[3] = (byte) ((a | b) & 0x00ff);
		ret[4] = (byte) ((ra >>> 21) & 0x00ff);
		ret[5] = (byte) ((ra >>> 13) & 0x00ff);
		ret[6] = (byte) ((ra >>> 5) & 0x00ff);
		a = (ra << 3) & 0x00f8;
		b = (dec >>> 27) & 0x0007;
		ret[7] = (byte) ((a | b) & 0x00ff);
		ret[8] = (byte) ((dec >>> 19) & 0x00ff);
		ret[9] = (byte) ((dec >>> 11) & 0x00ff);
		ret[10] = (byte) ((dec >>> 3) & 0x00ff);
		a = (dec << 5) & 0x00e0;
		b = mag & 0x001f;
		ret[11] = (byte) ((a | b) & 0x00ff);
		return ret;
	}

	public int[] decode8B(byte[] data) {
		int[] ret = new int[3];
		ret[0] = ((data[0] << 22) & 0x3fc00000) | ((data[1] << 14) & 0x003fc000) | ((data[2] << 6) & 0x00003fc0) | ((data[3] >>> 2) & 0x0000003f);
		ret[1] = (data[3] << 28) & 0x30000000 | ((data[4] << 20) & 0x0ff00000) | ((data[5] << 12) & 0x000ff000) | ((data[6] << 4) & 0x00000ff0) | ((data[7] >>> 4) & 0x0000000f);
		ret[2] = data[7] & 0x0000000f;
		return ret;
	}

	public int[] decode12B(byte[] data) {
		int[] ret = new int[4];
		ret[0] = (data[0] << 23) & 0x7f800000 | ((data[1] << 15) & 0x007f8000) | ((data[2] << 7) & 0x00007f80) | ((data[3] >>> 1) & 0x0000007f);
		ret[1] = (data[3] << 29) & 0x20000000 | ((data[4] << 21) & 0x1fe00000) | ((data[5] << 13) & 0x001fe000) | ((data[6] << 5) & 0x00001fe0) | ((data[7] >>> 3) & 0x0000001f);
		ret[2] = (data[7] << 27) & 0x38000000 | ((data[8] << 19) & 0x07f80000) | ((data[9] << 11) & 0x0007f800) | ((data[10] << 3) & 0x000007f8) | ((data[11] >>> 5) & 0x00000007);
		ret[3] = data[11] & 0x0000001f;
		return ret;
	}
}
