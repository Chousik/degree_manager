<template>
  <div class="flex flex-col lg:flex-row min-h-screen">
    <!-- Левая панель с информацией -->
    <aside class="w-full lg:w-1/4 bg-[#f9f9fa] p-6 lg:rounded-l-lg shadow-md flex flex-col justify-between">
      <div>
        <h2 class="text-xl sm:text-2xl lg:text-3xl mb-4">
          Веб-приложение для помощи сотрудникам кафедр в систематизации информации...
        </h2>
        <p class="text-base sm:text-lg lg:text-xl mb-1">
          <b>Автор:</b> Мироненко Дарья Андреевна
        </p>
        <p class="text-base sm:text-lg lg:text-xl mb-1">
          <b>Год:</b> 2025
        </p>
        <p class="text-base sm:text-lg lg:text-xl">
          <b>Научный руководитель:</b><br />
          Путинцева Елена Валентиновна
        </p>
      </div>

      <!-- Кнопки действия -->
      <div class="flex flex-col sm:flex-row lg:flex-col gap-4 mt-8">
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
const pdfUrl = '/Силаев Захар ВычМат (1).pdf'
</script>