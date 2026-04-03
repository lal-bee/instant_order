<template>
  <div class="page-order">
    <NavBar :status="status" />
    <div class="viewport">
      <!-- 未登录或无数据时提示 -->
      <div v-if="categoryList.length === 0" class="empty-tip">
        <p class="empty-msg">请先登录以加载菜单</p>
        <button class="btn-login" type="button" @click="goLogin">去登录</button>
      </div>
      <div v-else class="categories">
        <div class="primary">
          <div
            v-for="(item, index) in categoryList"
            :key="item.id"
            class="item"
            :class="{ active: index === activeIndex }"
            @click="getDishOrSetmealList(index)"
          >
            {{ item.name }}
          </div>
        </div>
        <div class="secondary">
          <div class="section">
            <div
              v-for="dish in dishList"
              :key="dish.id"
              class="dish"
              @click="goDishDetail(dish)"
            >
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
                  <img
                    v-if="getCopies(dish) > 0"
                    src="/icon/sub.png"
                    class="sub"
                    alt="-"
                    @click.stop="subDishAction(dish, '普通')"
                  />
                  <span v-if="getCopies(dish) > 0" class="dish_number">{{ getCopies(dish) }}</span>
                  <img src="/icon/add.png" class="add" alt="+" @click.stop="addDishAction(dish, '普通')" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 口味选择弹窗 -->
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
              >
                {{ item }}
              </span>
            </div>
          </div>
        </div>
        <button class="addToCart" @click="addToCart">加入购物车</button>
      </div>
      <div class="close_dialog" @click="visible = false">×</div>
    </div>

    <!-- 底部购物车栏：空 -->
    <div class="footer_order_buttom" v-if="cartList.length === 0">
      <div class="order_number">
        <img src="/images/cart_empty.png" class="order_number_icon" alt="" />
      </div>
      <div class="order_price"><span class="ico">￥</span> 0</div>
      <div class="order_btn">￥0起送</div>
    </div>
    <!-- 底部购物车栏：有商品 -->
    <div class="footer_order_buttom" v-else @click="openCartList = !openCartList">
      <div class="order_number">
        <img src="/images/cart_active.png" class="order_number_icon" alt="" />
        <span class="order_dish_num">{{ CartAllNumber }}</span>
      </div>
      <div class="order_price">
        <span class="ico">￥</span> {{ cartTotalPrice }}
      </div>
      <div class="order_btn_active" @click.stop="submitOrder">去结算</div>
    </div>

    <!-- 购物车弹层 -->
    <div class="pop_mask" v-show="openCartList" @click="openCartList = false">
      <div class="cart_pop" @click.stop>
        <div class="top_title">
          <span class="tit">购物车</span>
          <span class="clear" @click="clearCart">
            <img class="clear_icon" src="/icon/clear.png" alt="" /> 清空
          </span>
        </div>
        <div class="card_order_list">
          <div class="type_item" v-for="(obj, index) in cartList" :key="index">
            <div class="dish_img">
              <img :src="obj.pic" class="dish_img_url" alt="" />
            </div>
            <div class="dish_info">
              <div class="dish_name">{{ obj.name }}</div>
              <div class="dish_price"><span class="ico">￥</span> {{ obj.amount }}</div>
              <div class="dish_flavor">{{ obj.dishFlavor }}</div>
              <div class="dish_active">
                <img
                  v-if="obj.number > 0"
                  src="/icon/sub.png"
                  class="dish_sub"
                  alt="-"
                  @click.stop="subDishAction(obj, '购物车')"
                />
                <span v-if="obj.number > 0" class="dish_number">{{ obj.number }}</span>
                <img src="/icon/add.png" class="dish_add" alt="+" @click.stop="addDishAction(obj, '购物车')" />
              </div>
            </div>
          </div>
          <div class="seize_seat"></div>
        </div>
      </div>
    </div>

    <!-- 打烊遮罩 -->
    <div v-show="!status" class="close" @click="goBack">
      <span class="text">本店已打烊</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import NavBar from '@/components/NavBar.vue'
import { getStatusAPI } from '@/api/shop'
import { getCategoryAPI } from '@/api/category'
import { getDishListAPI } from '@/api/dish'
import { getSetmealListAPI } from '@/api/setmeal'
import { addToCartAPI, subCartAPI, getCartAPI, cleanCartAPI } from '@/api/cart'
import { showToast } from '@/utils/toast'

