<template>
  <header class="topbar">
    <div class="page-title"><h1>科目知识点</h1><p>维护题目分类，题目必须关联到一个知识点。</p></div>
    <UserDropdown />
  </header>
  <main class="main grid grid-2">
    <section class="card">
      <div class="card-header"><h2>科目</h2><el-button type="primary" @click="createSubject">新增科目</el-button></div>
      <div class="card-body">
        <el-form class="band"><el-input v-model="subjectForm.name" placeholder="例如 数学、英语、软考" /></el-form>
        <el-table :data="subjects" @row-click="selectSubject" highlight-current-row>
          <el-table-column prop="name" label="科目" />
          <el-table-column label="操作" width="140">
            <template #default="{ row }">
              <el-button link type="primary" @click.stop="openEditSubject(row)">编辑</el-button>
              <el-button link type="danger" @click.stop="removeSubject(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </section>
    <section class="card">
      <div class="card-header"><h2>知识点</h2><el-button type="primary" :disabled="!currentSubjectId" @click="createPoint">新增知识点</el-button></div>
      <div class="card-body">
        <el-form class="band"><el-input v-model="pointForm.name" placeholder="例如 代数、语法、网络协议" /></el-form>
        <el-table :data="points">
          <el-table-column prop="name" label="知识点" />
          <el-table-column prop="description" label="说明" />
          <el-table-column label="操作" width="140">
            <template #default="{ row }">
              <el-button link type="primary" @click="openEditPoint(row)">编辑</el-button>
              <el-popconfirm title="确定删除该知识点？如有题目关联将无法删除。" @confirm="removePoint(row.id)">
                <template #reference><el-button link type="danger">删除</el-button></template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </section>
  </main>
  <el-dialog v-model="editSubjectVisible" title="编辑科目" width="400">
    <el-form><el-form-item label="科目名称"><el-input v-model="editSubjectForm.name" /></el-form-item></el-form>
    <template #footer><el-button @click="editSubjectVisible = false">取消</el-button><el-button type="primary" @click="saveEditSubject">保存</el-button></template>
  </el-dialog>
  <el-dialog v-model="editPointVisible" title="编辑知识点" width="400">
    <el-form>
      <el-form-item label="知识点名称"><el-input v-model="editPointForm.name" /></el-form-item>
      <el-form-item label="说明"><el-input v-model="editPointForm.description" /></el-form-item>
    </el-form>
    <template #footer><el-button @click="editPointVisible = false">取消</el-button><el-button type="primary" @click="saveEditPoint">保存</el-button></template>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api/client'
import UserDropdown from '../components/UserDropdown.vue'

const subjects = ref([])
const points = ref([])
const currentSubjectId = ref(null)
const subjectForm = reactive({ name: '', description: '' })
const pointForm = reactive({ name: '', description: '' })
const editSubjectVisible = ref(false)
const editSubjectForm = reactive({ id: null, name: '' })
const editPointVisible = ref(false)
const editPointForm = reactive({ id: null, name: '', description: '' })

const loadSubjects = async () => {
  subjects.value = await api.get('/catalog/subjects')
  if (!currentSubjectId.value && subjects.value[0]) currentSubjectId.value = subjects.value[0].id
  await loadPoints()
}
const loadPoints = async () => {
  points.value = currentSubjectId.value ? await api.get('/catalog/knowledge-points', { params: { subjectId: currentSubjectId.value } }) : []
}
const selectSubject = (row) => { currentSubjectId.value = row.id; loadPoints() }
const createSubject = async () => {
  if (!subjectForm.name.trim()) return
  await api.post('/catalog/subjects', subjectForm); subjectForm.name = ''; loadSubjects()
}
const createPoint = async () => {
  if (!pointForm.name.trim()) return
  await api.post('/catalog/knowledge-points', { ...pointForm, subjectId: currentSubjectId.value }); pointForm.name = ''; loadPoints()
}
const removeSubject = async (id) => { await api.delete(`/catalog/subjects/${id}`); currentSubjectId.value = null; loadSubjects() }
const openEditSubject = (row) => { editSubjectForm.id = row.id; editSubjectForm.name = row.name; editSubjectVisible.value = true }
const saveEditSubject = async () => {
  await api.put(`/catalog/subjects/${editSubjectForm.id}`, { name: editSubjectForm.name })
  editSubjectVisible.value = false; loadSubjects()
}
const openEditPoint = (row) => { editPointForm.id = row.id; editPointForm.name = row.name; editPointForm.description = row.description || ''; editPointVisible.value = true }
const saveEditPoint = async () => {
  await api.put(`/catalog/knowledge-points/${editPointForm.id}`, { name: editPointForm.name, description: editPointForm.description })
  editPointVisible.value = false; loadPoints()
}
const removePoint = async (id) => {
  try { await api.delete(`/catalog/knowledge-points/${id}`); loadPoints() }
  catch (e) { ElMessage.error('删除失败，可能有题目关联该知识点') }
}
onMounted(loadSubjects)
</script>