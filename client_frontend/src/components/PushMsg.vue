<template>
  <div v-show="visible" class="push-msg-mask" @click.self="confirm">
    <div class="push-msg-pop">
      <div class="push-msg-title">温馨提示</div>
      <div class="push-msg-tip">已成功帮你催单~</div>
      <div class="push-msg-sure" @click="confirm">确定</div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const visible = ref(false)

function openPopup() {
  visible.value = true
}

function closePopup() {
  visible.value = false
}

function confirm() {
  closePopup()
}

defineExpose({ openPopup, closePopup })
</script>

<style scoped>
.push-msg-mask {
  position: fixed;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 16px;
}
.push-msg-pop {
  background: #fff;
  border-radius: 12px;
  width: min(100%, 320px);
  padding: 24px 20px;
  text-align: center;
}
.push-msg-title {
  font-size: 17px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
}
.push-msg-tip {
  font-size: 15px;
  color: #666;
  margin-bottom: 20px;
}
.push-msg-sure {
  padding-top: 12px;
  border-top: 1px solid #eee;
  font-size: 16px;
  color: #22ccff;
  font-weight: 500;
  cursor: pointer;
}
</style>