const router = useRouter()
const route = useRoute()

const status = ref(true)
const categoryList = ref([])
const activeIndex = ref(0)
const dishList = ref([])
const openCartList = ref(false)
const cartList = ref([])
const visible = ref(false)
const dialogDish = ref(null)
const flavors = ref([])
const chosedflavors = ref([])

const CartAllNumber = computed(() => cartList.value.reduce((acc, cur) => acc + cur.number, 0))
const CartAllPrice = computed(() => cartList.value.reduce((acc, cur) => acc + cur.amount * cur.number, 0))
const cartTotalPrice = computed(() => (Math.round(CartAllPrice.value * 100) / 100).toFixed(2))

function parseList(str) {
  if (!str) return []
  try {
    return typeof str === 'string' ? JSON.parse(str) : str
  } catch (e) {
    return []
  }
}

async function getCategoryData() {
  const res = await getCategoryAPI()
  categoryList.value = res.data || []
}

async function getDishOrSetmealList(index) {
  activeIndex.value = index
  const cat = categoryList.value[index]
  if (!cat) return
  if (cat.type === 1) {
    const res = await getDishListAPI(cat.id)
    dishList.value = res.data || []
  } else {
    const res = await getSetmealListAPI(cat.id)
    dishList.value = res.data || []
  }
}

async function getCartList() {
  const res = await getCartAPI()
  cartList.value = res.data || []
  if (cartList.value.length === 0) openCartList.value = false
}

function getCopies(dish) {
  const cat = categoryList.value[activeIndex.value]
  if (!cat) return 0
  if (cat.sort < 20) {
    return cartList.value.find((item) => item.dishId === dish.id)?.number || 0
  }
  return cartList.value.find((item) => item.setmealId === dish.id)?.number || 0
}

function goDishDetail(dish) {
  const cat = categoryList.value[activeIndex.value]
  if (!cat) return
  const query = { ...route.query }
  if (cat.sort < 20) query.dishId = dish.id
  else query.setmealId = dish.id
  router.push({ path: '/dish-detail', query })
}

function goLogin() {
  const redirect = route.fullPath || '/order'
  router.push({ path: '/login', query: { redirect } })
}

function chooseNorm(dish) {
  flavors.value = dish.flavors || []
  dialogDish.value = { ...dish }
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
  const dish = dialogDish.value
  if (!dish) return
  if (!chosedflavors.value || chosedflavors.value.length <= 0) {
    showToast('请选择规格')
    return
  }
  await addToCartAPI({ dishId: dish.id, dishFlavor: chosedflavors.value.join(',') })
  await getCartList()
  chosedflavors.value = []
  visible.value = false
}

async function addDishAction(item, form) {
  if (form === '购物车') {
    await addToCartAPI({
      dishId: item.dishId,
      setmealId: item.setmealId,
      dishFlavor: item.dishFlavor,
    })
  } else {
    const cat = categoryList.value[activeIndex.value]
    if (cat.sort < 20) await addToCartAPI({ dishId: item.id })
    else await addToCartAPI({ setmealId: item.id })
  }
  await getCartList()
}

