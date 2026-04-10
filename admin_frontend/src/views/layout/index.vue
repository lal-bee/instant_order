<script setup lang="ts" name="layout">
import { useRouter, useRoute, type RouteRecordRaw } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserInfoStore } from '@/store'
import { ref, reactive, computed } from 'vue'
import { fixPwdAPI } from '@/api/employee'
import { getStatusAPI, fixStatusAPI } from '@/api/shop'
import { layoutChildrenRoutes } from '@/router'
import { filterRoutesByRole, normalizeRole } from '@/utils/permission'

// ------ data ------
const dialogFormVisible = ref(false)
const dialogStatusVisible = ref(false)
const formLabelWidth = '80px'
const isCollapse = ref(false)
const userInfoStore = useUserInfoStore()
type MenuItem = {
  title: string
  path: string
  icon?: string
  children?: MenuItem[]
}

const resolvePath = (parentPath: string, childPath: string) => {
  if (childPath.startsWith('/')) return childPath
  if (!parentPath || parentPath === '/') return `/${childPath}`.replace(/\/+/g, '/')
  return `${parentPath}/${childPath}`.replace(/\/+/g, '/')
}

const buildMenuTree = (routes: RouteRecordRaw[], parentPath = ''): MenuItem[] => {
  return routes.reduce<MenuItem[]>((acc, route) => {
    const meta = (route.meta || {}) as { title?: string; icon?: string; hidden?: boolean }
    if (meta.hidden || !route.path) return acc

    const fullPath = resolvePath(parentPath, route.path)
    const children = route.children?.length ? buildMenuTree(route.children, fullPath) : []

    if (!meta.title) {
      if (children.length) acc.push(...children)
      return acc
    }

    acc.push({
      title: meta.title,
      path: fullPath,
      icon: meta.icon,
      children: children.length ? children : undefined,
    })
    return acc
  }, [])
}

const menuList = computed(() => {
  const normalizedRole = normalizeRole(userInfoStore.userInfo?.role)
  const roleRoutes = filterRoutesByRole(layoutChildrenRoutes, normalizedRole)
  return buildMenuTree(roleRoutes)
})

const form = reactive({
  oldPwd: '',
  newPwd: '',
  rePwd: '',
})
const pwdRef = ref()
const status = ref(1)
const status_active = ref(1) // 单选框绑定的动态值

// 自定义校验规则: 两次密码是否一致
const samePwd = (rules: any, value: any, callback: any) => {
  if (value !== form.newPwd) {
    // 如果验证失败，则调用 回调函数时，指定一个 Error 对象。
    callback(new Error('两次输入的密码不一致!'))
  } else {
    // 如果验证成功，则直接调用 callback 回调函数即可。
    callback()
  }
}
const rules = { // 表单的规则检验对象
  oldPwd: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9]{1,10}$/,
      message: '原密码必须是1-10的大小写字母数字',
      trigger: 'blur'
    }
  ],
  newPwd: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { pattern: /^\S{6,15}$/, message: '新密码必须是6-15的非空字符', trigger: 'blur' }
  ],
  rePwd: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { pattern: /^\S{6,15}$/, message: '新密码必须是6-15的非空字符', trigger: 'blur' },
    { validator: samePwd, trigger: 'blur' }
  ]
}

// ------ method ------
const router = useRouter()
const route = useRoute()
const activeMenuPath = computed(() => {
  const routeMeta = route.meta as { activeMenu?: string }
  return routeMeta.activeMenu || route.path
})

// 初始化时获取营业状态
const init = async () => {
  const { data: res } = await getStatusAPI()
  status.value = res.data
  status_active.value = res.data
}
init()

// 关闭修改店铺状态对话框
const cancelStatus = () => {
  ElMessage({
    type: 'info',
    message: '已取消修改',
  })
  dialogStatusVisible.value = false
}
// 关闭修改密码对话框
const cancelForm = () => {
  ElMessage({
    type: 'info',
    message: '已取消修改',
  })
  dialogFormVisible.value = false
}
// 修改店铺状态
const fixStatus = async () => {
  const { data: res } = await fixStatusAPI(status_active.value)
  if (res.code != 0) return   // 修改失败信息会在相应拦截器中捕获并提示
  // 修改成功才改变status的值
  status.value = status_active.value
  ElMessage({
    type: 'success',
    message: '修改成功',
  })
  dialogStatusVisible.value = false
}
// 修改密码
const fixPwd = async () => {
  const valid = await pwdRef.value.validate()
  if (valid) {
    const submitForm = {
      oldPwd: form.oldPwd,
      newPwd: form.newPwd,
    }
    const { data: res } = await fixPwdAPI(submitForm)
    if (res.code != 0) return   // 密码错误信息会在相应拦截器中捕获并提示
    ElMessage({
      type: 'success',
      message: '修改成功',
    })
    dialogFormVisible.value = false
  } else {
    return false
  }
}

