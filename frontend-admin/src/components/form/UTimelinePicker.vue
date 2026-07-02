<template>
  <div class="timeline-picker">
    <!-- Date selector -->
    <div class="timeline-header">
      <a-date-picker v-model:value="selectedDate" :disabled="disabled" :allowClear="false"
        valueFormat="YYYY-MM-DD" format="YYYY-MM-DD" @change="onDateChange" placeholder="选择日期" />
      <span class="timeline-hint" v-if="!disabled">点击或拖拽选择时间段</span>
      <span class="timeline-selected" v-if="displayRange">已选: {{ displayRange }}</span>
    </div>

    <!-- Time axis -->
    <div class="timeline-body" ref="timelineBody"
      @mousedown="onMouseDown" @mousemove="onMouseMove" @mouseup="onMouseUp" @mouseleave="onMouseUp">
      <!-- Hour labels -->
      <div class="timeline-labels">
        <div class="timeline-label" v-for="h in hours" :key="h"
          :style="{ left: getHourOffset(h) + 'px', width: slotWidth * slotsPerHour + 'px' }">
          {{ String(h).padStart(2, '0') + ':00' }}
        </div>
      </div>

      <!-- Grid -->
      <div class="timeline-grid" ref="timelineGrid">
        <!-- Slot cells -->
        <div class="timeline-slot" v-for="(slot, idx) in slots" :key="idx"
          :class="slotClass(idx)"
          :style="{ width: slotWidth + 'px' }"
          :title="slotTitle(idx)">
        </div>

        <!-- Existing reservations overlay -->
        <div class="timeline-reservation" v-for="(res, idx) in existingReservations" :key="'res-' + idx"
          :style="getReservationStyle(res)"
          :title="res.name + ' (' + res.booker + ')'">
          <span class="reservation-label">{{ res.name }}</span>
        </div>

        <!-- Selection overlay -->
        <div class="timeline-selection" v-if="selectionStyle"
          :style="selectionStyle">
          <span class="selection-label">{{ selectionLabel }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "UTimelinePicker"
}
</script>
<script setup>
import {ref, computed, watch} from "vue";
import dayjs from "dayjs";
import { listDataAction } from "@/api/api";

const props = defineProps({
  startTime: { type: String, default: '' },
  endTime: { type: String, default: '' },
  meetingRoomId: { type: String, default: '' },
  currentId: { type: String, default: '' },
  disabled: { type: Boolean, default: false },
  startHour: { type: Number, default: 8 },
  endHour: { type: Number, default: 21 },
  slotMinutes: { type: Number, default: 30 },
})

const emits = defineEmits(['update:startTime', 'update:endTime'])

// Config
const slotWidth = 40  // px per slot
const slotsPerHour = computed(() => 60 / props.slotMinutes)
const hours = computed(() => {
  let arr = []
  for (let h = props.startHour; h < props.endHour; h++) arr.push(h)
  return arr
})
const totalSlots = computed(() => (props.endHour - props.startHour) * slotsPerHour.value)

const slots = computed(() => {
  let arr = []
  for (let i = 0; i < totalSlots.value; i++) {
    let h = props.startHour + Math.floor(i / slotsPerHour.value)
    let m = (i % slotsPerHour.value) * props.slotMinutes
    arr.push({ hour: h, minute: m })
  }
  return arr
})

// State
const selectedDate = ref(dayjs().format('YYYY-MM-DD'))
const selStart = ref(-1)
const selEnd = ref(-1)
const dragging = ref(false)
const dragStartIdx = ref(-1)
const existingReservations = ref([])
const timelineGrid = ref(null)

// Initialize from props
watch(() => props.startTime, (val) => {
  if (val) {
    let d = dayjs(val)
    selectedDate.value = d.format('YYYY-MM-DD')
    selStart.value = timeToSlotIndex(d.hour(), d.minute())
  }
}, { immediate: true })

watch(() => props.endTime, (val) => {
  if (val) {
    let d = dayjs(val)
    selEnd.value = timeToSlotIndex(d.hour(), d.minute()) - 1
  }
}, { immediate: true })

// Load existing reservations when date or meeting room changes
watch([selectedDate, () => props.meetingRoomId], () => {
  loadExistingReservations()
}, { immediate: true })

function timeToSlotIndex(hour, minute) {
  let idx = (hour - props.startHour) * slotsPerHour.value + Math.floor(minute / props.slotMinutes)
  return Math.max(0, Math.min(idx, totalSlots.value - 1))
}

