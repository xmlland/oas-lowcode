<template>
  <div class="home-main workbench-home">
    <div class="home-grid">
      <div class="grid-item grid-top-left">
        <top-left></top-left>
      </div>
      <div class="grid-item grid-top-mid">
        <top-left2></top-left2>
      </div>
      <div class="grid-item grid-top-right">
        <top-right></top-right>
      </div>
      <div class="grid-item grid-bottom-left">
        <bottom-left></bottom-left>
      </div>
      <div class="grid-item grid-bottom-right">
        <my_calendar></my_calendar>
      </div>
    </div>
  </div>
</template>

<script>
import topLeft from './home_components/top_left.vue'
import topLeft2 from './home_components/top_left2.vue'
import bottomLeft from './home_components/bottom_left.vue'
import topRight from './home_components/top_right.vue'
import my_calendar from './home_components/my_calendar.vue'

export default {
  name: "admin_home",
  components: {
    topLeft,
    topLeft2,
    bottomLeft,
    topRight,
    my_calendar
  }
}
</script>

<style lang="less" scoped>
.home-main {
  min-height: 100%;
  height: auto;
  padding: 12px;
  overflow-y: auto;
  background: transparent;
  box-sizing: border-box;

  .home-grid {
    display: grid;
    grid-template-columns: repeat(12, minmax(0, 1fr));
    grid-auto-rows: auto;
    gap: 12px;
    width: 100%;
    max-width: 1920px;
    min-height: 0;
    margin: 0 auto;
    align-items: stretch;

    .grid-item {
      min-width: 0;
      overflow: visible;

      > * {
        height: 100%;
      }
    }

    .grid-top-left {
      grid-column: span 5;
      min-height: 320px;
    }

    .grid-top-mid {
      grid-column: span 4;
      min-height: 320px;
    }

    .grid-top-right {
      grid-column: span 3;
      min-height: 320px;
    }

    .grid-bottom-left {
      grid-column: span 5;
      min-height: 300px;
    }

    .grid-bottom-right {
      grid-column: span 7;
      min-height: 300px;
    }
  }

  :deep(.left-item),
  :deep(.bottom-left),
  :deep(.shortcut-container),
  :deep(.schedule-container) {
    height: 100%;
    min-height: 0;
    overflow: hidden;
    background: #fff;
    border: 1px solid #e6ebf2;
    border-radius: 8px;
    box-shadow: 0 8px 24px rgba(31, 41, 55, 0.04);
    box-sizing: border-box;
  }

  :deep(.left-item) {
    padding: 16px;

    .pr {
      position: relative;
      flex: 0 0 auto;
      min-height: 48px;
      border-bottom: 1px solid #eef2f7;
    }

    .title {
      top: 14px;
      left: 0;
      padding-bottom: 0;

      .xwzx {
        color: #1f2937;
        font-size: 14px;
        font-weight: 600;
      }
    }

    .rightTab {
      .ant-tabs-nav {
        margin-bottom: 0;
      }

      .ant-tabs-tab {
        padding: 14px 0 10px;
      }
    }

    .box-left {
      flex: 1 1 auto;
      min-height: 0;
      padding-top: 12px;
      overflow: hidden;
    }

    .right-list {
      min-width: 0;
      overflow: hidden;

      ul {
        margin: 0;
        padding-inline-start: 18px;
      }

      li {
        line-height: 28px;
      }

      a {
        color: #1f2937;
      }

      span {
        color: #98a2b3;
      }
    }

    .left-img {
      width: 36%;
      max-width: 260px;
      min-width: 180px;
      margin-right: 16px;

      .img-list img {
        width: 100%;
        height: 160px;
        max-height: 160px;
        object-fit: cover;
        image-rendering: auto;
        filter: none;
        border-radius: 8px;
      }
    }

    .more_button {
      padding-top: 10px;
    }
  }

  :deep(.shortcut-container),
  :deep(.schedule-container) {
    padding: 16px;
  }

  :deep(.shortcut-header),
  :deep(.schedule-header) {
    min-height: 44px;
    margin-bottom: 14px;
    padding: 0 0 12px;
    border-bottom: 1px solid #eef2f7;
  }

  :deep(.shortcut-title),
  :deep(.schedule-header .title-text),
  :deep(.bottom-left .lczx) {
    color: #1f2937;
    font-size: 14px;
    font-weight: 600;
  }

  :deep(.bottom-left) {
    .ant-card {
      height: 100%;
      background: transparent;
      box-shadow: none;
    }

    .ant-card-head {
      min-height: 48px;
      padding: 0 16px;
      border-bottom: 0;
    }

    .ant-card-head-title {
      min-height: 48px;
      padding: 14px 0 10px;
      border-bottom: 1px solid #eef2f7;
    }

    .ant-card-body {
      padding: 12px 16px 16px;
    }
  }

  :deep(.schedule-container) {
    height: 100%;

    .calendar-wrapper {
      margin-bottom: 12px;
    }

    .schedule-list {
      min-height: 0;
    }
  }

  :deep(.empty-state) {
    min-height: 160px;
    height: 100%;
  }
}

@media (max-width: 1599px) {
  .home-main .home-grid {
    .grid-top-left {
      grid-column: span 7;
    }

    .grid-top-mid {
      grid-column: span 5;
    }

    .grid-top-right,
    .grid-bottom-left {
      grid-column: span 6;
    }

    .grid-bottom-right {
      grid-column: span 12;
    }
  }
}

@media (max-width: 1199px) {
  .home-main .home-grid {
    .grid-top-left,
    .grid-top-mid,
    .grid-top-right,
    .grid-bottom-left,
    .grid-bottom-right {
      grid-column: span 12;
    }
  }
}

@media (max-width: 768px) {
  .home-main {
    padding: 8px;

    :deep(.left-item) {
      .box-left {
        flex-direction: column;
      }

      .left-img {
        width: 100%;
        margin-right: 0;
        margin-bottom: 12px;
      }
    }
  }
}
</style>
