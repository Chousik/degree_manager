import { defineStore } from 'pinia';
import {authFetch} from "@/utills/authFetch.js";

export const useWorksStore = defineStore('works', {
    state: () => ({
        works: []
    }),
    actions: {
        async fetchWorks(tok) {
            try {
                const response = await authFetch('http://localhost:8084/work', {
                    method: "GET",
                    headers: {
                        Authorization: `Bearer ${tok}`
                    },
                });
                console.log(response)
                this.works = response.data.map(work => ({
                    title: work.title,
                    author: work.authorFullName,
                    year: work.year,
                    supervisor: work.supervisorFullName, // адаптируй под своё DTO
                    progress: work.progress || 0,
                    status: work.status || 'Неизвестно',
                    link: encodeURIComponent(work.title), // или другой идентификатор
                    unique: work.id // или другой уникальный идентификатор
                }));
            } catch (error) {
                console.error('Ошибка при получении работ:', error);
            }
        },
        addWork(work) {
            this.works.push(work);
        }
    }
});
