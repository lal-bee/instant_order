<template>
  <div class="container">
    <h2 class="homeTitle">
      菜品总览
      <div class="more">
        <router-link to="/headquarters/dish">菜品管理</router-link>
        <el-icon>
          <ArrowRight />
        </el-icon>
      </div>
    </h2>
    <div class="orderviewBox">
      <ul>
        <li>
          <span class="status">
            <el-icon>
              <Finished />
            </el-icon>
            已启售
          </span>
          <span class="num">{{ dishesData.sold }}</span>
        </li>
        <li>
          <span class="status">
            <el-icon>
              <Lock />
            </el-icon>
            已停售
          </span>
          <span class="num">{{ dishesData.discontinued }}</span>
        </li>
        <li v-if="canOperate" class="add">
          <router-link to="/headquarters/dish/add">
            <el-icon>
              <CirclePlus />
            </el-icon>
            <p>新增菜品</p>
          </router-link>
        </li>
      </ul>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue'
import { useUserInfoStore } from '@/store'
import { isChairman } from '@/utils/permission'

// Define props
const props = defineProps<{
  dishesData: {
    sold: number,
    discontinued: number
  }
}>()

const userInfoStore = useUserInfoStore()
const canOperate = computed(() => isChairman(userInfoStore.userInfo?.role))
</script>

<style scoped></style>
