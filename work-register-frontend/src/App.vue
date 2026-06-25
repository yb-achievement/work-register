<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  CircleCheck,
  Clock,
  DocumentAdd,
  Finished,
  Refresh,
  WarningFilled
} from '@element-plus/icons-vue'
import {
  completeWorkItem,
  createWorkItem,
  fetchSummary,
  fetchWeeklyContentSummary,
  fetchWorkItems
} from './api/workItems'

const TOPIC_OPTIONS = ['数据', '系统功能', '常规性工作']

const statusMeta = {
  overdue: {
    title: '超时未完成',
    label: '已超时',
    empty: '暂无超时未完成工作',
    icon: WarningFilled
  },
  pending: {
    title: '未完成',
    label: '未完成',
    empty: '暂无未完成工作',
    icon: Clock
  },
  done: {
    title: '已完成',
    label: '已完成',
    empty: '暂无已完成工作',
    icon: CircleCheck
  }
}

const summaryConfig = [
  { key: 'week', title: '周总结', description: '上星期五至本星期四' },
  { key: 'month', title: '月总结', description: '自然月' },
  { key: 'year', title: '年度总结', description: '自然年' }
]

const loading = ref(false)
const saving = ref(false)
const completingId = ref(null)
const summaryLoading = ref(false)
const weeklyDialogVisible = ref(false)
const weeklyLoading = ref(false)
const createDialogVisible = ref(false)
const formRef = ref(null)

const workItems = ref([])
const summaries = reactive({
  week: null,
  month: null,
  year: null
})
const weeklySummary = reactive({
  completedThisWeek: [],
  nextWeekWork: [],
  modelType: 'rule'
})

const form = reactive({
  topic: '',
  content: '',
  executor: '',
  dueDate: ''
})

const rules = {
  topic: [{ required: true, message: '请选择工作主题', trigger: 'change' }],
  content: [{ required: true, message: '请填写工作内容', trigger: 'blur' }],
  executor: [{ required: true, message: '请填写执行人', trigger: 'blur' }],
  dueDate: [{ required: true, message: '请选择要求完成时间', trigger: 'change' }]
}

const groupedItems = computed(() => ({
  overdue: workItems.value.filter((item) => item.displayStatus === 'overdue'),
  pending: workItems.value.filter((item) => item.displayStatus === 'pending'),
  done: workItems.value.filter((item) => item.displayStatus === 'done')
}))

const hasAnyWork = computed(() => workItems.value.length > 0)

function formatDate(value) {
  if (!value) return '-'
  return String(value).slice(0, 10)
}

function formatDateTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 16)
}

function formatPeriod(summary) {
  if (!summary) return '-'
  return `${formatDate(summary.periodStart)} 至 ${formatDate(summary.periodEnd)}`
}

function resetForm() {
  form.topic = ''
  form.content = ''
  form.executor = ''
  form.dueDate = ''
  formRef.value?.clearValidate()
}

function openCreateDialog() {
  resetForm()
  createDialogVisible.value = true
}

async function loadWorkItems() {
  loading.value = true
  try {
    workItems.value = await fetchWorkItems()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

async function loadSummaries() {
  summaryLoading.value = true
  try {
    const [week, month, year] = await Promise.all(summaryConfig.map((item) => fetchSummary(item.key)))
    summaries.week = week
    summaries.month = month
    summaries.year = year
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    summaryLoading.value = false
  }
}

async function refreshAll() {
  await Promise.all([loadWorkItems(), loadSummaries()])
}

async function submitCreate() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    await createWorkItem({ ...form })
    ElMessage.success('保存成功')
    createDialogVisible.value = false
    resetForm()
    await refreshAll()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    saving.value = false
  }
}

async function handleComplete(item) {
  await ElMessageBox.confirm('确认将这项工作标记为已完成？', '确认完成', {
    type: 'success',
    confirmButtonText: '确认完成',
    cancelButtonText: '取消'
  })

  completingId.value = item.id
  try {
    await completeWorkItem(item.id)
    ElMessage.success('已确认完成')
    await refreshAll()
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    completingId.value = null
  }
}

async function openWeeklySummary() {
  weeklyDialogVisible.value = true
  weeklyLoading.value = true
  try {
    const data = await fetchWeeklyContentSummary()
    weeklySummary.completedThisWeek = data.completedThisWeek || []
    weeklySummary.nextWeekWork = data.nextWeekWork || []
    weeklySummary.modelType = data.modelType || 'rule'
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    weeklyLoading.value = false
  }
}

onMounted(refreshAll)
</script>

