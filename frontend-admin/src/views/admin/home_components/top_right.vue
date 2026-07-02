<template>
  <div class="shortcut-container">
    <div class="shortcut-header">
      <h4 class="shortcut-title">
        <AppstoreOutlined style="color: var(--ant-primary-color)" />
        <span class="title-text">快捷入口</span>
      </h4>
      <SettingOutlined style="color: var(--ant-primary-color)" @click="open = true"/>
    </div>

    <div v-if="lod" class="loading-state">
      <a-spin :spinning="true" />
    </div>

    <div v-else class="shortcut-grid">
      <!-- 快捷入口卡片 -->
      <div
          v-for="(item, index) in displayedItems"
          :key="index"
          :class="['shortcut-card', getCardVariant(index)]"
          @click="handleCardClick(item)"
      >
        <div class="card-icon">
          <u-icon :type="item.pageIcon" />
        </div>
        <div class="card-label">{{ item.pageName }}</div>
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <a-modal
        v-model:visible="open"
        width="540px"
        :bodyStyle="{
        maxHeight: '60vh',
        overflowY: 'auto',
        padding: '20px'
      }"
        title="编辑快捷入口"
        @ok="handleOk"
    >
      <div class="edit-container">
        <div class="edit-section">
          <h3>可添加入口</h3>
          <draggable
              class="draggable-list available-list"
              :list="myArray2"
              group="shortcuts"
              item-key="pageID"
          >
            <template #item="{element}">
              <div class="draggable-item available-item">
                <u-icon :type="element.pageIcon" class="item-icon" />
                <span class="item-name">{{ element.pageName }}</span>
                <PlusCircleOutlined class="add-icon" @click="plus(element.pageID)" />
              </div>
            </template>
          </draggable>
        </div>

        <div class="edit-section">
          <h3>已添加入口</h3>
          <draggable
              class="draggable-list"
              :list="myArray"
              group="shortcuts"
              item-key="pageID"
          >
            <template #item="{element}">
              <div class="draggable-item" :class="getItemVariant(myArray.indexOf(element))">
                <u-icon :type="element.pageIcon" class="item-icon" />
                <span class="item-name">{{ element.pageName }}</span>
                <MinusCircleOutlined class="remove-icon" @click="minus(element.pageID)" />
              </div>
            </template>
          </draggable>
          <p class="limit-hint" v-if="myArray.length > 6">最多只能添加6个快捷入口</p>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { message } from 'ant-design-vue';
import draggable from 'vuedraggable';
import { ref, onMounted, inject } from 'vue';
import { AppstoreOutlined, MinusCircleOutlined, PlusCircleOutlined } from '@ant-design/icons-vue';
import { getMyShortcut, getAllShortcut, saveMyShortcut } from '@/api/api';

// 注入添加标签的方法
const addTabToAdminContent = inject('addTabToAdminContent');

// 状态管理
const open = ref(false);
const lod = ref(true);
const myArray = ref([]);
const myArray2 = ref([]);

// 计算属性：显示的快捷入口（包含"更多"按钮）
const displayedItems = ref([]);

// 卡片样式变体（8种不同颜色）
const cardVariants = [
  'variant-blue',
  'variant-green',
  'variant-purple',
  'variant-orange',
  'variant-teal',
  'variant-pink',
  'variant-indigo',
  'variant-amber'
];

// 获取卡片样式
const getCardVariant = (index) => {
  // 排除最后一个"更多"按钮
  // if (index === displayedItems.value.length - 1) return 'variant-more';
  return cardVariants[index % cardVariants.length];
};

// 获取编辑列表项样式
const getItemVariant = (index) => cardVariants[index % cardVariants.length];

// 处理卡片点击
const handleCardClick = (item) => {
  const { pageName, pageUrl, permission } = item;

  if (pageUrl === 'more') {
    open.value = true;
    return;
  }

  let contentUrl = item.pageUrl;

  if (permission){
    contentUrl = pageUrl + permission;
  }

  addTabToAdminContent({
    attributes: { pageName },
    key: contentUrl,
    params: {},
    query: {}
  });
};

// 从已添加列表移除
const minus = (id) => {
  const itemToMove = myArray.value.find(item => item.pageID === id);
  if (itemToMove) {
    myArray2.value.push(itemToMove);
    myArray.value = myArray.value.filter(item => item.pageID !== id);
    updateDisplayedItems();
  }
};

// 添加到已添加列表
const plus = (id) => {
  if (myArray.value.length > 6) {
    message.warning('最多只能添加6个快捷入口');
    return;
  }

  const itemToMove = myArray2.value.find(item => item.pageID === id);
  if (itemToMove) {
    myArray.value.push(itemToMove);
    myArray2.value = myArray2.value.filter(item => item.pageID !== id);
    updateDisplayedItems();
  }
};

