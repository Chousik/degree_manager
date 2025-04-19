<template>
  <div class="flex h-screen bg-[#C9D7FF]">
    <sideBar />
    <section class="w-4/5  p-4 overflow-y-auto">
      <input
          v-model="searchQuery"
          type="text"
          placeholder="Поиск по работам"
          class="w-full p-2 rounded-lg mb-4 focus:outline-none focus:ring-2 focus:ring-indigo-500"
      />
      <h3 class="text-sm text-indigo-800 underline cursor-pointer mb-2">Фильтровать</h3>

      <div
          v-for="(work, index) in filteredWorks"
          :key="index"
            class="bg-[#7862BD3B] p-4 rounded-lg mb-2"
      >
        <p class="font-medium mb-2">{{ work.title }}</p>
        <p class="text-sm mb-1">Автор: {{ work.author }}, {{ work.year }}</p>
        <p class="text-sm">Научный руководитель: {{ work.supervisor }}</p>
        <div class="mt-2 flex gap-2">
          <router-link
              to="/preview"
              class="px-4 py-1 bg-gray-200 rounded hover:bg-gray-300"
          >
            Просмотр
          </router-link>
          <button class="px-4 py-1 bg-gray-700 text-white rounded hover:bg-black">
            Скачать
          </button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import sideBar from "../components/SideBar.vue"
const works = ref([
  {
    title: 'Веб приложение для помощи сотрудникам кафедр в систематизации информации о выпускных квалификационных работах обучающихся',
    author: 'Мироненко Дарья Андреевна',
    year: 2025,
    supervisor: 'Путиница Елена Валентиновна',
  },
  {
    title: 'Разработка параметрической управляющей программы...',
    author: '',
    year: '',
    supervisor: '',
  },
  {
    title: 'Разработка системы управления',
    author: '',
    year: '',
    supervisor: '',
  },
]);
const searchQuery = ref('')
const filteredWorks = computed(() =>
    works.value.filter(work =>
        work.title.toLowerCase().includes(searchQuery.value.toLowerCase())
    )
)
</script>