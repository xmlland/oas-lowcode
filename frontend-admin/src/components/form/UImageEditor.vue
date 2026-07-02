<template>
  <div class="image-editor-container">
    <div class="edit-box" id="edit-box">
      <canvas id="mask"></canvas>
    </div>
    <div class="button-group">
      <a-space warp>
        <a-button style="margin-left: 20px" @click="square"><img class="icon-style"
                                                                 src="@/assets/img/square0.png" alt="Square"></a-button>
        <a-button @click="ellipse"><img class="icon-style" src="@/assets/img/circle0.png" alt="Ellipse">
        </a-button>
        <a-button @click="arrow"><img class="icon-style" src="@/assets/img/arrow0.png" alt="Arrow">
        </a-button>
        <a-button @click="Pencil"><img class="icon-style" src="@/assets/img/Pencil0.png" alt="Pencil">
        </a-button>
        <a-button @click="textbox"><img class="icon-style" src="@/assets/img/textbox0.png" alt="Text">
        </a-button>
        <a-button @click="clickDelete"><img class="icon-style"
                                            src="@/assets/img/delete0.png" alt="Delete"></a-button>
        <a-button @click="rotate(90)"><img class="icon-style"
                                           src="@/assets/img/rotate0.png" alt="Rotate"></a-button>
        <a-button @click="saveFile"><img class="icon-style" src="@/assets/img/save0.png" alt="Save">
        </a-button>
        <a-button style="padding: 0"><input type="color" v-model="selectColor" @input="changeStrokeColor"
                                            style="background-color: white;border-radius:5px;border: none"></a-button>
      </a-space>
    </div>
  </div>
</template>
<script>
export default {
  name: 'UImageEditor'
}
</script>
<script setup>
import * as fabric from "fabric";
import {onMounted, ref} from "vue";
// import {onMounted, ref} from "vue";
import {postActionShowLoading} from "@/api/action";

const emits = defineEmits(['imgSaveSuccess'])
const props = defineProps({
  editFile: {
    type: Object,
    default: () => {
      return {
        url: '',
        path: '',

        name: ''
      }
    }
  },
  customFill: {
    default: ''
  },
  customStrokeWidth: {
    default: 2
  }
})
let canvas;
let currentType = null
let spot = null
let downPoint = ''
let currentSquare = null
let currentCircle = null
let currentEllipse = null
let currentTextBox = null
let customStrokeDashArray = null
let upPoint = null
let customFontSize = 18
let selectColor = ref("#ff0000")
let customStroke = 'red'
// let imgScale = 1

let currentImgObj;
const changeStrokeColor = (e) => {
  selectColor.value = e.target.value;
  customStroke = selectColor.value;
}
let imgRandomKey = ref(Math.random())

function deleteObject(eventData, transform) {
  let target = transform.target;
  let canvas = target.canvas;
  canvas.remove(target);
  canvas.requestRenderAll();
}

function renderIcon(icon) {
  return function renderIcon(ctx, left, top, styleOverride, fabricObject) {
    let size = this.cornerSize;
    ctx.save();
    ctx.translate(left, top);
    ctx.rotate(fabric.util.degreesToRadians(fabricObject.angle));
    ctx.drawImage(icon, -size / 2, -size / 2, size, size);
    ctx.restore();
  }
}

let editBoxWidth;
let editBoxHeight;
let backgroundScaleAgain = false,//图片在旋转的时候是否二次缩放，目前之碰到过90°和270°旋转的时候，新的背景高度或超出
    beforeRotateCanvasHeight,
    beforeRotateCanvasWidth

