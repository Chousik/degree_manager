import { defineStore } from 'pinia'
import axios from 'axios'

export const useDataStore = defineStore('data', {
    state: () => ({
        teachers: [],
        users: [],
        loading: false,
        error: null,
        token: null
    }),

    actions: {
        async fetchTeachers() {
            try {
                const response = await axios.get('http://localhost:8080/teachers', {
                    headers
                })
                this.teachers = response.data
            } catch (err) {
                this.error = 'Ошибка при получении преподавателей'
                console.error(err)
            } finally {
            }
        },

        async fetchUsers() {
            try {
                this.loading = true
                const response = await axios.get('http://localhost:8080/users', {
                    withCredentials: true
                })
                this.users = response.data
            } catch (err) {
                this.error = 'Ошибка при получении пользователей'
                console.error(err)
            } finally {
                this.loading = false
            }
        }
    }
})
