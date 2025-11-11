<template>
  <el-form
    :model="formData"
    :rules="formRules"
    ref="formRef"
    label-position="top"
    v-loading="loading || categoryLoading"
  >
    <el-divider content-position="left">
      <el-icon :size="16" style="margin-right: 8px"><Document /></el-icon>
      项目基本信息
    </el-divider>
    <el-form-item label="项目标题" prop="title">
      <el-input
        v-model="formData.title"
        placeholder="给你的项目起个响亮的名字"
        :prefix-icon="EditPen"
        size="large"
      ></el-input>
    </el-form-item>
    <el-form-item label="项目简介" prop="description">
      <el-input
        v-model="formData.description"
        type="textarea"
        :rows="3"
        placeholder="简要描述你的项目 (200字以内)"
        show-word-limit
        maxlength="200"
      ></el-input>
    </el-form-item>
    <el-form-item label="封面图片" prop="coverImage">
      <ImageUpload v-model="formData.coverImage" />
    </el-form-item>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-form-item label="目标金额 (元)" prop="goalAmount">
          <el-input-number
            v-model="formData.goalAmount"
            :min="1"
            :precision="2"
            :step="100"
            :prefix-icon="Money"
            size="large"
            style="width: 100%"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="截止日期" prop="endTime">
          <el-date-picker
            v-model="formData.endTime"
            type="datetime"
            placeholder="选择众筹截止时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            :prefix-icon="Calendar"
            size="large"
            style="width: 100%"
          />
        </el-form-item>
      </el-col>
    </el-row>
    <el-form-item label="项目分类" prop="categoryId">
      <el-select
        v-model="formData.categoryId"
        placeholder="请选择项目分类"
        :prefix-icon="Grid"
        size="large"
        style="width: 100%"
        :loading="categoryLoading"
      >
        <el-option
          v-for="category in categoryList"
          :key="category.id"
          :label="category.name"
          :value="category.id"
        />
      </el-select>
    </el-form-item>

    <el-divider content-position="left">
      <el-icon :size="16" style="margin-right: 8px"><CollectionTag /></el-icon>
      设置回报档位
    </el-divider>

    <div v-if="formData.rewards.length === 0" style="text-align: center; margin: 20px 0">
      <el-empty description="至少需要一个回报档位">
        <el-button type="primary" @click="addReward" :icon="Plus">添加第一个回报</el-button>
      </el-empty>
    </div>

    <div v-for="(reward, index) in formData.rewards" :key="index" class="reward-form-card">
      <div class="reward-card-header">
        <span>回报档位 {{ index + 1 }}</span>
        <el-button type="danger" link @click="removeReward(index)" :icon="Delete">
          删除此档位
        </el-button>
      </div>

      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item
            label="档位标题"
            :prop="'rewards.' + index + '.title'"
            :rules="{ required: true, message: '档位标题不能为空', trigger: 'blur' }"
          >
            <el-input v-model="reward.title" placeholder="例如：【早鸟票】"></el-input>
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item
            label="档位描述"
            :prop="'rewards.' + index + '.description'"
            :rules="{ required: true, message: '档位描述不能为空', trigger: 'blur' }"
          >
            <el-input v-model="reward.description" type="textarea" :rows="2"></el-input>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item
            label="支持金额 (元)"
            :prop="'rewards.' + index + '.amount'"
            :rules="[
              { required: true, message: '金额必须大于0', trigger: 'blur' },
              { type: 'number', min: 1, message: '金额必须大于0', trigger: 'blur' },
            ]"
          >
            <el-input-number v-model="reward.amount" :min="1" :precision="2" style="width: 100%" />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item
            label="库存名额 (0为不限量)"
            :prop="'rewards.' + index + '.stock'"
            :rules="[
              { required: true, message: '库存必须大于等于0', trigger: 'blur' },
              { type: 'number', min: 0, message: '库存必须大于等于0', trigger: 'blur' },
            ]"
          >
            <el-input-number v-model="reward.stock" :min="0" :step="1" style="width: 100%" />
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item
            label="回报图片"
            :prop="'rewards.' + index + '.imageUrl'"
            :rules="{ required: true, message: '请上传回报图片', trigger: 'change' }"
          >
            <ImageUpload v-model="reward.imageUrl" />
          </el-form-item>
        </el-col>
      </el-row>
    </div>

    <el-form-item v-if="formData.rewards.length > 0">
      <el-button @click="addReward" style="width: 100%" :icon="Plus" dashed size="large">
        + 继续添加回报档位
      </el-button>
    </el-form-item>
    <el-divider />
    <el-form-item>
      <el-button
        type="primary"
        @click="handleSubmit"
        :loading="loading"
        style="width: 100%"
        size="large"
      >
        {{ submitLabel }}
      </el-button>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { PropType } from 'vue'