let initCanvas = () => {
  canvas = new fabric.Canvas('mask');
  let editBoxDom = document.getElementById("edit-box")
  editBoxWidth = parseInt(window.getComputedStyle(editBoxDom).width, 10)
  editBoxHeight = parseInt(window.getComputedStyle(editBoxDom).height, 10)

  let deleteIcon = "data:image/svg+xml,%3C%3Fxml version='1.0' encoding='utf-8'%3F%3E%3C!DOCTYPE svg PUBLIC '-//W3C//DTD SVG 1.1//EN' 'http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd'%3E%3Csvg version='1.1' id='Ebene_1' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink' x='0px' y='0px' width='595.275px' height='595.275px' viewBox='200 215 230 470' xml:space='preserve'%3E%3Ccircle style='fill:%23F44336;' cx='299.76' cy='439.067' r='218.516'/%3E%3Cg%3E%3Crect x='267.162' y='307.978' transform='matrix(0.7071 -0.7071 0.7071 0.7071 -222.6202 340.6915)' style='fill:white;' width='65.545' height='262.18'/%3E%3Crect x='266.988' y='308.153' transform='matrix(0.7071 0.7071 -0.7071 0.7071 398.3889 -83.3116)' style='fill:white;' width='65.544' height='262.179'/%3E%3C/g%3E%3C/svg%3E";
  let deleteImg = document.createElement('img');
  deleteImg.src = deleteIcon;
  //设置全局删除，选中框样式
  fabric.Object.prototype.controls.deleteControl = new fabric.Control({
    x: 0.5,
    y: -0.5,
    offsetY: -16,
    offsetX: 16,
    cursorStyle: 'pointer',
    mouseUpHandler: deleteObject,
    render: renderIcon(deleteImg),
    cornerSize: 24
  });
  fabric.Object.prototype.transparentCorners = false;
  fabric.Object.prototype.cornerColor = '#293075';
  fabric.Object.prototype.cornerStyle = 'circle';
  fabric.Object.prototype.cornerSize = 5;
  // 创建一个带有背景图像的画布
  fabric.Image.fromURL('http://' + window.location.host + props.editFile.url + "&isEdit=true&randomKey=" + imgRandomKey.value, (img) => {
    let imgWidth = img.width
    let imgHeight = img.height
    if (!(imgWidth < editBoxWidth && imgHeight < editBoxHeight)) {
      if (imgHeight > imgWidth) {//如果图片的高大于宽，则按照高度进行缩（画布80%）
        imgWidth = (imgWidth / imgHeight) * (editBoxHeight * 0.8)
        imgHeight = editBoxHeight * 0.8
      } else {
        imgHeight = ((imgHeight * editBoxWidth * 0.7) / imgWidth)
        imgWidth = (editBoxWidth * 0.7)
      }
      if (imgHeight > editBoxHeight) {
        imgWidth = (imgWidth / imgHeight) * (editBoxHeight * 0.9)
        imgHeight = editBoxHeight * 0.9
      }
    }

    canvas.setWidth(imgWidth)
    canvas.setHeight(imgHeight)

    canvas.setBackgroundImage(img, canvas.renderAll.bind(canvas), {
      scaleX: canvas.width / img.width, scaleY: canvas.height / img.height,
    });

    if (props.editFile.editObjects != null) {
      let editObjects = JSON.parse(props.editFile.editObjects);
      let objects = editObjects.objects
      let backgroundAngle = editObjects['image-angle']
      objects.forEach(obj => {
        if (obj.type === 'ellipse') {
          canvas.add(new fabric.Ellipse(obj));
        } else if (obj.type === 'rect') {
          canvas.add(new fabric.Rect(obj));
        } else if (obj.type === 'circle') {
          canvas.add(new fabric.Circle(obj));
        } else if (obj.type === 'path') {
          canvas.add(new fabric.Path(obj.path, obj));
        } else if (obj.type === 'triangle') {
          canvas.add(new fabric.Triangle(obj));
        } else if (obj.type === 'textbox') {
          let text = new fabric.Textbox(obj.text, {editable: true, ...obj});
          canvas.add(text);
          currentTextBox = text;
          // 监听文本变化
          text.on('changed', function () {
            let text = this.text;
            let fontSize = this.fontSize;
            let fontFamily = this.fontFamily;

            // 创建一个临时文本对象来测量宽度
            let measuringText = new fabric.Text(text, {
              fontSize: fontSize,
              fontFamily: fontFamily,
              left: 0,
              top: 0,
              originX: 'left',
              originY: 'top'
            });

            // 计算文本宽度
            let textWidth = measuringText.width;

            // 设置文本框的宽度
            this.set({width: textWidth + this.padding * 2});
            // 移除临时文本对象
            canvas.remove(measuringText);
            // 重绘画布
            canvas.renderAll();
          });
        }
      })

      let h = canvas.height,
          w = canvas.width,
          backgroundImage = canvas.backgroundImage;
      backgroundImage.set("angle", backgroundAngle)
      if (backgroundAngle % 360 === 90 || backgroundAngle % 360 === 270) {
        canvas.setWidth(h)
        canvas.setHeight(w)
        if (backgroundAngle % 360 === 270) {
          backgroundImage.set('left', 0);
          backgroundImage.set('top', w);
        } else {
          backgroundImage.set('left', h);
          backgroundImage.set('top', 0);
        }
      } else if (backgroundAngle % 360 === 180 || backgroundAngle % 360 === 0) {
        if (backgroundAngle % 360 === 180) {
          backgroundImage.set('left', w);
          backgroundImage.set('top', h);
        } else {
          backgroundImage.set('left', 0);
          backgroundImage.set('top', 0);
        }
      }

      if (backgroundImage.get('angle') % 360 === 90 || backgroundImage.get('angle') % 360 === 270) {
        if (canvas.getHeight() > editBoxHeight) {
          if (!backgroundScaleAgain) {
            console.log("shit")
            backgroundScaleAgain = true
            beforeRotateCanvasHeight = h
            beforeRotateCanvasWidth = w
          }
          let newHeight = editBoxHeight * 0.9,
              newWidth = canvas.width / canvas.height * newHeight;
          canvas.setWidth(newWidth)
          canvas.setHeight(newHeight)
          if (backgroundImage.get('angle') % 360 === 90) {
            backgroundImage.set('left', newWidth);
            backgroundImage.set('top', 0);
          } else {
            backgroundImage.set('left', 0);
            backgroundImage.set('top', newHeight);
          }
          canvas.setBackgroundImage(backgroundImage, canvas.renderAll.bind(canvas), {
            scaleX: newHeight / backgroundImage.width,
            scaleY: newWidth / backgroundImage.height,
          })
          // canvas.forEachObject(obj=>{
          //   let pLeft = obj.left,
          //       pTop=obj.top;
          //   obj.set({
          //     left:pLeft*newWidth/beforeRotateCanvasHeight,
          //     top:pTop*newHeight/beforeRotateCanvasWidth
          //   })
          //   obj.setCoords()
          // })
        }
      } else if (backgroundImage.get('angle') % 360 === 180 || backgroundImage.get('angle') % 360 === 0) {
        if (backgroundScaleAgain) {//还原旋转90°和270°时，产生的缩放
          canvas.setWidth(beforeRotateCanvasWidth)
          canvas.setHeight(beforeRotateCanvasHeight)
          if (backgroundImage.get('angle') % 360 === 180) {
            backgroundImage.set('left', beforeRotateCanvasWidth);
            backgroundImage.set('top', beforeRotateCanvasHeight);
          }
          canvas.setBackgroundImage(backgroundImage, canvas.renderAll.bind(canvas), {
            scaleX: canvas.width / backgroundImage.width,
            scaleY: canvas.height / backgroundImage.height,
          })
          // canvas.forEachObject(obj=>{
          //   let pLeft = obj.left,
          //       pTop=obj.top;
          //   obj.set({
          //     left:pLeft*canvas.height/w,
          //     top:pTop*canvas.width/h
          //   })
          //   obj.setCoords()
          // })
          backgroundScaleAgain = false
        }
      }
      canvas.renderAll()
    }

  });
  canvas.on('object:selected', function (e) {
    let activeObject = e.target;
    activeObject.setControlVisible('mt', true);
    activeObject.setControlVisible('mb', true);
    activeObject.setControlVisible('mtr', true); // mtr for rotating in the middle
  });
  canvas.on("mouse:down", canvasMouseDown); // 鼠标在画布上按下
  canvas.on("mouse:up", canvasMouseUp); // 鼠标在画布上松开
  canvas.on("mouse:move", canvasMouseMove);
}
onMounted(() => {
  initCanvas()
})
const drawArrow = (fromX, fromY, toX, toY, theta, headlen) => {
  theta = typeof theta !== "undefined" ? theta : 30;
  headlen = typeof headlen !== "undefined" ? headlen : 10;
  // 计算各角度和对应的P2,P3坐标
  let angle = (Math.atan2(fromY - toY, fromX - toX) * 180) / Math.PI,
      angle1 = ((angle + theta) * Math.PI) / 180,
      angle2 = ((angle - theta) * Math.PI) / 180,
      topX = headlen * Math.cos(angle1),
      topY = headlen * Math.sin(angle1),
      botX = headlen * Math.cos(angle2),
      botY = headlen * Math.sin(angle2);
  let arrowX,
      arrowY;
  let path = " M " + fromX + " " + fromY;
  path += " L " + toX + " " + toY;
  arrowX = toX + topX;
  arrowY = toY + topY;
  path += " M " + arrowX + " " + arrowY;
  path += " L " + toX + " " + toY;
  arrowX = toX + botX;
  arrowY = toY + botY;
  path += " L " + arrowX + " " + arrowY;
  return path;
}

