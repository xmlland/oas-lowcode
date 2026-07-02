var version = "0.0.23";
var baseFiles = "./designer.style.css," +

    "./designer.umd.js,";

var specailFile = ""; //处理特殊页面需要添加的文件引用

importCssOrJs(baseFiles + specailFile

);

function importCssOrJs(importFiles) {

    if (null == importFiles) {

        console.log("没有文件内容!!!")

        return;

    }

    var files = importFiles.split(",");

    // console.log(files)

    //页面解析到当前为止所有的script标签
    var js = document.scripts;
    //js[js.length - 1] 就是当前的js文件的路径
    js = js[js.length - 1].src.substring(0, js[js.length - 1].src.lastIndexOf("/") + 1);


    for (var i = 0; i < files.length; i++) {

        var frameFile = files[i];

        if (frameFile.match(/.*\.js/)) { //引入的为js文件

            document.write("<script type=\"text/javascript\" src=" + js + frameFile + '?' + version + "></script>");

            continue; //找到跳出循环

        }

        if (frameFile.match(/.*\.css/)) { //引入的为css文件

            document.write("<link rel=\"stylesheet\" type=\"text/css\" href=" + js + frameFile + '?' + version + ">");

            continue; //找到跳出循环

        }

    }

}