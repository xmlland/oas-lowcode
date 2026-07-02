import {FormControlProps} from "@/dynamic/DynamicFormItemConfig";
import config from "@/config"

class UploadProps extends FormControlProps {
    /**
     * 是否显示
     * @type {boolean}
     */
    visible = true

    /**
     * 是否仅允许图片
     * @type {boolean}
     */
    picture = false

    /**
     * 文件最大 50M
     * @type {number}
     */
    maxSize = config.maxFileSize

    /**
     * 文件个数
     * @type {number}
     */
    fileCount = 5

    /**
     * 允许上传的文件类型
     * @type {*}
     */
    accepts = config.acceptFiles

    /**
     * 允许上传的文件类型 , 逗号分隔
     * @type {string}
     */
    acceptsStr = config.acceptFiles.join(',')

    /**
     * 是否允许删除上传的文件
     * @type {boolean}
     */
    deleteAble = true

    /**
     * 上传按钮文字
     * @type {string}
     */
    buttonText = '上传'

    /**
     * 上传提示文字
     * @type {string}
     */
    promptText = ''

    /**
     * 是否预览文件
     * @type {boolean}
     */
    previewFile = false

    /**
     * 是否允许上传文件夹
     * @type {boolean}
     */
    directory = false

    /**
     * 是否允许多选
     * @type {boolean}
     */
    multiple = false

    ignoreProperty = ['ignoreProperty', 'props', 'apply', 'disabled']
    props = {}
    apply = () => {
        let names = Object.getOwnPropertyNames(this);
        let _props = {}
        for (let name of names) {
            if (this.ignoreProperty.indexOf(name) === -1) {
                _props[name] = this[name]
            }
        }
        if (this.acceptsStr) {
            _props['accepts'] = this.acceptsStr.split(',')
        }
        let newObj = {}
        Object.assign(newObj, _props)
        this.props = newObj
    }
    constructor() {
        super();
    }
}

export default UploadProps
