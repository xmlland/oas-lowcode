package com.jeestudio.bpm.utils;

import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.OpenOfficeDocumentConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @Description: Word转PDF工具
 **/
public class WordToPdfUtil {

    private static final Logger logger = LoggerFactory.getLogger(WordToPdfUtil.class);

    // 将word格式的文件转换为pdf格式
    public static void WordToPDF(String startFile, String overFile) throws IOException {
        // 源文件目录
        File inputFile = new File(startFile);
        if (!inputFile.exists()) {
            System.out.println("源文件不存在！");
            return;
        }

        // 输出文件目录
        File outputFile = new File(overFile);

        //文件不存在
        if (inputFile.exists() && !outputFile.exists()) {
            boolean isSuccess = true;
            //启动服务
            String OpenOffice_HOME = "C:/Program Files (x86)/OpenOffice 4";// 这里是OpenOffice的安装目录
            if (OpenOffice_HOME.charAt(OpenOffice_HOME.length() - 1) != '/') {
                OpenOffice_HOME += "/";
            }
            Process pro = null;
            OpenOfficeConnection connection = null;
            // 启动OpenOffice的服务
            String command = OpenOffice_HOME + "program\\soffice.exe -headless -accept=\"socket,host=127.0.0.1,port=8100;urp;\" -nofirststartwizard";
            // connect to an OpenOffice.org instance running on port 8100
            try {
                pro = Runtime.getRuntime().exec(command);
                connection = new SocketOpenOfficeConnection(8100);
                connection.connect();

                // convert
                OpenOfficeDocumentConverter converter = new OpenOfficeDocumentConverter(connection);
                System.out.println(inputFile + "=" + outputFile);
                System.out.println("转换中...");
                converter.convert(inputFile, outputFile);
            } catch (Exception ex) {
                isSuccess = false;
                logger.error("Word转PDF失败", ex);

            } finally {
                // close the connection
                if (connection != null) {
                    connection.disconnect();
                    connection = null;
                }
                // 关闭OpenOffice服务的进程
                if (pro != null) {
                    pro.destroy();
                }
            }
            if (isSuccess) {
                System.out.println("生成PDF文件成功");
            } else {
                System.out.println("程序错误，生成失败!");
            }
        } else {
            System.out.println("源文件不存在或者文件已转化");
        }
    }
}
