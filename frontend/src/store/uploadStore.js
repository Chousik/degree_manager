import {defineStore} from 'pinia'
import {authFetch} from "@/utills/authFetch.js";

export const useUploadStore = defineStore('upload', {
    state: () => ({
        teachers: [],
        students: [],
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
                    id: t.id
                }));
            } catch (err) {
                this.error = 'Ошибка при получении учителей';
                console.error(err);
            }
        },
        async fetchStudents(tok) {
            try {
                const response = await authFetch('http://localhost:8085/student', {
                    method: "GET",
                    headers: {
                        Authorization: `Bearer ${tok}`
                    },
                });
                const data = await response.json();
                this.students = data.map(s => ({
                    ...s,
                    full_name: `${s.surname} ${s.name} ${s.middleName}`.trim(),
                    uuid: s.uuid
                }));
            } catch (err) {
                this.error = 'Ошибка при получении студентов';
                console.error(err);
            }
        }
    }
})
