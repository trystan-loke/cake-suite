<script setup lang="ts">
import { onMounted, onBeforeUnmount } from 'vue'
import { auth } from '../firebase'
import * as firebaseui from 'firebaseui'
import 'firebaseui/dist/firebaseui.css'
import { EmailAuthProvider } from 'firebase/auth'

let ui: firebaseui.auth.AuthUI | null = null

onMounted(() => {
  // Reuse existing instance if it exists (hot-reload friendly)
  ui = firebaseui.auth.AuthUI.getInstance() || new firebaseui.auth.AuthUI(auth as any)
  ui.start('#firebaseui-auth-container', {
    signInFlow: 'popup',
  credentialHelper: firebaseui.auth.CredentialHelper.NONE,
    signInOptions: [
      {
        provider: EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD,
        requireDisplayName: false,
        signInMethod: EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD,
        disableSignUp: {
          status: true
        }
      },
    ],
  tosUrl: undefined,
  privacyPolicyUrl: undefined,
    callbacks: {
      signInSuccessWithAuthResult: () => false,
    },
  } as any)
})

onBeforeUnmount(() => {
  try { ui?.reset() } catch {}
})
</script>

<template>
  <div id="firebaseui-auth-container"></div>
</template>

<style scoped>
</style>
