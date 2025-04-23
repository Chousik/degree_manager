<template>
  <div class="flex h-screen">
    <div class="w-1/5 p-4 bg-[#f9f9fa] rounded-l-lg">
      <h2 class="text-xl mb-4">
        Веб приложение для помощи сотрудникам кафедр в систематизации информации...
      </h2>
      <p class="text-xl mb-1"><b>Автор:</b> Мироненко Дарья Андреевна</p>
      <p class="text-xl mb-1"><b>Год:</b> 2025</p>
      <p class="text-xl"><b>Научный руководитель:</b><br />Путинцева Елена Валентиновна</p>
      <router-link
          to="/works"
          class="bg-black ml-2 text-white text-center text-4xl py-2 mb-4 w-1/12 absolute bottom-0 rounded hover:bg-gray-600 transition"
      >Назад</router-link>
      <a
          :href="pdfUrl"
          download
          class="bg-indigo-600 text-white text-center text-4xl px-4 py-2 mb-4 w-1/12 absolute bottom-0 left-56 rounded hover:bg-indigo-700 transition"
      >Скачать</a>
    </div>
    <div class="w-4/5 bg-white  flex flex-col items-center">
        <iframe
            :src="pdfUrl"
            class="w-full h-full"
            frameborder="0"
        ></iframe>
    </div>
  </div>
</template>

<script setup>
const pdfUrl = '/Силаев Захар ВычМат (1).pdf'
</script>