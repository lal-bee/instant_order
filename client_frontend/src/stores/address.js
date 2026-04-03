import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAddressStore = defineStore('address', () => {
  const addressBackUrl = ref('')
  const defaultCook = ref('按餐量提供')

  function updateAddressBackUrl(url) {
    addressBackUrl.value = url
  }

  return { addressBackUrl, defaultCook, updateAddressBackUrl }
})
