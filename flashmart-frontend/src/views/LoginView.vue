<script setup>
import { ref } from "vue";
import { useRoute, useRouter, RouterLink } from "vue-router";
import { useAuthStore } from "@/stores/auth";

const auth = useAuthStore();
const route = useRoute();
const router = useRouter();

const phone = ref("");
const password = ref("");
const err = ref("");
const loading = ref(false);

async function submit() {
  err.value = "";
  loading.value = true;
  try {
    await auth.login(phone.value.trim(), password.value);
    const redirect = route.query.redirect;
    router.push(typeof redirect === "string" ? redirect : "/");
  } catch (e) {
    err.value = e.message || "登录失败";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="page-inner narrow">
    <div class="card form-card">
      <h2>登录</h2>
      <p class="muted">使用注册时的手机号与密码</p>
      <form class="form" @submit.prevent="submit">
        <label>
          <span>手机号</span>
          <input v-model="phone" class="input" type="text" autocomplete="tel" required />
        </label>
        <label>
          <span>密码</span>
          <input
            v-model="password"
            class="input"
            type="password"
            autocomplete="current-password"
            required
          />
        </label>
        <p v-if="err" class="err-msg">{{ err }}</p>
        <button class="btn btn-primary full" type="submit" :disabled="loading">
          {{ loading ? "登录中…" : "登录" }}
        </button>
      </form>
      <p class="foot muted">
        还没有账号？
        <RouterLink to="/register">去注册</RouterLink>
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
  margin-top: 0.25rem;
}
.foot {
  margin-top: 1.25rem;
  text-align: center;
}
</style>
