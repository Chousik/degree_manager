<script setup>
import {ref, computed, watchEffect} from 'vue'
import {useRouter} from "vue-router";

const usname = "Путинцева Елена"
const currentYear = new Date().getFullYear()
const yearOptions = Array.from({length: 6}, (_, i) => currentYear - i)
const selectedYear = ref('')

const searchQuery = ref('')
const filterSupervisorInput = ref('')
const filterAuthorInput = ref('')
const selectedSupervisor = ref('')
const selectedAuthor = ref('')
const showSupervisorSuggestions = ref(false)
const showAuthorSuggestions = ref(false)
const router = useRouter();

const works = ref([
  {
    title: 'Веб приложение для помощи сотрудникам кафедр в систематизации информации о выпускных квалификационных работах обучающихся',
    author: 'Мироненко Дарья Андреевна',
    year: 2025,
    supervisor: 'Путинцева Елена Валентиновна',
  },
  {
    title: 'Разработка параметрической управляющей программы...',
    author: 'Иванов Сергей Петрович',
    year: 2023,
    supervisor: 'Иванов Иван Иванович',
  },
  {
    title: 'Разработка системы управления',
    author: 'Сидорова Анна Викторовна',
    year: 2021,
    supervisor: 'Петров Пётр Петрович',
  },
])

const uniqueAuthors = computed(() => {
  const names = works.value.map(w => w.author).filter(name => name)
  return names.length ? [...new Set(names)] : []  // Возвращаем пустой массив, если нет авторов
})

const uniqueSupervisors = computed(() => {
  const names = works.value.map(w => w.supervisor).filter(name => name)
  return names.length ? [...new Set(names)] : []  // Возвращаем пустой массив, если нет научных руководителей
})

const filteredAuthors = computed(() => {
  return uniqueAuthors.value.filter(author =>
      author.toLowerCase().includes(filterAuthorInput.value.toLowerCase())
  )
})

const filteredSupervisors = computed(() => {
  return uniqueSupervisors.value.filter(supervisor =>
      supervisor.toLowerCase().includes(filterSupervisorInput.value.toLowerCase())
  )
})

const filteredWorks = computed(() => {
  return works.value.filter(work => {
    const matchesTitle = work.title.toLowerCase().includes(searchQuery.value.toLowerCase())
    const matchesYear = selectedYear.value ? work.year === parseInt(selectedYear.value) : true
    const matchesSupervisor = selectedSupervisor.value
        ? work.supervisor.toLowerCase().includes(selectedSupervisor.value.toLowerCase())
        : true
    const matchesAuthor = selectedAuthor.value
        ? work.author.toLowerCase().includes(selectedAuthor.value.toLowerCase())
        : true

    return matchesTitle && matchesYear && matchesSupervisor && matchesAuthor
  })
})

function selectAuthor(author) {
  selectedAuthor.value = author
  filterAuthorInput.value = author
  showAuthorSuggestions.value = false
}

function selectSupervisor(name) {
  selectedSupervisor.value = name
  filterSupervisorInput.value = name
  showSupervisorSuggestions.value = false
}

function resetFilters() {
  searchQuery.value = ''
  selectedYear.value = ''
  selectedSupervisor.value = ''
  selectedAuthor.value = ''
  filterSupervisorInput.value = ''
  filterAuthorInput.value = ''
}

function handleClickOutside(event) {
  if (
      !event.target.closest('.relative') && // Фильтры по автору и научному руководителю
      !event.target.closest('input') // Поиск по названию или году
  ) {
    showSupervisorSuggestions.value = false
    showAuthorSuggestions.value = false
  }
}

function logout() {
  localStorage.removeItem('access_token');
  localStorage.removeItem('refresh_token');
  router.push('/login')
}
</script>

