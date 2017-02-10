package com.dtc.pdfboxer;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

/**
 * 顯示水平單列文字，不考慮換行以及右邊邊界
 */
public class TextLine {
	private PDPageContentStream contentStream;
	private PDFont pdfFont;
	private float fontSize;

	public TextLine(PDPageContentStream contentStream) {
		this(contentStream, PDType1Font.HELVETICA, 18);
	}

	public TextLine(PDPageContentStream contentStream, PDFont pdfFont, float fontSize) {
		this.contentStream = contentStream;
		this.pdfFont = pdfFont;
		this.fontSize = fontSize;
	}

	/**
	 * 顯示文字的 API
	 */
	public void showText(String text, float x, float y) throws IOException {
		showText(text, x, y, pdfFont, fontSize);
	}

	/**
	 * 顯示文字的 API，傳遞進來的參數不會改變 field 的值
	 */
	public void showText(String text, float x, float y, PDFont pdfFont, float fontSize) throws IOException {
		contentStream.beginText();
		contentStream.setFont(pdfFont, fontSize);
		contentStream.newLineAtOffset(x, y);
		contentStream.showText(text);
		contentStream.endText();
	}

	public PDFont getFont() {
		return pdfFont;
	}

	public void setFont(PDFont pdfFont) {
		this.pdfFont = pdfFont;
	}

	public float getFontSize() {
		return fontSize;
	}

	public void setFontSize(float fontSize) {
		this.fontSize = fontSize;
	}
}
