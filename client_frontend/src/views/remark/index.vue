<template>
  <div class="page-remark">
    <header class="detail-header">
      <span class="title">订单备注</span>
    </header>
    <div class="content">
      <p class="tip">该页面暂未启用，请在确认订单页填写备注。</p>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.page-remark {
  min-height: 100vh;
  max-width: 480px;
  margin: 0 auto;
  padding-top: calc(52px + env(safe-area-inset-top, 0px));
  background: #f6f7f9;
}

.detail-header {
  position: fixed;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 480px;
  height: calc(44px + env(safe-area-inset-top, 0px));
  padding: env(safe-area-inset-top, 0px) 12px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
}

.title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.content {
  padding: 14px;
}

.tip {
  margin: 0;
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  color: #6b7280;
  font-size: 14px;
  line-height: 1.5;
}
</style>
