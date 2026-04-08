<template>
  <div class="page-dish-detail">
    <!-- 顶部栏：返回 + 标题 -->
    <header class="detail-header">
      <button class="back-btn" type="button" @click="goBack" aria-label="返回">
        <img v-if="!backIconError" class="back" src="/icon/back.png" alt="返回" @error="backIconError = true" />
        <span v-else class="back-fallback">‹</span>
      </button>
      <span class="title">{{ pageTitle }}</span>
    </header>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-wrap">
      <span class="loading-text">加载中...</span>
    </div>

    <!-- 菜品详情 -->
    <div class="dish" v-else-if="dish">
      <div class="title">菜品详情</div>
      <img class="image" :src="dish.pic" alt="" />
      <div class="dishinfo">
        <div class="name ellipsis">{{ dish.name }}</div>
        <div class="detail ellipsis">{{ dish.detail }}</div>
        <div class="price">
          <span class="symbol">¥</span>
          <span class="number">{{ dish.price }}</span>
        </div>
        <template v-if="dish.flavors && dish.flavors.length > 0">
          <span class="choosenorm" @click.stop="chooseNorm(dish)">选择规格</span>
        </template>
        <div v-else class="sub_add">
          <img v-if="getCopies(dish) > 0" src="/icon/sub.png" class="sub" alt="-" @click.stop="subDishAction(dish, '菜品')" />
          <span v-if="getCopies(dish) > 0" class="dish_number">{{ getCopies(dish) }}</span>
          <img src="/icon/add.png" class="add" alt="+" @click.stop="addDishAction(dish, '菜品')" />
        </div>
      </div>
    </div>

    <!-- 套餐详情 -->
    <div class="setmeal" v-else-if="setmeal">
      <div class="title">套餐详情</div>
      <div v-for="item in setmeal.setmealDishes" :key="item.name" class="setmeal_item">
        <img :src="item.pic" alt="" />
        <div class="dishinfo">
          <div class="name ellipsis">{{ item.name }}</div>
          <div class="detail ellipsis">{{ item.detail }}</div>
        </div>
      </div>
      <div class="setmeal_info">
        <div class="detail ellipsis">{{ setmeal.detail }}</div>
        <div class="price">
          <span class="symbol">¥</span>
          <span class="number">{{ setmeal.price }}</span>
        </div>
        <div class="sub_add">
          <img v-if="getCopies(setmeal) > 0" src="/icon/sub.png" class="sub" alt="-" @click.stop="subDishAction(setmeal, '套餐')" />
          <span v-if="getCopies(setmeal) > 0" class="dish_number">{{ getCopies(setmeal) }}</span>
          <img src="/icon/add.png" class="add" alt="+" @click.stop="addDishAction(setmeal, '套餐')" />
        </div>
      </div>
    </div>

    <!-- 未加载到数据 -->
    <div v-else class="empty">暂无该商品信息</div>

    <!-- 口味弹窗 -->
    <div class="dialog" v-show="visible">
      <div class="flavor_pop">
        <div class="title">选择规格</div>
        <div class="scroll">
          <div v-for="flavor in flavors" :key="flavor.name" class="flavor">
            <div>{{ flavor.name }}</div>
            <div class="flavor-tags">
              <span
                v-for="(item, index) in parseList(flavor.list)"
                :key="index"
                class="flavorItem"
                :class="{ active: chosedflavors.indexOf(item) !== -1 }"
                @click="chooseFlavor(parseList(flavor.list), item)"
              >{{ item }}</span>
            </div>
          </div>
        </div>
        <button class="addToCart" @click="addToCart">加入购物车</button>
      </div>
      <div class="close_dialog" @click="visible = false">×</div>
    </div>

    <!-- 底部购物车栏 -->
    <div class="footer_order_buttom" v-if="cartList.length === 0">
      <div class="order_number"><img src="/images/cart_empty.png" class="order_number_icon" alt="" /></div>
      <div class="order_price"><span class="ico">￥</span> 0</div>
      <div class="order_btn">未选商品</div>
    </div>
    <div class="footer_order_buttom" v-else @click="openCartList = !openCartList">
      <div class="order_number">
        <img src="/images/cart_active.png" class="order_number_icon" alt="" />
        <span class="order_dish_num">{{ CartAllNumber }}</span>
      </div>
      <div class="order_price"><span class="ico">￥</span> {{ cartTotalPrice }}</div>
      <div class="order_btn_active" @click.stop="submitOrder">去结算</div>
    </div>

    <!-- 购物车弹层 -->
    <div class="pop_mask" v-show="openCartList" @click="openCartList = false">
      <div class="cart_pop" @click.stop>
        <div class="top_title">
          <span class="tit">购物车</span>
          <span class="clear" @click="clearCart"><img class="clear_icon" src="/icon/clear.png" alt="" /> 清空</span>
        </div>
        <div class="card_order_list">
          <div class="type_item" v-for="(obj, index) in cartList" :key="index">
            <div class="dish_img"><img :src="obj.pic" class="dish_img_url" alt="" /></div>
            <div class="dish_info">
              <div class="dish_name">{{ obj.name }}</div>
              <div class="dish_price"><span class="ico">￥</span> {{ obj.amount }}</div>
              <div class="dish_flavor">{{ obj.dishFlavor }}</div>
              <div class="dish_active">
                <img v-if="obj.number > 0" src="/icon/sub.png" class="dish_sub" alt="-" @click.stop="subDishAction(obj, '购物车')" />
                <span v-if="obj.number > 0" class="dish_number">{{ obj.number }}</span>
                <img src="/icon/add.png" class="dish_add" alt="+" @click.stop="addDishAction(obj, '购物车')" />
              </div>
            </div>
          </div>
          <div class="seize_seat"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getDishByIdAPI } from '@/api/dish'
