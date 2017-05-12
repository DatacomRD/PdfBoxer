package com.dtc.pdfboxer;

import java.io.File;
import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * 提供將 PDF 檔轉換成圖檔的功能，核心的 API 為 {@link #convert(File, File, ImageFormat, float, ImageType)}
 * <p>
 * 注意事項：來源的 PDF 檔不能內嵌<b>標楷體</b>、<b>細明體</b>、<b>新細明體</b>，因字體的文字無法正確顯示
 */
public class Converter {
	public static final float DEFAULT_DPI = 300;
	public static final ImageType DEFAULT_IMAGE_TYPE = ImageType.RGB;

	/**
	 * 轉換成 JPG，參考 {@link #convert(File, File, ImageFormat, float, ImageType)}
	 */
	public static boolean toJPG(File srcFile, File outputFolder) {
		return toJPG(srcFile, outputFolder, DEFAULT_DPI);
	}

	/**
	 * 轉換成 JPG，參考 {@link #convert(File, File, ImageFormat, float, ImageType)}
	 */
	public static boolean toJPG(File srcFile, File outputFolder, float dpi) {
		return convert(srcFile, outputFolder, ImageFormat.JPG, dpi, DEFAULT_IMAGE_TYPE);
	}

	/**
	 * 轉換成 PNG，參考 {@link #convert(File, File, ImageFormat, float, ImageType)}
	 */
	public static boolean toPNG(File srcFile, File outputFolder) {
		return toPNG(srcFile, outputFolder, DEFAULT_DPI);
	}

	/**
	 * 轉換成 PNG，參考 {@link #convert(File, File, ImageFormat, float, ImageType)}
	 */
	public static boolean toPNG(File srcFile, File outputFolder, float dpi) {
		return convert(srcFile, outputFolder, ImageFormat.PNG, dpi, DEFAULT_IMAGE_TYPE);
	}

	/**
	 * 轉換成 GIF，參考 {@link #convert(File, File, ImageFormat, float, ImageType)}
	 */
	public static boolean toGIF(File srcFile, File outputFolder) {
		return toGIF(srcFile, outputFolder, DEFAULT_DPI);
	}

	/**
	 * 轉換成 GIF，參考 {@link #convert(File, File, ImageFormat, float, ImageType)}
	 */
	public static boolean toGIF(File srcFile, File outputFolder, float dpi) {
		return convert(srcFile, outputFolder, ImageFormat.GIF, dpi, DEFAULT_IMAGE_TYPE);
	}

	/**
	 * 轉換成 BMP，參考 {@link #convert(File, File, ImageFormat, float, ImageType)}
	 */
	public static boolean toBMP(File srcFile, File outputFolder) {
		return toBMP(srcFile, outputFolder, DEFAULT_DPI);
	}

	/**
	 * 轉換成 BMP，參考 {@link #convert(File, File, ImageFormat, float, ImageType)}
	 */
	public static boolean toBMP(File srcFile, File outputFolder, float dpi) {
		return convert(srcFile, outputFolder, ImageFormat.BMP, dpi, DEFAULT_IMAGE_TYPE);
	}

	/**
	 * 將 PDF 轉成圖檔（一頁一個檔案），輸出的檔案放在 outputFolder 之下
	 * <p>
	 * 假設來源檔案為 foobar.pdf 且轉換格式為 {@link ImageFormat#JPG}，
	 * 則輸出檔案：foobar-1.jpg、foobar-2.jpg、...
	 *
	 * @param srcFile 來源 PDF 檔
	 * @param outputFolder 輸出檔案的目錄
	 * @param format 檔案格式
	 * @param dpi 設定 dpi 值來決定解析度
	 * @param type 設定圖片顏色型態
	 */
	public static boolean convert(File srcFile, File outputFolder, ImageFormat format, float dpi, ImageType type) {

		if (!outputFolder.isDirectory()) { throw new IllegalArgumentException(); }

		try (PDDocument document = PDDocument.load(srcFile)) {
			PDFRenderer pdfRenderer = new PDFRenderer(document);

			// 輸出檔名前綴
			String fnamePrefix = srcFile.getName();
			fnamePrefix = fnamePrefix.substring(0, fnamePrefix.lastIndexOf("."));

			for (int page = 0; page < document.getNumberOfPages(); page++) {
				ImageIO.write(
					pdfRenderer.renderImageWithDPI(page, dpi, type),
					format.name(),
					new File(outputFolder, fnamePrefix + "-" + (page+1) + format.extName)
				);
			}

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 支援的檔案格式以及對應的附檔名
	 */
	public static enum ImageFormat {
		BMP(".bmp"),
		JPG(".jpg"),
		PNG(".png"),
		GIF(".gif");
		public final String extName;
		ImageFormat(String extName) { this.extName = extName; }
	}
}
