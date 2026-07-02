package com.jeestudio.bpm.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

import javax.imageio.ImageIO;

import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description: 图片压缩工具
 */
public class PicUtil {

	protected static final Logger logger = LoggerFactory.getLogger(PicUtil.class);
	private static final int MAX_RECURSION_DEPTH = 10;

	public static String commpressPicForScale(String srcPath, String desPath, long desFileSize, double accuracy) throws IOException {
		if (StringUtils.isEmpty(srcPath) || StringUtils.isEmpty(desPath)) {
			throw new IllegalArgumentException("源路径和目标路径不得为空");
		}

		File srcFile = new File(srcPath);
		if (!srcFile.exists()) {
			throw new IllegalArgumentException("源文件不存在");
		}

		if (desFileSize <= 0 || accuracy <= 0 || accuracy > 1) {
			throw new IllegalArgumentException("所需的文件大小必须为正");
		}

		Thumbnails.of(srcPath).scale(1f).toFile(desPath);
		compressPicCycle(desPath, desFileSize, accuracy, 0);
		return desPath;
	}

	private static void compressPicCycle(String desPath, long desFileSize, double accuracy, int depth) throws IOException {
		if (depth > MAX_RECURSION_DEPTH) {
			return;
		}

		File desFile = new File(desPath);
		long fileSize = desFile.length();
		if (fileSize <= desFileSize * 1024) {
			return;
		}

		BufferedImage bim = ImageIO.read(desFile);
		int width = bim.getWidth();
		int height = bim.getHeight();
		int desWidth = new BigDecimal(width).multiply(new BigDecimal(accuracy)).intValue();
		int desHeight = new BigDecimal(height).multiply(new BigDecimal(accuracy)).intValue();

		Thumbnails.of(desPath).size(desWidth, desHeight).outputQuality(accuracy).toFile(desPath);
		compressPicCycle(desPath, desFileSize, accuracy, depth + 1);
	}

	public static void main(String[] args) {
		try {
			PicUtil.commpressPicForScale("C:\\Users\\123\\Desktop\\1.png",
					"C:\\Users\\123\\Desktop\\12.jpg", 500, 0.8);
		} catch (IOException e) {
			logger.error("Error while compressing image", e);
		}
	}
}