<template>
  <main class="page-shell">
    <header class="page-header">
      <div>
        <p class="eyebrow">个人工作登记</p>
        <h1>个人工作登记</h1>
        <p class="subtitle">记录工作、跟踪完成情况、查看阶段总结</p>
      </div>

      <div class="header-actions">
        <el-button :icon="Refresh" round @click="refreshAll">刷新</el-button>
        <el-button :icon="DocumentAdd" type="primary" round @click="openCreateDialog">添加新工作</el-button>
      </div>
    </header>

    <section v-loading="summaryLoading" class="summary-grid" aria-label="工作总结">
      <article v-for="item in summaryConfig" :key="item.key" class="summary-card">
        <div class="summary-heading">
          <div>
            <h2>{{ item.title }}</h2>
            <p>{{ item.description }}：{{ formatPeriod(summaries[item.key]) }}</p>
          </div>
          <el-icon class="summary-icon"><Finished /></el-icon>
        </div>

        <div class="stat-grid">
          <div class="stat-item">
            <strong>{{ summaries[item.key]?.totalCount ?? 0 }}</strong>
            <span>总数</span>
          </div>
          <div class="stat-item success">
            <strong>{{ summaries[item.key]?.doneCount ?? 0 }}</strong>
            <span>已完成</span>
          </div>
          <div class="stat-item warning">
            <strong>{{ summaries[item.key]?.pendingCount ?? 0 }}</strong>
            <span>未完成</span>
          </div>
          <div class="stat-item danger">
            <strong>{{ summaries[item.key]?.overdueCount ?? 0 }}</strong>
            <span>超时</span>
          </div>
        </div>

        <el-button
          v-if="item.key === 'week'"
          class="weekly-button"
          type="primary"
          plain
          @click="openWeeklySummary"
        >
          生成周总结
        </el-button>
      </article>
    </section>

    <section v-loading="loading" class="work-groups" aria-label="工作列表">
      <el-empty
        v-if="!loading && !hasAnyWork"
        class="empty-all"
        description="暂无工作记录"
      >
        <el-button type="primary" @click="openCreateDialog">添加新工作</el-button>
      </el-empty>

      <article v-for="status in ['overdue', 'pending', 'done']" :key="status" class="work-section">
        <div class="section-heading">
          <div class="section-title">
            <el-icon :class="`status-icon ${status}`">
              <component :is="statusMeta[status].icon" />
            </el-icon>
            <h2>{{ statusMeta[status].title }}</h2>
          </div>
          <span class="count-badge">{{ groupedItems[status].length }}</span>
        </div>

        <div v-if="groupedItems[status].length" class="work-list">
          <div
            v-for="item in groupedItems[status]"
            :key="item.id"
            :class="['work-card', item.displayStatus]"
          >
            <div class="work-main">
              <div class="work-title-row">
                <p class="work-content">{{ item.content }}</p>
                <el-tag :type="item.displayStatus === 'done' ? 'success' : item.displayStatus === 'overdue' ? 'danger' : 'warning'">
                  {{ statusMeta[item.displayStatus].label }}
                </el-tag>
              </div>

              <div class="work-meta">
                <span>主题：{{ item.topic }}</span>
                <span>执行人：{{ item.executor }}</span>
                <span>要求完成：{{ formatDate(item.dueDate) }}</span>
                <span>登记：{{ formatDateTime(item.createdAt) }}</span>
              </div>
            </div>

            <div class="work-actions">
              <el-button
                v-if="item.status !== 'done'"
                :loading="completingId === item.id"
                type="success"
                round
                @click="handleComplete(item)"
              >
                确认完成
              </el-button>
            </div>
          </div>
        </div>

        <el-empty v-else :description="statusMeta[status].empty" :image-size="88" />
      </article>
    </section>

    <el-dialog
      v-model="createDialogVisible"
      title="添加新工作"
      width="520px"
      :close-on-click-modal="false"
      destroy-on-close
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="主题" prop="topic">
          <el-select v-model="form.topic" class="full-width" placeholder="请选择工作主题">
            <el-option v-for="topic in TOPIC_OPTIONS" :key="topic" :label="topic" :value="topic" />
          </el-select>
        </el-form-item>

        <el-form-item label="工作内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="请输入具体工作内容"
          />
        </el-form-item>

        <el-form-item label="执行人" prop="executor">
          <el-input v-model="form.executor" placeholder="请输入执行人" />
        </el-form-item>

        <el-form-item label="要求完成时间" prop="dueDate">
          <el-date-picker
            v-model="form.dueDate"
            class="full-width"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择要求完成时间"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submitCreate">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="weeklyDialogVisible"
      title="周工作总结"
      width="620px"
      :close-on-click-modal="false"
    >
      <div v-loading="weeklyLoading" class="weekly-summary">
        <section>
          <h3>本周已完成工作：</h3>
          <ol v-if="weeklySummary.completedThisWeek.length">
            <li v-for="line in weeklySummary.completedThisWeek" :key="line">{{ line }}</li>
          </ol>
          <p v-else class="summary-empty">暂无本周已完成工作</p>
        </section>

        <section>
          <h3>下周工作：</h3>
          <ol v-if="weeklySummary.nextWeekWork.length">
            <li v-for="line in weeklySummary.nextWeekWork" :key="line">{{ line }}</li>
          </ol>
          <p v-else class="summary-empty">暂无下周工作</p>
        </section>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="weeklyDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </main>
