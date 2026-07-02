<template>
  <div class="business-form-container">
    <div class="intable-box m-newsbox" :class="$slots.icon?'':'default-icon'">
      <div class="g-ohide t-title03">
        <table>
          <tr>
            <td>
              <div v-if="$slots.icon" class="business-form-container-icon">
                <slot name="icon"></slot>
              </div>
            </td>
            <td>
              <h3>
                {{title}}
              </h3>
            </td>
          </tr>
        </table>
      </div>
      <div style="margin:10px 10px">
        <slot />
      </div>
    </div>

  </div>

</template>

<script>
export default {
  name: "UFormContainer"
}
</script>
<script setup>
defineProps({
  title: {
    type: String,
    default: ''
  },
})
</script>
<style lang="less" scoped>
.business-form-container {

  .m-intable td .intable-box {
    border: 1px solid #dee8ee;
    // height: 360px;
  }

  .g-ohide {
    overflow: hidden;
  }

  .t-title03 {
    height: 32px;
    line-height: 32px;
    background: url(~@/assets/img/uFormContainerTablebg_03.png) repeat-x;
    border-bottom: 1px solid #dee8ee;
    padding: 0 10px;
  }

  .g-left {
    float: left !important;
  }

  .business-form-container-icon {
    margin-right: 10px;
    height: 32px;
    font-weight: 600;
    display: flex;
    flex-direction: row;
    align-items: center;

    :deep(img) {
      height: 21px;
      margin-right: 20px;
    }
  }

  &.default-icon&::before {
    content: '';
    display: block;
    border-radius: 100%;
    width: 12px;
    height: 12px;
    background: #102039;
    margin-right: 10px;
  }

  .m-newsbox {
    border: 1px solid #dee8ee;
    background: #fff;
  }
}

// .business-form-title {
// 	font-size: 16px;
// 	font-weight: normal;
// 	color: #102039;
// 	display: flex;
// 	flex-direction: row;
// 	align-items: center;
// 	padding: 5px 30px;

// 	&.default-icon&::before {
// 		content: '';
// 		display: block;
// 		border-radius: 100%;
// 		width: 12px;
// 		height: 12px;
// 		background: #102039;
// 		margin-right: 10px;
// 	}

// 	.business-form-title-icon {
// 		margin-right: 10px;
// 		height: 21px;
// 		line-height: 21px;

// 		/deep/img {
// 			height: 21px;
// 		}

// 	}

// 	&::after {
// 		content: '';
// 		display: block;
// 		height: 1px;
// 		background: linear-gradient(90deg, rgba(16, 32, 57, 0.14), rgba(16, 32, 57, 0.5), rgba(16, 32, 57, 0.14));
// 		margin-left: 30px;
// 		flex: 1;
// 		opacity: .3;
// 	}
// }
</style>
