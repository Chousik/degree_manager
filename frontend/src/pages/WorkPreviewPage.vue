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
            :href="pdfUrl"
            download
            class="bg-indigo-600 text-white text-center text-lg sm:text-xl lg:text-2xl py-3 px-4 rounded hover:bg-indigo-700 transition"
        >
          Скачать
        </a>
      </div>
    </aside>

    <!-- Основная часть с PDF -->
    <main class="w-full lg:w-3/4 bg-white flex items-center justify-center overflow-hidden">
      <iframe
          :src="pdfUrl"
          class="w-full h-[80vh] lg:h-full border-none"
          frameborder="0"
      ></iframe>
    </main>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'
import { useWorksStore } from '@/store/auth'
import { ref } from 'vue' // Подключаем ref для создания реактивного состояния

const route = useRoute()
const worksStore = useWorksStore()

// Получаем название документа из параметров маршрута
const fileName = route.params.title
console.log('fileName:', fileName)

// Делаем отладку для вывода всех работ
console.log('worksStore.works:', worksStore.works)

// Ищем работу по переданному названию
const work = worksStore.works.find(w => decodeURIComponent(w.link) === fileName)
console.log('work:', work)

// Если работа найдена, создаем URL для PDF
const pdfUrl = work ? `/diplomas/${work.link}.pdf` : ''

// Создаем реактивное состояние для проверки на антиплагиат
const plagiarismCheckResult = ref(null)

// Функция для имитации проверки на антиплагиат
const checkPlagiarism = () => {
  // Задержка 5 секунд (5000 миллисекунд)
  setTimeout(() => {
    // Имитируем получение результата уникальности из объекта work
    plagiarismCheckResult.value = work ? work.unique : 'Неизвестно'
    console.log(plagiarismCheckResult.value)
  }, 4000)
}
</script>

<style scoped>
/* Дополнительные стили для кнопки и результата */
</style>
