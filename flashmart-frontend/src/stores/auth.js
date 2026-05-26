import { defineStore } from "pinia";
import { ref, computed } from "vue";
import client, { unwrap } from "@/api/client";

const TOKEN_KEY = "flashmart_token";

export const useAuthStore = defineStore("auth", () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || "");
  const userId = ref(Number(localStorage.getItem("flashmart_userId")) || null);
  const nickname = ref(localStorage.getItem("flashmart_nickname") || "");

  const isLoggedIn = computed(() => !!token.value);

  function persistSession(t, uid, name) {
    token.value = t;
    userId.value = uid;
    nickname.value = name || "";
    if (t) localStorage.setItem(TOKEN_KEY, t);
    else localStorage.removeItem(TOKEN_KEY);
    if (uid != null) localStorage.setItem("flashmart_userId", String(uid));
    else localStorage.removeItem("flashmart_userId");
    if (name) localStorage.setItem("flashmart_nickname", name);
    else localStorage.removeItem("flashmart_nickname");
  }

  async function login(phone, password) {
    const data = await unwrap(
      client.post("/auth/login", { phone, password })
    );
    persistSession(data.token, data.userId, data.nickname);
    return data;
  }

  /** 注册接口成功无 body，注册成功后自动登录 */
  async function register(phone, password, nicknameVal) {
    await unwrap(
      client.post("/auth/register", {
        phone,
        password,
        nickname: nicknameVal || undefined,
      })
    );
    return login(phone, password);
  }

  function logout() {
    persistSession("", null, "");
  }

  return {
    token,
    userId,
    nickname,
    isLoggedIn,
    login,
    register,
    logout,
  };
});