<template>
  <div class="flex h-screen bg-[#C9D7FF]" @click="handleClickOutside">
    <aside class="w-1/5 bg-white p-4 rounded-r-2xl shadow-2xl">
      <h2 class="text-4xl decorat mb-2">МЕНЮ</h2>
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
        <div class="mt-10">
          <hr class="border-1 border-gray-600 pb-4">
          <h3 class="text-2xl font-semibold mb-4 pb-1">Фильтры</h3>
          <div class="mb-4">
            <label for="aut" class="text-xl ">Автор</label>
            <input
                id="aut"
                v-model="filterAuthorInput"
                type="text"
                placeholder="Фильтр по автору"
                class="border-2  w-full p-2 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
                @input="showAuthorSuggestions = true"
            />
            <ul
                v-if="showAuthorSuggestions && filteredAuthors.length"
                class="absolute z-10  bg-white border border-gray-300 rounded mt-1 max-h-40 overflow-y-auto"
            >
              <li
                  v-for="(author, index) in filteredAuthors"
                  :key="index"
                  @click="selectAuthor(author)"
                  class="p-2 hover:bg-indigo-100 cursor-pointer"
              >
                {{ author }}
              </li>
            </ul>

          </div>
          <div class="mb-4">
            <label for="supervisor" class="text-xl ">Научный руководитель</label>
            <input
                id="supervisor"
                v-model="filterSupervisorInput"
                type="text"
                placeholder="Фильтр по научному руководителю"
                class="border-2 w-full p-2 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
                @input="showSupervisorSuggestions = true"
            />
            <ul
                v-if="showSupervisorSuggestions && filteredSupervisors.length"
                class="absolute bg-white border max-h-40 overflow-y-auto rounded-lg shadow-md z-10"
            >
              <li
                  v-for="(supervisor, index) in filteredSupervisors"
                  :key="index"
                  @click="selectSupervisor(supervisor)"
                  class="px-4 py-2 hover:bg-indigo-100 cursor-pointer"
              >
                {{ supervisor }}
              </li>
            </ul>
          </div>

          <div class="mb-4">
            <label for="year" class="text-xl">Год</label>
            <select v-model="selectedYear" id="year" class="w-full p-2 border-2 rounded-md">
              <option value="">Все годы</option>
              <option v-for="year in yearOptions" :key="year" :value="year">{{ year }}</option>
            </select>
          </div>

          <button
              class="mb-4 px-4 py-2 bg-gray-700 text-white rounded hover:bg-black"
              @click="resetFilters"
          >
            Сбросить фильтры
          </button>
        </div>

        <div class="flex items-center space-x-2 absolute bottom-0 left-0 shadow-inner rounded-br-2xl w-1/5">
          <p class="pt-5 text-4xl mt-auto pb-5 px-5">{{ usname }}</p>
          <button @click="logout" class="bg-transparent border-none">
            <img src="../icons/exit.png" alt="icon" class="h-12 px-3">
          </button>
        </div>
      </nav>
    </aside>

    <section class="w-4/5 p-4 overflow-y-auto">
      <input
          v-model="searchQuery"
          type="text"
          placeholder="Поиск по работам"
          class="w-full p-2 rounded-lg mb-4 focus:outline-none focus:ring-2 focus:ring-indigo-500"
      />

      <div v-for="(work, index) in filteredWorks" :key="index" class="bg-[#7862BD3B] p-4 rounded-lg mb-2">
        <p class="font-medium mb-2">{{ work.title }}</p>
        <p class="text-sm mb-1">Автор: {{ work.author }}, {{ work.year }}</p>
        <p class="text-sm">Научный руководитель: {{ work.supervisor }}</p>
        <div class="mt-2 flex gap-2">
          <router-link to="/preview" class="px-4 py-1 bg-gray-200 rounded hover:bg-gray-300">Просмотр</router-link>
          <button class="px-4 py-1 bg-gray-700 text-white rounded hover:bg-black">Скачать</button>
        </div>
      </div>
    </section>
  </div>
</template>