import { getSetmealAPI } from '@/api/setmeal'
import { addToCartAPI, subCartAPI, getCartAPI, cleanCartAPI } from '@/api/cart'
import { showToast } from '@/utils/toast'

const router = useRouter()
const route = useRoute()

const dish = ref(null)
const setmeal = ref(null)
const loading = ref(true)
const openCartList = ref(false)
const cartList = ref([])
const visible = ref(false)
const dialogDish = ref(null)
const flavors = ref([])
const chosedflavors = ref([])
const backIconError = ref(false)

const CartAllNumber = computed(() => cartList.value.reduce((acc, cur) => acc + cur.number, 0))
const CartAllPrice = computed(() => cartList.value.reduce((acc, cur) => acc + cur.amount * cur.number, 0))
const cartTotalPrice = computed(() => (Math.round(CartAllPrice.value * 100) / 100).toFixed(2))
const pageTitle = computed(() => (dish.value ? '菜品详情' : setmeal.value ? '套餐详情' : '商品详情'))

function parseList(str) {
  if (!str) return []
  try {
    return typeof str === 'string' ? JSON.parse(str) : str
  } catch (e) {
    return []
  }
}

async function getCartList() {
  try {
    const res = await getCartAPI()
    cartList.value = res.data || []
    if (cartList.value.length === 0) openCartList.value = false
  } catch (e) {
    cartList.value = []
  }
}

// 详情页：菜品按 dishId 汇总数量（含多规格）；套餐按 setmealId 取数量
function getCopies(item) {
  if (dish.value && item.id === dish.value.id) {
    return cartList.value.filter((i) => i.dishId === item.id).reduce((acc, i) => acc + (i.number || 0), 0)
  }
  if (setmeal.value && item.id === setmeal.value.id) {
    return cartList.value.find((i) => i.setmealId === item.id)?.number || 0
  }
  return 0
}

async function init() {
  const dishId = route.query.dishId
  const setmealId = route.query.setmealId
  if (dishId) {
    try {
      const res = await getDishByIdAPI(Number(dishId))
      dish.value = res.data
    } catch (e) {
      dish.value = null
    }
  } else if (setmealId) {
    try {
      const res = await getSetmealAPI(Number(setmealId))
      setmeal.value = res.data
    } catch (e) {
      setmeal.value = null
    }
  }
  loading.value = false
}

function chooseNorm(d) {
  flavors.value = d.flavors || []
  dialogDish.value = { ...d }
  delete dialogDish.value.flavors
  chosedflavors.value = []
  flavors.value.forEach((f) => {
    const list = parseList(f.list)
    if (list && list.length) chosedflavors.value.push(list[0])
  })
  if (dialogDish.value) dialogDish.value.flavors = chosedflavors.value.join(',')
  visible.value = true
}

function chooseFlavor(obj, flavor) {
  let ind = -1
  const findst = obj.some((n) => {
    ind = chosedflavors.value.indexOf(n)
    return ind !== -1
  })
  const indexInChosed = chosedflavors.value.indexOf(flavor)
  if (indexInChosed === -1 && !findst) {
    chosedflavors.value.push(flavor)
  } else if (indexInChosed === -1 && findst && ind >= 0) {
    chosedflavors.value.splice(ind, 1)
    chosedflavors.value.push(flavor)
  } else {
    chosedflavors.value.splice(indexInChosed, 1)
  }
  if (dialogDish.value) dialogDish.value.flavors = chosedflavors.value.join(',')
}

