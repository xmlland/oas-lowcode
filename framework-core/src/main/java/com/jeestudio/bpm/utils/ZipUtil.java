package com.jeestudio.bpm.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @Description: ZIP压缩工具
 */
public class ZipUtil {
    private static Logger logger = LoggerFactory.getLogger(ZipUtil.class);

    /**
     * 多文件zip压缩下载 （说明：读取文件流到zip流中，之后下载）
     *
     * @param response
     * @param downloadName 下载后的压缩包名，带后缀
     * @param map          存放文件信息的map ，key为该文件压缩后的目录层级路径如：/xxx/xxx/xxx.jpg，为文件名时则在压缩包的顶层
     *                     如：xxx.jpg。 value为下载文件所在的路径
     */
    public static void MultiFileZipDownload(HttpServletResponse response, String downloadName, Map<String, String> map, String fileRoot) {
        Integer contentLen;
        ZipOutputStream zos = null;
        try {
            File dir = new File(fileRoot + "/zipTempPath");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            zos = new ZipOutputStream(new FileOutputStream(new File(fileRoot + "/zipTempPath/" + downloadName)));
        } catch (FileNotFoundException e1) {
            logger.error("创建ZIP临时文件失败", e1);
        }
        try {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String name = entry.getKey();
                String path = entry.getValue();
                //String type = entry.getValue().split(",")[1];
                String type = "";
                InputStream is = null;
                BufferedInputStream in = null;
                byte[] buffer = new byte[1024];
                int len;
                //创建zip实体（一个文件对应一个ZipEntry）
                //name --->压缩包的层级路径
                ZipEntry zipObj = new ZipEntry(name);
                try {
                    //获取需要下载的文件流
                    File file = new File(path);
                    if (file.exists()) {
                        is = new FileInputStream(file);
                        zos.putNextEntry(zipObj);
                        byte[] fileBuffer = new byte[1024];
                        byte[] fileBuffer2 = new byte[1024];
                        //文件流循环写入ZipOutputStream
                        //判断类型，如果是文件，需要解密，压缩包之类的不需要解密
                        if (false && FileUtil.TYPE_FILE.equals(type)) {
                            while ((len = is.read(fileBuffer)) != -1) {
                                for (int j = 0; j < len; j++) {
                                    fileBuffer2[j] = (byte) (fileBuffer[j] ^ FileUtil.numOfEncAndDec);
                                }
                                zos.write(fileBuffer2, 0, len);
                            }
                        } else {
                            while ((len = is.read(fileBuffer)) != -1) {
                                zos.write(fileBuffer, 0, len);
                            }
                        }
                    } else {
                        System.out.println(name + "---------------path==" + path);
                    }
                } catch (Exception e) {
                    logger.info("下载全部附件--压缩文件出错", e);
                } finally {
                    if (entry != null) {
                        try {
                            if (zos != null) {
                                zos.closeEntry();
                            }
                        } catch (Exception e) {
                            logger.info("下载全部附件--zos实体关闭失败", e);
                        }
                    }
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Exception e) {
                            logger.info("下载全部附件--文in流关闭失败", e);
                        }
                    }
                    if (is != null) {
                        try {
                            is.close();
                        } catch (Exception e) {
                            logger.info("下载全部附件-is流关闭失败", e);
                        }
                    }
                }
            }
        } finally {
            if (zos != null) {
                try {
                    zos.close();
                } catch (Exception e2) {
                    logger.info("关闭zos流时出现错误{}", e2);
                }
            }
        }

        //下载，并删除临时文件
        FileInputStream fileInputStream = null;
        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            fileInputStream = new FileInputStream(fileRoot + "/zipTempPath/" + downloadName);

            response.setContentType("multipart/form-data");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(downloadName, "UTF-8"));
            response.addHeader("Transfer-Encoding", "chunked");
            byte[] fileBuffer = new byte[20480];
            while (fileInputStream.read(fileBuffer) != -1) {
                outputStream.write(fileBuffer);
            }
        } catch (FileNotFoundException e) {
            logger.error("读取ZIP文件失败", e);
        } catch (IOException e) {
            logger.error("下载ZIP文件失败", e);
        } finally {
            //close
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    logger.warn("关闭文件输入流失败", e);
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e2) {
                    logger.info("关闭outputStream时出现错误{}", e2);
                }
            }
            File file = new File(fileRoot + "/zipTempPath/" + downloadName);
            if (file.exists()) {
                file.delete();
            }
        }
    }
}
