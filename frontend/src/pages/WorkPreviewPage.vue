<template>
  <div class="flex h-screen">
    <div class="w-1/4 p-4 bg-gray-100 rounded-l-lg">
      <h2 class="text-sm mb-4">
        Веб приложение для помощи сотрудникам кафедр в систематизации информации...
      </h2>
      <p class="text-xs mb-1"><b>Автор:</b> Мироненко Дарья Андреевна</p>
      <p class="text-xs mb-1"><b>Год:</b> 2025</p>
      <p class="text-xs"><b>Научный руководитель:</b><br />Путиница Елена Валентиновна</p>
    </div>

    <div class="w-3/4 bg-white p-4 flex flex-col items-center">
      <img
          alt="Превью работы"
          class="max-w-full max-h-[90vh] border"
      />
      <div class="mt-4 flex gap-4">
        <button class="bg-black text-white px-4 py-2 rounded hover:bg-gray-800">Скачать</button>
        <router-link
            to="/works"
            class="bg-gray-200 px-4 py-2 rounded hover:bg-gray-400"
        >Назад</router-link>
        <button class="bg-indigo-500 text-white px-4 py-2 rounded hover:bg-indigo-700">
          Вперёд
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
</script>