// 更新显示的项目（添加"更多"按钮）
const updateDisplayedItems = () => {
  // displayedItems.value = [...myArray.value, {
  //   pageIcon: 'fa-ellipsis-h',
  //   pageName: '更多',
  //   pageUrl: 'more'
  // }];
  displayedItems.value = [...myArray.value];
};

// 保存快捷入口设置
const handleOk = () => {
  if (myArray.value.length > 6){
    message.warning('最多只能添加6个快捷入口');
    return;
  }
  saveMyShortcut({ shortcut: JSON.stringify(myArray.value) })
      .then(res => {
        if (res.code === 0) {
          message.success('保存成功');
          open.value = false;
          updateDisplayedItems();
        } else {
          message.error('保存失败');
        }
      })
      .catch(() => message.error('保存失败'));
};

// 获取我的快捷入口
const getMydata = () => {
  return new Promise((resolve, reject) => {
    getMyShortcut('')
        .then(res => {
          const shortcuts = JSON.parse(res.data.shortcut || '[]');
          myArray.value = shortcuts;
          updateDisplayedItems();
          resolve(shortcuts);
        })
        .catch(reject);
  });
};

// 获取所有可用快捷入口
const getAllShortData = () => {
  return new Promise((resolve, reject) => {
    getAllShortcut()
        .then(res => {
          const allShortcuts = JSON.parse(res.data.shortcut || '[]');
          // 过滤掉已添加的入口
          myArray2.value = allShortcuts.filter(
              item => !myArray.value.some(m => m.pageID === item.pageID)
          );
          lod.value = false;
          resolve(allShortcuts);
        })
        .catch(reject);
  });
};

// 初始化
onMounted(async () => {
  try {
    await getMydata();
    await getAllShortData();
  } catch (err) {
    message.error('加载快捷入口失败');
    lod.value = false;
  }
});
</script>

<style lang="less" scoped>
.shortcut-container {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.shortcut-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.shortcut-title {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #1f2937;
  margin: 0;

  .title-text {
    margin-left: 13px;
    font-weight: 600;
  }
}

.loading-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
}

.shortcut-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(118px, 1fr));
  gap: 12px;
}

.shortcut-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 72px;
  padding: 12px;
  border: 1px solid transparent;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 18px rgba(31, 41, 55, 0.08);
  }

  .card-icon {
    font-size: 20px;
    line-height: 24px;
  }

  .card-label {
    margin-top: 6px;
    font-size: 13px;
    font-weight: 500;
    line-height: 18px;
  }
}

// 卡片颜色变体
.variant-blue {
  background: #eff6ff;
  border-color: #bfdbfe;

  .card-icon,
  .card-label {
    color: #2563eb;
  }
}

.variant-green {
  background: #f0fdf4;
  border-color: #bbf7d0;

  .card-icon,
  .card-label {
    color: #16a34a;
  }
}

.variant-purple {
  background: #f5f3ff;
  border-color: #ddd6fe;

  .card-icon,
  .card-label {
    color: #6d28d9;
  }
}

.variant-orange {
  background: #fff7ed;
  border-color: #fed7aa;

  .card-icon,
  .card-label {
    color: #ea580c;
  }
}

.variant-teal {
  background: #ecfdf5;
  border-color: #a7f3d0;

  .card-icon,
  .card-label {
    color: #059669;
  }
}

.variant-pink {
  background: #fdf2f8;
  border-color: #fbcfe8;

  .card-icon,
  .card-label {
    color: #db2777;
  }
}

.variant-indigo {
  background: #eef2ff;
  border-color: #c7d2fe;

  .card-icon,
  .card-label {
    color: #4f46e5;
  }
}

.variant-amber {
  background: #fffbeb;
  border-color: #fde68a;

  .card-icon,
  .card-label {
    color: #d97706;
  }
}

.variant-more {
  background: #f5f5f5;

  .card-icon {
    color: #8c8c8c;
  }

  .card-label {
    color: #8c8c8c;
  }
}

// 编辑弹窗样式
.edit-container {
  display: flex;
  gap: 24px;

  .edit-section {
    flex: 1;

    h3 {
      margin-bottom: 16px;
      font-size: 16px;
      color: #1f2937;
      font-weight: 500;
    }
  }
}

.draggable-list {
  min-height: 120px;
  padding: 10px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;

  .draggable-item {
    display: flex;
    align-items: center;
    padding: 10px 16px;
    margin-bottom: 8px;
    border-radius: 4px;
    background: #f5f5f5;
    cursor: move;

    .item-icon {
      font-size: 16px;
      margin-right: 10px;
    }

    .item-name {
      flex: 1;
      font-size: 14px;
    }

    .remove-icon, .add-icon {
      cursor: pointer;
      transition: color 0.2s;

      &:hover {
        transform: scale(1.1);
      }
    }
  }
}

.available-list {
  .available-item {
    background: #f9f9f9;

    .add-icon {
      color: #1890ff;
    }
  }
}

.limit-hint {
  margin-top: 12px;
  font-size: 12px;
  color: #ff4d4f;
  text-align: right;
}
</style>
