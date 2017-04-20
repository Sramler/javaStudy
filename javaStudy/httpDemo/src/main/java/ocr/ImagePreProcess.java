package ocr;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;

/**
 * @author 张鹏科
 *
 */
public class ImagePreProcess {
	public static String tmpcookies = "";
	// public int isBlack(int colorInt) {
	// Color color = new Color(colorInt);
	// if (color.getRed() + color.getGreen() + color.getBlue() <= 100) {
	// return 1;
	// }
	// return 0;
	// }

	public static int isWhite(int colorInt) {
		Color color = new Color(colorInt);
		if (color.getRed() + color.getGreen() + color.getBlue() > 430) {
			return 1;
		}
		return 0;
	}

	public static String imgBinarization(String imgPath, String codesUrl) throws IOException {
		if (imgPath == null) {
			// imgPath = downloadImage(codesUrl);
		}
		BufferedImage img = ImageIO.read(new File(imgPath));
		int width = img.getWidth();
		int height = img.getHeight();
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				if (isWhite(img.getRGB(x, y)) == 1) {
					img.setRGB(x, y, Color.WHITE.getRGB());
				} else {
					img.setRGB(x, y, Color.BLACK.getRGB());
				}
			}
		}
		String[] str = imgPath.split("\\.")[0].split("/");
		ImageIO.write(img, "gif", new File(str[str.length - 1] + ".jpg"));
		return str[str.length - 1] + ".jpg";
	}

	public static String executeTesseract(String imgPath, String codesUrl) {
		String resultPath = null;
		// if (imgPath.split("\\.")[1].equals("gif")) {
		// resultPath = System.getProperty("user.dir") + "\\result\\" + "gif";
		// } else {
		// resultPath = System.getProperty("user.dir") + "\\result\\" + "jpg";
		// }
		String imgPath1 = "D:\\image.gif";

		Runtime runtime = Runtime.getRuntime();
		resultPath = System.getProperty("user.dir") + "\\result\\" + "jpg";
		try {

			String cmd = "tesseract " + imgPath1 + " " + resultPath;
			Process p = runtime.exec(cmd);
			p.waitFor();
		} catch (Exception e) {
			System.out.println("Error!");
		}

		return resultPath + ".txt";

	}

	public static String readTxtFile(String filePath, String codesUrl) {
		String txtValue = null;
		try {
			String encoding = "utf-8";
			File file = new File(filePath);
			if (file.isFile() && file.exists()) { // 判断文件是否存在
				InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);// 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				txtValue = bufferedReader.readLine();
				read.close();
			} else {
				System.out.println("找不到指定的文件");
			}
		} catch (Exception e) {
			System.out.println("读取文件内容出错");
			e.printStackTrace();
		}
		return txtValue;

	}

	public String resultCodesValue(String codesUrl) throws IOException {
		return codesUrl;
		// return
		// readTxtFile(executeTesseract(imgBinarization(downloadImage(codesUrl),
		// codesUrl), codesUrl), codesUrl);

	}

	public static void main(String[] args) throws Exception {
		String codesUrl = "https://passport.zhaopin.com/checkcode/imgrd";
		// downloadImage(codesUrl);
		String imgPath = "D:\\image.png";
		String resultPath = executeTesseract(imgPath, codesUrl);
		System.out.println(resultPath);

	}

}
