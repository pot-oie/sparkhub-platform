import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserDTO } from '@/api/types/user'
import type { LoginResponse } from '@/api/types/auth'

// 定义 Store
export const useUserStore = defineStore(
  'sparkhub-user',
  () => {
    // 1. 状态 (State)
    const token = ref<string | null>(null)
    // 直接存储 User DTO 对象
    const userInfo = ref<UserDTO | null>(null)
    // 未读通知计数
    const unreadCount = ref(0)

    // 2. Getters
    const isAuthenticated = () => {
      // 必须同时有 token 和 userInfo
      return token.value != null && userInfo.value != null
    }

    // 使 userRoles 能够健壮地处理 string[] 或 Role[]
    const userRoles = computed(() => {
      if (!userInfo.value?.roles) {
        return new Set<string>()
      }
      // (r.name || r) 会处理 r = {name: 'ROLE_USER'} 或 r = 'ROLE_USER'
      const roles = userInfo.value.roles.map((r: any) => r.name || r)
      return new Set<string>(roles)
    })
    // 角色 Getters (从 userInfo.roles 读取)
    const isUser = computed(() => userRoles.value.has('ROLE_USER'))
    const isCreator = computed(() => userRoles.value.has('ROLE_CREATOR'))
    const isAdmin = computed(() => userRoles.value.has('ROLE_ADMIN'))

    // 3. 操作 (Actions)
    // setLoginSession
    function setLoginSession(data: LoginResponse) {
      token.value = data.token
      userInfo.value = data.user
      unreadCount.value = 0
    }

    // clearLoginSession
    function clearLoginSession() {
      token.value = null
      userInfo.value = null
      unreadCount.value = 0
    }

    // 更新头像的 Action
    function setUserAvatar(newAvatarUrl: string) {
      if (userInfo.value) {
        userInfo.value.avatar = newAvatarUrl
      }
    }

    // 设置未读总数
    function setUnreadCount(count: number) {
      unreadCount.value = count
    }

    // 减少未读总数 (例如, 点击单条已读时)
    function decrementUnreadCount() {
      if (unreadCount.value > 0) {
        unreadCount.value--
      }
    }

    return {
      token,
      userInfo,
      unreadCount,
      setLoginSession,
      clearLoginSession,
      setUserAvatar,
      setUnreadCount,
      decrementUnreadCount,
      isAuthenticated,
      isUser,
      isCreator,
      isAdmin,
    }
  },
  {
    // 持久化所有状态 (token, userInfo, unreadCount)
    persist: true,
  },
)
