// stores/students.js
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useStudentsStore = defineStore('students', () => {
    const students = ref([])

    const fetchStudents = async () => {
        try {
            const data = {
                "students": [
                    // ... ваш JSON данных студентов ...
                ]
            }
            students.value = data.students
        } catch (error) {
            console.error('Ошибка загрузки студентов:', error)
        }
    }

    const getFullName = (student) => {
        return `${student.last_name} ${student.first_name} ${student.middle_name}`
    }

    return { students, fetchStudents, getFullName }
})