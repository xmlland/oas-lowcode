import {UUID} from "@/lib/tools";
import QRCode from "qrcode";

/**
 * 生成二维码
 * @param text  二维码内容
 * @param size  二维码大小
 * @param color 二维码颜色
 * @param bgColor 二维码背景色
 * @return {Promise<unknown>} 返回base64格式的图片
 */
export const generateQRCode = (text, size = 222, color = '#000000', bgColor= '') => {
    let id = 'qrcode-canvas-' + UUID();
    let htmlCanvasElement = document.createElement('canvas');
    htmlCanvasElement.setAttribute('id', id)
    htmlCanvasElement.width = size;
    htmlCanvasElement.height = size;
    document.body.appendChild(htmlCanvasElement);
    const canvas = document.getElementById(id);
    return new Promise((resolve, reject) => {
        QRCode.toCanvas(canvas, text, {
            width: size,
            height: size,
            color: {
                dark: color,
                light: bgColor
            },
        }, (error) => {
            if (error) {
                reject(error);
                //console.error('二维码生成出错:',error);
            } else {
                //console.log('二维码生成成功');
                let res = canvas.toDataURL('image/png');
                document.body.removeChild(canvas);
                resolve(res);
            }
        });
    })

}
