import { defineStore } from 'pinia';

export const useWorksStore = defineStore('works', {
    state: () => ({
        works: [
            {
                title: 'Веб приложение для помощи сотрудникам кафедр в систематизации информации о выпускных квалификационных работах обучающихся',
                author: 'Мироненко Дарья Андреевна',
                year: 2025,
                supervisor: 'Путинцева Елена Валентиновна',
                progress: 100,
                status: 'На проверке',
                link:  encodeURIComponent('ДипломМироненко'),
                unique: 77
            },
            {
                title: 'Разработка системы управления уровнем освящения производства на базе\n' +
                    'программируемого логического контроллера',
                author: 'Грибанов Александр Дмитриевич',
                year: 2025,
                supervisor: 'Никишечкин Анатолий Петрович',
                progress: 75,
                status: 'Проверено',
                link: encodeURIComponent('ДипломГрибанов'),
                unique: 71
            },
            {
                title: 'Разработка автоматизированной системы документооборота при регистрации\n' +
                    'объектов интеллектуальной собственности',
                author: 'Санталов Михаил Дмитриевич',
                year: 2025,
                supervisor: 'Евстафиева Светлана Владимировна',
                progress: 75,
                status: 'В работе',
                link: encodeURIComponent('ДипломСанталов'),
                unique: 68
            },
            {
                title: 'Разработка системы управления теплицей на базе\n' +
                    'программируемого логического контроллера',
                author: 'Савилов Игорь Олегович',
                year: 2025,
                supervisor: 'Евстафиева Светлана Владимировна',
                progress: 75,
                status: 'В работе',
                link: encodeURIComponent('ДипломСавилов'),
                unique: 80
            }
        ]
    }),
    actions: {
        addWork(work) {
            this.works.push(work);
        }
    }
});
