<template>
  <div class="container">
    <h2 class="homeTitle">
      套餐总览
      <div class="more">
        <router-link to="/headquarters/setmeal">套餐管理</router-link>
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
          <span class="num">{{ setMealData.sold }}</span>
        </li>
        <li>
          <span class="status"> 
            <el-icon>
              <Lock />
            </el-icon>
            已停售
          </span>
          <span class="num">{{ setMealData.discontinued }}</span>
        </li>
        <li v-if="canOperate" class="add">
          <router-link to="/headquarters/setmeal/add">
            <el-icon>
              <CirclePlus />
            </el-icon>
            <p>新增套餐</p>
          </router-link>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useUserInfoStore } from '@/store'
import { isChairman } from '@/utils/permission'

interface SetMealData {
  sold: number;
  discontinued: number;
}

const props = defineProps<{
  setMealData: SetMealData;
}>()

const userInfoStore = useUserInfoStore()
const canOperate = computed(() => isChairman(userInfoStore.userInfo?.role))

</script>