function slotToTime(idx) {
  let h = props.startHour + Math.floor(idx / slotsPerHour.value)
  let m = (idx % slotsPerHour.value) * props.slotMinutes
  return { hour: h, minute: m }
}

function formatSlotTime(idx) {
  let t = slotToTime(idx)
  return String(t.hour).padStart(2, '0') + ':' + String(t.minute).padStart(2, '0')
}

function getHourOffset(h) {
  return (h - props.startHour) * slotsPerHour.value * slotWidth
}

// Check if a slot is occupied by existing reservations
function isOccupied(idx) {
  let t = slotToTime(idx)
  let slotStart = t.hour * 60 + t.minute
  let slotEnd = slotStart + props.slotMinutes
  for (let res of existingReservations.value) {
    if (slotStart < res.endMin && slotEnd > res.startMin) {
      return true
    }
  }
  return false
}

function slotClass(idx) {
  let classes = []
  if (isOccupied(idx)) classes.push('occupied')
  if (idx >= selStart.value && idx <= selEnd.value) classes.push('selected')
  if (dragging.value) {
    let lo = Math.min(dragStartIdx.value, selEnd.value >= 0 ? selEnd.value : dragStartIdx.value)
    let hi = Math.max(dragStartIdx.value, selEnd.value >= 0 ? selEnd.value : dragStartIdx.value)
    if (idx >= lo && idx <= hi) classes.push('dragging')
  }
  return classes
}

function slotTitle(idx) {
  let t = slotToTime(idx)
  let next = slotToTime(Math.min(idx + 1, totalSlots.value - 1))
  let endT = idx === totalSlots.value - 1 ?
    { hour: props.endHour, minute: 0 } : next
  return `${String(t.hour).padStart(2, '0')}:${String(t.minute).padStart(2, '0')} - ${String(endT.hour).padStart(2, '0')}:${String(endT.minute).padStart(2, '0')}`
}

// Mouse events
function getSlotFromEvent(e) {
  if (!timelineGrid.value) return -1
  let rect = timelineGrid.value.getBoundingClientRect()
  let x = e.clientX - rect.left + timelineGrid.value.scrollLeft
  let idx = Math.floor(x / slotWidth)
  return Math.max(0, Math.min(idx, totalSlots.value - 1))
}

function onMouseDown(e) {
  if (props.disabled) return
  let idx = getSlotFromEvent(e)
  if (idx < 0) return
  dragging.value = true
  dragStartIdx.value = idx
  selStart.value = idx
  selEnd.value = idx
  e.preventDefault()
}

function onMouseMove(e) {
  if (!dragging.value) return
  let idx = getSlotFromEvent(e)
  if (idx < 0) return
  selStart.value = Math.min(dragStartIdx.value, idx)
  selEnd.value = Math.max(dragStartIdx.value, idx)
}

function onMouseUp(e) {
  if (!dragging.value) return
  dragging.value = false
  if (selStart.value >= 0 && selEnd.value >= 0) {
    emitTimeRange()
  }
}

function emitTimeRange() {
  let startT = slotToTime(selStart.value)
  let endSlotEnd = selEnd.value === totalSlots.value - 1
    ? { hour: props.endHour, minute: 0 }
    : slotToTime(selEnd.value + 1)
  let startStr = selectedDate.value + ' ' + String(startT.hour).padStart(2, '0') + ':' + String(startT.minute).padStart(2, '0')
  let endStr = selectedDate.value + ' ' + String(endSlotEnd.hour).padStart(2, '0') + ':' + String(endSlotEnd.minute).padStart(2, '0')
  emits('update:startTime', startStr)
  emits('update:endTime', endStr)
}

// Display
const displayRange = computed(() => {
  if (selStart.value < 0 || selEnd.value < 0) return ''
  let endSlotEnd = selEnd.value === totalSlots.value - 1
    ? String(props.endHour).padStart(2, '0') + ':00'
    : formatSlotTime(selEnd.value + 1)
  return formatSlotTime(selStart.value) + ' ~ ' + endSlotEnd
})

const selectionStyle = computed(() => {
  if (selStart.value < 0 || selEnd.value < 0) return null
  let left = selStart.value * slotWidth
  let width = (selEnd.value - selStart.value + 1) * slotWidth
  return { left: left + 'px', width: width + 'px' }
})

