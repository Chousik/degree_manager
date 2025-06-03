<template>
  <div class="flex flex-col lg:flex-row min-h-screen">
    <!-- Левая панель с информацией -->
    <aside class="w-full lg:w-1/4 bg-[#f9f9fa] p-6 lg:rounded-l-lg shadow-md flex flex-col justify-between">
      <div>
        <h2 class="text-xl sm:text-2xl lg:text-2xl mb-4">
          {{ work ? work.title : 'Загрузка...' }}
        </h2>
        <p class="text-base sm:text-lg lg:text-xl mb-1">
          <b>Автор:</b> {{ work ? work.author : 'Загрузка...' }}
        </p>
        <p class="text-base sm:text-lg lg:text-xl mb-1">
          <b>Год:</b> {{ work ? work.year : 'Загрузка...' }}
        </p>
        <p class="text-base sm:text-lg lg:text-xl">
          <b>Научный руководитель:</b><br />
          {{ work ? work.supervisor : 'Загрузка...' }}
        </p>
        <div v-if="plagiarismCheckResult !== null" class="mt-4">
          <p class="text-lg sm:text-xl lg:text-2xl">
            <b>Результат проверки на антиплагиат:</b> {{ plagiarismCheckResult }}%
          </p>
        </div>
      </div>

      <!-- Кнопки действия -->
      <div class="flex flex-col sm:flex-row lg:flex-col gap-4 mt-8">
        <button
            @click="checkPlagiarism"
            class="bg-blue-600 text-white text-center text-lg sm:text-xl lg:text-2xl py-3 px-4 rounded hover:bg-blue-700 transition"
        >
          Проверка на антиплагиат
        </button>
        <router-link
            to="/works"
            class="bg-black text-white text-center text-lg sm:text-xl lg:text-2xl py-3 px-4 rounded hover:bg-gray-700 transition"
        >
          Назад
        </router-link>
        <a
            v-if="pdfUrl"
            :href="pdfUrl"
            download="diplom.pdf"
            class="bg-indigo-600 text-white text-center text-lg sm:text-xl lg:text-2xl py-3 px-4 rounded hover:bg-indigo-700 transition"
        >
          Скачать
        </a>

      </div>
    </aside>

    <!-- Основная часть с PDF -->
    <main class="w-full lg:w-3/4 bg-white flex items-center justify-center overflow-hidden">
      <iframe
          v-if="pdfUrl"
          :src="pdfUrl"
          class="w-full h-[80vh] lg:h-full border-none"
          frameborder="0"
      />
    </main>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'
import { useWorksStore } from '@/store/worksStore'
import {onMounted, ref} from 'vue'
import {useAuthStore} from "@/store/authStore.js";
import {authFetch} from "@/utills/authFetch.js";

const route = useRoute()
const worksStore = useWorksStore()
const auth = useAuthStore()
const fileName = route.params.title


const work = worksStore.works.find(w => decodeURIComponent(w.link) === fileName)


const pdfUrl = ref('')
const plagiarismCheckResult = ref(null)
const checkPlagiarism = () => {
  setTimeout(() => {
    plagiarismCheckResult.value = work ? work.uniqueCount : 'Неизвестно'
  }, 4000)
}

onMounted(async () => {
  if (work) {
    try {
      const response = await authFetch(`http://localhost:8084/work/download/${work.key}`, {
        method: 'GET',
        headers: {
          Authorization: `Bearer ${auth.accessToken}`,
        }
      })

      const blob = await response.blob()
      pdfUrl.value = URL.createObjectURL(blob)

    } catch (error) {
      console.error('Ошибка загрузки PDF для просмотра:', error)
    }
  }
})
</script>

<style scoped>
/* Дополнительные стили для кнопки и результата */
</style>
