package com.dtc.pdfboxer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

/**
 * 顯示水平多列文字，提供自動換行功能
 * <p>
 * 注意事項：
 * <ul>
 * 	<li>內文過長時，下邊界將會一直延展，直到 page 的底端</li>
 * </ul>
 */
public class TextArea {
	private PDPageContentStream contentStream;
	private PDFont pdfFont;
	private float fontSize;
	private float lineSpacing;

	private boolean currentIsOneByte = true;

	public TextArea(PDPageContentStream contentStream) {
		this(contentStream, PDType1Font.HELVETICA, 18, 0);
	}

	public TextArea(PDPageContentStream contentStream, PDFont pdfFont, float fontSize, float lineSpacing) {
		this.contentStream = contentStream;
		this.pdfFont = pdfFont;
		this.fontSize = fontSize;
		this.lineSpacing = lineSpacing;
	}

	/**
	 * 考慮到斷行、自動換行等行為，並將 text 輸出到 contentStream
	 */
	public void showText(String text, PDRectangle rect) throws IOException {
		showText(text, rect, pdfFont, fontSize, lineSpacing);
	}

	/**
	 * 考慮到斷行、自動換行等行為，並將 text 輸出到 contentStream
	 * <p>
	 * 傳遞進來的參數不會改變 field 的值
	 */
	public void showText(String text, PDRectangle rect, PDFont pdfFont, float fontSize, float lineSpacing) throws IOException {
		List<String> lines = new ArrayList<String>();
		float leading = lineSpacing + fontSize;

		// 先把斷行處理掉
		String[] textArray = text.split("\n");

		for (String line : textArray) {
			lines.addAll(
				simpleBreaker(line, rect, pdfFont, fontSize, leading)
			);
		}

		contentStream.beginText();
		contentStream.setFont(pdfFont, fontSize);
		contentStream.newLineAtOffset(rect.getLowerLeftX(), rect.getUpperRightY());

		for (String line : lines) {
			contentStream.showText(line);
			contentStream.newLineAtOffset(0, -leading);
		}
		contentStream.endText();
	}

	/**
	 * 讓字串能夠在超過 {@link Rect#width} 時換行
	 */
	private List<String> simpleBreaker(String text, PDRectangle rect, PDFont pdfFont, float fontSize, float leading) throws IOException {
		List<String> lines = new ArrayList<String>();
		int lastPostition = -1;
		if (text.isEmpty()) {
			lines.add(""); // 如果這 text 是空白，也至少要加上一個空白列
		}

		while (text.length() > 0) {
			//找出下一個可以斷行的地方
			int breakIndex = findNextBreakIndex(text, lastPostition + 1);
			String testStr = text.substring(0, breakIndex).trim();

			//算出截下來的字串寬度
			float size = fontSize * pdfFont.getStringWidth(testStr) / 1000;

			if (size > rect.getWidth()) {
				//要斷行
				if (lastPostition < 0) {
					lastPostition = breakIndex;
				}

				String line = text.substring(0, lastPostition).trim();
				lines.add(line);
				text = text.substring(lastPostition).trim();
				lastPostition = -1;
			} else if (breakIndex == text.length()) {
				//後面沒有字了
				lines.add(text);
				break;
			} else {
				//不需要斷行，繼續補字
				lastPostition = breakIndex;
			}
		}

		return lines;
	}

	public float getLineSpacing() {
		return lineSpacing;
	}

	public void setLineSpacing(float lineSpacing) {
		this.lineSpacing = lineSpacing;
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

	/**
	 * 斷行可以是：
	 * 1. 空白字元
	 * 2. 非一個 byte 的字元（中文字可以任意斷行，但英文字不行）
	 * 3. 是否有中英交替過（例如：「I服了u」）
	 */
	private int findNextBreakIndex(String str, int fromIndex) {
		for (int i = fromIndex; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c == ' ') {
				return i;
			}

			// 先記錄上一次是不是 1 byte
			boolean lastIsOneByte = currentIsOneByte;
			currentIsOneByte = (c <= 255); // 不是很嚴謹的判斷方式，但很有效

			if (currentIsOneByte) {
				// 是字母，但前一個不是字母
				if (!lastIsOneByte) { return i; }
			} else {
				// 不是字母
				return i;
			}
		}
		return str.length();
	}

	/**
	 * 用文字起點的垂直位置以及水平 margin 來決定輸出文字的區塊
	 * @param yPos 文字起點的垂直位置
	 * @param xMargin 水平 margin
	 * @param mediaBox 文字區塊所在的 {@link PDPage#getMediaBox()}
	 */
	public static PDRectangle create(float yPos, float xMargin, PDRectangle mediaBox) {
		return new PDRectangle(
			mediaBox.getLowerLeftX() + xMargin,
			yPos,
			mediaBox.getWidth() - (2 * xMargin),
			0
		);
	}
	/**
	 * 指定起點 x, y 的位置，並提供右邊邊界 x 座標來決定文字區塊
	 * @param yPos y 座標
	 * @param xMargin x 軸的 margin
	 * @param rightBoundary 右邊邊界的 x 座標
	 */
	public static PDRectangle create(float xPos, float yPos, float rightBoundary) {
		return new PDRectangle(
			xPos,
			yPos,
			rightBoundary - xPos,
			0
		);
	}
}
