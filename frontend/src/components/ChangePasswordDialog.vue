<!-- /Users/trystan/workspace/cake-suite/frontend/src/components/ChangePasswordDialog.vue -->

<template>
  <v-dialog v-model="isOpen" max-width="400">
    <v-card>
      <v-card-title class="px-8 pt-4 pb-0">Change Password</v-card-title>

      <v-card-text>
        <v-form ref="form" @submit.prevent="handleChangePassword">
          <v-text-field v-model="currentPassword" label="Current Password" type="password" required
            :rules="[rules.required]" class="mb-4" />

          <v-text-field v-model="newPassword" label="New Password" type="password" required
            :rules="[rules.required, rules.minLength]" class="mb-4" />

          <v-text-field v-model="confirmPassword" label="Confirm Password" type="password" required
            :rules="[rules.required, passwordMatch]" />
        </v-form>
      </v-card-text>

      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn variant="text" @click="closeDialog">Cancel</v-btn>
        <v-btn color="primary" variant="tonal" @click="handleChangePassword" :loading="isLoading">
          Change Password
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { changePassword } from '../services/firebase-service'
import { useNotifications } from '../composables/notifications'

const { success, error } = useNotifications()

const isOpen = defineModel<boolean>('modelValue', { default: false })
const isLoading = ref(false)
const form = ref<any>(null)

const currentPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')

const rules = {
  required: (v: string) => !!v || 'This field is required',
  minLength: (v: string) => v.length >= 8 || 'Password must be at least 8 characters',
}

const passwordMatch = (v: string) =>
  v === newPassword.value || 'Passwords do not match'

const closeDialog = () => {
  isOpen.value = false
  resetForm()
}

const resetForm = () => {
  currentPassword.value = ''
  newPassword.value = ''
  confirmPassword.value = ''
  form.value?.reset()
}

const handleChangePassword = async () => {
  if (!form.value?.validate()) {
    return
  }

  isLoading.value = true

  try {
    await changePassword(currentPassword.value, newPassword.value)
    success('Password changed successfully')
    closeDialog()
  } catch (err: any) {
    const errorMessage = err?.code === 'auth/wrong-password'
      ? 'Current password is incorrect'
      : err?.message || 'Failed to change password'
    error(errorMessage)
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped></style>