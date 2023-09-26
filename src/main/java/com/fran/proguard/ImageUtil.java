package com.fran.proguard;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;


/**
 * @author 程良明
 * @date 2023/9/9
 * * * 说明: 图片处理工具类：
 * 功能：缩放图像、切割图像、图像类型转换、彩色转黑白、文字水印、图片水印等
 **/

public class ImageUtil {

	/**
	 * 几种常见的图片格式
	 */
	public static String IMAGE_TYPE_GIF = "gif";// 图形交换格式
	public static String IMAGE_TYPE_JPG = "jpg";// 联合照片专家组
	public static String IMAGE_TYPE_JPEG = "jpeg";// 联合照片专家组
	public static String IMAGE_TYPE_BMP = "bmp";// 英文Bitmap（位图）的简写，它是Windows操作系统中的标准图像文件格式
	public static String IMAGE_TYPE_PNG = "png";// 可移植网络图形
	public static String IMAGE_TYPE_PSD = "psd";// Photoshop的专用格式Photoshop

	/**
	 * 程序入口：用于测试
	 *
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		/**
		 // 1-缩放图像：
		 // 方法一：按比例缩放
		 ImageUtil.scale("e:/abc.jpg", "e:/abc_scale.jpg", 2, true);//测试OK
		 // 方法二：按高度和宽度缩放
		 ImageUtil.scale2("e:/abc.jpg", "e:/abc_scale2.jpg", 500, 300, true);//测试OK

		 // 2-切割图像：
		 // 方法一：按指定起点坐标和宽高切割
		 ImageUtil.cut("e:/abc.jpg", "e:/abc_cut.jpg", 0, 0, 400, 400 );//测试OK
		 // 方法二：指定切片的行数和列数
		 ImageUtil.cut2("e:/abc.jpg", "e:/", 2, 2 );//测试OK
		 // 方法三：指定切片的宽度和高度
		 ImageUtil.cut3("e:/abc.jpg", "e:/", 300, 300 );//测试OK

		 // 3-图像类型转换：
		 ImageUtil.convert("e:/abc.jpg", "GIF", "e:/abc_convert.gif");//测试OK

		 // 4-彩色转黑白：
		 ImageUtil.gray("e:/abc.jpg", "e:/abc_gray.jpg");//测试OK

		 // 5-给图片添加文字水印：
		 // 方法一：
		 ImageUtil.pressText("我是水印文字","e:/abc.jpg","e:/abc_pressText.jpg","宋体",Font.BOLD,Color.white,80, 0, 0, 0.5f);//测试OK
		 // 方法二：
		 ImageUtil.pressText2("我也是水印文字", "e:/abc.jpg","e:/abc_pressText2.jpg", "黑体", 36, Color.white, 80, 0, 0, 0.5f);//测试OK

		 // 6-给图片添加图片水印：
		 ImageUtil.pressImage("e:/abc2.jpg", "e:/abc.jpg","e:/abc_pressImage.jpg", 0, 0, 0.5f);//测试OK
		 */
		//ImageUtil.changeContrast("D:\\site\\web\\anzhi_play\\i\\15b40f768607c968672c4624b.jpg", "D:\\site\\web\\anzhi_play\\i\\test.jpg", 100);
		ImageUtil.randomImage(new File("c:/temp/icon.png"), new File("c:/temp/icon1.png"), "png", 10);//测试OK
	}

	/**
	 * 缩放图像（按比例缩放）
	 *
	 * @param srcImageFile 源图像文件地址
	 * @param result       缩放后的图像地址
	 * @param scale        缩放比例
	 * @param flag         缩放选择:true 放大; false 缩小;
	 */
	public final static void scale(String srcImageFile, String result, int scale, boolean flag) {
		try {
			BufferedImage src = ImageIO.read(new File(srcImageFile)); // 读入文件
			int width = src.getWidth(); // 得到源图宽
			int height = src.getHeight(); // 得到源图长
			if (flag) {// 放大
				width = width * scale;
				height = height * scale;
			} else {// 缩小
				width = width / scale;
				height = height / scale;
			}
			Image image = src.getScaledInstance(width, height, Image.SCALE_DEFAULT);
			BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics g = tag.getGraphics();
			g.drawImage(image, 0, 0, null); // 绘制缩小后的图
			g.dispose();
			ImageIO.write(tag, "JPEG", new File(result));// 输出到文件流
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * 无拉伸缩放
	 */
	public final static void scaleAdapt(String srcImageFile, String result, int width, int height) {
		File f = new File(srcImageFile);
		try {
			BufferedImage bi = ImageIO.read(f);
			int srcW = bi.getWidth();
			int srcH = bi.getHeight();
			if (srcW == width && srcH == height) {
				ImageIO.write(bi, "JPEG", new File(result));
				return;
			}
			float rate = 1;
			boolean targetWidther = (width / height) > 1;
			if (targetWidther) {
				rate = width / (float) srcW;
			} else {
				rate = height / (float) srcH;
			}
			int scaleW = (int) (srcW * rate);
			int scaleH = (int) (srcH * rate);

			if (scaleW < width) {
				rate = width / (float) scaleW;
				scaleW *= rate;
				scaleH *= rate;
			}
			if (scaleH < height) {
				rate = height / (float) scaleH;
				scaleW *= rate;
				scaleH *= rate;
			}
			Image image = bi.getScaledInstance(scaleW, scaleH, Image.SCALE_DEFAULT);
			bi = new BufferedImage(scaleW, scaleH, BufferedImage.TYPE_INT_RGB);
			Graphics g = bi.getGraphics();
			g.drawImage(image, 0, 0, null); // 绘制缩放后的图
			g.dispose();
			int x = 0, y = 0;
			// 高度不变，对齐中间切宽度
			x = (bi.getWidth() - width) / 2;
			// 宽度不变，对齐中间高度
			y = (bi.getHeight() - height) / 2;
			image = bi.getScaledInstance(bi.getWidth(), bi.getHeight(), Image.SCALE_DEFAULT);
			// 四个参数分别为图像起点坐标和宽高
			// 即: CropImageFilter(int x,int y,int width,int height)
			ImageFilter cropFilter = new CropImageFilter(x, y, width, height);
			Image img = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(image.getSource(), cropFilter));
			bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			g = bi.getGraphics();
			g.drawImage(img, 0, 0, width, height, null); // 绘制切割后的图
			g.dispose();
			// 输出为文件
			ImageIO.write(bi, "JPEG", new File(result));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public final static BufferedImage scaleAdapt(BufferedImage src, int width, int height) {
		BufferedImage bi = src;
		int srcW = bi.getWidth();
		int srcH = bi.getHeight();
		if (srcW == width && srcH == height) return bi;
		float rate = 1;
		boolean targetWidther = (width / height) > 1;
		if (targetWidther) {
			rate = width / (float) srcW;
		} else {
			rate = height / (float) srcH;
		}
		int scaleW = (int) (srcW * rate);
		int scaleH = (int) (srcH * rate);

		if (scaleW < width) {
			rate = width / (float) scaleW;
			scaleW *= rate;
			scaleH *= rate;
		}
		if (scaleH < height) {
			rate = height / (float) scaleH;
			scaleW *= rate;
			scaleH *= rate;
		}
		Image image = bi.getScaledInstance(scaleW, scaleH, Image.SCALE_SMOOTH);
		bi = new BufferedImage(scaleW, scaleH, BufferedImage.TYPE_INT_RGB);
		Graphics g = bi.getGraphics();
		g.drawImage(image, 0, 0, null); // 绘制缩放后的图
		g.dispose();
		int x = 0, y = 0;
		// 高度不变，对齐中间切宽度
		x = (bi.getWidth() - width) / 2;
		// 宽度不变，对齐中间高度
		y = (bi.getHeight() - height) / 2;
		image = bi.getScaledInstance(bi.getWidth(), bi.getHeight(), Image.SCALE_DEFAULT);
		// 四个参数分别为图像起点坐标和宽高
		// 即: CropImageFilter(int x,int y,int width,int height)
		ImageFilter cropFilter = new CropImageFilter(x, y, width, height);
		Image img = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(image.getSource(), cropFilter));
		bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		g = bi.getGraphics();
		g.drawImage(img, 0, 0, width, height, null); // 绘制切割后的图
		g.dispose();
		return bi;
	}

	private static BufferedImage cut(BufferedImage src, int x, int y, int width, int height) {
		Image image = src.getScaledInstance(src.getWidth(), src.getHeight(), Image.SCALE_DEFAULT);
		// 四个参数分别为图像起点坐标和宽高
		// 即: CropImageFilter(int x,int y,int width,int height)
		ImageFilter cropFilter = new CropImageFilter(x, y, width, height);
		Image img = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(image.getSource(), cropFilter));
		BufferedImage tmp = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics g = tmp.getGraphics();
		g.drawImage(img, 0, 0, width, height, null); // 绘制切割后的图
		g.dispose();
		return tmp;
	}

	/**
	 * 缩放并补白
	 */
	public static BufferedImage scale2(BufferedImage bi, int width, int height) {
		Image itemp = bi.getScaledInstance(width, height, bi.SCALE_SMOOTH);
		double ratiox = width / (double) bi.getWidth();
		double ratioy = height / (double) bi.getHeight();
		AffineTransformOp op = new AffineTransformOp(AffineTransform.getScaleInstance(ratiox, ratioy), null);
		itemp = op.filter(bi, null);

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, width, height);
		if (width == itemp.getWidth(null))
			g.drawImage(itemp, 0, (height - itemp.getHeight(null)) / 2, itemp.getWidth(null), itemp.getHeight(null), Color.white, null);
		else
			g.drawImage(itemp, (width - itemp.getWidth(null)) / 2, 0, itemp.getWidth(null), itemp.getHeight(null), Color.white, null);
		g.dispose();
		return image;
	}

	/**
	 * Converts a given Image into a BufferedImage
	 *
	 * @param img The Image to be converted
	 * @return The converted BufferedImage
	 */
	public static BufferedImage toBufferedImage(Image img) {
		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
		}

		// Create a buffered image with transparency
		BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		// Draw the image on to the buffered image
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();

		// Return the buffered image
		return bimage;
	}

	/**
	 * 缩放图像（按高度和宽度缩放）
	 *
	 * @param srcImageFile 源图像文件地址
	 * @param result       缩放后的图像地址
	 * @param height       缩放后的高度
	 * @param width        缩放后的宽度
	 * @param bb           比例不对时是否需要补白：true为补白; false为不补白;
	 */
	public final static void scale2(String srcImageFile, String result, int height, int width, boolean bb) {
		try {
			double ratio = 0.0; // 缩放比例
			File f = new File(srcImageFile);
			BufferedImage bi = ImageIO.read(f);
			Image itemp = bi.getScaledInstance(width, height, bi.SCALE_SMOOTH);
			if (bb) {//补白
				BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
				Graphics2D g = image.createGraphics();
				g.setColor(Color.white);
				g.fillRect(0, 0, width, height);
				if (width == itemp.getWidth(null))
					g.drawImage(itemp, 0, (height - itemp.getHeight(null)) / 2, itemp.getWidth(null), itemp.getHeight(null), Color.white, null);
				else
					g.drawImage(itemp, (width - itemp.getWidth(null)) / 2, 0, itemp.getWidth(null), itemp.getHeight(null), Color.white, null);
				g.dispose();
				itemp = image;
			}
			ImageIO.write(ImageUtil.toBufferedImage(itemp), "JPEG", new File(result));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void writeExplictJpeg(RenderedImage img, File out) throws IOException {
		ImageOutputStream ios = ImageIO.createImageOutputStream(out);
		Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
		ImageWriter writer = iter.next();
		ImageWriteParam iwp = writer.getDefaultWriteParam();
		iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		iwp.setCompressionQuality(0.9f);
		writer.setOutput(ios);
		writer.write(null, new IIOImage(img, null, null), iwp);
		writer.dispose();
		ios.close();
	}

	/**
	 * 图像切割(按指定起点坐标和宽高切割)
	 *
	 * @param srcImageFile 源图像地址
	 * @param result       切片后的图像地址
	 * @param x            目标切片起点坐标X
	 * @param y            目标切片起点坐标Y
	 * @param width        目标切片宽度
	 * @param height       目标切片高度
	 */
	public final static void cut(String srcImageFile, String result, int x, int y, int width, int height) {
		try {
			// 读取源图像
			BufferedImage bi = ImageIO.read(new File(srcImageFile));
			int srcWidth = bi.getHeight(); // 源图宽度
			int srcHeight = bi.getWidth(); // 源图高度
			if (srcWidth > 0 && srcHeight > 0) {
				Image image = bi.getScaledInstance(srcWidth, srcHeight, Image.SCALE_DEFAULT);
				// 四个参数分别为图像起点坐标和宽高
				// 即: CropImageFilter(int x,int y,int width,int height)
				ImageFilter cropFilter = new CropImageFilter(x, y, width, height);
				Image img = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(image.getSource(), cropFilter));
				BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
				Graphics g = tag.getGraphics();
				g.drawImage(img, 0, 0, width, height, null); // 绘制切割后的图
				g.dispose();
				// 输出为文件
				ImageIO.write(tag, "JPEG", new File(result));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 图像切割（指定切片的行数和列数）
	 *
	 * @param srcImageFile 源图像地址
	 * @param descDir      切片目标文件夹
	 * @param rows         目标切片行数。默认2，必须是范围 [1, 20] 之内
	 * @param cols         目标切片列数。默认2，必须是范围 [1, 20] 之内
	 */
	public final static void cut2(String srcImageFile, String descDir, int rows, int cols) {
		try {
			if (rows <= 0 || rows > 20) rows = 2; // 切片行数
			if (cols <= 0 || cols > 20) cols = 2; // 切片列数
			// 读取源图像
			BufferedImage bi = ImageIO.read(new File(srcImageFile));
			int srcWidth = bi.getHeight(); // 源图宽度
			int srcHeight = bi.getWidth(); // 源图高度
			if (srcWidth > 0 && srcHeight > 0) {
				Image img;
				ImageFilter cropFilter;
				Image image = bi.getScaledInstance(srcWidth, srcHeight, Image.SCALE_DEFAULT);
				int destWidth = srcWidth; // 每张切片的宽度
				int destHeight = srcHeight; // 每张切片的高度
				// 计算切片的宽度和高度
				if (srcWidth % cols == 0) {
					destWidth = srcWidth / cols;
				} else {
					destWidth = (int) Math.floor(srcWidth / cols) + 1;
				}
				if (srcHeight % rows == 0) {
					destHeight = srcHeight / rows;
				} else {
					destHeight = (int) Math.floor(srcWidth / rows) + 1;
				}
				// 循环建立切片
				// 改进的想法:是否可用多线程加快切割速度
				for (int i = 0; i < rows; i++) {
					for (int j = 0; j < cols; j++) {
						// 四个参数分别为图像起点坐标和宽高
						// 即: CropImageFilter(int x,int y,int width,int height)
						cropFilter = new CropImageFilter(j * destWidth, i * destHeight, destWidth, destHeight);
						img = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(image.getSource(), cropFilter));
						BufferedImage tag = new BufferedImage(destWidth, destHeight, BufferedImage.TYPE_INT_RGB);
						Graphics g = tag.getGraphics();
						g.drawImage(img, 0, 0, null); // 绘制缩小后的图
						g.dispose();
						// 输出为文件
						ImageIO.write(tag, "JPEG", new File(descDir + "_r" + i + "_c" + j + ".jpg"));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 图像切割（指定切片的宽度和高度）
	 *
	 * @param srcImageFile 源图像地址
	 * @param descDir      切片目标文件夹
	 * @param destWidth    目标切片宽度。默认200
	 * @param destHeight   目标切片高度。默认150
	 */
	public final static void cut3(String srcImageFile, String descDir, int destWidth, int destHeight) {
		try {
			if (destWidth <= 0) destWidth = 200; // 切片宽度
			if (destHeight <= 0) destHeight = 150; // 切片高度
			// 读取源图像
			BufferedImage bi = ImageIO.read(new File(srcImageFile));
			int srcWidth = bi.getHeight(); // 源图宽度
			int srcHeight = bi.getWidth(); // 源图高度
			if (srcWidth > destWidth && srcHeight > destHeight) {
				Image img;
				ImageFilter cropFilter;
				Image image = bi.getScaledInstance(srcWidth, srcHeight, Image.SCALE_DEFAULT);
				int cols = 0; // 切片横向数量
				int rows = 0; // 切片纵向数量
				// 计算切片的横向和纵向数量
				if (srcWidth % destWidth == 0) {
					cols = srcWidth / destWidth;
				} else {
					cols = (int) Math.floor(srcWidth / destWidth) + 1;
				}
				if (srcHeight % destHeight == 0) {
					rows = srcHeight / destHeight;
				} else {
					rows = (int) Math.floor(srcHeight / destHeight) + 1;
				}
				// 循环建立切片
				// 改进的想法:是否可用多线程加快切割速度
				for (int i = 0; i < rows; i++) {
					for (int j = 0; j < cols; j++) {
						// 四个参数分别为图像起点坐标和宽高
						// 即: CropImageFilter(int x,int y,int width,int height)
						cropFilter = new CropImageFilter(j * destWidth, i * destHeight, destWidth, destHeight);
						img = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(image.getSource(), cropFilter));
						BufferedImage tag = new BufferedImage(destWidth, destHeight, BufferedImage.TYPE_INT_RGB);
						Graphics g = tag.getGraphics();
						g.drawImage(img, 0, 0, null); // 绘制缩小后的图
						g.dispose();
						// 输出为文件
						ImageIO.write(tag, "JPEG", new File(descDir + "_r" + i + "_c" + j + ".jpg"));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 图像类型转换：GIF->JPG、GIF->PNG、PNG->JPG、PNG->GIF(X)、BMP->PNG
	 *
	 * @param srcImageFile  源图像地址
	 * @param formatName    包含格式非正式名称的 String：如JPG、JPEG、GIF等
	 * @param destImageFile 目标图像地址
	 */
	public final static void convert(String srcImageFile, String formatName, String destImageFile) {
		try {
			File f = new File(srcImageFile);
			f.canRead();
			f.canWrite();
			BufferedImage src = ImageIO.read(f);
			ImageIO.write(src, formatName, new File(destImageFile));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 彩色转为黑白
	 *
	 * @param srcImageFile  源图像地址
	 * @param destImageFile 目标图像地址
	 */
	public final static void gray(String srcImageFile, String destImageFile) {
		try {
			BufferedImage src = ImageIO.read(new File(srcImageFile));
			ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
			ColorConvertOp op = new ColorConvertOp(cs, null);
			src = op.filter(src, null);
			ImageIO.write(src, "JPEG", new File(destImageFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 给图片添加文字水印
	 *
	 * @param pressText     水印文字
	 * @param srcImageFile  源图像地址
	 * @param destImageFile 目标图像地址
	 * @param fontName      水印的字体名称
	 * @param fontStyle     水印的字体样式
	 * @param color         水印的字体颜色
	 * @param fontSize      水印的字体大小
	 * @param x             修正值
	 * @param y             修正值
	 * @param alpha         透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
	 */
	public final static void pressText(String pressText, String srcImageFile, String destImageFile, String fontName, int fontStyle, Color color, int fontSize, int x, int y, float alpha) {
		try {
			File img = new File(srcImageFile);
			Image src = ImageIO.read(img);
			int width = src.getWidth(null);
			int height = src.getHeight(null);
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = image.createGraphics();
			g.drawImage(src, 0, 0, width, height, null);
			g.setColor(color);
			g.setFont(new Font(fontName, fontStyle, fontSize));
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
			// 在指定坐标绘制水印文字
			g.drawString(pressText, x, y);
			g.dispose();
			ImageIO.write((BufferedImage) image, "JPEG", new File(destImageFile));// 输出到文件流
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 给图片添加文字水印
	 *
	 * @param pressText     水印文字
	 * @param srcImageFile  源图像地址
	 * @param destImageFile 目标图像地址
	 * @param fontName      字体名称
	 * @param fontStyle     字体样式
	 * @param color         字体颜色
	 * @param fontSize      字体大小
	 * @param x             修正值
	 * @param y             修正值
	 * @param alpha         透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
	 */
	public final static void pressText2(String pressText, String srcImageFile, String destImageFile, String fontName, int fontStyle, Color color, int fontSize, int x, int y, float alpha) {
		try {
			File img = new File(srcImageFile);
			Image src = ImageIO.read(img);
			int width = src.getWidth(null);
			int height = src.getHeight(null);
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = image.createGraphics();
			g.drawImage(src, 0, 0, width, height, null);
			g.setColor(color);
			g.setFont(new Font(fontName, fontStyle, fontSize));
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
			// 在指定坐标绘制水印文字
			g.drawString(pressText, (width - (getLength(pressText) * fontSize)) / 2 + x, (height - fontSize) / 2 + y);
			g.dispose();
			ImageIO.write((BufferedImage) image, "JPEG", new File(destImageFile));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 给图片添加图片水印
	 *
	 * @param pressImg      水印图片
	 * @param srcImageFile  源图像地址
	 * @param destImageFile 目标图像地址
	 * @param x             修正值。 默认在中间
	 * @param y             修正值。 默认在中间
	 * @param alpha         透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
	 */
	public final static void pressImage(String pressImg, String srcImageFile, String destImageFile, int x, int y, float alpha) {
		try {
			File img = new File(srcImageFile);
			Image src = ImageIO.read(img);
			int wideth = src.getWidth(null);
			int height = src.getHeight(null);
			BufferedImage image = new BufferedImage(wideth, height, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = image.createGraphics();
			g.drawImage(src, 0, 0, wideth, height, null);
			// 水印文件
			Image src_biao = ImageIO.read(new File(pressImg));
			int wideth_biao = src_biao.getWidth(null);
			int height_biao = src_biao.getHeight(null);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
			g.drawImage(src_biao, (wideth - wideth_biao) / 2, (height - height_biao) / 2, wideth_biao, height_biao, null);
			// 水印文件结束
			g.dispose();
			ImageIO.write((BufferedImage) image, "JPEG", new File(destImageFile));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 计算text的长度（一个中文算两个字符）
	 *
	 * @param text
	 * @return
	 */
	public final static int getLength(String text) {
		int length = 0;
		for (int i = 0; i < text.length(); i++) {
			if (new String(text.charAt(i) + "").getBytes().length > 1) {
				length += 2;
			} else {
				length += 1;
			}
		}
		return length / 2;
	}

	public static int[] decodeColor(int color, int rgb[]) {
		if (rgb == null) rgb = new int[3];
		rgb[0] = (color & 0x00ff0000) >> 16;
		rgb[1] = (color & 0x0000ff00) >> 8;
		rgb[2] = (color & 0x000000ff);
		return rgb;
	}

	public static int encodeColor(int rgb[]) {
		int color = (255 << 24) | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
		return color;
	}

	public static int getBrightness(int color) {
		int r = (color & 0x00ff0000) >> 16;
		int g = (color & 0x0000ff00) >> 8;
		int b = (color & 0x000000ff);
		int y = Math.round(0.3f * r + 0.59f * g + 0.11f * b);
		y = y < 0 ? 0 : y;
		y = y > 255 ? 255 : y;
		return y;
	}

	public static float[] convertRGBToYHS(int color, float yhs[]) {
		if (yhs == null) yhs = new float[3];
		int r = (color & 0x00ff0000) >> 16;
		int g = (color & 0x0000ff00) >> 8;
		int b = (color & 0x000000ff);

		yhs[0] = (float) (0.3 * r + 0.59 * g + 0.11 * b);
		double c1 = 0.7 * r - 0.59 * g - 0.11 * b;
		double c2 = -0.3 * r - 0.59 * g + 0.89 * b;
		yhs[2] = (float) Math.sqrt(c1 * c1 + c2 * c2);
		if (yhs[2] < 0.005) yhs[1] = 0;
		else {
			yhs[1] = (float) Math.atan2(c1, c2);
			if (yhs[1] < 0) yhs[1] += (float) Math.PI * 2;
		}


		return yhs;
	}

	public static int convertYHSToRGB(float yhs[]) {
		double c1 = yhs[2] * Math.sin(yhs[1]);
		double c2 = yhs[2] * Math.cos(yhs[1]);
		int r = (int) Math.round(yhs[0] + c1);
		r = r < 0 ? 0 : r;
		r = r > 255 ? 255 : r;
		int g = (int) Math.round(yhs[0] - 0.3 * c1 / 0.9 - 0.11 * c2 / 0.59);
		g = g < 0 ? 0 : g;
		g = g > 255 ? 255 : g;
		int b = (int) Math.round(yhs[0] + c2);
		b = b < 0 ? 0 : b;
		b = b > 255 ? 255 : b;

		int color = (255 << 24) | (r << 16) | (g << 8) | b;
		return color;
	}

	public static BufferedImage grayScale(BufferedImage srcImage) {
		int width = srcImage.getWidth();
		int height = srcImage.getHeight();
		int srcRGBs[] = srcImage.getRGB(0, 0, width, height, null, 0, width);
		BufferedImage destImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		int rgb[] = new int[3];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				rgb[0] = ImageUtil.getBrightness(srcRGBs[j * width + i]);
				rgb[1] = rgb[2] = rgb[0];
				destImage.setRGB(i, j, ImageUtil.encodeColor(rgb));
			}
		}
		return destImage;
	}

	public final static void changeYHS(String srcImageFile, String destImageFile, float deltY, float deltH, float nS) {
		try {
			BufferedImage bi = ImageIO.read(new File(srcImageFile));
			bi = changeYHS(bi, deltY, deltH, nS);
			String ext = Util.getExt(destImageFile).toUpperCase();
			if ("JPG".equals(ext)) ext = "JPEG";
			if ("JPEG".equals(ext)) writeExplictJpeg(bi, new File(destImageFile));
			else ImageIO.write(bi, ext, new File(destImageFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static BufferedImage changeYHS(BufferedImage srcImage, float deltY, float deltH, float nS) {
		int width = srcImage.getWidth();
		int height = srcImage.getHeight();
		int srcRGBs[] = srcImage.getRGB(0, 0, width, height, null, 0, width);
		BufferedImage destImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		float yhs[] = new float[3];
		int rgb[] = new int[3];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				ImageUtil.convertRGBToYHS(srcRGBs[j * width + i], yhs);
				yhs[0] += deltY;
				yhs[1] += deltH;
				yhs[2] *= nS;
				destImage.setRGB(i, j, ImageUtil.convertYHSToRGB(yhs));
			}
		}
		return destImage;
	}

	public final static void changeContrast(String srcImageFile, String destImageFile, int deltY) {
		try {
			BufferedImage bi = ImageIO.read(new File(srcImageFile));
			bi = changeContrast(bi, deltY);
			ImageIO.write(bi, "JPEG", new File(destImageFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 改变对比度
	 *
	 * @param srcImage
	 * @param deltY
	 * @return
	 */
	public static BufferedImage changeContrast(BufferedImage srcImage, int deltY) {
		int width = srcImage.getWidth();
		int height = srcImage.getHeight();
		int srcRGBs[] = srcImage.getRGB(0, 0, width, height, null, 0, width);
		BufferedImage destImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		float yhs[] = new float[3];
		int rgb[] = new int[3];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				ImageUtil.convertRGBToYHS(srcRGBs[j * width + i], yhs);
				yhs[0] = (255.0f + 2 * deltY) * (yhs[0]) / (255.0f) - deltY;
				destImage.setRGB(i, j, ImageUtil.convertYHSToRGB(yhs));
			}
		}
		return destImage;
	}

	public static int[] getHistInfo(BufferedImage srcImage, int histArray[]) {
		if (histArray == null) histArray = new int[256];
		for (int i = 0; i < 256; i++) histArray[i] = 0;

		int width = srcImage.getWidth();
		int height = srcImage.getHeight();
		int srcRGBs[] = srcImage.getRGB(0, 0, width, height, null, 0, width);

		float yhs[] = new float[3];
		int rgb[] = new int[3];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				ImageUtil.convertRGBToYHS(srcRGBs[j * width + i], yhs);
				int hist = Math.round(yhs[0]);
				hist = hist < 0 ? 0 : hist;
				hist = hist > 255 ? 255 : hist;
				histArray[hist]++;
			}
		}
		return histArray;
	}

	public static BufferedImage histPlane(BufferedImage srcImage) {
		int width = srcImage.getWidth();
		int height = srcImage.getHeight();
		int srcRGBs[] = srcImage.getRGB(0, 0, width, height, null, 0, width);
		BufferedImage destImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		class HistPlaneInnerStruct {    //灰度均衡内部结构
			int x;
			int y;
			float h;        //色相
			float s;        //饱和度

			public HistPlaneInnerStruct(int x, int y, float h, float s) {
				this.x = x;
				this.y = y;
				this.h = h;
				this.s = s;
			}
		}
		float yhs[] = new float[3];
		LinkedList histIndexs[] = new LinkedList[256];
		for (int i = 0; i < 256; i++) histIndexs[i] = new LinkedList();
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				ImageUtil.convertRGBToYHS(srcRGBs[j * width + i], yhs);
				int hist = Math.round(yhs[0]);
				hist = hist < 0 ? 0 : hist;
				hist = hist > 255 ? 255 : hist;
				histIndexs[hist].addLast(new HistPlaneInnerStruct(i, j, yhs[1], yhs[2]));
			}
		}

		int avCount = width * height / 256;
		int index = 255;
		ListIterator it = histIndexs[index].listIterator();
		for (int i = 255; i >= 0; i--) {
			for (int j = avCount; j > 0; j--) {
				while (!it.hasNext()) {
					index--;
					it = histIndexs[index].listIterator();
				}
				HistPlaneInnerStruct hpi = (HistPlaneInnerStruct) it.next();
				yhs[0] = i;
				yhs[1] = hpi.h;
				yhs[2] = hpi.s;
				destImage.setRGB(hpi.x, hpi.y, ImageUtil.convertYHSToRGB(yhs));
			}
		}

		return destImage;
	}

	public static void setBorder(BufferedImage frame, int borderWidth, int color) {
		int width = frame.getWidth();
		int height = frame.getHeight();
		// border top
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < borderWidth; y++)
				frame.setRGB(x, y, color);
		}
		// border bottom
		for (int x = 0; x < width; x++) {
			for (int y = height - borderWidth; y < height; y++)
				frame.setRGB(x, y, color);
		}
		// border left
		for (int x = 0; x < borderWidth; x++) {
			for (int y = 0; y < height; y++)
				frame.setRGB(x, y, color);
		}
		// border right
		for (int x = width - borderWidth; x < width; x++) {
			for (int y = 0; y < height; y++)
				frame.setRGB(x, y, color);
		}
	}

	/**
	 * 改变亮度和对比度
	 * 亮度默认值为1.0f
	 * 对比度默认值为1.5f
	 * 对比度 contrast的最佳取值范围在[0 ~ 4],
	 * 亮度 brightness的最佳取值范围在[0~ 2]之间
	 */
	public static void changeBrightness(BufferedImage frame, float contrast, float brightness) {
		// adjust brightness
		int width = frame.getWidth();
		int height = frame.getHeight();

		int[] rgbmeans = new int[3];
		double redSum = 0, greenSum = 0, blueSum = 0;
		double total = height * width;
		for (int y = 0; y < height; y++) {
			int ta = 0, tr = 0, tg = 0, tb = 0;
			for (int x = 0; x < width; x++) {
				int raw = frame.getRGB(x, y);
				ta = (raw >> 24) & 0xff;
				tr = (raw >> 16) & 0xff;
				tg = (raw >> 8) & 0xff;
				tb = raw & 0xff;
				redSum += tr;
				greenSum += tg;
				blueSum += tb;
			}
		}

		rgbmeans[0] = (int) (redSum / total);
		rgbmeans[1] = (int) (greenSum / total);
		rgbmeans[2] = (int) (blueSum / total);

		// adjust contrast and brightness algorithm, here
		for (int y = 0; y < height; y++) {
			int ta = 0, tr = 0, tg = 0, tb = 0;
			for (int x = 0; x < width; x++) {
				int raw = frame.getRGB(x, y);
				ta = (raw >> 24) & 0xff;
				tr = (raw >> 16) & 0xff;
				tg = (raw >> 8) & 0xff;
				tb = raw & 0xff;

				// remove means
				tr -= rgbmeans[0];
				tg -= rgbmeans[1];
				tb -= rgbmeans[2];

				// adjust contrast now !!!
				tr = (int) (tr * contrast);
				tg = (int) (tg * contrast);
				tb = (int) (tb * contrast);

				// adjust brightness
				tr += (int) (rgbmeans[0] * brightness);
				tg += (int) (rgbmeans[1] * brightness);
				tb += (int) (rgbmeans[2] * brightness);
				raw = (ta << 24) | (clamp(tr) << 16) | (clamp(tg) << 8) | clamp(tb);
				frame.setRGB(x, y, raw);
			}
		}
	}

	private static int clamp(int value) {
		return value > 255 ? 255 : (value < 0 ? 0 : value);
	}

	public static BufferedImage randBackground(BufferedImage frame, BufferedImage background) {
		BufferedImage ti = background;
		int tiw = ti.getWidth();
		int tih = ti.getHeight();
		// 背景随机位置
		int x = Util.random(0, tiw);
		int y = Util.random(0, tih);
		int width = frame.getWidth();
		int height = frame.getHeight();
		BufferedImage tmp = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for (int k = 0; k < width; k++) {
			for (int j = 0; j < height; j++) {
				int tx = x + k;
				if (tx >= tiw) tx = (tx % tiw);
				int ty = y + j;
				if (ty >= tih) ty = (ty % tih);
				tmp.setRGB(k, j, ti.getRGB(tx, ty));
			}
		}
		tmp.createGraphics().drawImage(frame, 0, 0, null);
		return tmp;
	}

	/**
	 * @param image
	 * @param rate  每rate个点就加入一个干扰像素。
	 */
	public static void random(BufferedImage image, int rate) {
		Graphics graphics = image.getGraphics();
		// 验证码图片的宽高
		int imgWidth = image.getWidth();
		int imgHeight = image.getHeight();
		int c = Math.max(5, (imgWidth * imgHeight) / rate);
		// 画噪点(颜色及位置随机)
		for (int i = 0; i < c; i++) {
			// 随机位置
			int x = Math.max(0, Util.random(0, imgWidth - 3));
			int y = Math.max(0, Util.random(0, imgHeight - 3));
			int pixel = image.getRGB(x, y);
			// 透明区域忽略
			if ((pixel >> 24) == 0x00) continue;
			// 随机颜色
			int rInt = Util.random(0, 255);
			int gInt = Util.random(0, 255);
			int bInt = Util.random(0, 255);
			int aInt = Util.random(50, 100);
			graphics.setColor(new Color(rInt, gInt, bInt, aInt));
			graphics.drawOval(x, y, 1, 1);
		}
		graphics.dispose();
	}

	public static void randomImage(File file, File out, String ext, int rate) throws IOException {
		if (out.exists() && !out.equals(file)) out.delete();
		Util.makeIfDir(out);
		out.createNewFile();
		if (ext == null) ext = Util.getExt(file.getName(), "jpg");
		if ("gif".equalsIgnoreCase(ext)) {
			byte[] bs = Util.read(file);
			Util.writeFile(out.getAbsolutePath(), bs);
		} else {
			BufferedImage image = ImageIO.read(file);
			random(image, rate);
			ImageIO.write(image, ext, out);
		}
	}

	public final static void scale(String srcImageFile, String result, int height, int width, boolean bb) {
		try {
			double ratio = 0.0; // 缩放比例
			File f = new File(srcImageFile);
			BufferedImage bi = ImageIO.read(f);
			Image itemp = bi.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH);
			itemp = convertToBufferedImage(itemp);
			// 计算比例
			if ((bi.getHeight() > height) || (bi.getWidth() > width)) {
				if (bi.getHeight() > bi.getWidth()) {
					ratio = Integer.valueOf(height).doubleValue() / bi.getHeight();
				} else {
					ratio =Integer.valueOf(width).doubleValue() / bi.getWidth();
				}
				AffineTransformOp op = new AffineTransformOp(AffineTransform.getScaleInstance(ratio, ratio), null);
				itemp = op.filter(bi, null);
			}
			if (bb) {//补白
				BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = image.createGraphics();
				g.setColor(Color.white);
				g.fillRect(0, 0, width, height);
				if (width == itemp.getWidth(null))
					g.drawImage(itemp, 0, (height - itemp.getHeight(null)) / 2, itemp.getWidth(null), itemp.getHeight(null), Color.white, null);
				else
					g.drawImage(itemp, (width - itemp.getWidth(null)) / 2, 0, itemp.getWidth(null), itemp.getHeight(null), Color.white, null);
				g.dispose();
				itemp = image;
			}
			ImageIO.write((RenderedImage) itemp, Util.getExt(result).toUpperCase(), new File(result));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static BufferedImage convertToBufferedImage(Image image) {
		BufferedImage newImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = newImage.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
		return newImage;
	}
}