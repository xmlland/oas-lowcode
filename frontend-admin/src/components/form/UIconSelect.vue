<template>
  <div>
    <a-input-group class="icon-select" compact @click="onSearch">
      <a-input
          v-model:value="currentValue"
          :placeholder="placeholder"
          :disabled="true"
      >
        <template v-if="currentValue" #addonBefore>
          <u-icon :type="currentValue"/>
        </template>
      </a-input>
      <a-button v-if="!disabled">
        <template #icon>
          <SearchOutlined/>
        </template>
      </a-button>
    </a-input-group>
    <u-input v-if="isShowInput" :disabled="disabled" defaultValue="" v-model:value="currentValue" placeholder="请输入图标"/>
    <a-form-item style="width: 0;height: 0;display: none;">
      <u-modal ref="modal" :width="modalWidth" :formDisabled="true" :customOK="true" @clickOk="clickOk">
        <div class="icon-select-modal-content" id="icon-select-modal-content">
          <template v-bind:key="index" v-for="(item,index) in iconTabs">
            <a-divider orientation="left">{{ item.type }}</a-divider>
            <a-row :gutter="[16,16]">
              <a-col :id="'icon-id-fa-'+icon" v-bind:key="icon" v-for="(icon) in item.data" :span="4">
                <a-checkable-tag :checked="currentValue==='fa-'+icon"
                                 @change="handleChange('fa-'+icon)">
                  <u-icon :type="'fa-'+icon"/>
                  {{ 'fa-' + icon }}
                </a-checkable-tag>
              </a-col>

            </a-row>
          </template>
        </div>
      </u-modal>
    </a-form-item>
  </div>
</template>

<script>
export default {
  name: "UIconSelect"
}
</script>
<script setup>
import {computed, getCurrentInstance, ref, watch} from "vue";
import {message} from "ant-design-vue";

