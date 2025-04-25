<template>
  <div class="min-h-screen bg-[#C9D7FF]" @click="handleClickOutside">
    <main class="flex h-screen flex-col lg:flex-row">
      <aside class="w-full lg:w-1/4 bg-white p-4 rounded-b-2xl lg:rounded-r-2xl shadow-2xl flex flex-col h-full">
        <div class="flex-grow">
          <h2 class="text-3xl lg:text-4xl mb-2">МЕНЮ</h2>
          <hr class="border-2 border-[#757575] rounded-full">
          <nav>
            <ul>
              <li class="mb-4 flex items-center space-x-2 pt-8">
                <img src="../icons/folder-up.png" alt="icon" class="h-12 lg:h-16">
                <router-link to="/upload" class="hover:underline text-xl lg:text-2xl">Загрузить работу</router-link>
              </li>
              <li class="mb-4 flex items-center space-x-2 pt-4">
                <img src="../icons/File_dock.png" alt="icon" class="h-12 lg:h-16">
                <router-link to="/works" class="hover:underline text-xl lg:text-2xl">Список работ</router-link>
              </li>
            </ul>

            <div class="mt-10">
              <hr class="border border-gray-600">
              <h3 class="text-xl lg:text-2xl font-semibold mb-4">Фильтры</h3>

              <!-- Автор -->
              <div class="mb-4 relative">
                <label for="aut" class="text-base lg:text-xl">Автор</label>
                <input
                    id="aut"
                    v-model="filterAuthorInput"
                    type="text"
                    placeholder="Фильтр по автору"
                    class="border-2 w-full p-2 rounded-lg focus:ring-2 focus:ring-indigo-500"
                    @input="showAuthorSuggestions = true"
                />
                <ul
                    v-if="showAuthorSuggestions && filteredAuthors.length"
                    class="absolute z-10 bg-white border border-gray-300 rounded mt-1 max-h-40 overflow-y-auto w-full"
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

              <!-- Научный руководитель -->
              <div class="mb-4 relative">
                <label for="supervisor" class="text-base lg:text-xl">Научный руководитель</label>
                <input
                    id="supervisor"
                    v-model="filterSupervisorInput"
                    type="text"
                    placeholder="Фильтр по научному руководителю"
                    class="border-2 w-full p-2 rounded-lg focus:ring-2 focus:ring-indigo-500"
                    @input="showSupervisorSuggestions = true"
                />
                <ul
                    v-if="showSupervisorSuggestions && filteredSupervisors.length"
                    class="absolute bg-white border max-h-40 overflow-y-auto rounded-lg shadow-md z-10 w-full"
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

              <!-- Год -->
              <div class="mb-4">
                <label for="year" class="text-base lg:text-xl">Год</label>
                <select v-model="selectedYear" id="year" class="w-full p-2 border-2 rounded-md">
                  <option value="">Все годы</option>
                  <option v-for="year in yearOptions" :key="year" :value="year">{{ year }}</option>
                </select>
              </div>

              <button
                  class="mb-4 px-4 py-2 bg-gray-700 text-white rounded hover:bg-black w-full"
                  @click="resetFilters"
              >
                Сбросить фильтры
              </button>
            </div>
          </nav>
        </div>

        <!-- Пользователь и выход -->
        <div class="flex justify-between items-center pt-4 border-t border-gray-300 mt-auto">
          <p class="text-xl lg:text-2xl">{{ usname }}</p>
          <button @click="logout" class="bg-transparent border-none">
            <img src="../icons/exit.png" alt="icon" class="h-10 lg:h-12">
          </button>
        </div>
      </aside>

      <section class="w-full lg:w-3/4 p-4 overflow-y-auto">
        <input
            v-model="searchQuery"
            type="text"
            placeholder="Поиск по работам"
            class="w-full p-2 rounded-lg mb-4 focus:ring-2 focus:ring-indigo-500"
        />

        <div
            v-for="(work, index) in filteredWorks"
            :key="index"
            class="bg-[#7862BD3B] p-4 rounded-lg mb-2 flex justify-between items-center"
        >
          <div class="w-3/4">
            <p class="font-medium mb-2">{{ work.title }}</p>
            <p class="text-sm mb-1">Автор: {{ work.author }}, {{ work.year }}</p>
            <p class="text-sm">Научный руководитель: {{ work.supervisor }}</p>
            <div class="mt-2 flex flex-wrap gap-2">
              <router-link :to="`/preview/${work.link}`" class="px-4 py-1 bg-gray-200 rounded hover:bg-gray-300">
                Просмотр
              </router-link>
              <a :href="`${work.link}.pdf`" download class="px-4 py-1 bg-black text-white rounded hover:bg-gray-300">
                Скачать
              </a>
            </div>
          </div>

          <!-- Индикатор прогресса - теперь справа -->
          <div class="relative w-12 h-12 md:w-16 md:h-16">
            <svg class="w-full h-full" viewBox="0 0 100 100">
              <circle
                  cx="50"
                  cy="50"
                  :r="circleRadius"
                  fill="none"
                  stroke="#e0e0e0"
                  stroke-width="8"
              />
              <circle
                  cx="50"
                  cy="50"
                  :r="circleRadius"
                  fill="none"
                  stroke="#90ee90"
                  stroke-width="8"
                  stroke-linecap="round"
                  :stroke-dasharray="circumference"
                  :stroke-dashoffset="getStrokeDashoffset(work.progress)"
                  transform="rotate(-90 50 50)"
              />
            </svg>
            <span
                class="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 text-xs md:text-sm font-bold text-black">
            {{ work.progress }}%
          </span>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useWorksStore } from '@/store/auth'

const worksStore = useWorksStore()
const searchQuery = ref('')
const selectedYear = ref('')
const selectedSupervisor = ref('')
const selectedAuthor = ref('')
const filterAuthorInput = ref('')
const filterSupervisorInput = ref('')
const showSupervisorSuggestions = ref(false)
const showAuthorSuggestions = ref(false)

const currentYear = new Date().getFullYear()
const yearOptions = Array.from({ length: 6 }, (_, i) => currentYear - i)

const circleRadius = 40
const circumference = 2 * Math.PI * circleRadius

const getStrokeDashoffset = (progress) => {
  return circumference - (progress / 100) * circumference
}

const uniqueAuthors = computed(() => {
  const names = worksStore.works.map(w => w.author).filter(name => name)
  return names.length ? [...new Set(names)] : []
})

const uniqueSupervisors = computed(() => {
  const names = worksStore.works.map(w => w.supervisor).filter(name => name)
  return names.length ? [...new Set(names)] : []
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
  return worksStore.works.filter(work => {
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
      !event.target.closest('.relative') &&
      !event.target.closest('input')
  ) {
    showSupervisorSuggestions.value = false
    showAuthorSuggestions.value = false
  }
}


</script>