async function subDishAction(item, form) {
  if (form === '购物车') {
    await subCartAPI({
      dishId: item.dishId,
      setmealId: item.setmealId,
      dishFlavor: item.dishFlavor,
    })
  } else {
    const cat = categoryList.value[activeIndex.value]
    if (cat.sort < 20) await subCartAPI({ dishId: item.id })
    else await subCartAPI({ setmealId: item.id })
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
  router.replace('/')
}

onMounted(async () => {
  try {
    const res = await getStatusAPI()
    status.value = res.data === 1
  } catch (e) {
    status.value = false
  }
  try {
    await getCategoryData()
    if (categoryList.value.length) await getDishOrSetmealList(0)
    await getCartList()
  } catch (e) {
    // 401 等已由 request 拦截器 toast，这里只避免未捕获异常导致白屏
  }
})
</script>

<style scoped>
.page-order {
  padding-top: 130px;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  padding-bottom: 100px;
}

.viewport {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.empty-tip {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
}
.empty-tip .empty-msg {
  margin: 0 0 20px;
  font-size: 15px;
  color: #666;
}
.empty-tip .btn-login {
  padding: 10px 32px;
  font-size: 15px;
  color: #fff;
  background: #07c160;
  border: none;
  border-radius: 8px;
}

.categories {
  flex: 1;
  min-height: 200px;
  display: flex;
}

.primary {
  width: 24vw;
  max-width: 96px;
  flex-shrink: 0;
  background: #f6f6f6;
  overflow-y: auto;
}

.primary .item {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  color: #595c63;
  border-bottom: 1px solid #e3e4e7;
}

.primary .item.active {
  background: #fff;
  border-bottom-color: transparent;
}

.secondary {
  flex: 1;
  background: #fff;
  overflow-y: auto;
}

.section {
  padding: 12px 0;
  display: flex;
  flex-wrap: wrap;
}

.dish {
  width: 100%;
  display: flex;
  padding: 10px 16px;
  gap: 12px;
  box-sizing: border-box;
  cursor: pointer;
}

.dish .image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
}

.dishinfo {
  flex: 1;
  min-width: 0;
  position: relative;
  padding-right: 80px;
}

.ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dishinfo .name { font-size: 14px; color: #222; margin-bottom: 4px; }
.dishinfo .detail { font-size: 12px; color: #333; margin-bottom: 4px; }
.dishinfo .price { font-size: 13px; color: #cf4444; }

.choosenorm {
  position: absolute;
  right: 0;
  bottom: 0;
  font-size: 12px;
  color: #10B981;
  padding: 4px 10px;
  border: 1px solid #10B981;
  border-radius: 12px;
}

.sub_add {
  position: absolute;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  gap: 6px;
}

.sub_add .sub, .sub_add .add { width: 22px; height: 22px; cursor: pointer; }
.dish_number { font-size: 13px; min-width: 18px; text-align: center; }

/* 口味弹窗 */
.dialog {
  position: fixed;
  inset: 0;
  z-index: 1000;
  background: rgba(0,0,0,0.6);
}

.flavor_pop {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 70%;
  max-height: 40vh;
  padding: 20px;
  border-radius: 12px;
  background: #fff;
}

.flavor_pop .title { font-size: 16px; font-weight: bold; margin-bottom: 12px; }
.scroll { max-height: 28vh; overflow-y: auto; }
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
  padding: 10px 20px;
  background: #10B981;
  color: #fff;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  float: right;
}
.close_dialog {
  position: absolute;
  top: 75%;
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

/* 底部购物车 */
.footer_order_buttom {
  position: fixed;
  bottom: 24px;
  left: 16px;
  right: 16px;
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
  width: 100px;
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
  width: 100px;
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

/* 购物车弹层 */
.pop_mask {
  position: fixed;
  inset: 0;
  z-index: 500;
  background: rgba(0,0,0,0.4);
}
.cart_pop {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  max-height: 60vh;
  background: #fff;
  border-radius: 16px 16px 0 0;
  padding: 16px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.top_title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}
.tit { font-size: 18px; font-weight: bold; }
.clear { font-size: 14px; color: #999; display: flex; align-items: center; gap: 4px; cursor: pointer; }
.clear_icon { width: 16px; height: 16px; }
.card_order_list {
  flex: 1;
  overflow-y: auto;
  padding-top: 12px;
}
.type_item {
  display: flex;
  gap: 12px;
  padding-bottom: 16px;
  margin-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}
.dish_img_url { width: 64px; height: 64px; border-radius: 8px; object-fit: cover; }
.dish_info { flex: 1; min-width: 0; position: relative; padding-bottom: 32px; }
.dish_name { font-size: 15px; font-weight: 600; color: #333; }
.dish_price { font-size: 15px; color: #e94e3c; }
.dish_flavor { font-size: 12px; color: #666; margin-top: 4px; }
.dish_active {
  position: absolute;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}
.dish_sub, .dish_add { width: 28px; height: 28px; cursor: pointer; }
.dish_number { font-size: 14px; min-width: 20px; text-align: center; }
.seize_seat { height: 40px; }

/* 打烊遮罩 */
.close {
  position: fixed;
  inset: 0;
  z-index: 1001;
  background: rgba(0,0,0,0.2);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding-bottom: 120px;
}
.close .text {
  padding: 16px 40px;
  background: rgba(0,0,0,0.5);
  color: #fff;
  font-size: 16px;
  border-radius: 8px;
}
</style>
