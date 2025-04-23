<template>
  <div class="min-h-screen bg-[#C6D0FF] p-6 flex flex-col relative">
    <!-- Header -->
    <header class="flex justify-between items-center mb-6">
      <!-- Логотип -->
      <img src="/stankin_logo_main_color_ru_rgb_01_1x.png" alt="Станкин" class="h-20" />

      <!-- Кнопка бургер-меню -->
      <div class="relative z-50">
        <button @click="toggleMenu" class="w-12 h-12 flex flex-col justify-center items-center space-y-1">
          <span
              class="w-8 h-1 bg-gray-700 rounded transition-transform duration-300"
              :class="menuOpen ? 'rotate-45 translate-y-2' : ''"
          ></span>
          <span
              class="w-8 h-1 bg-gray-700 rounded transition-opacity duration-300"
              :class="menuOpen ? 'opacity-0' : ''"
          ></span>
          <span
              class="w-8 h-1 bg-gray-700 rounded transition-transform duration-300"
              :class="menuOpen ? '-rotate-45 -translate-y-2' : ''"
          ></span>
        </button>

        <!-- Меню (по желанию можно раскрывать блок) -->
        <div
            v-if="menuOpen"
            class="absolute right-0 mt-2 bg-white rounded-lg shadow-lg p-4 space-y-2">
          <p>{{username}}</p>
          <a href="/login" class="block hover:underline">Выход</a>
        </div>
      </div>
    </header>

    <!-- Кнопка "Добавить пользователя" -->
    <div class="mb-6 text-right">
      <button class="bg-white rounded-full px-6 py-2 text-black font-medium shadow-md hover:bg-gray-100 transition">
        Добавить пользователя
      </button>
    </div>

    <!-- Поиск -->
    <div class="mb-6">
      <input
          type="text"
          v-model="searchQuery"
          placeholder="Поиск по преподавателям"
          class="w-full py-3 px-5 rounded-full shadow-md focus:outline-none focus:ring-2 focus:ring-indigo-400"
      />
    </div>

    <!-- Список преподавателей -->
    <div class="space-y-4">
      <div
          v-for="(teacher, index) in filteredTeachers"
          :key="index"
          class="bg-[#B3B8F5] p-4 rounded-2xl shadow-md"
      >
        <p class="text-lg font-medium">{{ teacher.name }}</p>
        <div class="mt-2 flex flex-wrap gap-2">
          <button class="bg-white px-4 py-1 rounded-full text-sm hover:bg-gray-200 transition">Изменить пароль</button>
          <button class="bg-white px-4 py-1 rounded-full text-sm hover:bg-gray-200 transition">Удалить аккаунт</button>
          <button class="bg-white px-4 py-1 rounded-full text-sm hover:bg-gray-200 transition">Сделать админом</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const username = "adminka"

const menuOpen = ref(false)
const toggleMenu = () => {
  menuOpen.value = !menuOpen.value
}

const searchQuery = ref('')
const teachers = ref([
  { name: 'Никишечкин Анатолий Петрович к.т.н., доцент' },
  { name: 'Червоннова Надежда Юрьевна ст. преп.' },
  { name: 'Путиницева Елена Валентиновна к.т.н., доцент' },
])

const filteredTeachers = computed(() => {
  return teachers.value.filter(t =>
      t.name.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})
</script>