const quitFn = () => {
  // 为了让用户体验更好，来个确认提示框
  ElMessageBox.confirm(
    '走了，爱是会消失的吗?',
    '退出登录',
    {
      confirmButtonText: 'OK',
      cancelButtonText: 'Cancel',
      type: 'warning',
    }
  )
    .then(() => {
      ElMessage({
        type: 'success',
        message: '退出成功',
      })
      // 清除用户信息，包括token
      userInfoStore.userInfo = null
      router.push('/login')
    })
    .catch(() => {
      ElMessage({
        type: 'info',
        message: '已取消退出',
      })
    })
}

// refs
</script>

<template>
  <div class="common-layout">
    <el-dialog v-model="dialogStatusVisible" title="店铺状态设置" width="500">
      <el-radio-group v-model="status_active">
        <el-radio :value="1" size="large">营业中
          <span>当前餐厅处于营业状态，自动接收任何订单，可点击打烊进入店铺打烊状态。</span>
        </el-radio>
        <el-radio :value="0" size="large">打烊中
          <span>当前餐厅处于打烊状态，仅接受营业时间内的预定订单，可点击营业中手动恢复营业状态。</span>
        </el-radio>
      </el-radio-group>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancelStatus">取消</el-button>
          <el-button type="primary" @click="fixStatus">确定</el-button>
        </div>
      </template>
    </el-dialog>
    <el-dialog v-model="dialogFormVisible" title="修改密码" width="500">
      <el-form :model="form" :rules="rules" ref="pwdRef">
        <el-form-item prop="oldPwd" label="原密码" :label-width="formLabelWidth">
          <el-input v-model="form.oldPwd" autocomplete="off" />
        </el-form-item>
        <el-form-item prop="newPwd" label="新密码" :label-width="formLabelWidth">
          <el-input v-model="form.newPwd" autocomplete="off" />
        </el-form-item>
        <el-form-item prop="rePwd" label="确认密码" :label-width="formLabelWidth">
          <el-input v-model="form.rePwd" autocomplete="off" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancelForm">取消</el-button>
          <el-button type="primary" @click="fixPwd">确定</el-button>
        </div>
      </template>
    </el-dialog>
    <el-container>
      <el-header>
        <el-icon class="icon1" v-if="isCollapse">
          <Expand @click.stop="isCollapse = !isCollapse" />
        </el-icon>
        <el-icon class="icon1" v-else>
          <Fold @click.stop="isCollapse = !isCollapse" />
        </el-icon>
        <div class="status">{{ status == 1 ? '营业中' : "打烊中" }}</div>
        <el-dropdown style="float: right">
          <el-button type="primary">
            {{ userInfoStore.userInfo ? userInfoStore.userInfo.account : '未登录' }}
            <el-icon class="arrow-down-icon"><arrow-down /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="dialogFormVisible = true">修改密码</el-dropdown-item>
              <el-dropdown-item @click="quitFn">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button class="status-change" @click="dialogStatusVisible = true">店铺状态设置</el-button>
      </el-header>
      <el-container class="box1">
        <!-- 左侧导航菜单区域 -->
        <el-menu :style="{ width: isCollapse ? '85px' : '220px' }" :default-active="activeMenuPath" :collapse="isCollapse"
          background-color="#22aaee" text-color="#fff" unique-opened router>
          <!-- 加了router模式，就会在激活导航时以 :index 作为path进行路径跳转（nb!不用自己写路由了!） -->
          <!-- 根据不同情况选择menu-item/submenu进行遍历，所以外层套template遍历，里面组件做判断看是否该次遍历到自己 -->
          <template v-for="item in menuList" :key="item.path">
            <el-sub-menu v-if="item.children && item.children.length > 0" :index="item.path">
              <template #title>
                <el-icon v-if="item.icon">
                  <component :is="item.icon" />
                </el-icon>
                <span>{{ item.title }}</span>
              </template>
              <el-menu-item v-for="child in item.children" :key="child.path" :index="child.path">
                <el-icon v-if="child.icon">
                  <component :is="child.icon" />
                </el-icon>
                <span>{{ child.title }}</span>
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item v-else :index="item.path">
              <el-icon v-if="item.icon">
                <component :is="item.icon" />
              </el-icon>
              <span>{{ item.title }}</span>
            </el-menu-item>
          </template>
        </el-menu>

        <el-container class="mycontainer">
          <el-main>
            <router-view></router-view>
          </el-main>
          <el-footer>© 2024.5.21 hanye-take-out Tech and Fun. All rights reserved.</el-footer>
        </el-container>
      </el-container>
    </el-container>
  </div>
