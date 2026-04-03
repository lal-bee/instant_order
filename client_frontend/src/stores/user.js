import { defineStore } from 'pinia'
import { ref } from 'vue'

const TOKEN_KEY = 'token'
const PROFILE_KEY = 'profile'

export const useUserStore = defineStore('user', () => {
  const profile = ref(null)

  function setProfile(val) {
    profile.value = val
    if (val && val.token) localStorage.setItem(TOKEN_KEY, val.token)
    if (val) localStorage.setItem(PROFILE_KEY, JSON.stringify(val))
  }

  function clearProfile() {
    profile.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(PROFILE_KEY)
  }

  return { profile, setProfile, clearProfile }
})
