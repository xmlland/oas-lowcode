<template>
  <div style="padding: 10px; height: 100%">
    <div style="background: #ffffff; border-radius: 4px; height: 100%">
      <a-calendar v-model:value="value">
        <template #dateCellRender="{ current }">
          <div :class="getDateClass(current)">
            <ul class="events">
              <li v-for="item in getListData(current)" :key="item.content">
                <a-badge :status="item.type" :text="item.content" />
              </li>
            </ul>
          </div>
        </template>
        <template #monthCellRender="{ current }">
          <div v-if="getMonthData(current)" class="notes-month">
            <section>{{ getMonthData(current) }}</section>
            <span>日程数量</span>
          </div>
        </template>
      </a-calendar>
    </div>
  </div>
</template>

<script>
export default {
  name: "my_calendar_list",
}
</script>
<script setup>
import { ref } from 'vue';
import dayjs from 'dayjs';
import { postAction } from "@/api/action";

const value = ref(dayjs()); // 当前日期
const today = ref(dayjs().format('YYYY-MM-DD')); // 获取今天的日期
const events = {}; // 存储事件
const tileContent = ref({}); // 存储日期内容

// 模拟获取数据的函数
const getListData = (date) => {
  const dateKey = dayjs(date).format('YYYY-MM-DD'); // 使用 dayjs() 转换为实例
  return events[dateKey] || []; // 返回对应日期的事件
};

// 获取月份数据的函数
const getMonthData = (date) => {
  const dateKey = dayjs(date).format('YYYY-MM'); // 使用 dayjs() 转换为实例
  return events[dateKey] ? events[dateKey].length : 0; // 返回该月的事件数量
};

// 获取日期的样式类
const getDateClass = (date) => {
  const dateKey = dayjs(date).format('YYYY-MM-DD'); // 使用 dayjs() 转换为实例
  return tileContent.value[dateKey]?.className || ''; // 返回对应日期的类名
};

// 假设的请求数据
const fetchData = async () => {
  const infoRes = await postAction('dynamic/zform/datamap?path=path&traceFlag=&formNo=oa_work_calendar&parentId=', {});
  infoRes.rows.forEach(row => {
    const startTime = row.start_time;
    if (startTime && startTime.length >= 10) {
      const dateKey = startTime.substring(0, 10);
      events[dateKey] = events[dateKey] || [];
      events[dateKey].push({ type: 'success', content: row.title }); // 假设事件类型为 'success'

      // 更新 tileContent
      tileContent.value[dateKey] = {
        className: dateKey === today.value ? 'current-date' : 'not-current-date',
        content: ''
      };
    }
  });
};

fetchData(); // 调用数据获取函数
</script>

<style scoped>
.events {
  list-style: none;
  margin: 0;
  padding: 0;
}
.events .ant-badge-status {
  overflow: hidden;
  white-space: nowrap;
  width: 100%;
  text-overflow: ellipsis;
  font-size: 12px;
}
.notes-month {
  text-align: center;
  font-size: 28px;
}
.notes-month section {
  font-size: 28px;
}
/* 非当前日期样式 */
:deep(.not-current-date) {
  position: relative; /* 为绝对定位提供上下文 */
}

:deep(.not-current-date::before) {
  content: ''; /* 伪元素需要有内容 */
  position: absolute; /* 绝对定位 */
  top: 0; /* 距离顶部 */
  left: 0; /* 距离左侧 */
  width: 8px; /* 点的宽度 */
  height: 8px; /* 点的高度 */
  background-color: var(--ant-primary-color); /* 主色调 */
  border-radius: 50%; /* 使其成为圆形 */
}
</style>