import type { ProjectCreateDTO } from '@/api/types/project'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import ImageUpload from '@/components/ImageUpload.vue'
import {
  EditPen,
  Document,
  Money,
  Calendar,
  Grid,
  CollectionTag,
  Plus,
  Delete,
} from '@element-plus/icons-vue'
import { getCategoryListApi } from '@/api/category'
import type { CategoryDTO } from '@/api/types/category'

// --- 1. Props & Emits (不变) ---
const props = defineProps({
  formData: {
    type: Object as PropType<ProjectCreateDTO>,
    required: true,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  submitLabel: {
    type: String,
    default: '提交',
  },
})
const emit = defineEmits(['submit'])

// --- 2. 内部逻辑 (不变) ---
const formRef = ref<FormInstance>()
const formRules = reactive<FormRules>({
  title: [{ required: true, message: '请输入项目标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入项目简介', trigger: 'blur' }],
  coverImage: [{ required: true, message: '请上传封面图片', trigger: 'blur' }],
  goalAmount: [
    { required: true, message: '请输入目标金额', trigger: 'blur' },
    { type: 'number', min: 1, message: '目标金额必须大于0', trigger: 'blur' },
  ],
  endTime: [{ required: true, message: '请选择截止日期', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择项目分类', trigger: 'change' }],
})

// --- 3. 加载分类 (不变) ---
const categoryList = ref<CategoryDTO[]>([])
const categoryLoading = ref(true)

onMounted(async () => {
  try {
    categoryList.value = await getCategoryListApi()
  } catch (err: any) {
    ElMessage.error(err.message || '分类加载失败')
  } finally {
    categoryLoading.value = false
  }
})

// --- 4. 回报档位 (修改) ---

/**
 * [修改] 添加一个新的回报档位
 */
const addReward = () => {
  props.formData.rewards.push({
    title: '',
    description: '',
    amount: 10,
    stock: 100,
    imageUrl: '', // <-- [新增] 必须添加 imageUrl 字段
  })
}

/**
 * 移除一个回报档位 (不变)
 */
const removeReward = (index: number) => {
  props.formData.rewards.splice(index, 1)
}

// --- 5. 提交 (不变) ---
const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (err) {
    ElMessage.error('表单填写有误，请检查。')
    return
  }
  if (props.formData.rewards.length === 0) {
    ElMessage.warning('请至少添加一个回报档位。')
    return
  }
  emit('submit')
}
</script>

<style scoped>
/* (样式不变) */
:deep(.el-form-item__label) {
  font-size: 14px;
  font-weight: 500;
}
:deep(.el-divider__text) {
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
}
.reward-form-card {
  margin-bottom: 20px;
  background-color: var(--spark-bg-color);
  border: 1px solid var(--spark-border-color);
  border-radius: 8px;
  padding: 20px;
}
.reward-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--spark-border-color);
}
.reward-card-header span {
  font-size: 16px;
  font-weight: 600;
  color: var(--spark-primary-color);
}
</style>
