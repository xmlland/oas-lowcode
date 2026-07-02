import HelpButton from "./help/HelpButton";

import QueryArea from "./query/QueryArea";
import QueryField from "./query/QueryField";
import SingleTable from "./table/SingleTable"
import SingleTableView from "./view/SingleTableView";
import TreeTableView from "./view/TreeTableView";
import LeftTreeRightTableView from "./view/LeftTreeRightTableView";

import UMap from "@/components/map/UMap";
import UModal from "@/components/modal/UModal";
import USelectDataModal from "@/components/modal/USelectDataModal";
import UIcon from "@/components/icon/UIcon";
import UFileItem from "@/components/ext/UFileItem";
import UQRCode from "@/components/ext/UQRCode";
import UCard from "@/components/card/UCard";
import UPermission from "@/components/role/UPermission";
import URole from "@/components/role/URole";
import UNavCard from "@/components/nav/UNavCard";
import UNavTab from "@/components/nav/UNavTab";
import UTabs from "@/components/nav/UTabs";
import UTitle from "@/components/text/UTitle";
import UTree from "@/components/tree/UTree";
import UMenu from "@/components/menu/UMenu";
import UFormTitle from "@/components/form/UFormTitle";
import UForm from "@/components/form/UForm";
import UArea from "@/components/form/UArea";
import UDate from "@/components/form/UDate";
import UDateRange from "@/components/form/UDateRange";
import UDynamicFormItem from "@/components/form/UDynamicFormItem";
import UInput from "@/components/form/UInput";
import UIconSelect from "@/components/form/UIconSelect";
import UJson from "@/components/form/UJson";
import ULngLatSelect from "@/components/form/ULngLatSelect";
import UModalSelect from "@/components/form/UModalSelect";
import UModalMultiSelect from "@/components/form/UModalMultiSelect";
import USelect from "@/components/form/USelect";
import USwitch from "@/components/form/USwitch";
import UTinymce from "@/components/form/UTinymce";
import UTreeSelect from "@/components/form/UTreeSelect";
import UYesNo from "@/components/form/UYesNo";
import UUpload from "@/components/form/UUpload";
import USerialNo from "@/components/form/USerialNo";
import UTimelinePicker from "@/components/form/UTimelinePicker";
import UOfficeSelect from "@/components/form/sys/UOfficeSelect";
import UUserSelect from "@/components/form/sys/UUserSelect";
import UUsersSelect from "@/components/form/sys/UUsersSelect";
import UMessageLink from "@/components/text/UMessageLink";
import USysAnnounceListForm from "@/components/ext/USysAnnounceListForm";
import USysMsgListForm from "@/components/ext/USysMsgListForm";
import UExportFileQueue from "@/components/ext/UExportFileQueue";
import UCodeMirror from "@/components/form/code/UCodeMirror";

// 存储组件列表
const components = [
    HelpButton,
    QueryArea,
    QueryField,
    SingleTable,
    SingleTableView,
    TreeTableView,
    LeftTreeRightTableView,
    UMap,
    UModal,
    USelectDataModal,
    UFileItem,
    UQRCode,
    UIcon,
    UCard,
    UPermission,
    URole,
    UNavCard,
    UNavTab,
    UTabs,
    UTitle,
    UTree,
    UMenu,
    UForm,
    UFormTitle,
    UArea,
    UDate,
    UDateRange,
    UDynamicFormItem,
    UInput,
    UIconSelect,
    UJson,
    ULngLatSelect,
    UModalSelect,
    UModalMultiSelect,
    USelect,
    USwitch,
    UTinymce,
    UTreeSelect,
    UYesNo,
    UUpload,
    USerialNo,
    UTimelinePicker,
    UOfficeSelect,
    UUserSelect,
    UUsersSelect,
    UMessageLink,
    USysAnnounceListForm,
    USysMsgListForm,
    UExportFileQueue,
    UCodeMirror
]
// 定义 install 方法，接收 Vue 作为参数。如果使用 use 注册插件，则所有的组件都将被注册
const install = function (Vue) {
    // 判断是否安装
    if (install.installed) return
    // 遍历注册全局组件

    components.map(component =>component && Vue.component(component.name, component))
}
export default {
    // 导出的对象必须具有 install，才能被 Vue.use() 方法安装
    install,
    // 以下是具体的组件列表
    HelpButton,
    QueryArea,
    QueryField,

    SingleTable,

    SingleTableView,
    TreeTableView,
    LeftTreeRightTableView,

    UMap,
    UModal,
    USelectDataModal,
    UFileItem,
    UQRCode,
    UIcon,
    UCard,
    UPermission,
    URole,
    UNavCard,
    UNavTab,
    UTabs,
    UTitle,
    UTree,
    UMenu,
    UForm,
    UFormTitle,
    UArea,
    UDate,
    UDateRange,
    UDynamicFormItem,
    UInput,
    UIconSelect,
    UJson,
    ULngLatSelect,
    UModalSelect,
    UModalMultiSelect,
    USelect,
    USwitch,
    UTinymce,
    UTreeSelect,
    UYesNo,
    UUpload,
    USerialNo,
    UTimelinePicker,
    UOfficeSelect,
    UUserSelect,
    UUsersSelect,
    UMessageLink,
    UExportFileQueue
}