async function addToCart() {
  const d = dialogDish.value
  if (!d) return
  if (!chosedflavors.value || chosedflavors.value.length <= 0) {
    showToast('请选择规格')
    return
  }
  await addToCartAPI({ dishId: d.id, dishFlavor: chosedflavors.value.join(',') })
  await getCartList()
  chosedflavors.value = []
  visible.value = false
}

async function addDishAction(item, form) {
  if (form === '购物车') {
    await addToCartAPI({ dishId: item.dishId, setmealId: item.setmealId, dishFlavor: item.dishFlavor })
  } else if (form === '菜品') {
    await addToCartAPI({ dishId: dish.value.id })
  } else {
    await addToCartAPI({ setmealId: setmeal.value.id })
  }
  await getCartList()
}

async function subDishAction(item, form) {
  if (form === '购物车') {
    await subCartAPI({ dishId: item.dishId, setmealId: item.setmealId, dishFlavor: item.dishFlavor })
  } else if (form === '菜品') {
    await subCartAPI({ dishId: dish.value.id })
  } else {
    await subCartAPI({ setmealId: setmeal.value.id })
  }
  await getCartList()
}

async function clearCart() {
  await cleanCartAPI()
  await getCartList()
  openCartList.value = false
}

function submitOrder() {
  router.push({ path: '/submit', query: route.query })
}

function goBack() {
  router.replace({ path: '/order', query: route.query })
}

onMounted(async () => {
  await getCartList()
  await init()
})
</script>

<style scoped>
.page-dish-detail {
  min-height: 100vh;
  max-width: 480px;
  margin: 0 auto;
  padding-bottom: calc(96px + env(safe-area-inset-bottom, 0px));
  padding-top: calc(52px + env(safe-area-inset-top, 0px));
  background: #fff;
}

.detail-header {
  position: fixed;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 480px;
  height: calc(44px + env(safe-area-inset-top, 0px));
  display: flex;
  align-items: center;
  justify-content: center;
  padding: env(safe-area-inset-top, 0px) 12px 0;
  background: #fff;
  border-bottom: 1px solid #eee;
  z-index: 100;
}
.detail-header .back-btn {
  position: absolute;
  left: 12px;
  top: calc(env(safe-area-inset-top, 0px) + 10px);
  width: 24px;
  height: 24px;
  border: none;
  border-radius: 12px;
  background: rgba(0, 0, 0, 0.08);
  padding: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.detail-header .back {
  width: 18px;
  height: 18px;
}
.detail-header .back-fallback {
  font-size: 20px;
  line-height: 1;
  color: #111827;
  transform: translateY(-1px);
}
.detail-header .title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.loading-wrap {
  padding: 60px 20px;
  text-align: center;
}
.loading-text {
  font-size: 14px;
  color: #999;
}

.dish,
.setmeal {
  padding: 16px;
}

.title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 12px;
}

.dish .image {
  width: 100%;
  max-height: 240px;
  min-height: 160px;
  object-fit: cover;
  border-radius: 8px;
  background: #f5f5f5;
}

.dishinfo {
  margin-top: 12px;
  position: relative;
  padding-right: 100px;
}

.ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dishinfo .name { font-size: 16px; color: #222; }
.dishinfo .detail { font-size: 13px; color: #666; margin-top: 4px; }
.dishinfo .price { font-size: 16px; color: #cf4444; margin-top: 8px; }

.choosenorm {
  position: absolute;
  right: 0;
  bottom: 0;
  font-size: 12px;
  color: #10B981;
  padding: 6px 12px;
  border: 1px solid #10B981;
  border-radius: 12px;
}

.sub_add {
  position: absolute;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.sub_add .sub, .sub_add .add { width: 28px; height: 28px; cursor: pointer; }
.dish_number { font-size: 14px; min-width: 20px; text-align: center; }

.setmeal_item {
  display: flex;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #eee;
}

.setmeal_item img {
  width: 72px;
  height: 72px;
  flex-shrink: 0;
  object-fit: cover;
  border-radius: 8px;
  background: #f5f5f5;
}

.setmeal_info {
  margin-top: 16px;
  padding: 12px;
  background: #f9f9f9;
  border-radius: 8px;
  position: relative;
  padding-right: 100px;
}

.setmeal_info .price { margin-top: 8px; }
.setmeal_info .sub_add { bottom: 12px; }

.empty {
  padding: 40px 20px;
  text-align: center;
  color: #999;
}

/* 口味弹窗、底部购物车、购物车弹层 - 与 order 页一致 */
.dialog {
  position: fixed;
  inset: 0;
  z-index: 1000;
  background: rgba(0,0,0,0.6);
  padding: 16px;
}

.flavor_pop {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: min(100%, 360px);
  max-height: min(70vh, 520px);
  padding: 16px;
  border-radius: 12px;
  background: #fff;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.flavor_pop .title { font-size: 16px; font-weight: bold; margin-bottom: 12px; }
.scroll { max-height: min(48vh, 360px); overflow-y: auto; }
.flavor { padding: 8px 0; }
.flavor-tags { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 6px; }
.flavorItem {
  padding: 6px 12px;
  border: 1px solid #10B981;
  border-radius: 12px;
  font-size: 12px;
  cursor: pointer;
}
.flavorItem.active { background: #10B981; color: #fff; }
.addToCart {
  margin-top: 12px;
  min-height: 42px;
  width: 100%;
  padding: 0 16px;
  background: #10B981;
  color: #fff;
  border: none;
  border-radius: 21px;
  font-size: 14px;
  cursor: pointer;
}
.close_dialog {
  position: absolute;
  top: calc(50% + min(36vh, 240px));
  left: 50%;
  transform: translateX(-50%);
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(0,0,0,0.6);
  color: #fff;
  font-size: 24px;
  line-height: 36px;
  text-align: center;
  cursor: pointer;
}

.footer_order_buttom {
  position: fixed;
  bottom: calc(14px + env(safe-area-inset-bottom, 0px));
  left: 50%;
  transform: translateX(-50%);
  width: calc(100% - 24px);
  max-width: 456px;
  height: 52px;
  display: flex;
  align-items: center;
  background: rgba(0,0,0,0.9);
  border-radius: 26px;
  padding: 0 12px;
  z-index: 100;
  box-sizing: border-box;
}

.order_number { position: relative; width: 56px; }
.order_number_icon { width: 56px; height: 56px; position: absolute; left: 0; bottom: 0; }
.order_dish_num {
  position: absolute;
  right: 0;
  top: -4px;
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  padding: 0 6px;
  font-size: 12px;
  background: #e94e3c;
  color: #fff;
  border-radius: 10px;
  text-align: center;
}
.order_price { flex: 1; color: #fff; font-size: 18px; padding-left: 12px; }
.order_price .ico { font-size: 14px; }
.order_btn {
  width: 96px;
  height: 36px;
  line-height: 36px;
  border-radius: 18px;
  background: #d8d8d8;
  color: #fff;
  font-size: 14px;
  text-align: center;
  border: none;
}
.order_btn_active {
  width: 96px;
  height: 36px;
  line-height: 36px;
  border-radius: 18px;
  background: #10B981;
  color: #fff;
  font-size: 14px;
  text-align: center;
  border: none;
  cursor: pointer;
}

.pop_mask { position: fixed; inset: 0; z-index: 500; background: rgba(0,0,0,0.4); }
.cart_pop {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 480px;
  max-height: 60vh;
  background: #fff;
  border-radius: 16px 16px 0 0;
  padding: 14px 12px calc(10px + env(safe-area-inset-bottom, 0px));
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.top_title { display: flex; justify-content: space-between; align-items: center; padding-bottom: 12px; border-bottom: 1px solid #ebeef5; }
.tit { font-size: 18px; font-weight: bold; }
.clear { font-size: 14px; color: #999; display: flex; align-items: center; gap: 4px; cursor: pointer; }
.clear_icon { width: 16px; height: 16px; }
.card_order_list { flex: 1; overflow-y: auto; padding-top: 12px; }
.type_item { display: flex; gap: 12px; padding-bottom: 16px; margin-bottom: 16px; border-bottom: 1px solid #ebeef5; }
.dish_img_url { width: 64px; height: 64px; border-radius: 8px; object-fit: cover; }
.dish_info { flex: 1; min-width: 0; position: relative; padding-bottom: 32px; }
.dish_name { font-size: 15px; font-weight: 600; color: #333; }
.dish_price { font-size: 15px; color: #e94e3c; }
.dish_flavor { font-size: 12px; color: #666; margin-top: 4px; }
.dish_active { position: absolute; right: 0; bottom: 0; display: flex; align-items: center; gap: 8px; }
.dish_sub, .dish_add { width: 28px; height: 28px; cursor: pointer; }
.seize_seat { height: 40px; }
</style>
