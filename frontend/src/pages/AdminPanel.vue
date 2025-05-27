<template>
  <div class="min-h-screen bg-[#C6D0FF] p-6 flex flex-col relative">
    <!-- Header -->
    <header class="flex justify-between items-center mb-6">
      <img src="/stankin_logo_main_color_ru_rgb_01_1x.png" alt="Станкин" class="h-20" />

      <!-- Меню -->
      <div class="relative z-50">
        <button @click="toggleMenu" class="w-12 h-12 flex flex-col justify-center items-center space-y-1">
          <span class="w-8 h-1 bg-gray-700 rounded transition-transform duration-300"
                :class="menuOpen ? 'rotate-45 translate-y-2' : ''"></span>
          <span class="w-8 h-1 bg-gray-700 rounded transition-opacity duration-300"
                :class="menuOpen ? 'opacity-0' : ''"></span>
          <span class="w-8 h-1 bg-gray-700 rounded transition-transform duration-300"
                :class="menuOpen ? '-rotate-45 -translate-y-2' : ''"></span>
        </button>

        <div v-if="menuOpen" class="absolute right-0 mt-2 bg-white rounded-lg shadow-lg p-4 space-y-2">
          <p>{{ username }}</p>
          <a href="/login" @click="logout" class="block hover:underline">Выход</a>
        </div>
      </div>
    </header>

    <div class="mb-6 text-right">
      <button
          @click="showAddForm = !showAddForm"
          class="bg-white rounded-full px-6 py-2 text-black font-medium shadow-md hover:bg-gray-100 transition"
      >
        {{ showAddForm ? 'Скрыть форму' : 'Добавить пользователя' }}
      </button>
    </div>

    <!-- Форма -->
    <div v-if="showAddForm" class="bg-white p-6 rounded-xl shadow-md mb-6">
      <h3 class="text-lg font-semibold mb-4">Добавление пользователя</h3>

      <div class="mb-4 relative">
        <label class="block mb-1">Преподаватель</label>
        <input
            v-model="teacherSearch"
            type="text"
            placeholder="Введите ФИО преподавателя"
            class="w-full border border-gray-300 rounded px-3 py-2"
            @focus="showSuggestions = true"
            @input="showSuggestions = true"
            @blur="() => setTimeout(() => showSuggestions.value = false, 150)"
        />
        <ul
            v-if="showSuggestions && filteredTeacherOptions.length"
            class="absolute z-10 bg-white border border-gray-300 rounded shadow-md mt-1 w-full max-h-40 overflow-y-auto"
        >
          <li
              v-for="teacher in filteredTeacherOptions"
              :key="teacher.id"
              @mousedown.prevent="selectTeacher(teacher)"
              class="px-3 py-2 hover:bg-indigo-100 cursor-pointer"
          >
            {{ teacher.full_name }} — {{ teacher.academic_status }}
          </li>
        </ul>
      </div>

      <div class="mb-4">
        <label class="block mb-1">Логин</label>
        <input v-model="newLogin" type="text" class="w-full border border-gray-300 rounded px-3 py-2"
               placeholder="Введите логин" />
      </div>

      <div class="mb-4">
        <label class="block mb-1">Пароль</label>
        <input v-model="newPassword" type="password" class="w-full border border-gray-300 rounded px-3 py-2"
               placeholder="Введите пароль" />
      </div>

      <div v-if="addUserError" class="text-red-600 text-sm mb-2">{{ addUserError }}</div>

      <button @click="addUser" class="bg-indigo-500 text-white px-4 py-2 rounded hover:bg-indigo-600 transition">
        Добавить
      </button>
    </div>

    <!-- Поиск -->
    <div class="mb-6">
      <input
          type="text"
          v-model="searchQuery"
          placeholder="Поиск по пользователям"
          class="w-full py-3 px-5 rounded-full shadow-md focus:outline-none focus:ring-2 focus:ring-indigo-400"
      />
    </div>

    <!-- Список пользователей -->
    <div class="space-y-4">
      <div
          v-for="(user, index) in filteredUsers"
          :key="index"
          class="bg-[#B3B8F5] p-4 rounded-2xl shadow-md"
      >
        <p class="text-lg font-medium">
           {{ user.teacher }} <span v-if="user.isAdmin" class="text-green-500">(Admin)</span>
        </p>
        <div class="mt-2 flex flex-wrap gap-2">
          <button @click="changePassword(user)" class="bg-white px-4 py-1 rounded-full text-sm hover:bg-gray-200 transition">
            Изменить пароль
          </button>
          <button @click="deleteUser(index)" class="bg-white px-4 py-1 rounded-full text-sm hover:bg-gray-200 transition">
            Удалить аккаунт
          </button>
          <button @click="makeAdmin(user)" class="bg-white px-4 py-1 rounded-full text-sm hover:bg-gray-200 transition">
            Сделать админом
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { useDataStore } from '@/store/dataStore'

const authStore = useAuthStore()
const dataStore = useDataStore()
const router = useRouter()

const username = authStore.userInfo.sub

// Меню
const menuOpen = ref(false)
const toggleMenu = () => (menuOpen.value = !menuOpen.value)
function logout() {
  authStore.clearAuth()
  router.push('/login')
}

// Поиск пользователей
const searchQuery = ref('')
const filteredUsers = computed(() =>
    dataStore.users.filter(u =>
        u.teacher.toLowerCase().includes(searchQuery.value.toLowerCase())
    )
)

const showAddForm = ref(false)
const teacherSearch = ref('')
const selectedTeacher = ref(null)
const newLogin = ref('')
const newPassword = ref('')
const addUserError = ref('')
const showSuggestions = ref(false)

const filteredTeacherOptions = computed(() =>
    dataStore.teachers.filter(t =>
        `${t.full_name} ${t.academic_status}`.toLowerCase().includes(teacherSearch.value.toLowerCase())
    )
)

function selectTeacher(teacher) {
  selectedTeacher.value = teacher
  teacherSearch.value = teacher.full_name
  showSuggestions.value = false
}

function addUser() {
  if (!newLogin.value || !newPassword.value || !selectedTeacher.value) {
    addUserError.value = 'Заполните все поля'
    return
  }

  const newUser = {
    name: newLogin.value,
    password: newPassword.value,
    teacher: selectedTeacher.value.full_name,
    isAdmin: false
  }

  dataStore.users.push(newUser)

  addUserError.value = ''
  alert('Пользователь добавлен')
  showAddForm.value = false
  newLogin.value = ''
  newPassword.value = ''
  selectedTeacher.value = null
  teacherSearch.value = ''
}

function changePassword(user) {
  const newPass = prompt('Введите новый пароль для пользователя', user.password)
  if (newPass) {
    user.password = newPass
    alert('Пароль изменен')
  }
}

function deleteUser(index) {
  const confirmDelete = confirm('Вы уверены, что хотите удалить этого пользователя?')
  if (confirmDelete) {
    dataStore.users.splice(index, 1)
    alert('Пользователь удален')
  }
}

function makeAdmin(user) {
  user.isAdmin = !user.isAdmin
  alert(user.isAdmin ? 'Пользователь стал администратором' : 'Пользователь больше не администратор')
}

onMounted(() => {
  dataStore.fetchUsers()
  dataStore.fetchTeachers()
})
</script>
