import {defineStore} from 'pinia'
import axios from 'axios'

export const useDataStore = defineStore('data', {
    state: () => ({
        teachers: [],
        users: [],
        error: null,
    }),

    actions: {
        async fetchTeachers(tok) {
            try {
                const response = await axios.get('http://localhost:8085/teacher', {
                    headers: {
                        Authorization: `Bearer ${tok}`
                    },
                });

                this.teachers = response.data;
                console.log(this.teachers)
            } catch (err) {
                this.error = 'Ошибка при получении учителей';
                console.error(err);
            }
        },

        async fetchUsers(tok) {
            try {
                const response = await axios.get('http://localhost:8071/api/users', {
                    headers: {
                        Authorization: `Bearer ${tok}`
                    },
                });

                this.users = response.data;
                console.log(this.users)
            } catch (err) {
                this.error = 'Ошибка при получении пользователей';
                console.error(err);
            }
        }

    }
})