function base64ToBlob(base64) {
  let arr = base64.split(",")
  let mime = arr[0].match(/:(.*?);/)[1]
  let bstr = atob(arr[1])
  let n = bstr.length
  let u8arr = new Uint8Array(n);
  while (n--) {
    u8arr[n] = bstr.charCodeAt(n);
  }
  return new Blob([u8arr], {
    type: mime,
  });
}

const saveFile = () => {
  let backgroundImage = canvas.backgroundImage,
      canvasPrimaryWidth = canvas.width,
      canvasPrimaryHeight = canvas.height,
      imgPrimaryWidth = backgroundImage.width,
      imgPrimaryHeight = backgroundImage.height,
      backgroundAngle = backgroundImage.angle,
      multiplier;
  if (imgPrimaryWidth !== canvasPrimaryWidth && imgPrimaryHeight !== canvasPrimaryHeight) {
    if (backgroundAngle % 360 === 90 || backgroundAngle % 360 === 270) {
      multiplier = imgPrimaryHeight / canvasPrimaryWidth;
    } else {
      multiplier = imgPrimaryHeight / canvasPrimaryHeight;
    }
  }
  let objs = [].concat(canvas.getObjects())
  let editObjects = {
    "image-angle": backgroundImage.get("angle"),
    "objects": objs
  }
  let blob = base64ToBlob(canvas.toDataURL({
    quality: 1,
    multiplier: multiplier
  }))
  let file = new File([blob], props.editFile.name, {
    type: 'image/png'
  })
  let formData = new FormData()
  formData.append('file', file)
  formData.append('editObjects', JSON.stringify(editObjects))
  formData.append('id', props.editFile.id)
  postActionShowLoading(`system/sysFile/updateFileByPath`, formData).then(res => {
    if (res.code === 0) {
      initCanvas()
      emits('imgSaveSuccess', true)
    } else {
      emits('imgSaveSuccess', false)
    }
  })
}
const square = () => {
  if (currentType === "square") {
    currentType = "";
  } else {
    currentType = "square";
  }
  canvas.selection = false;
  canvas.isDrawingMode = false;
  canvas.discardActiveObject().renderAll();
  disableOtherObjects()
}

