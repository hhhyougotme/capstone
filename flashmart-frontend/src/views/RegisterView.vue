<script setup>
import { ref } from "vue";
import { useRouter, RouterLink } from "vue-router";
import { useAuthStore } from "@/stores/auth";

const auth = useAuthStore();
const router = useRouter();

const phone = ref("");
const password = ref("");
const nickname = ref("");
const err = ref("");
const loading = ref(false);

async function submit() {
  err.value = "";
  if (password.value.length < 6) {
    err.value = "密码至少 6 位";
    return;
  }
  loading.value = true;
  try {
    await auth.register(phone.value.trim(), password.value, nickname.value.trim());
    router.push("/");
  } catch (e) {
    err.value = e.message || "注册失败";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="page-inner narrow">
    <div class="card form-card">
      <h2>注册</h2>
      <p class="muted">手机号唯一，注册成功后将自动登录</p>
      <form class="form" @submit.prevent="submit">
        <label>
          <span>手机号</span>
          <input v-model="phone" class="input" type="text" autocomplete="tel" required />
        </label>
        <label>
          <span>密码（6–64 位）</span>
          <input
            v-model="password"
            class="input"
            type="password"
            autocomplete="new-password"
            required
            minlength="6"
          />
        </label>
        <label>
          <span>昵称（可选）</span>
          <input v-model="nickname" class="input" type="text" maxlength="64" />
        </label>
        <p v-if="err" class="err-msg">{{ err }}</p>
        <button class="btn btn-primary full" type="submit" :disabled="loading">
          {{ loading ? "提交中…" : "注册" }}
        </button>
      </form>
      <p class="foot muted">
        已有账号？
        <RouterLink to="/login">去登录</RouterLink>
      </p>
    </div>
  </div>
</template>

<style scoped>
.narrow {
  max-width: 420px;
}
.form-card {
  padding: 1.75rem;
  margin-top: 1rem;
}
.form-card h2 {
  margin: 0 0 0.25rem;
}
.form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-top: 1.25rem;
}
label span {
  display: block;
  font-size: 0.875rem;
  font-weight: 600;
  margin-bottom: 0.35rem;
}
.full {
  width: 100%;
}
.foot {
  margin-top: 1.25rem;
  text-align: center;
}
</style>