</template>

<style lang="less" scoped>
.common-layout {
  height: 100%;
  background-color: #eee;
}

.el-header {
  background-color: #00aaff;
  color: #ffffff;
  line-height: 60px;

  .icon1 {
    position: absolute;
    top: 18px;
    margin: 5px 10px 0 0;
  }

  .status {
    display: inline-block;
    align-items: center;
    vertical-align: top;
    line-height: 30px;
    margin: 15px 50px;
    padding: 0 10px;
    border-radius: 5px;
    background-color: #eebb00;
    color: #fff;
  }
}

.status-change {
  float: right;
  margin: 14px 20px;
  background-color: rgba(255, 255, 255, 0.3);
  border: none;
  color: #fff;
}

.el-dropdown .el-button {
  float: right;
  width: 80px;
  margin: 14px 20px;
  background-color: #eebb00;
  border-color: #eebb00;
  color: #fff;

  .arrow-down-icon {
    margin-left: 5px;
  }
}

.box1 {
  display: flex;
  height: 92vh;
}

.mycontainer {
  display: flex;
  flex: 6;
  flex-direction: column;
}

.el-main {
  flex: 1;
  background-color: #e9f5ff;
  color: #333;
}

.el-footer {
  background-color: #eee;
  font-size: 12px;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>



<style lang="less">
.el-dialog {
  border-radius: 2%;
}

.el-dialog__header {
  height: 60px;
  line-height: 60px;
  padding: 0 30px;
  font-weight: bold;
}

.el-dialog__body {
  padding: 10px 30px 30px;

  .el-radio,
  .el-radio__input {
    white-space: normal; // 设置其自动换行，别撑不下还挤在一起...
  }

  .el-radio__label {
    padding-top: 15px;
    color: #445588;
    font-weight: 700;

    span {
      display: block;
      line-height: 20px;
      padding: 12px 0 20px 0;
      color: #666;
      font-weight: normal;
    }
  }

  .el-radio-group {
    &>.is-checked {
      border: 1px solid #00aaff;
    }
  }

  .el-radio {
    width: 410px; // 本来想设置100%的，但是设置成固定值能去除el-radio-last-child的样式影响
    height: 100px;
    background: #fbfbfa;
    border: 1px solid #e5e4e4;
    border-radius: 4px;
    padding: 14px 22px;
    margin-top: 20px;
  }

}

.el-badge__content.is-fixed {
  top: 24px;
  right: 2px;
  width: 18px;
  height: 18px;
  font-size: 10px;
  line-height: 16px;
  font-size: 10px;
  border-radius: 50%;
  padding: 0;
}

.badgeW {
  .el-badge__content.is-fixed {
    width: 30px;
    border-radius: 20px;
  }
}

.el-menu {
  padding: 30px 0 0 0;
  background-color: #445566;
  width: 220px;
  overflow-x: hidden;
  border-right: none;
}

.el-menu-item {
  margin: 10px;
  padding-right: 12px;
  border-radius: 10px;
}

.el-sub-menu__title {
  margin: 10px;
  border-radius: 10px;
  padding-right: 12px;
}

.el-menu-item,
.el-sub-menu__title {
  display: flex;
  align-items: center;
  width: calc(100% - 20px);
  box-sizing: border-box;
  min-width: 0;
  overflow: hidden;
}

.el-menu-item > span,
.el-sub-menu__title > span {
  flex: 1 1 auto;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.el-menu-item.is-active {
  background-color: #22ccff;
  color: #fff;
}

.el-menu--collapse {
  width: 85px;
}

.el-menu--collapse .el-menu-item,
.el-menu--collapse .el-sub-menu__title {
  width: calc(100% - 20px);
}
</style>

