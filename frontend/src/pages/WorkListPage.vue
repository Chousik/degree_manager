<template>
  <div class="flex h-screen">
    <aside class="w-1/4 bg-white p-4 rounded-l-lg shadow-md">
      <h2 class="text-xl font-bold mb-6">МЕНЮ</h2>
      <nav>
        <ul>
          <li class="mb-4 flex items-center space-x-2">
            <span>📁</span>
            <router-link to="/upload" class="hover:underline">Загрузить работу</router-link>
          </li>
          <li class="mb-4 flex items-center space-x-2">
            <span>📄</span>
            <router-link to="/works" class="hover:underline">Список работ</router-link>
          </li>
        </ul>
      </nav>
    </aside>

    <section class="w-3/4 bg-[#cbd3fc] p-4 overflow-y-auto">
      <input
          type="text"
          placeholder="Поиск по работам"
          class="w-full p-2 rounded-lg mb-4 focus:outline-none focus:ring-2 focus:ring-indigo-500"
      />
      <h3 class="text-sm text-indigo-800 underline cursor-pointer mb-2">Фильтровать</h3>

      <div
          v-for="(work, index) in works"
          :key="index"
          class="bg-[#aeb5f5] p-4 rounded-lg mb-2"
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
const works = [
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
];
</script>