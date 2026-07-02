<template>
  <div class="scroll-container" ref="scrollContainer">
    <div
        class="stretchable-image"
        @wheel.prevent="handleWheel"
        @keydown="handleKeyDown"
        @keyup="handleKeyUp"
        tabindex="0"
        :style="{ width: containerWidth + 'px' }"
    >
      <img :src="imageUrl" alt="可横向拉伸的图片">
    </div>
  </div>
</template>

<script>
export default {
  props: {
    imageUrl: {
      type: String,
      required: true,
    },
    duration: {
      type: Number,
      default: 20,
    }
  },
  data() {
    return {
      containerWidth: 1200,
      isCtrlPressed: false,
      scrollLeft: 0
    }
  },
  mounted() {
    window.addEventListener('keydown', this.handleKeyDown);
    window.addEventListener('keyup', this.handleKeyUp);
    this.$refs.scrollContainer.addEventListener('scroll', this.handleScroll);
  },
  beforeUnmount() {
    window.removeEventListener('keydown', this.handleKeyDown);
    window.removeEventListener('keyup', this.handleKeyUp);
    this.$refs.scrollContainer.removeEventListener('scroll', this.handleScroll);
  },
  methods: {
    handleKeyDown(e) {
      if (e.key === 'Control') this.isCtrlPressed = true;
    },
    handleKeyUp(e) {
      if (e.key === 'Control') this.isCtrlPressed = false;
    },
    handleWheel(e) {
      if (!this.isCtrlPressed) {
        // 非Ctrl状态时允许横向滚动
        this.$refs.scrollContainer.scrollLeft += e.deltaY;
        e.preventDefault();
        return;
      }

      const delta = e.deltaY > 0 ? -40 : 40;
      this.containerWidth = Math.max(200, this.containerWidth + delta);

      // 保持滚动位置稳定
      requestAnimationFrame(() => {
        this.$refs.scrollContainer.scrollLeft = this.scrollLeft;
      });
    },
    handleScroll() {
      this.scrollLeft = this.$refs.scrollContainer.scrollLeft;
    }
  }
}
</script>

<style scoped>
/* 外层滚动容器 */
.scroll-container {
  width: 100%;
  height: 120px;
  overflow-x: auto;
  overscroll-behavior-x: contain;
  scrollbar-width: thin;
  scrollbar-color: #ccc transparent;
  position: relative;
}

/* 可拉伸图片容器 */
.stretchable-image {
  height: 100px;
  min-width: 200px;
  border: 1px dashed #ccc;
  position: relative;
  outline: none;
  display: inline-block;
}

.stretchable-image:focus {
  box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.3);
}

.stretchable-image img {
  height: 100%;
  width: 100%;
}

.scroll-container::-webkit-scrollbar {
  height: 8px;
}

.scroll-container::-webkit-scrollbar-track {
  background: transparent;
  border-radius: 4px;
}

.scroll-container::-webkit-scrollbar-thumb {
  background: rgba(147,147,153,0.5);
  border-radius: 4px;
}

.scroll-container::-webkit-scrollbar-thumb:hover {
  background: rgba(147,147,153,0.8);
}
</style>
