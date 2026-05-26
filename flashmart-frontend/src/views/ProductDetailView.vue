<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import client, { unwrap } from "@/api/client";
import { formatMoney } from "@/utils/format";

const props = defineProps({
  id: { type: String, required: true },
});

const router = useRouter();
const product = ref(null);
const loading = ref(true);
const err = ref("");

async function load() {
  loading.value = true;
  err.value = "";
  try {
    product.value = await unwrap(client.get(`/products/${props.id}`));
  } catch (e) {
    err.value = e.message || "加载失败";
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <div class="page-inner narrow">
    <button type="button" class="back muted" @click="router.back()">← 返回</button>

    <p v-if="loading" class="muted">加载中…</p>
    <p v-else-if="err" class="err-msg">{{ err }}</p>

    <article v-else-if="product" class="card detail">
      <div v-if="product.imageUrl" class="img-wrap">
        <img :src="product.imageUrl" :alt="product.name" />
      </div>
      <h1>{{ product.name }}</h1>
      <p class="price">¥{{ formatMoney(product.price) }}</p>
      <p v-if="product.description" class="desc">{{ product.description }}</p>
      <p class="muted small">库存 {{ product.stock }} · 店铺商户 ID {{ product.merchantId }}</p>
      <button type="button" class="btn btn-outline" @click="router.push(`/merchants/${product.merchantId}`)">
        进入店铺
      </button>
    </article>
  </div>
</template>

<style scoped>
.narrow {
  max-width: 520px;
}
.back {
  border: none;
  background: none;
  cursor: pointer;
  padding: 0 0 1rem;
  font-size: 0.9rem;
}
.detail {
  padding: 1.5rem;
}
.img-wrap {
  margin: -1.5rem -1.5rem 1rem;
  max-height: 220px;
  overflow: hidden;
  background: var(--fm-bg);
}
.img-wrap img {
  width: 100%;
  display: block;
  object-fit: cover;
}
.detail h1 {
  margin: 0 0 0.5rem;
  font-size: 1.35rem;
}
.price {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--fm-brand);
  margin: 0 0 0.75rem;
}
.desc {
  margin: 0 0 1rem;
  line-height: 1.55;
}
.small {
  font-size: 0.85rem;
  margin-bottom: 1rem;
}
</style>
