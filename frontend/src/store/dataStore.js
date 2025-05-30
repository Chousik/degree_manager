import {defineStore} from 'pinia'
import {authFetch} from "@/store/authFetch.js";

export const useDataStore = defineStore('data', {
    state: () => ({
        teachers: [],
        users: [],
        error: null,
    }),

    actions: {
        async fetchTeachers(tok) {
            try {
                const response = await authFetch('http://localhost:8085/teacher', {
                    method: "GET",
                    headers: {
                        Authorization: `Bearer ${tok}`
                    },
                });
                const data = await response.json();
                this.teachers = data.map(t => ({
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
                const response = await authFetch('http://localhost:8071/api/users', {
                    method: "GET",
                    headers: {
                        Authorization: `Bearer ${tok}`
                    },
                });
                const data = await response.json();
                this.users = data;
            } catch (err) {
                this.error = 'Ошибка при получении пользователей';
                console.error(err);
            }
        }
    }
})
