<template>
 <div class="body">
   <div style="display: flex ; margin: 0;height: 100vh;background-color: #eee;" id="box">

     <!-- 侧边栏 -->
     <div style="width: 200px; z-index: 3;background-color: #001529;"
          class="ceBian flex column  yinYing"
     >
       <!-- 侧边头像栏 -->
       <div class="headerLeft">
         <div style="margin-left: 10px">特殊符号Ω</div>
       </div>
       <!-- 导航栏 -->
       <div style="flex: 1;">
         <div class="navigationItem" :class="currentNavigationItem==='commonUse'?'selectedNavigationItem':''"
              @click="currentNavigationItem='commonUse'">常用
         </div>
         <div class="navigationItem" :class="currentNavigationItem==='subIndex'?'selectedNavigationItem':''"
              @click="currentNavigationItem='subIndex'">上标
         </div>
         <div class="navigationItem" :class="currentNavigationItem==='topIndex'?'selectedNavigationItem':''"
              @click="currentNavigationItem='topIndex'">下标
         </div>
         <div class="navigationItem" :class="currentNavigationItem==='serialNumber'?'selectedNavigationItem':''"
              @click="currentNavigationItem='serialNumber'">序号
         </div>
         <div class="navigationItem" :class="currentNavigationItem==='unit'?'selectedNavigationItem':''"
              @click="currentNavigationItem='unit'">单位
         </div>
         <div class="navigationItem" :class="currentNavigationItem==='romanSymbols'?'selectedNavigationItem':''"
              @click="currentNavigationItem='romanSymbols'">罗马符号
         </div>
         <div class="navigationItem" :class="currentNavigationItem==='mathSymbols'?'selectedNavigationItem':''"
              @click="currentNavigationItem='mathSymbols'">数学符号
         </div>
         <div class="navigationItem" :class="currentNavigationItem==='specialSymbols'?'selectedNavigationItem':''"
              @click="currentNavigationItem='specialSymbols'">特殊符号
         </div>
         <div class="navigationItem" :class="currentNavigationItem==='chineseCharacter'?'selectedNavigationItem':''"
              @click="currentNavigationItem='chineseCharacter'">汉字
         </div>
         <div class="navigationItem" :class="currentNavigationItem==='greekAlphabet'?'selectedNavigationItem':''"
              @click="currentNavigationItem='greekAlphabet'">希腊字母
         </div>
       </div>
     </div>
     <!-- 主区域 -->
     <div style="flex: 1;display: flex;flex-direction: column;">
       <!-- 头部栏 -->
       <div class="headerMiddle">
         <!-- 字符内容-->
         <span style="margin: auto 10px">内容: </span>
         <input v-model="content" style="height: 38px;font-size: 28px">
         <button style="height: 38px;width: 80px;margin-left: 10px;font-size: 22px;line-height: 38px;" @click="copyButton">复制</button>
       </div>
       <!-- 内容区 -->
       <div style="flex: 1;display: flex;flex-direction: column; overflow: auto">
         <!-- 左区 -->
         <div style="flex: 3;display: flex;flex-direction: column;margin: 8px">

           <!-- 列表区，列表数据可以无限增加 -->
           <div class=" column" style="display: flex">
             <!-- 列表项 -->
             <div class=" yinYing" style="background-color: #fff;margin-top: 8px ;border-radius: 10px">
               <div style="display: flex;justify-content: flex-start;align-items: center; flex-wrap: wrap;">

                 <template v-for="item in selectedData" :key="item.id">
                   <div class="listItem" @click="selectSymbolItem(item.value)">{{ item.value }}</div>
                 </template>

               </div>
             </div>

           </div>
         </div>
       </div>
     </div>
   </div>
 </div>
</template>

<script>
export default {
  name: "SpecialSymbols"
}
</script>
<script setup>
import symbolData from './symbolData.js';
import {onMounted, watch,ref} from "vue";
let content =ref('');
let currentNavigationItem =ref('commonUse');
let selectedData =ref([{}]);
let allData =ref({});

const selectSymbolItem = (value) => {
  content.value +=value
}

const copyButton  = async () => {
  try {
    await navigator.clipboard.writeText(content.value);
    alert('文本已复制!');
  } catch (err) {
    alert('复制失败!', err);
  }
}
watch(()=>currentNavigationItem.value,(newValue) => {

  selectedData.value = allData.value[newValue]
})

onMounted(()=>{
  allData.value = symbolData.symbolData
  selectedData.value = allData.value[currentNavigationItem.value]
})

</script>
<style scoped>
.body {
  margin: 0;
  padding: 0;
}

.flex {
  display: flex;
}

/* 竖向 */
.column {
  flex-direction: column;
}

/* 阴影 */
.yinYing {
  box-shadow: rgb(0 0 0 / 20%) 0px 2px 1px -1px,
  rgb(0 0 0 / 14%) 0px 1px 1px 0px, rgb(0 0 0 / 12%) 0px 1px 3px 0px;
}

/* 导航条 */
.navigationItem {
  cursor: pointer;
  /*padding: 10px 20px;*/
  margin: 6px 12px;
  font-size: 16px;
  color: #e6e8ea;
  text-align: center;
  line-height: 40px;
  height: 40px;
}

.navigationItem:hover {
  background-color: #c7dbf3;
  color: #007aff;
  border-radius: 10px;
}

.selectedNavigationItem {
  background-color: #c7dbf3;
  color: #007aff;
  border-radius: 10px;
}

.headerLeft {
  padding: 10px;
  height: 70px;
  font-size: 16px;
  align-items: center;
  justify-content: center;
  /*border-bottom: 1px solid #999;*/
  display: flex;
  color: #e6e8ea;
}

.headerMiddle {
  height: 70px;
  background-color: #fff;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  font-size: 20px;
  line-height: 70px;
  box-shadow: rgb(0 0 0 / 20%) 0px 2px 1px -1px,
  rgb(0 0 0 / 14%) 0px 1px 1px 0px, rgb(0 0 0 / 12%) 0px 1px 3px 0px;
}

.listItem {
  background-color: #f7f7f7;
  width: 64px;
  height: 64px;
  border-radius: 10px;
  margin: 24px;
  border: #8f8f94 2px solid;
  text-align: center;
  line-height: 64px;
  font-size: 22px;
  cursor: pointer;
}

.listItem:hover {
  background-color: #f2f8ff;
  border: #007aff 2px solid;
}

/* 当屏幕宽度小于600px时要改变的css */
@media (max-width: 600px) {
  /* 小屏幕上取消侧边栏，后面学js时会教怎么弹出 */
  .ceBian {
    display: none;
  }

}
</style>