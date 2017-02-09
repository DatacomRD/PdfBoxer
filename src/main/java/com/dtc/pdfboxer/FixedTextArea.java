package com.dtc.pdfboxer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

/**
 * <a href='http://stackoverflow.com/questions/19635275/how-to-generate-multiple-lines-in-pdf-using-apache-pdfbox'>Reference</a>
 * <p>
 * 注意事項：
 * <ul>
 * 	<li>內文過長時，下邊界將會一直延展，直到 page 的底端</li>
 * </ul>
 */
public class FixedTextArea {

	private PDPageContentStream contentStream;
	private Rect rect = Rect.create(PDRectangle.LETTER);
	private PDFont pdfFont = PDType1Font.HELVETICA;
	private float fontSize = 18;
	private float lineHeight = 1f;
	private boolean considerFontHeight = false;
	private boolean currentIsOneByte = true;

	public FixedTextArea(PDPageContentStream contentStream) {
		this.contentStream = contentStream;
	}

	public void showText(String text) throws IOException {
		showText(text, rect, pdfFont, fontSize, lineHeight);
	}

	/**
	 * 考慮到斷行、自動換行等行為，並將 text 輸出到 contentStream
	 * @throws IOException
	 */
	public void showText(String text, Rect rect, PDFont pdfFont, float fontSize, float lineHeight) throws IOException {
		List<String> lines = new ArrayList<String>();
		float leading = lineHeight + fontSize;

		// 先把斷行處理掉
		String[] textArray = text.replace("\r\n", "\n").split("\n");

		for (String line : textArray) {
			lines.addAll(
				simpleBreaker(line, rect, pdfFont, fontSize, leading)
			);
		}

		// startY 如果設在 PDRectangle 最上方，就算是可視範圍內， render 的文字仍可能超過。
		if (considerFontHeight) {
			rect.startY = rect.startY - pdfFont.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
		}

		contentStream.beginText();
		contentStream.setFont(pdfFont, fontSize);
		contentStream.newLineAtOffset(rect.startX, rect.startY);

		for (String line: lines) {
			contentStream.showText(line);
			contentStream.newLineAtOffset(0, -leading);
		}
		contentStream.endText();
	}

	/**
	 * 讓字串能夠在超過 {@link Rect#width} 時換行
	 */
	private List<String> simpleBreaker(String text, Rect rect, PDFont pdfFont, float fontSize, float leading) throws IOException {
		List<String> lines = new ArrayList<String>();
		int lastPostition = -1;
		if (text.isEmpty()) {
			lines.add(""); // 如果這一列是空白，也至少要加上一個空白列
		}

		while (text.length() > 0) {
			//找出下一個可以斷行的地方
			int breakIndex = findNextBreakIndex(text, lastPostition + 1);
			String testStr = text.substring(0, breakIndex).trim();

			//算出截下來的字串寬度
			float size = fontSize * pdfFont.getStringWidth(testStr) / 1000;
			System.out.printf("'%s' - %f of %f\n", testStr, size, rect.width);

			if (size > rect.width) {
				//要斷行
				if (lastPostition < 0) {
					lastPostition = breakIndex;
				}

				String line = text.substring(0, lastPostition).trim();
				lines.add(line);
				text = text.substring(lastPostition).trim();
				System.out.printf("'%s' is line\n", line);
				lastPostition = -1;
			} else if (breakIndex == text.length()) {
				//後面沒有字了
				lines.add(text);
				System.out.printf("'%s' is Final\n", text);
				break;
			} else {
				//不需要斷行，繼續補字
				lastPostition = breakIndex;
			}
		}

		return lines;
	}

	public float getLineHeight() {
		return lineHeight;
	}

	/**
	 * 這是一個 offset 值
	 */
	public void setLineHeight(float lineHeight) {
		this.lineHeight = lineHeight;
	}

	public Rect getRect() {
		return rect;
	}

	public void setRect(Rect rect) {
		this.rect = rect;
	}

	public PDFont getPdfFont() {
		return pdfFont;
	}

	public void setPdfFont(PDFont pdfFont) {
		this.pdfFont = pdfFont;
	}

	public float getFontSize() {
		return fontSize;
	}

	public void setFontSize(float fontSize) {
		this.fontSize = fontSize;
	}

	public boolean isConsiderFontHeight() {
		return considerFontHeight;
	}

	public void setConsiderFontHeight(boolean considerFontHeight) {
		this.considerFontHeight = considerFontHeight;
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
			currentIsOneByte = String.valueOf(c).getBytes().length == 1; // 不是很嚴謹的判斷方式，但很有效

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
	 * 用來表達文字可以顯示的範圍區塊
	 */
	public static class Rect {
		private float startX;
		private float startY;
		private float width;

		public Rect(float startX, float startY, float width) {
			super();
			this.startX = startX;
			this.startY = startY;
			this.width = width;
		}

		/**
		 * 以 {@link PDRectangle} 作為寬度、起點的依據
		 */
		public static Rect create(PDRectangle mediaBox) {
			return new Rect(
				mediaBox.getLowerLeftX(),
				mediaBox.getUpperRightY(),
				mediaBox.getWidth()
			);
		}

		/**
		 * 指定起點 y 的位置，並提供水平的 margin 來產生文字區塊
		 * @param yPos y 座標
		 * @param xMargin x 軸的 margin
		 * @param mediaBox PDPage 的 size
		 */
		public static Rect create(float yPos, float xMargin, PDRectangle mediaBox) {
			return new Rect(
				mediaBox.getLowerLeftX() + xMargin,
				yPos,
				mediaBox.getWidth() - (2 * xMargin)
			);
		}

		/**
		 * 指定起點 x, y 的位置，並提供右邊邊界來決定文字區塊
		 * @param yPos y 座標
		 * @param xMargin x 軸的 margin
		 * @param rightBoundary 右邊邊界的 x 座標
		 */
		public static Rect create(float xPos, float yPos, float rightBoundary) {
			return new Rect(
				xPos,
				yPos,
				rightBoundary - xPos
			);
		}
	}
}