</template>

<style scoped>
.page-shell {
  width: min(1160px, calc(100% - 32px));
  margin: 0 auto;
  padding: 32px 0 48px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 22px;
}

.eyebrow {
  margin: 0 0 8px;
  color: var(--color-primary);
  font-size: 14px;
  font-weight: 700;
}

h1 {
  margin: 0;
  color: var(--color-text);
  font-size: 28px;
  line-height: 1.2;
  letter-spacing: 0;
}

.subtitle {
  margin: 10px 0 0;
  color: var(--color-muted);
  font-size: 15px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 22px;
}

.summary-card,
.work-section {
  border: 1px solid var(--color-border);
  border-radius: 8px;
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
}

.summary-card {
  padding: 18px;
}

.summary-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.summary-heading h2,
.section-heading h2 {
  margin: 0;
  color: var(--color-text);
  font-size: 19px;
  line-height: 1.25;
  letter-spacing: 0;
}

.summary-heading p {
  margin: 7px 0 0;
  color: var(--color-muted);
  font-size: 13px;
}

.summary-icon {
  color: var(--color-primary);
  font-size: 22px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
  margin-top: 16px;
}

.stat-item {
  min-width: 0;
  padding: 10px 6px;
  border: 1px solid #edf1f6;
  border-radius: 8px;
  background: #f8fafc;
  text-align: center;
}

.stat-item strong {
  display: block;
  color: var(--color-text);
  font-size: 23px;
  line-height: 1;
}

.stat-item span {
  display: block;
  margin-top: 7px;
  color: var(--color-muted);
  font-size: 12px;
}

.stat-item.success strong {
  color: var(--color-success);
}

.stat-item.warning strong {
  color: var(--color-warning);
}

.stat-item.danger strong {
  color: var(--color-danger);
}

.weekly-button {
  width: 100%;
  margin-top: 14px;
}

.work-groups {
  display: grid;
  gap: 16px;
}

.empty-all {
  border: 1px dashed var(--color-border);
  border-radius: 8px;
  background: var(--color-surface);
}

.work-section {
  overflow: hidden;
}

.section-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 18px;
  border-bottom: 1px solid var(--color-border);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-icon {
  font-size: 20px;
}

.status-icon.overdue {
  color: var(--color-danger);
}

.status-icon.pending {
  color: var(--color-warning);
}

.status-icon.done {
  color: var(--color-success);
}

.count-badge {
  min-width: 34px;
  padding: 4px 10px;
  border-radius: 999px;
  background: #eef2f7;
  color: var(--color-muted);
  font-size: 13px;
  font-weight: 700;
  text-align: center;
}

.work-list {
  display: grid;
  gap: 12px;
  padding: 16px;
}

.work-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 16px;
  align-items: center;
  padding: 15px 16px;
  border: 1px solid var(--color-border);
  border-left-width: 5px;
  border-radius: 8px;
  background: var(--color-surface);
}

.work-card.overdue {
  border-left-color: var(--color-danger);
  background: var(--color-danger-soft);
}

.work-card.pending {
  border-left-color: var(--color-warning);
  background: var(--color-warning-soft);
}

.work-card.done {
  border-left-color: var(--color-success);
  background: var(--color-success-soft);
}

.work-main {
  min-width: 0;
}

.work-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.work-content {
  min-width: 0;
  margin: 0;
  color: var(--color-text);
  font-size: 16px;
  font-weight: 700;
  line-height: 1.55;
  overflow-wrap: anywhere;
}

.work-card.done .work-content {
  color: #475467;
}

.work-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 18px;
  margin-top: 10px;
  color: var(--color-muted);
  font-size: 13px;
}

.work-actions {
  display: flex;
  justify-content: flex-end;
}

.full-width {
  width: 100%;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.weekly-summary {
  min-height: 180px;
  color: var(--color-text);
}

.weekly-summary section + section {
  margin-top: 22px;
}

.weekly-summary h3 {
  margin: 0 0 10px;
  font-size: 17px;
  line-height: 1.3;
}

.weekly-summary ol {
  margin: 0;
  padding-left: 22px;
}

.weekly-summary li {
  margin: 8px 0;
  line-height: 1.55;
}

.summary-empty {
  margin: 0;
  color: var(--color-muted);
}

@media (max-width: 920px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .page-shell {
    width: min(100% - 24px, 1160px);
    padding-top: 24px;
  }

  .page-header {
    align-items: stretch;
    flex-direction: column;
  }

  .header-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .header-actions .el-button {
    width: 100%;
  }

  .work-card {
    grid-template-columns: 1fr;
  }

  .work-actions {
    justify-content: stretch;
  }

  .work-actions .el-button {
    width: 100%;
  }

  .stat-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