const selectionLabel = computed(() => {
  return displayRange.value
})

// Existing reservations
function loadExistingReservations() {
  if (!props.meetingRoomId || !selectedDate.value) {
    existingReservations.value = []
    return
  }
  let dateStart = selectedDate.value + ' 00:00'
  let dateEnd = selectedDate.value + ' 23:59'
  let param = {
    meeting_room: { id: props.meetingRoomId },
    start_time: dateStart,
    queryRuleStart_time: 'le',
    end_time: dateStart,
    queryRuleEnd_time: 'ge',
  }
  listDataAction('oas_meeting_reservation', {
    pageParam: { pageNo: 1, pageSize: 100 },
    queryParamType: {start_time: "", end_time: "", meeting_room: ""},
    meeting_room: { id: props.meetingRoomId },
    start_time: dateStart,
    end_time: dateStart
  }).then(res => {
    let list = res?.data?.data || res?.data?.list || res?.data || []
    if (!Array.isArray(list)) {
      if (list.list) list = list.list
      else list = []
    }
    existingReservations.value = list.filter(item => {
      // Exclude current record when editing
      if (props.currentId && item.id === props.currentId) return false
      return true
    }).map(item => {
      let s = dayjs(item.start_time || item.startTime)
      let e = dayjs(item.end_time || item.endTime)
      return {
        name: item.name || item.s01 || '已预定',
        booker: item.booker__name || item.bookerName || '',
        startMin: s.hour() * 60 + s.minute(),
        endMin: e.hour() * 60 + e.minute(),
      }
    })
  }).catch(() => {
    existingReservations.value = []
  })
}

function getReservationStyle(res) {
  let startSlot = (res.startMin - props.startHour * 60) / props.slotMinutes
  let endSlot = (res.endMin - props.startHour * 60) / props.slotMinutes
  startSlot = Math.max(0, startSlot)
  endSlot = Math.min(totalSlots.value, endSlot)
  return {
    left: startSlot * slotWidth + 'px',
    width: (endSlot - startSlot) * slotWidth + 'px',
  }
}

function onDateChange() {
  selStart.value = -1
  selEnd.value = -1
  emits('update:startTime', '')
  emits('update:endTime', '')
}
</script>

<style lang="less" scoped>
.timeline-picker {
  width: 100%;
}

.timeline-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.timeline-hint {
  color: #999;
  font-size: 12px;
}

.timeline-selected {
  color: #1890ff;
  font-size: 13px;
  font-weight: 500;
}

.timeline-body {
  user-select: none;
  overflow-x: auto;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  padding: 4px 0;
}

.timeline-labels {
  position: relative;
  height: 24px;
  margin-bottom: 2px;
}

.timeline-label {
  position: absolute;
  top: 0;
  font-size: 11px;
  color: #666;
  text-align: left;
  padding-left: 2px;
  border-left: 1px solid #d9d9d9;
  line-height: 24px;
  box-sizing: border-box;
}

.timeline-grid {
  position: relative;
  display: flex;
  height: 48px;
  cursor: pointer;
}

.timeline-slot {
  height: 100%;
  border-right: 1px solid #f0f0f0;
  border-left: none;
  background: #fafafa;
  box-sizing: border-box;
  flex-shrink: 0;
  transition: background 0.1s;

  &:nth-child(even) {
    border-right-color: #d9d9d9;
  }

  &:hover:not(.occupied) {
    background: #e6f7ff;
  }

  &.occupied {
    background: #fff1f0;
    cursor: not-allowed;
  }

  &.selected {
    background: #bae7ff;
  }

  &.dragging {
    background: #91d5ff;
  }
}

.timeline-reservation {
  position: absolute;
  top: 2px;
  height: 20px;
  background: #ff7875;
  border-radius: 3px;
  display: flex;
  align-items: center;
  padding: 0 4px;
  overflow: hidden;
  pointer-events: none;
  z-index: 2;
}

.reservation-label {
  font-size: 11px;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.timeline-selection {
  position: absolute;
  top: 24px;
  height: 22px;
  background: rgba(24, 144, 255, 0.3);
  border: 1px solid #1890ff;
  border-radius: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
  z-index: 3;
}

.selection-label {
  font-size: 11px;
  color: #1890ff;
  font-weight: 500;
  white-space: nowrap;
}
</style>