let instance = getCurrentInstance();
let props = defineProps({
  value: {
    type: String,
    default: ''
  },
  disabled: {
    type: Boolean,
    default: false
  },
  placeholder: {
    type: String,
    default: ''
  },
  modalWidth: {
    type: Number,
    default: 1200
  },
  showInput: {
    type: [String, Boolean],
    default: false
  }
})
let currentValue = ref(null)
let isShowInput = computed(() => {
  if (typeof props.showInput === 'string') {
    return props.showInput === '1' || props.showInput === 'true'
  }
  return props.showInput
})
const iconTabs = [
  {
    type: '网页常用图标',
    data: ["address-book", "address-book-o", "address-card", "address-card-o", "adjust", "american-sign-language-interpreting", "anchor", "archive", "area-chart", "arrows", "arrows-h", "arrows-v", "assistive-listening-systems", "asterisk", "at", "audio-description", "automobile", "balance-scale", "ban", "bank", "bar-chart", "bar-chart-o", "barcode", "bars", "bath", "battery-0", "battery-1", "battery-2", "battery-3", "battery-4", "battery-empty", "battery-full", "battery-half", "battery-quarter", "battery-three-quarters", "bed", "beer", "bell", "bell-o", "bell-slash", "bell-slash-o", "bicycle", "binoculars", "birthday-cake", "blind", "bluetooth", "bluetooth-b", "bolt", "bomb", "book", "bookmark", "bookmark-o", "braille", "briefcase", "bug", "building", "building-o", "bullhorn", "bullseye", "bus", "cab", "calculator", "calendar", "calendar-check-o", "calendar-minus-o", "calendar-o", "calendar-plus-o", "calendar-times-o", "camera", "camera-retro", "car", "caret-square-o-down", "caret-square-o-left", "caret-square-o-right", "caret-square-o-up", "cart-arrow-down", "cart-plus", "cc", "certificate", "check", "check-circle", "check-circle-o", "check-square", "check-square-o", "child", "circle", "circle-o", "circle-o-notch", "circle-thin", "clock-o", "clone", "close", "cloud", "cloud-download", "cloud-upload", "code", "code-fork", "coffee", "cog", "cogs", "comment", "comment-o", "commenting", "commenting-o", "comments", "comments-o", "compass", "copyright", "creative-commons", "credit-card", "crop", "crosshairs", "cube", "cubes", "cutlery", "dashboard", "database", "deaf", "desktop", "diamond", "dot-circle-o", "download", "edit", "ellipsis-h", "ellipsis-v", "envelope", "envelope-o", "envelope-open", "envelope-open-o", "envelope-square", "eraser", "exchange", "exclamation", "exclamation-circle", "exclamation-triangle", "external-link", "external-link-square", "eye", "eye-slash", "eyedropper", "fax", "feed", "female", "fighter-jet", "file-archive-o", "file-audio-o", "file-code-o", "file-excel-o", "file-image-o", "file-movie-o", "file-pdf-o", "file-photo-o", "file-picture-o", "file-powerpoint-o", "file-sound-o", "file-video-o", "file-word-o", "file-zip-o", "film", "filter", "fire", "fire-extinguisher", "flag", "flag-checkered", "flag-o", "flash", "flask", "folder", "folder-o", "folder-open", "folder-open-o", "frown-o", "futbol-o", "gamepad", "gavel", "gear", "gears", "gift", "glass", "globe", "graduation-cap", "group", "hand-grab-o", "hand-lizard-o", "hand-paper-o", "hand-peace-o", "hand-pointer-o", "hand-rock-o", "hand-scissors-o", "hand-spock-o", "hand-stop-o", "handshake-o", "hdd-o", "headphones", "heart", "heart-o", "heartbeat", "history", "home", "hotel", "hourglass", "hourglass-1", "hourglass-2", "hourglass-3", "hourglass-end", "hourglass-half", "hourglass-o", "hourglass-start", "i-cursor", "id-badge", "id-card", "id-card-o", "image", "inbox", "industry", "info", "info-circle", "institution", "key", "keyboard-o", "language", "laptop", "leaf", "legal", "lemon-o", "level-down", "level-up", "life-bouy", "life-buoy", "life-ring", "life-saver", "lightbulb-o", "line-chart", "location-arrow", "lock", "low-vision", "magic", "magnet", "mail-forward", "mail-reply", "mail-reply-all", "male", "map", "map-marker", "map-o", "map-pin", "map-signs", "meetup", "meh-o", "microchip", "microphone", "microphone-slash", "minus", "minus-circle", "minus-square", "minus-square-o", "mobile", "mobile-phone", "money", "moon-o", "mortar-board", "motorcycle", "mouse-pointer", "music", "navicon", "newspaper-o", "object-group", "object-ungroup", "paint-brush", "paper-plane", "paper-plane-o", "paw", "pencil", "pencil-square", "pencil-square-o", "percent", "phone", "phone-square", "photo", "picture-o", "pie-chart", "plane", "plug", "plus", "plus-circle", "plus-square", "plus-square-o", "podcast", "power-off", "print", "puzzle-piece", "qrcode", "question", "question-circle", "question-circle-o", "quote-left", "quote-right", "random", "recycle", "refresh", "registered", "remove", "reorder", "reply", "reply-all", "retweet", "road", "rocket", "rss", "rss-square", "search", "search-minus", "search-plus", "send", "send-o", "server", "share", "share-alt", "share-alt-square", "share-square", "share-square-o", "shield", "ship", "shopping-bag", "shopping-basket", "shopping-cart", "shower", "sign-in", "sign-out", "signal", "sitemap", "sliders", "smile-o", "snowflake-o", "soccer-ball-o", "sort", "sort-alpha-asc", "sort-alpha-desc", "sort-amount-asc", "sort-amount-desc", "sort-asc", "sort-desc", "sort-down", "sort-numeric-asc", "sort-numeric-desc", "sort-up", "space-shuttle", "spinner", "spoon", "square", "square-o", "star", "star-half", "star-half-empty", "star-half-full", "star-half-o", "star-o", "sticky-note", "sticky-note-o", "street-view", "suitcase", "sun-o", "support", "tablet", "tachometer", "tag", "tags", "tasks", "taxi", "television", "terminal", "thermometer-empty", "thermometer-full", "thermometer-half", "thermometer-quarter", "thermometer-three-quarters", "thumb-tack", "thumbs-down", "thumbs-o-down", "thumbs-o-up", "thumbs-up", "ticket", "times", "times-circle", "times-circle-o", "tint", "toggle-down", "toggle-left", "toggle-off", "toggle-on", "toggle-right", "toggle-up", "trademark", "trash", "trash-o", "tree", "trophy", "truck", "tty", "tv", "umbrella", "universal-access", "university", "unlock", "unlock-alt", "unsorted", "upload", "user", "user-circle", "user-circle-o", "user-o", "user-plus", "user-secret", "user-times", "users", "video-camera", "volume-control-phone", "volume-down", "volume-off", "volume-up", "warning", "wheelchair", "wheelchair-alt", "wifi", "window-close", "window-close-o", "window-maximize", "window-minimize", "window-restore", "wrench"]
  },
  {
    type: '表单控制类图标',
    data: ["check-square", "check-square-o", "circle", "circle-o", "dot-circle-o", "minus-square", "minus-square-o", "plus-square", "plus-square-o", "square", "square-o"]
  },
  {
    type: '方向箭头图标',
    data: ["angle-double-down", "angle-double-left", "angle-double-right", "angle-double-up", "angle-down", "angle-left", "angle-right", "angle-up", "arrow-circle-down", "arrow-circle-left", "arrow-circle-o-down", "arrow-circle-o-left", "arrow-circle-o-right", "arrow-circle-o-up", "arrow-circle-right", "arrow-circle-up", "arrow-down", "arrow-left", "arrow-right", "arrow-up", "arrows", "arrows-alt", "arrows-h", "arrows-v", "caret-down", "caret-left", "caret-right", "caret-square-o-down", "caret-square-o-left", "caret-square-o-right", "caret-square-o-up", "caret-up", "chevron-circle-down", "chevron-circle-left", "chevron-circle-right", "chevron-circle-up", "chevron-down", "chevron-left", "chevron-right", "chevron-up", "exchange", "hand-o-down", "hand-o-left", "hand-o-right", "hand-o-up", "long-arrow-down", "long-arrow-left", "long-arrow-right", "long-arrow-up", "toggle-down", "toggle-left", "toggle-right", "toggle-up"]
  },
  {
    type: '视频播放器图标',
    data: ["arrows-alt", "backward", "compress", "eject", "expand", "fast-backward", "fast-forward", "forward", "pause", "pause-circle", "pause-circle-o", "play", "play-circle", "play-circle-o", "random", "step-backward", "step-forward", "stop", "stop-circle", "stop-circle-o", "youtube-play"]
  },
  {
    type: '品牌类图标',
    data: ["500px", "adn", "amazon", "android", "angellist", "apple", "bandcamp", "behance", "behance-square", "bitbucket", "bitbucket-square", "bitcoin", "black-tie", "bluetooth", "bluetooth-b", "btc", "buysellads", "cc-amex", "cc-diners-club", "cc-discover", "cc-jcb", "cc-mastercard", "cc-paypal", "cc-stripe", "cc-visa", "chrome", "codepen", "codiepie", "connectdevelop", "contao", "credit-card-alt", "css3", "dashcube", "delicious", "deviantart", "digg", "dribbble", "dropbox", "drupal", "edge", "eercast", "empire", "envira", "etsy", "expeditedssl", "facebook", "facebook-f", "facebook-official", "facebook-square", "firefox", "first-order", "flickr", "font-awesome", "fonticons", "fort-awesome", "forumbee", "foursquare", "free-code-camp", "ge", "get-pocket", "gg", "gg-circle", "git", "git-square", "github", "github-alt", "github-square", "gitlab", "gittip", "glide", "glide-g", "google", "google-plus", "google-plus-official", "google-plus-square", "google-wallet", "gratipay", "grav", "hacker-news", "hashtag", "houzz", "html5", "imdb", "instagram", "instagram", "internet-explorer", "ioxhost", "joomla", "jsfiddle", "lastfm", "lastfm-square", "leanpub", "linkedin", "linkedin-square", "linode", "linux", "maxcdn", "meanpath", "medium", "mixcloud", "modx", "odnoklassniki", "odnoklassniki-square", "opencart", "openid", "opera", "optin-monster", "pagelines", "paypal", "pied-piper", "pied-piper", "pied-piper-alt", "pinterest", "pinterest-p", "pinterest-square", "product-hunt", "qq", "quora", "ra", "ravelry", "rebel", "reddit", "reddit-alien", "reddit-square", "renren", "safari", "scribd", "sellsy", "share-alt", "share-alt-square", "shirtsinbulk", "sign-language", "simplybuilt", "skyatlas", "skype", "slack", "slideshare", "snapchat", "snapchat-ghost", "snapchat-square", "soundcloud", "spotify", "stack-exchange", "stack-overflow", "steam", "steam-square", "stumbleupon", "stumbleupon-circle", "superpowers", "telegram", "tencent-weibo", "themeisle", "trello", "tripadvisor", "tumblr", "tumblr-square", "twitch", "twitter", "twitter-square", "usb", "viacoin", "viadeo", "viadeo-square", "vimeo", "vimeo-square", "vine", "vk", "wechat", "weibo", "weixin", "whatsapp", "wikipedia-w", "windows", "wordpress", "wpbeginner", "wpexplorer", "wpforms", "xing", "xing-square", "y-combinator", "y-combinator-square", "yahoo", "yc", "yc-square", "yelp", "yoast", "youtube", "youtube-play", "youtube-square"]
  },
  {
    type: '网银支付类图标',
    data: ["cc-amex", "cc-diners-club", "cc-discover", "cc-jcb", "cc-mastercard", "cc-paypal", "cc-stripe", "cc-visa", "credit-card", "google-wallet", "paypal"]
  },
  {
    type: '图表类图标',
    data: ["area-chart", "bar-chart", "bar-chart-o", "line-chart", "pie-chart"]
  },
  {
    type: '旋转类图标',
    data: ["circle-o-notch", "cog", "gear", "refresh", "spinner"]
  },
  {
    type: '医疗类图标',
    data: ["ambulance", "h-square", "heart", "heart-o", "heartbeat", "hospital-o", "medkit", "plus-square", "stethoscope", "user-md", "wheelchair", "wheelchair-alt"]
  },
  {
    type: '运输交通类图标',
    data: ["ambulance", "automobile", "bicycle", "bus", "cab", "car", "fighter-jet", "motorcycle", "plane", "rocket", "ship", "space-shuttle", "subway", "taxi", "train", "truck", "wheelchair", "wheelchair-alt"]
  },

  {
    type: '货币类图标',
    data: ["bitcoin", "btc", "cny", "credit-card-alt", "dollar", "eur", "euro", "gbp", "gg", "gg-circle", "ils", "inr", "jpy", "krw", "money", "rmb", "rouble", "rub", "ruble", "rupee", "shekel", "sheqel", "try", "turkish-lira", "usd", "won", "yen"]
  },
  {
    type: '手势动作图标',
    data: ["hand-grab-o", "hand-lizard-o", "hand-o-down", "hand-o-left", "hand-o-right", "hand-o-up", "hand-paper-o", "hand-peace-o", "hand-pointer-o", "hand-rock-o", "hand-scissors-o", "hand-spock-o", "hand-stop-o", "thumbs-down", "thumbs-o-down", "thumbs-o-up", "thumbs-up"]
  },
  {
    type: '性别类图标',
    data: ["genderless", "intersex", "mars", "mars-double", "mars-stroke", "mars-stroke-h", "mars-stroke-v", "mercury", "neuter", "transgender", "transgender-alt", "venus", "venus-double", "venus-mars"]
  },
  {
    type: '文件类型图标',
    data: ["file", "file-archive-o", "file-audio-o", "file-code-o", "file-excel-o", "file-image-o", "file-movie-o", "file-o", "file-pdf-o", "file-photo-o", "file-picture-o", "file-powerpoint-o", "file-sound-o", "file-text", "file-text-o", "file-video-o", "file-word-o", "file-zip-o"]
  },
  {
    type: '文字编辑器图标',
    data: ["align-center", "align-justify", "align-left", "align-right", "bold", "chain", "chain-broken", "clipboard", "columns", "copy", "cut", "dedent", "eraser", "file", "file-o", "file-text", "file-text-o", "files-o", "floppy-o", "font", "header", "indent", "italic", "link", "list", "list-alt", "list-ol", "list-ul", "outdent", "paperclip", "paragraph", "paste", "repeat", "rotate-left", "rotate-right", "save", "scissors", "strikethrough", "subscript", "superscript", "table", "text-height", "text-width", "th", "th-large", "th-list", "underline", "undo", "unlink"]
  },
  {
    type: '可访问图标',
    data: ["american-sign-language-interpreting", "assistive-listening-systems", "audio-description", "blind", "braille", "deaf", "deaf", "low-vision", "question-circle-o", "universal-access", "volume-control-phone", "wheelchair-alt"]
  }

]
watch(() => props.value, () => {
  if (props.value) {
    currentValue.value = props.value
  }
}, {immediate: true})

const onSearch = () => {
  if (!props.disabled) {
    instance.refs.modal.open('选择图标', null, false, () => {
      if (props.value) {

        document.getElementById('icon-select-modal-content').scrollTo(0, document.getElementById('icon-id-' + props.value).offsetTop - 120)

      }
    })

  }
}

const handleChange = (icon) => {
  currentValue.value = icon
}
let emits = defineEmits(['update:value'])
const clickOk = () => {
  if (currentValue.value) {
    emits('update:value', currentValue.value)
    instance.refs.modal.close()
  } else {
    message.warning('请选择图标！')
  }

}
</script>
<style lang="less" scoped>
.icon-select {
  display: flex;
}

</style>
<style lang="less">
.icon-select-modal-content {

  height: calc(75vh - 55px);
  overflow-y: auto;
  overflow-x: hidden;

  .ant-tag-checkable {
    display: flex;
    flex-direction: column;
    align-items: center;
    overflow: hidden;
    padding: 10px;
    border: 1px solid #e2e3e3;
    border-radius: 10px;

    .fa {
      font-size: 16px;
    }
  }

  .ant-tag-checkable:hover {
    transform: scale(1.1);
  }
}
</style>