const rotate = () => {
  canvas.forEachObject(object => {
    let angle_p = object.get('angle') % 90;//对有角度的元素记录已选择的角度，用于判断
    object.set('angle', object.get('angle') + 90); // 每次旋转90度
    let l = canvas.height - object.top - object.height;
    let t = object.left;
    let w = object.height;
    if (object.get('angle') % 360 === 90 + angle_p) {
      object.set('left', l + w);
      object.set('top', t);
    } else if (object.get('angle') % 360 === 180 + angle_p) {
      object.set('left', l + w);
      object.set('top', t);
    } else if (object.get('angle') % 360 === 270 + angle_p) {
      object.set('left', l + w);
      object.set('top', t);
    } else if (object.get('angle') % 360 === angle_p) {
      object.set('left', l + w);
      object.set('top', t);
      object.set('angle', angle_p)
    }
    // 更新元素的坐标
    object.setCoords();
  })

  //
  let h = canvas.height;
  let w = canvas.width;
  canvas.setWidth(h);
  canvas.setHeight(w);
  let backgroundImage = canvas.backgroundImage;
  backgroundImage.set('angle', backgroundImage.get('angle') + 90); // 每次旋转90度
  if (backgroundImage.get('angle') % 360 === 90) {
    backgroundImage.set('left', h);
    backgroundImage.set('top', 0);
  } else if (backgroundImage.get('angle') % 360 === 180) {
    backgroundImage.set('left', h);
    backgroundImage.set('top', w);
  } else if (backgroundImage.get('angle') % 360 === 270) {
    backgroundImage.set('left', 0);
    backgroundImage.set('top', w);
  } else if (backgroundImage.get('angle') % 360 === 0) {
    backgroundImage.set('left', 0);
    backgroundImage.set('top', 0);
    backgroundImage.set('angle', 0)
  }
  if (backgroundImage.get('angle') % 360 === 90 || backgroundImage.get('angle') % 360 === 270) {
    if (canvas.getHeight() > editBoxHeight) {
      if (!backgroundScaleAgain) {
        backgroundScaleAgain = true
        beforeRotateCanvasHeight = h
        beforeRotateCanvasWidth = w
      }
      let newHeight = editBoxHeight * 0.9,
          newWidth = canvas.width / canvas.height * newHeight;
      canvas.setWidth(newWidth)
      canvas.setHeight(newHeight)
      if (backgroundImage.get('angle') % 360 === 90) {
        backgroundImage.set('left', newWidth);
        backgroundImage.set('top', 0);
      } else {
        backgroundImage.set('left', 0);
        backgroundImage.set('top', newHeight);
      }
      canvas.setBackgroundImage(backgroundImage, canvas.renderAll.bind(canvas), {
        scaleX: newHeight / backgroundImage.width,
        scaleY: newWidth / backgroundImage.height,
      })
      canvas.forEachObject(obj=>{
        let pLeft = obj.left,
            pTop=obj.top;
        obj.set({
          left:pLeft*newWidth/h,
          top:pTop*newHeight/w
        })
        obj.setCoords()
      })
    }
  } else if (backgroundImage.get('angle') % 360 === 180 || backgroundImage.get('angle') % 360 === 0) {
    if (backgroundScaleAgain) {//还原旋转90°和270°时，产生的缩放
      console.log('shit')
      canvas.setWidth(beforeRotateCanvasWidth)
      canvas.setHeight(beforeRotateCanvasHeight)
      if (backgroundImage.get('angle') % 360 === 180) {
        backgroundImage.set('left', beforeRotateCanvasWidth);
        backgroundImage.set('top', beforeRotateCanvasHeight);
      }
      canvas.setBackgroundImage(backgroundImage, canvas.renderAll.bind(canvas), {
        scaleX: canvas.width / backgroundImage.width,
        scaleY: canvas.height / backgroundImage.height,
      })
      canvas.forEachObject(obj=>{
        let pLeft = obj.left,
            pTop=obj.top;
        obj.set({
          left:pLeft*canvas.height/w,
          top:pTop*canvas.width/h
        })
        obj.setCoords()
      })
      backgroundScaleAgain = false
    }
  }

  canvas.requestRenderAll();
}
//画椭圆
const ellipse = () => {
  if (currentType === "ellipse") {
    currentType = "";
  } else {
    currentType = "ellipse";
  }
  canvas.selection = false;
  canvas.isDrawingMode = false;
  canvas.discardActiveObject().renderAll();
  disableOtherObjects()
}
//画箭头
const arrow = () => {
  if (currentType === "arrow") {
    currentType = "";
  } else {
    currentType = "arrow";
  }
  canvas.selection = false;
  canvas.isDrawingMode = false;
  canvas.discardActiveObject().renderAll();
  disableOtherObjects()
}
//画笔
const Pencil = () => {
  if (currentType === "Pencil") {
    currentType = "";
    canvas.isDrawingMode = false;
  } else {
    currentType = "Pencil";
    canvas.isDrawingMode = true;
    canvas.freeDrawingBrush.width = props.customStrokeWidth;
    canvas.freeDrawingBrush.color = customStroke;
  }
  canvas.selection = false;
  canvas.discardActiveObject().renderAll();
  disableOtherObjects()
}
const disableOtherObjects = () => {
  //其他元素不可选中
  canvas.getObjects().forEach((item) => {
    item.selectable = false;
  });
}
//输入文字
const textbox = () => {
  if (currentType === "textbox") {
    currentType = "";
  } else {
    currentType = "textbox";
  }
  canvas.selection = false;
  canvas.isDrawingMode = false;
  canvas.discardActiveObject().renderAll();
  disableOtherObjects()
}
// 删除
const clickDelete = () => {
  let activeObject = canvas.getActiveObject();
  let getActiveObjects = canvas.getActiveObjects();
  if (getActiveObjects.length > 0) {
    getActiveObjects.forEach((item) => {
      canvas.remove(item);
    });
  } else {
    canvas.remove(activeObject);
  }
  canvas.discardActiveObject().renderAll();
}
const canvasMouseMove = e => {
      spot = e.pointer;
      canvas.getObjects().forEach((item) => {
        if (item.arrow && currentType === "arrow") {
          canvas.remove(item);
          canvas.add(
              new fabric.Path(
                  drawArrow(
                      downPoint.x,
                      downPoint.y,
                      spot.x,
                      spot.y,
                      30,
                      30
                  ),
                  {
                    stroke: customStroke,
                    fill: props.customFill,
                    strokeWidth: props.customStrokeWidth,
                    arrow: true,
                  }
              )
          );
        }
      });

      if (currentType === "ellipse" && currentEllipse) {
        const currentPoint = e.absolutePointer;
        let rx = Math.abs(downPoint.x - currentPoint.x) / 2;
        let ry = Math.abs(downPoint.y - currentPoint.y) / 2;
        let top =
            currentPoint.y > downPoint.y
                ? downPoint.y
                : downPoint.y - ry * 2;
        let left =
            currentPoint.x > downPoint.x
                ? downPoint.x
                : downPoint.x - rx * 2;

        // 分别设置圆形的半径、top和left
        currentEllipse.set("rx", rx);
        currentEllipse.set("ry", ry);
        currentEllipse.set("top", top);
        currentEllipse.set("left", left);

        canvas.requestRenderAll();
      }

      if (currentType === "circle" && currentCircle) {
        const currentPoint = e.absolutePointer;
        // 半径：用短边来计算圆形的直径，最后除以2，得到圆形的半径
        let radius =
            Math.max(
                Math.abs(downPoint.x - currentPoint.x),
                Math.abs(downPoint.y - currentPoint.y)
            ) / 2;
        // 计算圆形的top和left坐标位置
        let top =
            currentPoint.y > downPoint.y
                ? downPoint.y
                : downPoint.y - radius * 2;
        let left =
            currentPoint.x > downPoint.x
                ? downPoint.x
                : downPoint.x - radius * 2;

        // 分别设置圆形的半径、top和left
        currentCircle.set("radius", radius);
        currentCircle.set("top", top);
        currentCircle.set("left", left);

        canvas.requestRenderAll();
      }

      if (currentType === "square" && currentSquare) {
        const currentPoint = e.absolutePointer;

        // 创建矩形
        // 矩形参数计算（前面总结的4条公式）
        let top = Math.min(downPoint.y, currentPoint.y);
        let left = Math.min(downPoint.x, currentPoint.x);
        let width = Math.abs(downPoint.x - currentPoint.x);
        let height = Math.abs(downPoint.y - currentPoint.y);

        if (!width || !height) {
          return false;
        }
        currentSquare
            .set("top", top)
            .set("left", left)
            .set("width", width)
            .set("height", height);
        canvas.renderAll();
      }
    },
