import { defineStore } from 'pinia';
import {authFetch} from "@/utills/authFetch.js";

export const useWorksStore = defineStore('works', {
    state: () => ({
        works: [],
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

                const data = await response.json()
                console.log(data)
                this.works = data.map(work => ({
                    title: work.title,
                    author: work.author,
                    year: work.year,
                    supervisor: work.supervisor,
                    progress: work.completion || 0,
                    link: encodeURIComponent(work.title),
                    uuid: work.uuid,
                    key: work.key
                }));
            } catch (error) {
                console.error('Ошибка при получении работ:', error);
            }
        },
    }
});
