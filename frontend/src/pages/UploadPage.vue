<template>
  <div class="min-h-screen bg-[#C9D7FF]">
    <main class=" flex flex-col lg:flex-row">
      <!-- Боковая панель -->
      <aside class="w-full h-5/6 lg:w-1/4 bg-white p-4 rounded-b-2xl lg:rounded-r-2xl shadow-2xl flex flex-col justify-between">
        <div>
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
          </nav>
        </div>

        <!-- Пользователь и выход -->
        <div class="flex justify-between items-center mt-6 pt-6 border-t border-gray-300">
          <p class="text-xl lg:text-2xl">{{ usname }}</p>
          <button @click="logout" class="bg-transparent border-none">
            <img src="../icons/exit.png" alt="icon" class="h-10 lg:h-12">
          </button>
        </div>
      </aside>

      <!-- Основной контент -->
      <section class="w-full lg:w-3/4 p-4 overflow-y-auto">
        <div class="bg-white rounded-2xl p-8 shadow-lg w-full max-w-3xl mx-auto">
          <h1 class="text-4xl text-center mb-8">ЗАГРУЗИТЬ РАБОТУ</h1>

          <!-- ФИО автора -->
          <div class="mb-6">
            <label class="block text-xl mb-2">ФИО АВТОРА</label>
            <div class="relative">
              <input
                  v-model="authorInput"
                  type="text"
                  placeholder="Начните вводить ФИО студента"
                  class="w-full p-3 border-2 rounded-lg"
                  @input="showAuthorSuggestions = true"
              />
              <ul v-if="showAuthorSuggestions && filteredAuthors.length"
                  class="absolute z-10 w-full bg-white border border-gray-300 rounded-lg mt-1 max-h-60 overflow-y-auto shadow-lg">
                <li
                    v-for="(author, index) in filteredAuthors"
                    :key="index"
                    @click="selectAuthor(author)"
                    class="p-3 hover:bg-indigo-100 cursor-pointer"
                >
                  {{ author }}
                </li>
              </ul>
            </div>
          </div>

          <!-- ФИО руководителя -->
          <div class="mb-6">
            <label class="block text-xl mb-2">ФИО НАУЧНОГО РУКОВОДИТЕЛЯ</label>
            <div class="relative">
              <input
                  v-model="supervisorInput"
                  type="text"
                  placeholder="Начните вводить ФИО научного руководителя"
                  class="w-full p-3 border-2 rounded-lg"
                  @input="showSupervisorSuggestions = true"
              />
              <ul v-if="showSupervisorSuggestions && filteredSupervisors.length"
                  class="absolute z-10 w-full bg-white border border-gray-300 rounded-lg mt-1 max-h-60 overflow-y-auto shadow-lg">
                <li
                    v-for="(supervisor, index) in filteredSupervisors"
                    :key="index"
                    @click="selectSupervisor(supervisor)"
                    class="p-3 hover:bg-indigo-100 cursor-pointer"
                >
                  {{ supervisor }}
                </li>
              </ul>
            </div>
          </div>

          <!-- Год написания -->
          <div class="mb-6">
            <label class="block text-xl mb-2">ГОД НАПИСАНИЯ</label>
            <input
                v-model="workYear"
                type="number"
                class="w-full p-3 border-2 rounded-lg"
                :max="currentYear"
            />
          </div>

          <!-- Загрузка файла -->
          <div class="mb-8">
            <label class="block text-xl font-medium mb-2">ФАЙЛ РАБОТЫ (PDF)</label>
            <div
                @dragover.prevent="dragOver = true"
                @dragleave="dragOver = false"
                @drop.prevent="handleFileDrop"
                :class="{
                'border-indigo-500 bg-indigo-50': dragOver,
                'border-gray-300': !dragOver
              }"
                class="border-2 border-dashed rounded-lg p-8 text-center cursor-pointer transition-colors"
                @click="triggerFileInput"
            >
              <input
                  ref="fileInput"
                  type="file"
                  accept=".pdf"
                  class="hidden"
                  @change="handleFileSelect"
              />
              <img src="../icons/folder-up.png" alt="Upload" class="h-16 mx-auto mb-4">
              <p v-if="!selectedFile">Перетащите файл сюда или кликните для выбора</p>
              <p v-else class="text-lg font-medium text-indigo-600">{{ selectedFile.name }}</p>
              <p class="text-sm text-gray-500 mt-2">Поддерживаются только PDF-файлы</p>
            </div>
          </div>

          <!-- Кнопка отправки -->
          <button
              @click="uploadWork"
              :disabled="!isFormValid"
              :class="{
              'bg-indigo-600 hover:bg-indigo-700': isFormValid,
              'bg-gray-400 cursor-not-allowed': !isFormValid
            }"
              class="w-full py-4 text-white text-xl font-bold rounded-lg transition-colors"
          >
            ЗАГРУЗИТЬ ДИПЛОМ
          </button>
        </div>
      </section>
    </main>
  </div>
</template>


<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const usname = "Путинцева Елена";
const currentYear = new Date().getFullYear();

const students = ref([
  "Иванов Иван Иванович",
  "Петров Петр Петрович",
  "Сидорова Анна Викторовна",
]);

const supervisors = ref([
  "Путинцева Елена Валентиновна",
  "Иванов Иван Иванович",
  "Петров Пётр Петрович",
]);

const authorInput = ref('');
const supervisorInput = ref('');
const workYear = ref(currentYear);
const selectedFile = ref(null);
const dragOver = ref(false);
const fileInput = ref(null);
const showAuthorSuggestions = ref(false);
const showSupervisorSuggestions = ref(false);

const filteredAuthors = computed(() => {
  return students.value.filter(student =>
      student.toLowerCase().includes(authorInput.value.toLowerCase())
  );
});

const filteredSupervisors = computed(() => {
  return supervisors.value.filter(supervisor =>
      supervisor.toLowerCase().includes(supervisorInput.value.toLowerCase())
  );
});

const isFormValid = computed(() => {
  return authorInput.value && supervisorInput.value && workYear.value && selectedFile.value;
});

const selectAuthor = (author) => {
  authorInput.value = author;
  showAuthorSuggestions.value = false;
};

const selectSupervisor = (supervisor) => {
  supervisorInput.value = supervisor;
  showSupervisorSuggestions.value = false;
};

const triggerFileInput = () => {
  fileInput.value.click();
};

const handleFileSelect = (event) => {
  const file = event.target.files[0];
  if (file && file.type === 'application/pdf') {
    selectedFile.value = file;
  } else {
    alert('Пожалуйста, выберите файл в формате PDF');
  }
};

const handleFileDrop = (event) => {
  dragOver.value = false;
  const file = event.dataTransfer.files[0];
  if (file && file.type === 'application/pdf') {
    selectedFile.value = file;
  } else {
    alert('Пожалуйста, перетащите файл в формате PDF');
  }
};

const uploadWork = () => {
  if (!isFormValid.value) return;

  console.log('Отправка данных:', {
    author: authorInput.value,
    supervisor: supervisorInput.value,
    year: workYear.value,
    file: selectedFile.value.name
  });

  alert('Работа успешно загружена!');
  // router.push('/works');
};

const logout = () => {
  localStorage.removeItem('access_token');
  localStorage.removeItem('refresh_token');
  router.push('/login');
};
</script>
