<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-tr from-[#a2aace] from-50% ...">
    <div class="bg-white p-8 rounded-lg shadow-md w-auto h-auto border border-gray-300">
      <div class="grid grid-cols-[50px_1fr] mb-6">
        <img src="/favicon.png" class="h-11 place-self-center">
        <h2 class="text-center place-self-start ml-4 mt-2
                  text-xl font-medium text-gray-800">Stankin Works</h2></div>
      <form @submit.prevent="handleSubmit">
        <div class="mb-4">
          <label class="block text-gray-700 mb-1">Login</label>
          <input
              v-model="login"
              type="text"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg
              focus:outline-none focus:border-indigo-400"
          />
        </div>
        <div class="mb-6">
          <label class="block text-gray-700 mb-1">Password</label>
          <input
              v-model="password"
              type="password"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg
              focus:outline-none focus:border-indigo-400"
          />
        </div>
        <button
            type="submit"
            class="w-full bg-black text-white py-2 rounded-lg hover:bg-gray-800 transition"
        >
          Sign In
        </button>
      </form>
      <div class="mt-4 text-center">
        <a href="#" class="text-sm text-black underline hover:text-gray-700">
          Forgot password?
        </a>
      </div>
    </div>
  </div>
</template>

<script >
import {ref} from 'vue'
export default {
  name: 'Login',
}
const login = ref('')
const password = ref('')

function handleSubmit() {
  console.log('Email:', login.value)
  console.log('Password:', password.value)
}
</script>

