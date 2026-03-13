import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useCountdownStore = defineStore('countdown', () => {
  const showM = ref(0)
  const showS = ref(0)
  const timer = ref(null)

  return { showM, showS, timer }
})