// 鼠标在画布上按下
    canvasMouseDown = e => {
      console.log(e.target)
      if (e.target && e.target === currentImgObj) {
        // 点击了图片对象
        // 在这里可以添加点击图片对象时的操作
        return
      }
      // 鼠标左键按下时，将当前坐标 赋值给 downPoint。{x: xxx, y: xxx} 的格式
      downPoint = e.absolutePointer;

      if (currentType !== "Pencil") {
        canvas.isDrawingMode = false;
      }

      if (currentType !== "") {
        if (currentType === "arrow") {
          canvas.add(
              new fabric.Path(
                  drawArrow(
                      downPoint.x,
                      downPoint.y,
                      spot.x,
                      spot.y,
                      30,
                      30
                  ),
                  {
                    stroke: customStroke,
                    fill: props.customFill,
                    strokeWidth: props.customStrokeWidth,
                    arrow: true,
                  }
              )
          );
        }

        if (currentType === "square") {
          currentSquare = new fabric.Rect({
            width: 0,
            height: 0,
            left: downPoint.x,
            top: downPoint.y,
            fill: props.customFill,
            stroke: customStroke,
            strokeWidth: props.customStrokeWidth,
            strokeDashArray: customStrokeDashArray,
          });
          canvas.add(currentSquare);
        }

        if (currentType === "ellipse") {
          currentEllipse = new fabric.Ellipse({
            top: downPoint.y,
            left: downPoint.x,
            rx: 0,
            ry: 0,
            fill: props.customFill,
            stroke: customStroke,
            strokeWidth: props.customStrokeWidth,
            strokeDashArray: customStrokeDashArray,
          });
          canvas.add(currentEllipse);
        }

        if (currentType === "circle") {
          // 使用 Fabric.js 提供的api创建圆形，此时圆形的半径是0
          currentCircle = new fabric.Circle({
            top: downPoint.y,
            left: downPoint.x,
            radius: 0,
            fill: props.customFill,
            stroke: customStroke,
            strokeWidth: props.customStrokeWidth,
            strokeDashArray: customStrokeDashArray,
          });
          canvas.add(currentCircle);
        }

      }

      //点击其他内容时退出文字编辑
      if (currentTextBox && e.target !== currentTextBox) {
        currentTextBox.exitEditing();
        canvas.renderAll();
      }
    },

