package com.dtc.pdfboxer;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * 提供將 PDF 檔轉換成圖檔的功能
 */
public class Converter {
	/**
	 * 轉換成 JPG，參考 {@link #convert(File, File, ImageFormat, float, ImageType)}
	 */
	public static boolean toJPG(File srcFile, File outputFolder) {
		return convert(srcFile, outputFolder, ImageFormat.JPG, 300, ImageType.RGB);
	}

	/**
	 * 轉換成 PNG，參考 {@link #convert(File, File, ImageFormat, float, ImageType)}
	 */
	public static boolean toPNG(File srcFile, File outputFolder) {
		return convert(srcFile, outputFolder, ImageFormat.PNG, 300, ImageType.RGB);
	}

	/**
	 * 轉換成 GIF，參考 {@link #convert(File, File, ImageFormat, float, ImageType)}
	 */
	public static boolean toGIF(File srcFile, File outputFolder) {
		return convert(srcFile, outputFolder, ImageFormat.GIF, 300, ImageType.RGB);
	}

	/**
	 * 轉換成 BMP，參考 {@link #convert(File, File, ImageFormat, float, ImageType)}
	 */
	public static boolean toBMP(File srcFile, File outputFolder) {
		return convert(srcFile, outputFolder, ImageFormat.BMP, 300, ImageType.RGB);
	}

	/**
	 * 將 PDF 轉成圖檔，如果 PDF 有多頁就轉成多頁，輸出的檔案統一放在 outputFolder
	 * <p>
	 * 假設來源檔案為 foobar.pdf 且轉換格式為 {@link ImageFormat#JPG}
	 * <ul>
	 * 	<li>輸出檔案（單頁）：foorbar.jpg</li>
	 * 	<li>輸出檔案（多頁）：foorbar-1.jpg、foorbar-2.jpg、...</li>
	 * </ul>
	 * 注意事項：來源的 PDF 檔不能有內嵌標楷體（該字體的文字無法正確顯示）
	 *
	 * @param srcFile 來源 PDF 檔
	 * @param outputFolder 輸出檔案的目錄
	 * @param format 檔案格式，參考 {@link ImageFormat}
	 * @param dpi 設定 dpi 值來決定解析度
	 * @param type 設定圖片顏色型態，參考 {@link ImageType}
	 */
	public static boolean convert(File srcFile, File outputFolder, ImageFormat format, float dpi, ImageType type) {
		PDDocument document = null;
		try {
			document = PDDocument.load(srcFile);
			PDFRenderer pdfRenderer = new PDFRenderer(document);

			// 輸出檔名前綴
			String fnamePrefix = srcFile.getName();
			fnamePrefix = fnamePrefix.substring(0, fnamePrefix.lastIndexOf("."));

			if (document.getNumberOfPages() == 1) {
				ImageIO.write(
					pdfRenderer.renderImageWithDPI(0, dpi, type),
					format.name(),
					new File(outputFolder, fnamePrefix + format.extName)
				);
			} else {
				for (int page = 0; page < document.getNumberOfPages(); page++) {
					ImageIO.write(
						pdfRenderer.renderImageWithDPI(page, dpi, type),
						format.name(),
						new File(outputFolder, fnamePrefix + "-" + (page+1) + format.extName)
					);
				}
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (document != null) {
				try {
					document.close();
				} catch (IOException e) {
					//Ignore
				}
			}
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
