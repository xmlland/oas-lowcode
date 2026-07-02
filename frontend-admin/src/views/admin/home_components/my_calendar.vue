<template>
  <div class="schedule-container">
    <!-- 头部标题区域 -->
    <div class="schedule-header">
      <div class="header-title">
        <CalendarOutlined class="header-icon" />
        <span class="title-text">日程安排</span>
        <span class="current-date">{{ currentDateFormatted }}</span>
      </div>
      <div class="header-actions">
        <span class="week-indicator">第 <span class="week-number">{{ currentWeek }}</span> 周</span>
      </div>
    </div>

    <!-- 周历导航区域 -->
    <div class="calendar-wrapper">
      <!-- 自定义周历 -->
      <div class="week-calendar">
        <div class="calendar-weekdays">
          <div v-for="day in weekdays" :key="day.value" class="weekday-cell">
            <span class="weekday-name">{{ day.name }}</span>
          </div>
        </div>
        <div class="calendar-days">
          <div
              v-for="day in weekDays"
              :key="day.date"
              :class="['day-cell', {
              'current-month': day.isCurrentMonth,
              'other-month': !day.isCurrentMonth,
              'today': day.isToday,
              'selected': day.isSelected,
              'has-events': day.hasEvents
            }]"
              @click="selectDate(day.date)"
          >
            <div class="day-number">{{ day.date.date() }}</div>
            <span v-if="day.hasEvents" class="event-indicator"></span>
          </div>
        </div>
      </div>
    </div>

    <!-- 日程列表区域 -->
    <div class="schedule-list">
      <div v-if="loading" class="list-loading">
        <a-spin size="small" :delay="3000"/>
        <span class="loading-text">加载中...</span>
      </div>

      <div v-else-if="schedules.length > 0" class="event-list">
        <div v-for="(item, index) in schedules" :key="index" class="event-card">
          <div class="event-header">
            <a-tag :color="getStatusColor(item.status)" class="event-tag">
              {{ item.title }}
            </a-tag>
            <span class="event-time">{{ formatTime(item.start_time) }}</span>
          </div>
          <div class="event-content">
<!--            <a-icon type="desktop" class="content-icon" />-->
            <span class="content-text">{{ item.content }}</span>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <a-empty
            description="暂无日程安排"
            :image="aEmptyImage"
            imageStyle="{ height: '64px' }"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
/*eslint-disable*/
import { ref, onMounted, computed } from 'vue';
import { CalendarOutlined } from '@ant-design/icons-vue';
import dayjs from 'dayjs';
import isoWeek from 'dayjs/plugin/isoWeek';
import 'dayjs/locale/zh-cn';
import { Empty } from 'ant-design-vue';
const aEmptyImage = Empty.PRESENTED_IMAGE_SIMPLE;
import {postAction} from "@/api/action";

// 引入 dayjs 插件
dayjs.extend(isoWeek);
dayjs.locale('zh-cn');

// 状态管理
const currentDate = ref(dayjs());
const selectedDate = ref(dayjs());
const loading = ref(false);
const schedules = ref([]);
const weekdays = [
  { name: '一', value: 0 },
  { name: '二', value: 1 },
  { name: '三', value: 2 },
  { name: '四', value: 3 },
  { name: '五', value: 4 },
  { name: '六', value: 5 },
  { name: '日', value: 6 },
];

// 计算属性
const currentWeek = computed(() => selectedDate.value.isoWeek());
const currentMonthDisplay = computed(() => selectedDate.value.format('YYYY年MM月'));
const currentDateFormatted = computed(() => selectedDate.value.format('YYYY-MM-DD'));
const currentDateNextOneDayFormmated = computed(() => selectedDate.value.add(1, 'day').format('YYYY-MM-DD'));

// 获取当周日历数据
const weekDays = computed(() => {
  const startOfWeek = selectedDate.value.startOf('week');
  const days = [];

  for (let i = 0; i < 7; i++) {
    const date = startOfWeek.add(i, 'day');
    const today = dayjs();

    days.push({
      date: date,
      isCurrentMonth: date.month() === selectedDate.value.month(),
      isToday: date.isSame(today, 'day'),
      isSelected: date.isSame(selectedDate.value, 'day'),
      hasEvents: false // 这里可以根据实际事件数据设置
    });
  }

  return days;
});

// 方法
const selectDate = (date) => {
  selectedDate.value = date;
  fetchSchedules(date);
};

const prevWeek = () => {
  selectedDate.value = selectedDate.value.subtract(1, 'week');
  fetchSchedules(selectedDate.value);
};

const nextWeek = () => {
  selectedDate.value = selectedDate.value.add(1, 'week');
  fetchSchedules(selectedDate.value);
};

const today = () => {
  const now = dayjs();
  selectedDate.value = now;
  fetchSchedules(now);
};

