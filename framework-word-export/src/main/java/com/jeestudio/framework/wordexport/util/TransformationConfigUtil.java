package com.jeestudio.framework.wordexport.util;

import com.deepoove.poi.data.PictureRenderData;
import com.deepoove.poi.data.Pictures;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * @Description: Word转换配置工具
 */
@Component
public class TransformationConfigUtil {

	private static final Logger logger = LoggerFactory.getLogger(TransformationConfigUtil.class);

	private static String fileServiceToken = "";

	@Value("${project.file-service-token:}")
	private String fileServiceTokenConfig;

	@PostConstruct
	public void init() {
		fileServiceToken = this.fileServiceTokenConfig;
		if (fileServiceToken == null || fileServiceToken.isEmpty()) {
			logger.warn("project.file-service-token is not configured. File service token will be empty.");
		}
	}


	/**
	 * config可用参数：
	 *
	 * resizeInProportion：bool = true 是否等比例缩放（若同时设定宽高度则设为true无效）
	 * pictureWidth: Integer 宽度（会被最大宽度覆盖）
	 * pictureHeight: Integer 高度（会被最大高度覆盖）
	 * pictureSizeLimit：String = "limit", ["limit","noLimit"] 最大宽高配置 (同时有宽高时默认为noLimit；设置为noLimit时最大宽高无效)
	 * pictureMaxWidth: Integer 最大宽度
	 * pictureMaxHeight: Integer = 150 最大高度
	 * pictureAlign: String = "left", ["left"(居左),"center"(居中),"right"(局右),"default"(不变)] 图片位置
	 *
	* */
	public static PictureRenderData transToPicture(String url, Map<String,Object> config){
		/*
		* **** 思路 ****
		*
		* 当宽高都有时： 直接设置图片大小，且默认图片大小限制为"noLimit"
		* 当只有宽或只有高时：
		* 	当等比例缩放为true（默认）时：
		* 		等比例缩放
		* 	为false时：
		* 		直接改变宽高
		*
		* 当图片大小限制不为"noLimit"时：
		* 	当宽高未设定（为null）时：
		* 		默认高度限制150
		* 	当仅有宽或高作限制时：
		*			对宽/高进行限制
		* 	当同时有宽高限制时：
		* 		默认先限制宽后限制高
		*
		* */
		// url 请求，拼接添加一个新的参数 token 如果不含有 token参数情况下
		if (!url.contains("token=") && fileServiceToken != null && !fileServiceToken.isEmpty()){
			if (url.contains("?")){
				url += "&token=" + fileServiceToken;
			}else{
				url += "?token=" + fileServiceToken;
			}
		}

		Pictures.PictureBuilder picture = Pictures.ofUrl(url);

		Object widthObj = config.get("pictureWidth");
		Object heightObj = config.get("pictureHeight");
		Object maxHeightObj = config.get("pictureMaxHeight");
		Object maxWidthObj = config.get("pictureMaxWidth");
		Object resizeInProportionObj = config.get("resizeInProportion");      // 等比例缩放，默认为true
		Object sizeLimitObj = config.get("pictureSizeLimit");      // 图片大小限制配置，默认为"limit"，同时有宽高时默认为"noLimit"
		Object pictureAlignObj = config.get("pictureAlign");

		String pictureAlign;  // 图片位置 默认为Left（居左）
		if (pictureAlignObj == null) {
			pictureAlign = "left";
		} else {
			pictureAlign = (String) pictureAlignObj;
		}

		BufferedImage image;
		try {
			image = getImageByUrl(url);
		} catch (Exception e) {
			e.printStackTrace();
			return picture.create();
		}

		int newHeight = image.getHeight();
		int newWidth = image.getWidth();
		String sizeLimit;
		if (sizeLimitObj == null) {
			sizeLimit = "limit";
		} else {
			sizeLimit = (String) sizeLimitObj;
		}

		boolean resizeInProportion = true;
		if (resizeInProportionObj != null) {
			resizeInProportion = (boolean) resizeInProportionObj;
		}

		// 设置图片宽高
		if (widthObj != null || heightObj != null) {
			if (widthObj != null && heightObj != null) {
				newWidth = (int) widthObj;
				newHeight = (int) heightObj;
				if (sizeLimitObj == null) {
					sizeLimit = "noLimit";
				}
			} else {
				if (widthObj != null) {
					int toWidth = (int) widthObj;
					if (resizeInProportion) {
						double aspectRatio = (double) newHeight / (double) newWidth; // 计算图片的纵横比
						newHeight = (int) (toWidth * aspectRatio);
						newWidth = toWidth;
					} else {
						newWidth = toWidth;
					}
				} else {
					int toHeight = (int) heightObj;
					if (resizeInProportion) {
						double aspectRatio = (double) newWidth / (double) newHeight; // 计算图片的纵横比
						newWidth = (int) (toHeight * aspectRatio);
						newHeight = toHeight;
					} else {
						newHeight = toHeight;
					}
				}
			}
			picture.size(newWidth, newHeight);
		}

		// 图片大小限制
		if (!"noLimit".equals(sizeLimit)) {
			boolean needResizeFlag = false;
			if(maxHeightObj == null && maxWidthObj == null){
				maxHeightObj = 150;
			}
			if (maxWidthObj != null) {
				int toWidth = (int) maxWidthObj;
				if (toWidth < newWidth) {
					if (resizeInProportion) {
						double aspectRatio = (double) newHeight / (double) newWidth; // 计算图片的纵横比
						newHeight = (int) (toWidth * aspectRatio);
						newWidth = toWidth;
					} else {
						newWidth = toWidth;
					}
					needResizeFlag = true;
				}
			}
			if (maxHeightObj != null) {
				int toHeight = (int) maxHeightObj;
				if (toHeight < newHeight) {
					if (resizeInProportion) {
						double aspectRatio = (double) newWidth / (double) newHeight; // 计算图片的纵横比
						newWidth = (int) (toHeight * aspectRatio);
						newHeight = toHeight;
					} else {
						newHeight = toHeight;
					}
					needResizeFlag = true;
				}
			}
			if (needResizeFlag) {
				picture.size(newWidth, newHeight);
			}
		}
		return setPictureAlign(picture, pictureAlign).create();
	}

	private static Pictures.PictureBuilder setPictureAlign(Pictures.PictureBuilder picture,String align){
		switch (align.toLowerCase()){
			case "left":
				return picture.left();
			case "right":
				return picture.right();
			case "center":
				return picture.center();
			case "default":
				return picture;
			default:
				return picture;
		}
	}

	private static BufferedImage getImageByUrl(String url) throws Exception{
		BufferedImage image;
		image = ImageIO.read(new URL(url));
		if (image == null) {
			throw new RuntimeException("Can not find image by url:" + url);
		}
		return image;
	}
}
