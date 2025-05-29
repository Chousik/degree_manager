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

                this.teachers = response.data.map(t => ({
                    ...t,
                    full_name: `${t.surname} ${t.name} ${t.middleName}`.trim(),
                    academic_status: t.academicStatus
                }));
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
            } catch (err) {
                this.error = 'Ошибка при получении пользователей';
                console.error(err);
            }
        }
    }
})