const formatTime = (timeStr) => {
  return timeStr.slice(11, 16); // 假设时间格式是YYYY-MM-DD HH:mm:ss
};

const getStatusColor = (status) => {
  const statusMap = {
    '待审批': 'orange',
    '已批准': 'green',
    '进行中': 'blue',
    '已完成': 'purple',
    '已拒绝': 'red'
  };
  return statusMap[status] || 'blue';
};

// 模拟数据获取
const fetchSchedules = (date) => {
  loading.value = true;
  let queryParams = {
    "beginStart_time": currentDateFormatted.value,
    "endStart_time": currentDateNextOneDayFormmated.value,
    "pageParam": {
      "orderBy": "a.start_time asc",
      "pageNo": "1",
      "pageSize": "1000"
    },
    "queryParamType": {
      "beginStart_time": "between",
      "endStart_time": "between"
    }
  }

  postAction(`dynamic/zform/datamap/${new Date().getTime()}/?path=path&traceFlag=&formNo=oa_work_calendar&parentId=`, queryParams).then(infoRes => {
    schedules.value = infoRes.rows;
    loading.value = false;
  });
};

onMounted(() => {
  // // 初始化周数据
  // weekDays.value.forEach(day => {
  //   // 模拟某些日期有事件
  //   day.hasEvents = Math.random() > 0.5;
  // });

  // 获取当天日程
  fetchSchedules(selectedDate.value);
});
</script>

<style lang="less" scoped>
.schedule-container {
  background: #fff;
  border-radius: 8px;
  padding: 0 16px;
  height: 100%;
  min-height: 300px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

/* 头部样式 */
.schedule-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  //padding-bottom: 12px;
  padding: 16px 0;
  margin-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;

  .header-title {
    display: flex;
    align-items: center;

    .header-icon {
      color: #3498db;
      margin-right: 8px;
      font-size: 14px;
    }

    .title-text {
      font-size: 14px;
      font-weight: 600;
      margin-right: 13px;
    }

    .current-date {
      font-size: 14px;
      color: #666;
    }
  }

  .header-actions {
    .week-indicator {
      font-size: 14px;
      color: #666;

      .week-number {
        color: #3498db;
        font-weight: 500;
        margin: 0 4px;
      }
    }
  }
}

/* 日历区域样式 */
.calendar-wrapper {
  margin-bottom: 16px;
}

.calendar-toolbar {
  margin-bottom: 12px;
  font-size: 14px;
  align-items: center;
}

.month-display {
  font-weight: 500;
}

/* 周历样式 */
.week-calendar {
  width: 100%;

  .calendar-weekdays {
    display: flex;
    margin-bottom: 4px;
  }

  .weekday-cell {
    flex: 1;
    text-align: center;
    padding: 4px 0;

    .weekday-name {
      font-size: 12px;
      color: #666;
    }
  }

  .calendar-days {
    display: flex;
  }

  .day-cell {
    flex: 1;
    height: 56px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    border-radius: 6px;
    margin: 0 2px;
    cursor: pointer;
    position: relative;
    transition: all 0.2s ease;

    .day-number {
      font-size: 14px;
      position: relative;
      z-index: 1;
    }

    .event-indicator {
      position: absolute;
      bottom: 8px;
      width: 4px;
      height: 4px;
      border-radius: 50%;
      background-color: #3498db;
    }

    &:hover {
      background-color: #f5f5f5;
    }
  }

  .other-month {
    .day-number {
      color: #ccc;
    }
  }

  .today {
    .day-number {
      font-weight: 500;
      color: #3498db;
    }
  }

  .selected {
    background-color: #3498db;

    .day-number {
      color: #fff;
      font-weight: 500;
    }

    .event-indicator {
      background-color: #fff;
    }
  }
}

/* 日程列表样式 */
.schedule-list {
  flex: 1;
  overflow-y: auto;
  padding-right: 8px;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: #ddd;
    border-radius: 2px;
  }
}

.list-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 120px;
  color: #666;

  .loading-text {
    margin-left: 8px;
    font-size: 14px;
  }
}

.event-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.event-card {
  padding: 12px;
  background: #f9f9f9;
  border-radius: 6px;
  border-left: 3px solid #3498db;
  transition: all 0.2s ease;

  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    transform: translateY(-1px);
  }
}

.event-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;

  .event-tag {
    font-size: 12px;
  }

  .event-time {
    font-size: 12px;
    color: #666;
  }
}

.event-content {
  display: flex;
  align-items: center;

  .content-icon {
    font-size: 14px;
    color: #999;
    margin-right: 8px;
  }

  .content-text {
    font-size: 14px;
    color: #333;
  }
}

.empty-state {
  min-height: 160px;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  :deep(.ant-empty-description) {
    color: #999;
    margin-top: 8px;
  }
}
</style>
