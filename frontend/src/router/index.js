import { createRouter, createWebHistory } from 'vue-router'
import Dashboard from '../views/Dashboard.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Profile from '../views/Profile.vue'
import Questions from '../views/Questions.vue'
import QuestionForm from '../views/QuestionForm.vue'
import Subjects from '../views/Subjects.vue'
import ExamSetup from '../views/ExamSetup.vue'
import Study from '../views/Study.vue'
import Exam from '../views/Exam.vue'
import ExamResult from '../views/ExamResult.vue'
import Errors from '../views/Errors.vue'
import History from '../views/History.vue'
import HistoryDetail from '../views/HistoryDetail.vue'
import Analytics from '../views/Analytics.vue'
import ImportQuestions from '../views/ImportQuestions.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: Login },
    { path: '/register', component: Register },
    { path: '/', component: Dashboard },
    { path: '/profile', component: Profile },
    { path: '/questions', component: Questions },
    { path: '/questions/new', component: QuestionForm },
    { path: '/questions/:id', component: QuestionForm },
    { path: '/subjects', component: Subjects },
    { path: '/import', component: ImportQuestions },
    { path: '/exam-setup', component: ExamSetup },
    { path: '/study', component: Study },
    { path: '/exam', component: Exam },
    { path: '/exam-result', component: ExamResult },
    { path: '/errors', component: Errors },
    { path: '/history', component: History },
    { path: '/history/:id', component: HistoryDetail },
    { path: '/analytics', component: Analytics }
  ]
})

router.beforeEach((to) => {
  if (to.path !== '/login' && to.path !== '/register' && !localStorage.getItem('token')) {
    return '/login'
  }
})

export default router
