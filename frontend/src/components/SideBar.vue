<script setup>
const usname = "Елена Путиница"
</script>

<template>
  <aside class="w-1/5 bg-white p-4 rounded-r-2xl shadow-2xl">
    <h2 class="text-4xl decorat mb-2 ">МЕНЮ</h2>
    <hr class="border-2 border-[#757575] rounded-full">
    <nav>
      <ul>
        <li class="mb-4 flex items-center space-x-2 pt-10">
          <img src="../icons/folder-up.png" alt="icon" class="h-16">
          <router-link to="/upload" class="hover:underline text-4xl">Загрузить работу</router-link>
        </li>
        <li class="mb-4 flex items-center space-x-2 pt-5">
          <img src="../icons/File_dock.png" alt="icon" class="h-16">
          <router-link to="/works" class="hover:underline text-4xl">Список работ</router-link>
        </li>
      </ul>
      <div class=" flex items-center space-x-2 absolute bottom-0 left-0 shadow-inner rounded-br-2xl w-1/5">
        <p class="pt-5 text-4xl mt-auto pb-5 px-5">{{usname}}</p>
        <router-link to="/"><img src="../icons/exit.png" alt="icon" class="h-12 px-3"></router-link>
      </div>
    </nav>
  </aside>
</template>