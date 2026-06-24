<template>
  <header class="topbar">
    <div class="page-title"><h1>Excel 导入</h1><p>导入题目到指定科目和知识点，Excel 无需包含科目/知识点列。</p></div>
    <div class="top-actions"><el-button @click="showTemplate = true">查看模板</el-button><el-button @click="$router.push('/questions')">返回题库</el-button><UserDropdown /></div>
  </header>
  <main class="main">
    <section class="card band">
      <div class="card-header"><h3>导入设置</h3></div>
      <div class="card-body">
        <el-form label-position="top">
          <div class="grid grid-2">
            <el-form-item label="科目">
              <el-select v-model="importConfig.subjectId" placeholder="选择科目" @change="onSubjectChange">
                <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="知识点">
              <el-select v-model="importConfig.knowledgePointId" placeholder="选择知识点">
                <el-option v-for="p in points" :key="p.id" :label="p.name" :value="p.id" />
              </el-select>
            </el-form-item>
          </div>
        </el-form>
      </div>
    </section>
    <section class="card">
      <div class="card-body">
        <el-upload drag :http-request="upload" accept=".xlsx,.xls" :show-file-list="false">
          <p>拖拽 Excel 到此处，或点击选择文件</p>
        </el-upload>
        <el-alert class="band" type="info" show-icon :closable="false" title="Excel 只需包含：题型、题目、选项列（表头写'选项A''选项B'...，数量不限）、正确答案、难度、解析。科目和知识点由上方选择器指定。" />
        <el-table v-if="result" :data="result.errors || []">
          <el-table-column label="导入错误"><template #default="{ row }">{{ row }}</template></el-table-column>
        </el-table>
        <p v-if="result">成功 {{ result.success }} 条，失败 {{ result.failed }} 条。</p>
      </div>
    </section>
  </main>
  <el-dialog v-model="showTemplate" title="导入模板格式" width="90%">
    <img :src="templateImg" style="width: 100%; border-radius: 8px;" />
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api/client'
import UserDropdown from '../components/UserDropdown.vue'
import templateImg from '../assets/import-template.png'

const subjects = ref([])
const points = ref([])
const result = ref(null)
const showTemplate = ref(false)
const importConfig = reactive({ subjectId: null, knowledgePointId: null })

const loadSubjects = async () => {
  subjects.value = await api.get('/catalog/subjects')
}

const onSubjectChange = async () => {
  importConfig.knowledgePointId = null
  if (importConfig.subjectId) {
    points.value = await api.get('/catalog/knowledge-points', { params: { subjectId: importConfig.subjectId } })
  } else {
    points.value = []
  }
}

const upload = async ({ file }) => {
  if (!importConfig.subjectId || !importConfig.knowledgePointId) {
    ElMessage.warning('请先选择科目和知识点')
    return
  }
  const data = new FormData()
  data.append('file', file)
  data.append('subjectId', importConfig.subjectId)
  data.append('knowledgePointId', importConfig.knowledgePointId)
  result.value = await api.post('/import/questions', data)
  ElMessage.success('导入完成')
}

onMounted(loadSubjects)
</script>