// 鼠标在画布上松开
    canvasMouseUp = e => {
      upPoint = e.absolutePointer;
      canvas.getObjects().forEach((item) => {
        if (item.arrow) {
          item.arrow = null;
        }
      });
      canvas.selection = true;

      if (currentType === "square") {
        if (
            JSON.stringify(downPoint) === JSON.stringify(e.absolutePointer)
        ) {
          canvas.remove(currentSquare);
        } else {
          if (currentSquare) {
            //重新绘制
            canvas.add(
                new fabric.Rect({
                  width: currentSquare.width,
                  height: currentSquare.height,
                  left: currentSquare.left,
                  top: currentSquare.top,
                  fill: currentSquare.fill,
                  stroke: currentSquare.stroke,
                  strokeWidth: currentSquare.strokeWidth,
                  strokeDashArray: currentSquare.strokeDashArray,
                })
            );
            //移除原有形状
            canvas.remove(currentSquare);
            currentSquare = null;
          }
        }
      }

      if (currentType === "ellipse") {
        // 如果鼠标点击和松开是在同一个坐标，那就不会创建圆形（其实是把刚创建半径为0的圆形删掉）
        if (
            JSON.stringify(downPoint) === JSON.stringify(e.absolutePointer)
        ) {
          canvas.remove(currentEllipse);
        } else {
          if (currentEllipse) {
            canvas.add(
                new fabric.Ellipse({
                  left: currentEllipse.left,
                  top: currentEllipse.top,
                  rx: currentEllipse.rx,
                  ry: currentEllipse.ry,
                  fill: currentEllipse.fill,
                  stroke: currentEllipse.stroke,
                  strokeWidth: currentEllipse.strokeWidth,
                  strokeDashArray: currentEllipse.strokeDashArray,
                })
            );
            //移除原有形状
            canvas.remove(currentEllipse);
            currentEllipse = null;
          }
        }
      }

      if (currentType === "circle") {
        // 如果鼠标点击和松开是在同一个坐标，那就不会创建圆形（其实是把刚创建半径为0的圆形删掉）
        if (
            JSON.stringify(downPoint) === JSON.stringify(e.absolutePointer)
        ) {
          canvas.remove(currentCircle);
        } else {
          if (currentCircle) {
            canvas.add(
                new fabric.Circle({
                  left: currentCircle.left,
                  top: currentCircle.top,
                  radius: currentCircle.radius,
                  fill: currentCircle.fill,
                  stroke: currentCircle.stroke,
                  borderColor: currentCircle.borderColor,
                  strokeWidth: currentCircle.strokeWidth,
                  strokeDashArray: currentCircle.strokeDashArray,
                })
            );
            //移除原有形状
            canvas.remove(currentCircle);
            currentCircle = null;
          }
        }
      }

      if (currentType === "textbox") {
        let text = new fabric.Textbox("", {
          fill: customStroke,
          width: 200,
          top: upPoint.y,
          left: upPoint.x,
          fontSize: customFontSize,
          editable: true,
          textAlign: "left", // 文字对齐
          // lockRotation: true, // 禁止旋转
          lockScalingY: true, // 禁止Y轴伸缩
          lockScalingFlip: true, // 禁止负值反转
          splitByGrapheme: true, // 拆分中文，可以实现自动换行\
        });
        canvas.add(text);
        text.enterEditing();
        currentTextBox = text;
        // 监听文本变化
        text.on('changed', function () {
          let text = this.text;
          let fontSize = this.fontSize;
          let fontFamily = this.fontFamily;

          // 创建一个临时文本对象来测量宽度
          let measuringText = new fabric.Text(text, {
            fontSize: fontSize,
            fontFamily: fontFamily,
            left: 0,
            top: 0,
            originX: 'left',
            originY: 'top'
          });

          // 计算文本宽度
          let textWidth = measuringText.width;

          // 设置文本框的宽度
          this.set({width: textWidth + this.padding * 2});
          // 移除临时文本对象
          canvas.remove(measuringText);
          // 重绘画布
          canvas.renderAll();
        });
      }
      currentType = "";
      canvas.isDrawingMode = false;
      //其他元素可选中
      canvas.getObjects().forEach((item) => {
        if (item.id !== "bj") {
          item.selectable = true;
        }
      });
    }
</script>
<style scoped>
.image-editor-container {
  width: 100%;
  height: 100%;
  border: 1px solid rgba(112, 128, 144, 0.19);
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.edit-box {
  height: 90%;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #303133;
  overflow: auto
}

.button-group {
  border-top: 1px solid rgba(112, 128, 144, 0.19);
  border-radius: 10px;
  display: flex;
  flex-direction: row;
  height: 10%;
  justify-content: center;
  align-items: center;
}

.icon-style {
  width: 20px;
  height: 20px;
  margin: 0 auto
}
</style>
